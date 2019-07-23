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

import com.android.build.gradle.api.AndroidBasePlugin;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.tasks.VariantAwareTask;
import com.android.build.gradle.tasks.AndroidJavaCompile;
import com.android.build.gradle.tasks.ProcessAnnotationsTask;
import com.android.builder.model.BuildType;
import com.github.noproxy.android.api.annotationprocessor.AnnotationProcessorArguments;
import com.github.noproxy.android.internal.InternalErrorException;
import com.github.noproxy.android.internal.InvalidUsageException;
import com.github.noproxy.android.internal.NotSupportException;
import com.google.common.truth.Truth;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.gradle.internal.KaptTask;
import org.jetbrains.kotlin.gradle.internal.KaptWithKotlincTask;
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonMap;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

@SuppressWarnings("UnstableApiUsage")
public class EvaluatedAnnotationProcessorArguments implements AnnotationProcessorArguments {
    private static final String KOTLIN_KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt";
    private final Project project;
    private final Pattern kaptTaskNamePattern = Pattern.compile("^kapt(.*)Kotlin$");

    EvaluatedAnnotationProcessorArguments(Project project) {
        this.project = project;
    }

    @Override
    public void registerArgumentAdderTask(Object... dependencies) {
        project.getTasks().withType(AndroidJavaCompile.class).all(androidJavaCompile -> {
            androidJavaCompile.dependsOn(dependencies);
        });
        project.getTasks().withType(ProcessAnnotationsTask.class).all(processAnnotationsTask -> {
            processAnnotationsTask.dependsOn(dependencies);
        });
        project.getTasks().withType(JavaCompile.class, javaCompile -> {
            javaCompile.dependsOn(dependencies);
        });
        project.getTasks().withType(KaptTask.class).all(kaptTask -> {
            kaptTask.dependsOn(dependencies);
        });
    }

    @Override
    public void addArgument(@NotNull String key, @NotNull String value) {
        if (project.getPlugins().hasPlugin(AndroidBasePlugin.class)) {
            // NOTE: when kapt work with android, kapt always reads arguments from android, so we just need
            // add arguments to android.
            addArgumentForAndroid(Specs.satisfyAll(), key, value);
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
            addArgumentForAndroid(Specs.satisfyAll(), provider);
        } else if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            project.getTasks().named(COMPILE_JAVA_TASK_NAME, JavaCompile.class, javaCompile ->
                    javaCompile.getOptions().getCompilerArgumentProviders().add(provider));
        } else {
            throw new InternalErrorException("failed to add annotation processor CommandLineArgumentProvider" +
                    " because no supported plugin found: java, android or kapt.");
        }
    }

    private void addArgumentForAndroid(Spec<VariantAwareTask> taskSpec, CommandLineArgumentProvider provider) {
        assertOptionEvaluated();
        if (project.getTasks().withType(ProcessAnnotationsTask.class).isEmpty()) {
            project.getTasks().withType(AndroidJavaCompile.class).matching(taskSpec).all(androidJavaCompile ->
                    androidJavaCompile.getOptions().getCompilerArgumentProviders().add(provider));
        } else {
            project.getTasks().withType(ProcessAnnotationsTask.class).matching(taskSpec).all(processAnnotationsTask ->
                    processAnnotationsTask.getOptions().getCompilerArgumentProviders().add(provider));
        }
    }

    /**
     * Assert the Android has created tasks. So any change to options will be ignored.
     */
    private void assertOptionEvaluated() {
        Truth.assertThat(project.getTasks().withType(AndroidJavaCompile.class).isEmpty()
                && project.getTasks().withType(ProcessAnnotationsTask.class).isEmpty()).isFalse();
    }

    //TODO register as input
    private void addArgumentForAndroid(Spec<String> variantNameSpec, String key, String value) {
        assertOptionEvaluated();
        if (project.getTasks().withType(ProcessAnnotationsTask.class).isEmpty()) {
            project.getTasks().withType(AndroidJavaCompile.class).matching(task ->
                    variantNameSpec.isSatisfiedBy(task.getVariantName())).all(androidJavaCompile -> {
                androidJavaCompile.getOptions().getCompilerArgs().add("-A" + key + "=" + value);
            });
            project.getTasks().withType(KaptTask.class).matching(element -> {
                final String taskName = element.getName();
                final Matcher matcher = kaptTaskNamePattern.matcher(taskName);
                Truth.assertThat(matcher.matches()).named("kapt task name should match kapt${VariantName}Kotlin: %s", taskName).isTrue();
                final String variantName = matcher.group(1);
                return variantNameSpec.isSatisfiedBy(StringGroovyMethods.uncapitalize(variantName));
            }).all(kaptTask -> {
                if (kaptTask instanceof KaptWithoutKotlincTask) {
                    final KaptWithoutKotlincTask task = ((KaptWithoutKotlincTask) kaptTask);
                    task.setProcessorOptions(plus(task.getProcessorOptions(), singletonMap(key, value)));
                } else if (kaptTask instanceof KaptWithKotlincTask) {
                    throw new NotSupportException("Currently we cannot add any options if the kapt task has been configured");
                    // This won't work.
                    // In method 'org.jetbrains.kotlin.gradle.internal.Kapt3KotlinGradleSubplugin.buildAndAddOptionsTo',
                    // the values in KaptExtension have been read and the list is not reactive.
                    // So there's no point to change kapt extensions any longer.
                    /*
                    project.getExtensions().getByType(KaptExtension.class).arguments(options -> {
                        final BaseVariant variant = (BaseVariant) options.getVariant();
                        if (variant == null || variantNameSpec.isSatisfiedBy(variant.getName())) {
                            options.arg(kaptTask, value);
                        }
                        return Unit.INSTANCE;
                    });
                     */
                    /*
                    TODO we can change API style to persuade user to configure arguments by this way:
                    kapt {
                        arguments { // this closure has a property named `variant`
                            BaseVariant baseVariant = variant
                            arg("applicationId",baseVariant.applicationId)
                        }
                    }
                     */
                }
            });
        } else {
            project.getTasks().withType(ProcessAnnotationsTask.class)
                    .matching(task -> variantNameSpec.isSatisfiedBy(task.getVariantName()))
                    .all(processAnnotationsTask -> {
                        processAnnotationsTask.getOptions().getCompilerArgs().add("-A" + key + "=" + value);
                    });
        }
    }

    @Override
    public void addArgument(@NotNull BuildType buildType, @NotNull String key, @NotNull String value) {
        addArgumentForAndroid(variantName -> variantName.contains(buildType.getName()), key, value);
    }

    @Override
    public void addArgument(@NotNull BuildType buildType, @NotNull CommandLineArgumentProvider provider) {
        addArgumentForAndroid(element -> element.getVariantName().contains(buildType.getName()), provider);
    }

    @Override
    public void addArgument(@NotNull BaseVariant variant, @NotNull CommandLineArgumentProvider provider) {
        addArgumentForAndroid(element -> element.getVariantName().equals(variant.getName()), provider);
    }

    @Override
    public void addArgument(@NotNull BaseVariant variant, @NotNull String key, @NotNull String value) {
        addArgumentForAndroid(variantName -> {
            return variantName.equals(variant.getName());
        }, key, value);
    }
}
