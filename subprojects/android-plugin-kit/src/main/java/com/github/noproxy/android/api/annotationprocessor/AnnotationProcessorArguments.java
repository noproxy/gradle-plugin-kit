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

package com.github.noproxy.android.api.annotationprocessor;

import com.android.build.gradle.api.BaseVariant;
import com.android.builder.model.BuildType;
import org.gradle.api.Incubating;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;

@Incubating
public interface AnnotationProcessorArguments {

    void registerArgumentAdderTask(Object... dependencies);

    /**
     * Add a pair of argument to annotation processing. You can call it at any time before the annotation process task is executed.
     *
     * @param key   of the argument
     * @param value of the argument
     */
    void addArgument(@NotNull String key, @NotNull String value);

    void addArgument(@NotNull BuildType buildType, @NotNull String key, @NotNull String value);

    void addArgument(@NotNull BaseVariant variant, @NotNull String key, @NotNull String value);

    /**
     * Add an argument of type CommandLineArgumentProvider to annotation processing. You can call it at any time before the annotation process task is executed.
     *
     * @param provider of the argument
     */
    @SuppressWarnings("UnstableApiUsage")
    @Incubating
    void addArgument(@NotNull CommandLineArgumentProvider provider);

    @SuppressWarnings("UnstableApiUsage")
    @Incubating
    void addArgument(@NotNull BuildType buildType, @NotNull CommandLineArgumentProvider provider);

    @SuppressWarnings("UnstableApiUsage")
    @Incubating
    void addArgument(@NotNull BaseVariant variant, @NotNull CommandLineArgumentProvider provider);
}
