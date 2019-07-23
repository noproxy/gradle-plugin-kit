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

package com.github.noproxy.android.internal.api.annotationprocessor;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.AndroidBasePlugin;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.AndroidJavaCompile;
import com.android.build.gradle.tasks.ProcessAnnotationsTask;
import com.android.builder.model.BuildType;
import com.github.noproxy.android.api.annotationprocessor.AnnotationProcessorArguments;
import com.github.noproxy.android.api.annotationprocessor.AnnotationProcessorUtils;
import com.github.noproxy.android.internal.InternalErrorException;
import com.google.inject.Provider;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class DefaultAnnotationProcessorUtils implements AnnotationProcessorUtils {
    private static final String KAPT_CONFIGURATION_NAME = "kapt";
    private static final String KOTLIN_KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt";
    private final Project project;
    private final DependencyHandler dependencies;
    private final AnnotationProcessorArguments beforeEvaluating;
    private final AnnotationProcessorArguments evaluated;

    /**
     * Please use {@link #DefaultAnnotationProcessorUtils(Project)} if you don't use Dependency Inject.
     */
    @Inject
    public DefaultAnnotationProcessorUtils(Project project, DependencyHandler dependencies, Provider<BaseExtension> androidExtensionProvider) {
        new Provider<BaseExtension>() {

            @Override
            public BaseExtension get() {
                return null;
            }
        };
        this.project = project;
        this.dependencies = dependencies;
        evaluated = new EvaluatedAnnotationProcessorArguments(project);
        beforeEvaluating = new BeforeEvaluatingAnnotationProcessorArguments(project, androidExtensionProvider);
    }

    @SuppressWarnings("unused")
    public DefaultAnnotationProcessorUtils(Project project) {
        this.project = project;
        this.dependencies = project.getDependencies();
        evaluated = new EvaluatedAnnotationProcessorArguments(project);
        beforeEvaluating = new BeforeEvaluatingAnnotationProcessorArguments(project, () -> {
            try {
                return project.getExtensions().getByType(AppExtension.class);
            } catch (Exception e) {
                return project.getExtensions().getByType(LibraryExtension.class);
            }
        });
    }

    @Override
    public void registerArgumentAdderTask(Object... dependencies) {
        // always use the evaluated impl
        evaluated.registerArgumentAdderTask(dependencies);
    }

    @Override
    public void addArgument(@NotNull String key, @NotNull String value) {
        assetNotNull(key, value);
        getArguments().addArgument(key, value);
    }

    private void assetNotNull(Object... value) {
        for (Object o : value) {
            Objects.requireNonNull(o);
        }
    }

    @Override
    public void addArgument(@NotNull BuildType buildType, @NotNull String key, @NotNull String value) {
        assetNotNull(buildType, key, value);
        getArguments().addArgument(buildType, key, value);
    }

    @Override
    public void addArgument(@NotNull BaseVariant variant, @NotNull String key, @NotNull String value) {
        assetNotNull(variant, key, value);
        getArguments().addArgument(variant, key, value);
    }

    @Override
    @Incubating
    public void addArgument(@NotNull CommandLineArgumentProvider provider) {
        assetNotNull(provider);
        getArguments().addArgument(provider);
    }

    @Override
    @Incubating
    public void addArgument(@NotNull BuildType buildType, @NotNull CommandLineArgumentProvider provider) {
        assetNotNull(buildType, provider);
        getArguments().addArgument(buildType, provider);
    }

    @Override
    @Incubating
    public void addArgument(@NotNull BaseVariant variant, @NotNull CommandLineArgumentProvider provider) {
        assetNotNull(variant, provider);
        getArguments().addArgument(variant, provider);
    }

    /*
    Kapt will remove one configuration whose dependencies set is empty when 'afterEvaluate', so that all later added dependencies
    will be ignored and never run in KaptTask.
    So if kapt applied, we should add processor right now. if not, we schedule it at 'afterEvaluate' which will come first than kapt's 'afterEvaluate'.
    See: org/jetbrains/kotlin/gradle/internal/kapt/Kapt3KotlinGradleSubplugin.kt:232
     */
    private void maybeAfterEvaluate(Action<? super Project> action) {
        final boolean kaptApplied = project.getPluginManager().hasPlugin(KOTLIN_KAPT_PLUGIN_ID);
        if (kaptApplied) {
            action.execute(project);
        } else {
            project.afterEvaluate(action);
        }
    }

    @Override
    public void addProcessor(String dependencyNotation) {
        addProcessor(dependencyNotation, (Closure) null);
    }

    @Override
    public void addProcessor(String dependencyNotation, Action<ModuleDependency> configureAction) {
        maybeAfterEvaluate(p -> {
            final ModuleDependency dependency = (ModuleDependency) dependencies.add(getAnnotationProcessorConfigurationName(), dependencyNotation);
            if (dependency != null) {
                configureAction.execute(dependency);
            }
        });
    }

    @Override
    public void addProcessor(String dependencyNotation, Closure configureAction) {
        maybeAfterEvaluate(p -> dependencies.add(getAnnotationProcessorConfigurationName(), dependencyNotation, configureAction));
    }

    @Override
    public void addProcessor(Dependency dependency) {
        maybeAfterEvaluate(p -> dependencies.add(getAnnotationProcessorConfigurationName(), dependency));
    }

    @Override
    public Configuration getAnnotationProcessorConfiguration() {
        return project.getConfigurations().getByName(getAnnotationProcessorConfigurationName());
    }

    private String getAnnotationProcessorConfigurationName() {
        if (project.getPluginManager().hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
            return KAPT_CONFIGURATION_NAME;
        } else if (project.getPlugins().hasPlugin(AndroidBasePlugin.class)) {
            return JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME;
        } else if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            return JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME;
        } else {
            throw new InternalErrorException("failed to find annotationProcessor configuration because no support plugin found: java, android or kapt.");
        }
    }

    private AnnotationProcessorArguments getArguments() {
        return optionNotEvaluated() ? beforeEvaluating : evaluated;
    }

    /**
     * Any change to options is ignored if the Android has created tasks.
     *
     * @return whether the change to options will take effect now
     */
    private boolean optionNotEvaluated() {
        return project.getTasks().withType(AndroidJavaCompile.class).isEmpty()
                && project.getTasks().withType(ProcessAnnotationsTask.class).isEmpty();
    }
}
