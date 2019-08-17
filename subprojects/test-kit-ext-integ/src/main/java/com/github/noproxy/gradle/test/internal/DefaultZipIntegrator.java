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

import com.github.noproxy.gradle.test.api.ZipIntegrator;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.jetbrains.annotations.NotNull;
import org.spockframework.util.NotThreadSafe;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@NotThreadSafe
class DefaultZipIntegrator extends DefaultFileIntegrator implements ZipIntegrator, Closeable {
    private final File destZipFile;

    DefaultZipIntegrator(@NotNull File destZipFile, @NotNull FileIntegratorInternal parent) {
        super(parent.newTempDir("zip", FilenameUtils.getBaseName(destZipFile.getName())), parent);
        this.destZipFile = destZipFile;
    }

    @Override
    public void close() throws IOException {
        super.close();

        try (final FileOutputStream foo = new FileOutputStream(destZipFile);
             final ZipOutputStream zipOOS = new ZipOutputStream(foo)) {
            Path root = getRoot().toPath();
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path relativize = root.relativize(file);
                    final ZipEntry entry = new ZipEntry(relativize.toString());
                    entry.setMethod(ZipEntry.DEFLATED);
                    zipOOS.putNextEntry(entry);
                    if (attrs.isDirectory()) {
                        // nothing
                    } else if (attrs.isRegularFile()) {
                        zipOOS.write(ResourceGroovyMethods.getBytes(file.toFile()));
                    } else {
                        throw new AssertionError("not support file type: " + attrs);
                    }
                    zipOOS.closeEntry();

                    return super.visitFile(file, attrs);
                }
            });
        }
    }


    @NotNull
    @Override
    public File getDestZipFile() {
        return destZipFile;
    }
}
