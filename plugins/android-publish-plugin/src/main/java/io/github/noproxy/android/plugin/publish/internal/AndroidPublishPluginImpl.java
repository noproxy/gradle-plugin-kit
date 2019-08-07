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

package io.github.noproxy.android.plugin.publish.internal;

import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.android.build.gradle.api.LibraryVariant;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

@SuppressWarnings("ALL")
public class AndroidPublishPluginImpl {
    private final Project project;
    private final SoftwareComponentFactory softwareComponentFactory;
    private final PublishingExtension publishing;

    public AndroidPublishPluginImpl(Project project, SoftwareComponentFactory softwareComponentFactory) {
        this.project = project;
        this.softwareComponentFactory = softwareComponentFactory;
        project.getPluginManager().apply(MavenPublishPlugin.class);
        publishing = project.getExtensions().getByType(PublishingExtension.class);
    }


    public void configureAndroidLibraryPublish() {


        publishing.publications(publications -> {
            publications.create("androidLibrary", MavenPublication.class, mavenPublication -> {
                // SoftwareComponent只包含依赖，依赖管理，产物
                mavenPublication.from(androidLibrary);
                final ConfigurablePublishArtifact artifact = androidLibrary.getArtifactProvider().get();
                configurePublication(mavenPublication, artifact, moduleProvider.getModule(), androidLibrary.getAndroidVariant());
            });
            logger.info("publishing {} by component", androidLibrary.getName());
        });
    }

    public NamedDomainObjectContainer<Configuration> getPublishConfigurations(NamedDomainObjectContainer<LibraryVariant> variants) {
        NamedDomainObjectContainer<Configuration> container = project.container(Configuration.class);
        variants.all(libraryVariant -> container.add(libraryVariant.getRuntimeConfiguration()));
        return container;
    }

    public NamedDomainObjectContainer<LibraryVariant> getAndroidVariants() {
        final NamedDomainObjectContainer<LibraryVariant> variants = project.container(LibraryVariant.class);
        project.getPluginManager().withPlugin("com.android.library", appliedPlugin -> {
            ((LibraryExtension) ((LibraryPlugin) appliedPlugin).getExtension()).getLibraryVariants().all(variants::add);
        });
        return variants;
    }

    public void configureComponents() {
        final AdhocComponentWithVariants android = softwareComponentFactory.adhoc("android");

        getPublishConfigurations(getAndroidVariants()).all(files -> {
            android.addVariantsFromConfiguration(files, details -> {
                ConfigurationVariant variant = details.getConfigurationVariant();

                details.mapToMavenScope();
                variant.

            });
        });
        project.getComponents().add(android);
    }
}
