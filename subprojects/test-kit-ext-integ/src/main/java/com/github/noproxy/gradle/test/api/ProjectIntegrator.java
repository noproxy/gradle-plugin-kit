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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import java.io.Closeable;
import java.io.File;
import java.util.Map;

public interface ProjectIntegrator extends Closeable, AutoCloseable {
    void buildFile(String append);

    void buildFile(@DelegatesTo(value = ScriptContext.class,
            strategy = Closure.DELEGATE_FIRST)
                           Closure closure);

    void buildscript(@DelegatesTo(value = ScriptContext.class,
            strategy = Closure.DELEGATE_FIRST)
                             Closure closure);

    void plugins(@DelegatesTo(value = PluginsContext.class,
            strategy = Closure.DELEGATE_FIRST)
                         Closure closure);

    File settings();

    void settings(String append);

    void settings(@DelegatesTo(value = File.class,
            strategy = Closure.DELEGATE_FIRST)
                          Closure closure);

    File properties();

    File properties(Action<File> action);

    void property(String propertyKey, String propertyValue);

    void property(Map<String, String> properties);

    void setRootProjectName(String name);

    void src(@DelegatesTo(value = SrcIntegrator.class,
            strategy = Closure.DELEGATE_FIRST) Closure srcConfigure);

    void android(@DelegatesTo(value = AndroidIntegrator.class,
            strategy = Closure.DELEGATE_FIRST) Closure androidConfigure);
}
