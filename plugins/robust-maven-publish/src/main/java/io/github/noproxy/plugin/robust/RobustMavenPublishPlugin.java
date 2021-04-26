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

package io.github.noproxy.plugin.robust;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.ApplicationVariant;
import io.github.noproxy.plugin.robust.api.Resolver;
import io.github.noproxy.plugin.robust.api.RobustMavenPublishExtension;
import io.github.noproxy.plugin.robust.api.RobustMavenResolverExtension;
import io.github.noproxy.plugin.robust.internal.*;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@SuppressWarnings({"UnstableApiUsage"})
public class RobustMavenPublishPlugin implements Plugin<Project> {
    private static void withApplicationVariants(Project project, Action<? super ApplicationVariant> action) {
        project.getPlugins().withId("com.android.application", plugin -> {
            final AppPlugin appPlugin = (AppPlugin) plugin;
            project.getExtensions().getByType(AppExtension.class).getApplicationVariants().all(action);
        });
    }

    @Override
    public void apply(@NotNull Project project) {
        project.getPluginManager().apply(MavenPublishPlugin.class);

        final RobustMavenPublishExtensionInternal publishExtension =
                (RobustMavenPublishExtensionInternal) project.getExtensions().create(RobustMavenPublishExtension.class,
                        "robustPublish", DefaultRobustMavenPublishExtension.class);

        final RobustMavenResolverExtensionInternal resolverExtension = (RobustMavenResolverExtensionInternal) project.getExtensions()
                .create(RobustMavenResolverExtension.class, "robustResolver",
                        DefaultRobustMavenResolverExtension.class, project);

        project.getPluginManager().withPlugin("robust", appliedPlugin -> project.afterEvaluate(ignored -> {
            configurePublishing(project, publishExtension);
        }));

        Resolver resolver = ((ExtensionAware) resolverExtension).getExtensions().create(Resolver.class, "api", DefaultResolver.class,
                project, resolverExtension, publishExtension);

        project.getPluginManager().withPlugin("auto-patch-plugin", appliedPlugin -> project.afterEvaluate(ignored ->
                configureResolving(project, resolver)));
    }

    private void configurePublishing(Project project, RobustMavenPublishExtensionInternal publishExtension) {
        withApplicationVariants(project, variant -> {
            final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

            final MavenVariantArtifactsLocator locator = publishExtension.getLocatorFactory().createMavenLocator(variant, publishExtension);
            variant.getOutputs().all(baseVariantOutput -> configuringAndroidArtifacts(project, variant, publishing, locator));
        });
    }

    private void configuringAndroidArtifacts(Project project, ApplicationVariant variant,
                                             PublishingExtension publishing, MavenVariantArtifactsLocator locator) {
        publishing.getPublications().create("Robust" + capitalize(variant.getName()), MavenPublication.class, publication -> {
            publication.setGroupId(locator.getGroupId());
            publication.setArtifactId(locator.getArtifactId());
            publication.setVersion(locator.getVersion());

            final File methodMapping = project.file("build/outputs/robust/methodsMap.robust");
            TaskProvider<Task> robustTask = project.getTasks().named("transformClassesWithRobustFor" + capitalize(variant.getName()));
            publication.artifact(methodMapping, artifact -> {
                artifact.setExtension(locator.getExtension(ArtifactType.METHOD_MAPPING));
                artifact.setClassifier(locator.getClassifier(ArtifactType.METHOD_MAPPING));
            }).builtBy(robustTask);
            if (variant.getBuildType().isMinifyEnabled()) {
                publication.artifact(variant.getMappingFileProvider().get().getSingleFile(), artifact -> {
                    artifact.setExtension(locator.getExtension(ArtifactType.MAPPING));
                    artifact.setClassifier(locator.getClassifier(ArtifactType.MAPPING));
                });
            } else {
                project.getLogger().info("RobustMavenPublish: skip publish mapping.txt for '" + variant.getName() + "' because minifyEnabled = false");
            }
        });
    }

    private void configureResolving(Project project, Resolver resolver) {
        withApplicationVariants(project, variant -> {
            final String variantName = capitalize(variant.getName());
            TaskProvider<Task> robustTask = project.getTasks().named("transformClassesWithAutoPatchTransformFor" + variantName);

            robustTask.configure(task -> {
                final Provider<File> methodMapProvider = project.provider(() -> {
                            final File methodMapping = Objects.requireNonNull(resolver.resolveMethodMapping(variant),
                                    "Cannot find base method map file in Maven repository");
                            project.getLogger().quiet("build robust with method mapping: " + methodMapping);

                            return methodMapping;
                        }
                );
                task.getInputs().file(methodMapProvider).withPathSensitivity(PathSensitivity.NONE);

                final Provider<File> mappingProvider = project.provider(() -> {
                    final File mapping = resolver.resolveMapping(variant);
                    project.getLogger().quiet("build robust with mapping: " + mapping);
                    if (mapping == null || !mapping.exists()) {
                        return null;
                    }
                    return mapping;
                });
                task.getInputs().file(mappingProvider).optional().withPathSensitivity(PathSensitivity.NONE);

                task.doFirst(ignored -> {
                    File methodMap = methodMapProvider.get();
                    try {
                        File file = project.file("robust/methodsMap.robust");
                        FileUtils.copyFile(methodMap, file);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                task.doFirst(ignored -> {
                    File mapping = mappingProvider.getOrNull();
                    try {
                        File file = project.file("robust/mapping.txt");
                        if (mapping != null) {
                            FileUtils.copyFile(mapping, file);
                        } else {
                            // create empty file
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });
        });
    }
}
