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
import io.github.noproxy.plugin.android.publish.api.AndroidVariantArtifactMapping;
import io.github.noproxy.plugin.android.publish.api.ScopeMapping;
import io.github.noproxy.plugin.android.publish.internal.api.ComponentWithSoftwareComponentVariant;
import io.github.noproxy.plugin.android.publish.internal.api.DefaultMavenAwareSoftwareComponentVariant;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("WeakerAccess")
@NonNullApi
class AndroidComponentsFactory {
    private static final String ARTIFACTS_CONFIGURATION = "androidArtifacts";
    private final Project project;

    AndroidComponentsFactory(Project project) {
        this.project = project;
        project.getConfigurations().create(ARTIFACTS_CONFIGURATION, configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
    }

    PublishArtifact createArtifact(Object notation) {
        return project.getArtifacts().add(ARTIFACTS_CONFIGURATION, notation);
    }

    PublishArtifact createArtifact(Object notation, Action<? super ConfigurablePublishArtifact> configureAction) {
        return project.getArtifacts().add(ARTIFACTS_CONFIGURATION, notation, configureAction);
    }

    ComponentWithSoftwareComponentVariant createAndroidLibraryWithVariants(String name, @Nullable AndroidVariantArtifactMapping mapping, @Nullable Spec<LibraryVariant> spec) {
        final ComponentWithSoftwareComponentVariant component = createAndroidLibraryComponent(name);
        AndroidKits.provider(project).getAndroidVariants().matching(Optional.ofNullable(spec).orElse(Specs.satisfyAll()))
                .all(variant -> {
                    final AndroidVariantArtifactMapping finalMapping = Optional.ofNullable(mapping)
                            .orElseGet(VariantNameAsClassifier::new);
                    component.addSoftwareComponentVariant(createApiVariant(variant, finalMapping));
                    component.addSoftwareComponentVariant(createRuntimeVariant(variant, finalMapping));
                });
        return component;
    }

    ComponentWithSoftwareComponentVariant createAndroidLibraryComponent(String name) {
        return new DefaultComponentWithSoftwareComponentVariant(project, name);
    }

    DefaultMavenAwareSoftwareComponentVariant createApiVariant(LibraryVariant androidVariant, AndroidVariantArtifactMapping mapping) {
        final Configuration configuration = AndroidKits.provider(project).getApiElements(androidVariant);
        final PublishArtifact artifact = createArtifact(androidVariant.getPackageLibraryProvider(), mapping.toAction(androidVariant, ScopeMapping.compile));
        return new DefaultMavenAwareSoftwareComponentVariant(configuration, artifact, ScopeMapping.compile);
    }

    DefaultMavenAwareSoftwareComponentVariant createRuntimeVariant(LibraryVariant androidVariant, AndroidVariantArtifactMapping mapping) {
        final Configuration configuration = AndroidKits.provider(project).getRuntimeElements(androidVariant);
        final PublishArtifact artifact = createArtifact(androidVariant.getPackageLibraryProvider(), mapping.toAction(androidVariant, ScopeMapping.runtime));
        return new DefaultMavenAwareSoftwareComponentVariant(configuration, artifact, ScopeMapping.runtime);
    }
}
