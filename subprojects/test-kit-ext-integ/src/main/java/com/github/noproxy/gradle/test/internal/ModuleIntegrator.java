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

package com.github.noproxy.gradle.test.internal;

import com.github.noproxy.gradle.test.api.AarIntegrator;
import com.github.noproxy.gradle.test.api.FileIntegrator;
import com.github.noproxy.gradle.test.api.JarIntegrator;
import com.github.noproxy.gradle.test.api.ZipIntegrator;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ModuleIntegrator extends FileIntegrator {
    @NotNull
    String getGroupId();

    @NotNull
    String getArtifactId();

    @NotNull
    String getVersion();

    void jar(String classifier, @DelegatesTo(value = JarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void aar(String classifier, @DelegatesTo(value = AarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void archive(String classifier, String ext, @DelegatesTo(value = ZipIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void file(@Nullable String classifier, String ext, @DelegatesTo(value = File.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void jar(@DelegatesTo(value = JarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void aar(@DelegatesTo(value = AarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);

    void archive(String ext, @DelegatesTo(value = ZipIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure);
}
