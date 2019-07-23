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

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.AndroidBasePlugin;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.JavaCompileOptions;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.tasks.AndroidJavaCompile;
import com.android.build.gradle.tasks.ProcessAnnotationsTask;
import com.android.builder.model.BuildType;
import com.github.noproxy.android.api.annotationprocessor.AnnotationProcessorArguments;
import com.github.noproxy.android.internal.InternalErrorException;
import com.github.noproxy.android.internal.InvalidUsageException;
import com.github.noproxy.android.internal.NotSupportException;
import com.google.inject.Provider;
import kotlin.Unit;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.gradle.plugin.KaptExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

@SuppressWarnings("UnstableApiUsage")
public class BeforeEvaluatingAnnotationProcessorArguments implements AnnotationProcessorArguments {
    private static final String KOTLIN_KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt";
    private final Project project;
    private final Provider<BaseExtension> androidExtensionProvider;

    BeforeEvaluatingAnnotationProcessorArguments(Project project, Provider<BaseExtension> androidExtensionProvider) {
        this.project = project;
        this.androidExtensionProvider = androidExtensionProvider;
    }

    @Override
    public void registerArgumentAdderTask(Object... dependencies) {
        throw new NotSupportException();
    }

    @Override
    public void addArgument(@NotNull String key, @NotNull String value) {
        if (project.getPluginManager().hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
            final KaptExtension kaptExtension = project.getExtensions().getByType(KaptExtension.class);
            kaptExtension.arguments(options -> {
                options.arg(key, value);
                return Unit.INSTANCE;
            });
        } else if (project.getPlugins().hasPlugin(AndroidBasePlugin.class)) {
            addArgumentForAndroid(androidExtensionProvider.get().getDefaultConfig().getJavaCompileOptions(), key, value);
        } else if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            project.getTasks().named(COMPILE_JAVA_TASK_NAME, JavaCompile.class, javaCompile ->
                    javaCompile.getOptions().getCompilerArgs().add("-A" + key + "=" + value));
        } else {
            throw new InternalErrorException("failed to add annotation processor argument because no supported plugin found: java, android or kapt.");
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void addArgument(@NotNull CommandLineArgumentProvider provider) {
        final boolean hasAndroid = project.getPlugins().hasPlugin(AndroidBasePlugin.class);
        // The kapt reads all the 'CommandLineArgumentProvider' from android compile options but it doesn't consider pure java project case.
        // libraries/tools/kotlin-gradle-plugin/src/main/kotlin/org/jetbrains/kotlin/gradle/plugin/android/Android25ProjectHandler.kt:183
        if (project.getPluginManager().hasPlugin(KOTLIN_KAPT_PLUGIN_ID) && !hasAndroid) {
            throw new InvalidUsageException("not support this situation: kapt applied without android plugin");
        } else if (hasAndroid) {
            addArgumentForAndroid(androidExtensionProvider.get().getDefaultConfig().getJavaCompileOptions(), provider);
        } else if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            project.getTasks().named(COMPILE_JAVA_TASK_NAME, JavaCompile.class, javaCompile ->
                    javaCompile.getOptions().getCompilerArgumentProviders().add(provider));
        } else {
            throw new InternalErrorException("failed to add annotation processor CommandLineArgumentProvider" +
                    " because no supported plugin found: java, android or kapt.");
        }
    }

    private void addArgumentForAndroid(JavaCompileOptions options, CommandLineArgumentProvider provider) {
        assertNotEvaluated();
        options.getAnnotationProcessorOptions().getCompilerArgumentProviders().add(provider);
    }

    /**
     * Assert the Android has not created tasks. So any change to options will take effect.
     */
    private void assertNotEvaluated() {
        assertThat(project.getTasks().withType(AndroidJavaCompile.class).isEmpty()
                && project.getTasks().withType(ProcessAnnotationsTask.class).isEmpty()).isTrue();
    }

    private void addArgumentForAndroid(JavaCompileOptions options, String key, String value) {
        assertNotEvaluated();
        options.getAnnotationProcessorOptions().getArguments().put(key, value);
    }

    @Override
    public void addArgument(@NotNull BuildType buildType, @NotNull String key, @NotNull String value) {
        addArgumentForAndroid(((CoreBuildType) buildType).getJavaCompileOptions(), key, value);
    }

    @Override
    public void addArgument(@NotNull BuildType buildType, @NotNull CommandLineArgumentProvider provider) {
        addArgumentForAndroid(((CoreBuildType) buildType).getJavaCompileOptions(), provider);
    }

    @Override
    public void addArgument(@NotNull BaseVariant variant, @NotNull CommandLineArgumentProvider provider) {
        addArgumentForAndroid(variant.getJavaCompileOptions(), provider);
    }

    @Override
    public void addArgument(@NotNull BaseVariant variant, @NotNull String key, @NotNull String value) {
        addArgumentForAndroid(variant.getJavaCompileOptions(), key, value);
    }
}
