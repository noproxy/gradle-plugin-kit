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

package com.github.noproxy.gradle.test.api;

import com.github.noproxy.gradle.test.internal.Integrator;
import com.github.noproxy.gradle.test.internal.ModuleIntegrator;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import java.io.Closeable;

//TODO support SNAPSHOT version
// support not create pom
// support modify pom
public interface MavenIntegrator extends Closeable, Integrator {
    void javaModule(String group, String artifact, String version, Action<JarIntegrator> action);

    void androidModule(String group, String artifact, String version, Action<AarIntegrator> action);

    void archiveModule(String group, String artifact, String version, String packaging, Action<ZipIntegrator> action);

    void module(String group, String artifact, String version, String packaging, Action<ModuleIntegrator> action);

    void javaModule(String group, String artifact, String version,
                    @DelegatesTo(value = JarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void androidModule(String group, String artifact, String version,
                       @DelegatesTo(value = AarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void archiveModule(String group, String artifact, String version, String packaging,
                       @DelegatesTo(value = ZipIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void module(String group, String artifact, String version, String packaging,
                @DelegatesTo(value = ModuleIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);
}
