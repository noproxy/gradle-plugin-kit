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
import com.github.noproxy.gradle.test.api.ZipIntegrator;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@NonNullApi
public class DefaultFileIntegrator implements FileIntegrator, FileIntegratorInternal {
    private final File root;
    private final Set<Closeable> children = Sets.newHashSet();
    @Nullable
    private final FileIntegratorInternal parent;

    public DefaultFileIntegrator(File root) {
        this.root = root;
        this.parent = null;
    }

    DefaultFileIntegrator(File root, FileIntegrator parent) {
        this.root = root;
        this.parent = (FileIntegratorInternal) parent;
    }

    @Override
    public void reset() {
        for (File child : Objects.requireNonNull(root.listFiles())) {
            FileUtils.deleteQuietly(child);
        }
    }

    @Override
    public File file(String path) {
        return new File(getRoot(), path);
    }

    @Override
    public File file(String path, Action<File> fileAction) {
        final File file = file(path);
        return configure(file, fileAction);
    }

    private <T> T configure(T t, Action<T> action) {
        Integrators.with(t).configure(action);
        return t;
    }

    private <T> T configure(T t, Closure action) {
        Integrators.with(t).configure(action);
        return t;
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
    public File newZip(String path, Action<ZipIntegrator> action) {
        final File zipFile = file(path);
        action.execute(Integrators.zip(zipFile, this));
        return zipFile;
    }

    @Override
    public File newZip(String path, Closure closure) {
        return newZip(path, Actions.of(closure));
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    public FileIntegrator child(String path) {
        return configure(new DefaultFileIntegrator(newDir(path), this), this::addCloseable);
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
        return configure(new DefaultFileIntegrator(newDir(file), this), this::addCloseable);
    }

    @Override
    public FileIntegrator child(File file, Action<FileIntegrator> fileAction) {
        return configure(child(file), fileAction);
    }

    @Override
    public FileIntegrator child(File file, Closure closure) {
        return configure(child(file), closure);
    }

    /**
     * 1. Composition: Any children closeable should be closed before parent;
     * 2. Inheritance: Derived closeable should be closed before super.
     * <p>
     * If your integrator has some resources to close:
     * do it by {@link #addCloseable(Closeable)} if they should be closed before parent and this;
     * or override {@link #close()} to close them after children(you can custom the order against parent by
     * changing the call super location.)
     *
     * @throws IOException when some error happens
     */
    // TODO use Junit Rule impl auto close
    @Override
    public void close() throws IOException {
        if (close) {
            return;
        }

        close = true;
        children.forEach(closeable -> Actions.close().execute(closeable));
    }

    private boolean close = false;

    @Override
    public void addCloseable(Closeable closeable) {
        children.add(closeable);
    }

    @Override
    public File newTempDir(String type, String name) {
        return newTempFile(type, name).getRoot();
    }

    @Override
    public FileIntegrator newTempFile(String type, String name) {
        if (parent != null) {
            return parent.newTempFile(type, name);
        }

        final String path = FileIntegrator.join(HIDING_DIRECTORY, type, name, "" + System.currentTimeMillis());
        return child(newDir(path));
    }

    @Override
    public FileIntegrator newFixDir(String type, String name) {
        if (parent != null) {
            return parent.newFixDir(type, name);
        }

        final String path = FileIntegrator.join(HIDING_DIRECTORY, type, name);
        return child(newDir(path));
    }

    @Override
    public FileIntegrator mavenDefaults() {
        return newFixDir(MAVEN_DEFAULTS_TYPE, MAVEN_DEFAULTS_NAME);
    }
}
