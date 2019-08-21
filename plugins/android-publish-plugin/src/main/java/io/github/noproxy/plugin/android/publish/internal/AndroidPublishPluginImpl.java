/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.noproxy.plugin.android.publish.internal;

import com.android.build.gradle.api.LibraryVariant;
import com.github.noproxy.android.api.AndroidKits;
import com.github.noproxy.android.api.AndroidProvider;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.github.noproxy.plugin.android.publish.api.AndroidPublishExtension;
import io.github.noproxy.plugin.android.publish.internal.api.ComponentWithSoftwareComponentVariant;
import io.github.noproxy.plugin.android.publish.internal.legacy.DashNamingVersionSuffixAndroidVariantModuleMapping;
import io.github.noproxy.plugin.android.publish.internal.legacy.DefaultModuleCoordinate;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.specs.Specs;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class AndroidPublishPluginImpl {
    private static final String LEGACY_COMPONENT_SUFFIX = "Legacy";
    private final Project project;
    private final SoftwareComponentFactory softwareComponentFactory;
    private final PublishingExtension publishing;
    private final AndroidComponentsFactory components;
    private final DefaultAndroidPublishExtension androidPublishing;
    private final Map<SoftwareComponent, LibraryVariant> legacyPublishMap = Maps.newHashMap();

    //TODO 支持增加源码等artifacts
    //TODO 我们可以将runtime和compile分离，然后实现接口实现分离
    public AndroidPublishPluginImpl(Project project, SoftwareComponentFactory softwareComponentFactory) {
        this.project = project;
        this.softwareComponentFactory = softwareComponentFactory;
        project.getPluginManager().apply(MavenPublishPlugin.class);
        publishing = project.getExtensions().getByType(PublishingExtension.class);
        androidPublishing = (DefaultAndroidPublishExtension) project.getExtensions().create(AndroidPublishExtension.class, "androidPublishing", DefaultAndroidPublishExtension.class);
        components = new AndroidComponentsFactory(project);
    }

    public void configureComponents() {
        final ComponentWithSoftwareComponentVariant library = components.createAndroidLibraryWithVariants("androidLibrary",
                androidPublishing.getVariantMapping(),
                androidPublishing.getVariantSpec());
        project.getComponents().add(library);

        project.afterEvaluate(ignored -> {
            if (androidPublishing.isLegacyPublish()) {
                final AndroidProvider provider = AndroidKits.provider(project);
                provider.getAndroidVariants()
                        .matching(Optional.ofNullable(androidPublishing.getVariantSpec()).orElse(Specs.satisfyAll()))
                        .all(variant -> {
                            final AdhocComponentWithVariants adhocComponent = softwareComponentFactory.adhoc(variant.getBaseName() + LEGACY_COMPONENT_SUFFIX);

                            adhocComponent.addVariantsFromConfiguration(provider.getRuntimeElements(variant), filterAarAs("runtime"));
                            adhocComponent.addVariantsFromConfiguration(provider.getApiElements(variant), filterAarAs("compile"));
                            legacyPublishMap.put(adhocComponent, variant);
                            project.getComponents().add(adhocComponent);
                        });
            }
        });
    }

    private Action<ConfigurationVariantDetails> filterAarAs(final String scope) {
        return details -> {
            final ConfigurationVariant configurationVariant = details.getConfigurationVariant();
            if (!configurationVariant.getArtifacts().isEmpty()) {
                // We just use the Api/RuntimeElements to populate dependencies and will register artifacts by Publications.
                // In most case, android registers all variant artifacts expect aar in elements. This means that the main configuration has no artifacts
                // and the variant configurations contain android-classes, resources and so on.
                // So we only add the empty VariantConfiguration.

                if (configurationVariant.getClass().getName().endsWith("$DefaultConfigurationVariant")) {
                    // In case, user adds new artifacts
                    throw new AssertionError("We expects the ConfigurationVariantMapping$DefaultConfigurationVariant has no artifacts but it has "
                            + configurationVariant.getArtifacts());
                }
                details.skip();
            }
            details.mapToMavenScope(scope);
        };
    }

    public void configurePublications() {
        final PublicationContainer publications = publishing.getPublications();

        // wait android variants
        AndroidKits.afterAndroidEvaluate(project, basePlugin -> {
            publications.create("androidLibrary", MavenPublication.class, publication -> {
                publication.from(project.getComponents().findByName("androidLibrary"));
                Optional.ofNullable(getCompatibleArtifact()).ifPresent(publication::artifact);
            });

            project.getComponents().withType(AdhocComponentWithVariants.class)
                    .matching(component -> component.getName().endsWith(LEGACY_COMPONENT_SUFFIX))
                    .all(component -> publications.create(component.getName(), MavenPublication.class, publication -> {
                        publication.from(component);
                        final LibraryVariant variant = legacyPublishMap.get(component);
                        if (variant == null) {
                            throw new AssertionError("Cannot find variant by component: " + component);
                        }
                        publication.artifact(components.createArtifact(variant.getPackageLibraryProvider()));

                        final DefaultModuleCoordinate coordinate = new DefaultModuleCoordinate(publication.getGroupId(), publication.getArtifactId(), publication.getVersion());
                        Optional.ofNullable(androidPublishing.getLegacyVariantMapping()).orElseGet(DashNamingVersionSuffixAndroidVariantModuleMapping::new)
                                .execute(coordinate, variant);

                        publication.setGroupId(coordinate.getGroupId());
                        publication.setArtifactId(coordinate.getArtifactId());
                        publication.setVersion(coordinate.getVersion());
                    }));
        });
    }

    @Nullable
    private PublishArtifact getCompatibleArtifact() {
        final String compatiblePublishConfig = androidPublishing.getCompatiblePublishConfig();
        if (Strings.isNullOrEmpty(compatiblePublishConfig)) {
            return null;
        }
        final LibraryVariant mainVariant = AndroidKits.provider(project).getAndroidVariants().findByName(compatiblePublishConfig);
        if (mainVariant != null) {
            return components.createArtifact(mainVariant.getPackageLibraryProvider());
        } else {
            return components.createArtifact(project.provider(() -> {
                if (compatiblePublishConfig.equals(AndroidPublishExtension.DEFAULT_COMPATIBLE_PUBLISH_CONFIG)) {
                    throw new InvalidUserDataException(
                            String.format("Cannot find the configuration as compatiblePublishConfig '%s'. If you don't want to publish compatible artifact, you can set 'compatiblePublishConfig = null' to disable this feature.", compatiblePublishConfig));
                }
                throw new InvalidUserDataException(
                        String.format("Cannot find the configuration as compatiblePublishConfig '%s'", compatiblePublishConfig));
            }));
        }
    }
}
