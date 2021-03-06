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

import com.github.noproxy.gradle.test.internal.Closer;

import org.jetbrains.annotations.Nullable;

@Closer
public interface BuildRunner {
    void buildArgument(String... additionArguments);

    void quiet();

    void setWithPluginClasspath(boolean withPluginClasspath);

    void setGradleVersion(@Nullable String gradleVersion);

    void forwardOutput(boolean forward);

    @Closer
    void run(String... tasksAndArguments);

    String getOutput();

    @Closer
    void assemble(String... additionArguments);

    @Closer
    void configure(String... additionArguments);

    void fail();

    void success();

    boolean isEnableStackTrace();

    void setEnableStackTrace(boolean enable);
}
