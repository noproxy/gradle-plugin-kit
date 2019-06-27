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

import com.github.noproxy.gradle.test.api.FileIntegrator;

import com.google.common.collect.Sets;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import groovy.lang.Closure;

import static com.github.noproxy.gradle.test.api.extension.KitDefaultMethods.configure;

@NonNullApi
public class DefaultFileIntegrator implements FileIntegrator, FileIntegratorInternal {
    private final File root;
    private final Set<Closeable> children = Sets.newHashSet();

    public DefaultFileIntegrator(File root) {
        this.root = root;
    }

    @Override
    public File file(String path) {
        return new File(getRoot(), path);
    }

    @Override
    public File file(String path, Action<File> fileAction) {
        return configure(file(path), fileAction);
    }

    @Override
    public File file(String path, Closure closure) {
        return configure(file(path), closure);
    }

    @Override
    public File newDir(String path) {
        return configure(new File(getRoot(), path), Actions.mkdirs());
    }

    @Override
    public File newDir(String path, Action<File> fileAction) {
        return configure(newDir(path), fileAction);
    }

    @Override
    public File newDir(String path, Closure closure) {
        return configure(newDir(path), closure);
    }

    @Override
    public File newDir(File file) {
        return configure(file, Actions.mkdirs());
    }

    @Override
    public File newDir(File file, Action<File> fileAction) {
        return configure(newDir(file), fileAction);
    }

    @Override
    public File newDir(File file, Closure closure) {
        return configure(newDir(file), closure);
    }

    @Override
    public File newFile(String path) {
        return configure(file(path), file -> {
            newDir(file.getParentFile());
            newFile(file);
        });
    }

    @Override
    public File newFile(String path, Action<File> fileAction) {
        return configure(newFile(path), fileAction);
    }

    @Override
    public File newFile(String path, Closure closure) {
        return configure(newFile(path), closure);
    }

    @Override
    public File newFile(File file) {
        return configure(file, Actions.createFile());
    }

    @Override
    public File newFile(File file, Action<File> fileAction) {
        return configure(newFile(file), fileAction);
    }

    @Override
    public File newFile(File file, Closure closure) {
        return configure(newFile(file), closure);
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    public FileIntegrator child(String path) {
        return configure(new DefaultFileIntegrator(newDir(path)), this::addCloseable);
    }

    @Override
    public FileIntegrator child(String path, Action<FileIntegrator> fileAction) {
        return configure(child(path), fileAction);
    }

    @Override
    public FileIntegrator child(String path, Closure closure) {
        return configure(child(path), closure);
    }

    @Override
    public FileIntegrator child(File file) {
        return configure(new DefaultFileIntegrator(newDir(file)), this::addCloseable);
    }

    @Override
    public FileIntegrator child(File file, Action<FileIntegrator> fileAction) {
        return configure(child(file), fileAction);
    }

    @Override
    public FileIntegrator child(File file, Closure closure) {
        return configure(child(file), closure);
    }

    @Override
    public void close() throws IOException {
        children.forEach(closeable -> Actions.close().execute(closeable));
    }

    @Override
    public void addCloseable(Closeable closeable) {
        children.add(closeable);
    }
}
