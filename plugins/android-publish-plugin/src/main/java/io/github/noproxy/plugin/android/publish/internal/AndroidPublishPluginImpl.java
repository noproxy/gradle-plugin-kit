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

import com.android.build.gradle.internal.dependency.AndroidTypeAttr;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;
import com.github.noproxy.android.api.AndroidKits;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.specs.Spec;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class AndroidPublishPluginImpl {
    private final Project project;
    private final SoftwareComponentFactory softwareComponentFactory;
    private final PublishingExtension publishing;
    private final AndroidVariantCoordinateMapping variantCoordinateMapping = new DashNamingVersionAndroidVariantCoordinateMapping();
    private final Spec<ConfigurationVariant> VARIANT_TYPE_SPEC = element ->
            element.getArtifacts().stream().allMatch(artifact -> AndroidArtifacts.ArtifactType.AAR.getType().equals(artifact.getType()));

    //TODO 我们可以将runtime和compile分离，然后实现接口实现分离
    public AndroidPublishPluginImpl(Project project, SoftwareComponentFactory softwareComponentFactory) {
        this.project = project;
        this.softwareComponentFactory = softwareComponentFactory;
        project.getPluginManager().apply(MavenPublishPlugin.class);
        publishing = project.getExtensions().getByType(PublishingExtension.class);
    }

    public void configureComponents() {
        final AndroidLibrarySoftwareComponent androidLibrary = new AndroidLibrarySoftwareComponent();

        AndroidKits.provider(project).getAndroidVariants().all(variant -> {
            final AdhocComponentWithVariants variantComponent = softwareComponentFactory.adhoc(variant.getName());
            final Configuration runtimeElements = project.getConfigurations().getByName(variant.getName() + "RuntimeElements");
            final Configuration apiElements = project.getConfigurations().getByName(variant.getName() + "ApiElements");

            // android仅publish了aar中的子内容，aar没有publish
            // TODO 在consumer中，可能会因此报错： aar被transform分解后和子内容重复；如果出错就只能改为自行创建Configurations后操作。
            apiElements.getOutgoing().artifact(variant.getPackageLibraryProvider());
            runtimeElements.getOutgoing().artifact(variant.getPackageLibraryProvider());


            variantComponent.addVariantsFromConfiguration(apiElements, details -> {
                final ConfigurationVariant configurationVariant = details.getConfigurationVariant();
                if (VARIANT_TYPE_SPEC.isSatisfiedBy(configurationVariant)) {
                    project.getLogger().quiet("{} compile {}: {}", variant.getName(),
//                            configurationVariant.getAttributes().getAttribute(ArtifactAttributes.ARTIFACT_FORMAT),
                            configurationVariant.getAttributes().getAttribute(AndroidTypeAttr.ATTRIBUTE),
//                            configurationVariant.getAttributes(),
                            configurationVariant.getArtifacts().stream().map(publishArtifact -> publishArtifact.getName() + "." + publishArtifact.getExtension()).collect(Collectors.joining()));
                    details.mapToMavenScope("compile");
                } else {
                    details.skip();
                }
            });
            // 在Gradle中，每一个Configuration的outgoing可能有多个ConfigurationVariant：
            // 如果一个Configuration里有多种不同的Artifacts， 就会产生多个ConfigurationVariant，一个ConfigurationVariant只有一个Artifact。
            // 比如apiElements中，包含aar, classes, res等等，每一个Artifact都是一个ConfigurationVariant。
            //
            // 这个方法（addVariantsFromConfiguration）会把configuration本身，以及所有的ConfigurationVariant依次注册到Components中。
            // 所有这个lambda表达式会被调用多次。
            // 在这里，我们只想publish aar，所以任何包含非aar的ConfigurationVariant都应skip
            variantComponent.addVariantsFromConfiguration(runtimeElements, details -> {
                final ConfigurationVariant configurationVariant = details.getConfigurationVariant();
                if (VARIANT_TYPE_SPEC.isSatisfiedBy(configurationVariant)) {
                    project.getLogger().quiet("{} runtime {}: {}", variant.getName(),
//                            configurationVariant.getAttributes().getAttribute(ArtifactAttributes.ARTIFACT_FORMAT),
                            configurationVariant.getAttributes().getAttribute(AndroidTypeAttr.ATTRIBUTE),
//                            configurationVariant.getAttributes(),
                            configurationVariant.getArtifacts().stream().map(publishArtifact -> publishArtifact.getName() + "." + publishArtifact.getExtension()).collect(Collectors.joining()));
                    details.mapToMavenScope("runtime");
                } else {
                    details.skip();
                }
            });

            androidLibrary.addVariant(variantComponent, variant);
            project.getComponents().add(variantComponent);
        });
        project.getComponents().add(androidLibrary);
    }

    public void configureAndroidLibraryPublish() {
        final PublicationContainer publications = publishing.getPublications();
        final AndroidLibrarySoftwareComponent androidLibrary = (AndroidLibrarySoftwareComponent) project.getComponents().getByName("androidLibrary");

        // wait android variants
        project.getPlugins().withId("com.android.library", plugin -> {
            project.afterEvaluate(ignored -> {
                androidLibrary.forEach((component, variant) -> publications.create(variant.getName() + "AndroidLibrary",
                        MavenPublication.class, mavenPublication -> {
                            mavenPublication.from(component);

                            final ModuleVersionIdentifier identifier = DefaultModuleVersionIdentifier.newId(
                                    mavenPublication.getGroupId(),
                                    mavenPublication.getArtifactId(),
                                    mavenPublication.getVersion()
                            );
                            final ModuleVersionIdentifier variantCoordinate = variantCoordinateMapping.getVariantCoordinate(identifier, variant);

                            mavenPublication.setGroupId(variantCoordinate.getGroup());
                            mavenPublication.setArtifactId(variantCoordinate.getName());
                            mavenPublication.setVersion(variantCoordinate.getVersion());
                        }));
                publications.create("androidLibrary", MavenPublication.class, mavenPublication -> {
                    // SoftwareComponent只包含依赖，依赖管理，产物
                    mavenPublication.from(androidLibrary);
                });
            });
        });
    }
}
