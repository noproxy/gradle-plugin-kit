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
import com.google.common.base.Joiner;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;

import java.io.Closeable;
import java.io.File;

@NonNullApi
public interface FileIntegrator extends Closeable, Integrator {
    static String join(String... paths) {
        return Joiner.on(File.separator).join(paths);
    }

    File file(String path);

    File file(String path, Action<File> fileAction);

    File file(String path, @DelegatesTo(File.class) Closure closure);

    File newDir(String path);

    File newDir(String path, Action<File> fileAction);

    File newDir(String path, @DelegatesTo(File.class) Closure closure);

    File newDir(File file);

    File newDir(File file, Action<File> fileAction);

    File newDir(File file, @DelegatesTo(File.class) Closure closure);

    File newFile(String path);

    File newFile(String path, Action<File> fileAction);

    File newFile(String path, @DelegatesTo(File.class) Closure closure);

    File newFile(File file);

    File newFile(File file, Action<File> action);

    File newFile(File file, @DelegatesTo(File.class) Closure closure);

    File newZip(String path, Action<ZipIntegrator> action);

    File newZip(String path, @DelegatesTo(ZipIntegrator.class) Closure closure);

    File getRoot();

    FileIntegrator child(String path);

    FileIntegrator child(String path, Action<FileIntegrator> fileAction);

    FileIntegrator child(String path, @DelegatesTo(FileIntegrator.class) Closure closure);

    FileIntegrator child(File file);

    FileIntegrator child(File file, Action<FileIntegrator> fileAction);

    FileIntegrator child(File file, @DelegatesTo(FileIntegrator.class) Closure closure);
}
