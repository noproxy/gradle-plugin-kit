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

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

@Incubating
public interface AnnotationProcessorUtils extends AnnotationProcessorArguments {
    /**
     * Add a dependency to annotation processor configuration. You can call it before the configuration is frozen by resolving.
     */
    void addProcessor(String dependencyNotation);

    void addProcessor(String dependencyNotation, Action<ModuleDependency> configureAction);

    void addProcessor(String dependencyNotation, Closure configureAction);

    void addProcessor(Dependency dependency);

    Configuration getAnnotationProcessorConfiguration();
}
