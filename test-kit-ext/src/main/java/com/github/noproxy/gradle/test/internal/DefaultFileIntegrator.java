/*
 *    Copyright 2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.noproxy.gradle.test.internal;

import com.github.noproxy.gradle.test.api.FileIntegrator;

import org.gradle.internal.impldep.org.testng.collections.Sets;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

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
    public File newDir(String path) {
        final File dir = new File(getRoot(), path);
        dir.mkdirs();
        return dir;
    }

    @Override
    public File newDir(File file) {
        file.mkdirs();
        return file;
    }

    @Override
    public File newFile(String path) {
        final File file = file(path);
        newDir(file.getParentFile());
        return newFile(file);
    }

    @Override
    public File newFile(File file) {
        Actions.createFile().execute(file);
        return file;
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    public FileIntegrator child(String path) {
        return new DefaultFileIntegrator(newDir(path));
    }

    @Override
    public FileIntegrator child(File file) {
        return new DefaultFileIntegrator(newDir(file));
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
