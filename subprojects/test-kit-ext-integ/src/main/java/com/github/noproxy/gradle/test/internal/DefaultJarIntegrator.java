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
import com.github.noproxy.gradle.test.api.JarIntegrator;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.jetbrains.annotations.NotNull;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.github.noproxy.gradle.test.internal.Actions.execute;
import static com.github.noproxy.gradle.test.internal.Actions.setText;

class DefaultJarIntegrator extends DefaultZipIntegrator implements JarIntegrator {
    private static final boolean DEBUG = false;
    private final FileIntegrator sourceDir;
    private final List<File> toCompiledSources = new ArrayList<>();

    DefaultJarIntegrator(@NotNull File destZipFile, @NotNull FileIntegratorInternal parent) {
        super(destZipFile, parent);
        sourceDir = newTempFile("java_sources", FilenameUtils.getBaseName(destZipFile.getName()));
    }

    @Override
    public void newClass(String className, String javaSource) {
        final int split = className.lastIndexOf('.');
        final String simpleName;
        final String packageName;
        if (split > 0) {
            simpleName = className.substring(split + 1);
            packageName = className.substring(0, split);
        } else {
            simpleName = className;
            packageName = "";
        }
        newClass(packageName, simpleName, javaSource);
    }

    @Override
    public void newClass(String packageName, String simpleClassName, String javaSource) {
        final String path = packageName.replace('.', File.separatorChar);

        sourceDir.newFile(FileIntegrator.join(path, simpleClassName + ".java"), file -> {
            toCompiledSources.add(file);
            execute(file, setText(javaSource));
        });
    }

    @Override
    public void close() throws IOException {
        if (toCompiledSources.isEmpty()) {
            System.out.println("skip compile for no sources");
            return;
        }

        compile(sourceDir.getRoot());
        archiveClassFile();

        // at last
        super.close();
    }

    private void compile(File sourceDir) throws IOException {
        if (DEBUG) {
            System.out.println("prepare compile, source dir: " + sourceDir);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, Charset.forName("UTF-8"));

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(toCompiledSources);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, getOptions(), null, compilationUnits);
        if (DEBUG) {
            System.out.println("prepare compile, source dir: " + sourceDir);
            System.out.println("start compile");
        }
        boolean success = task.call();
        if (DEBUG) {
            System.out.println("compile end");
        }
        fileManager.close();

        if (!success) {
            final Path root = sourceDir.toPath();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                Path file = Paths.get(diagnostic.getSource().toUri());

                System.err.printf("%s:%d%n %s\n", root.relativize(file), diagnostic.getLineNumber(), diagnostic.getMessage(Locale.US));
            }
            throw new RuntimeException("failed to compile files in " + sourceDir);
        }
    }

    private List<String> getOptions() {
        final String property = System.getProperty("java.class.path");
        final String classpath;
        if (property == null) {
            classpath = getAndroidSdkPath();
        } else {
            classpath = property + File.pathSeparatorChar + getAndroidSdkPath() + "/platforms/android-28/android.jar";
        }

        if (DEBUG)
            System.out.println("classpath=" + classpath);

        return new ArrayList<>(Arrays.asList("-classpath", classpath));
    }

    //TODO improve
    @NotNull
    private String getAndroidSdkPath() {
        final String sdkLocation = System.getProperty("android.home");
        if (sdkLocation == null) {
            throw new IllegalStateException("unable find android.home property, please check you configure it for test task.");
        }
        return sdkLocation;
    }


    private void archiveClassFile() throws IOException {
        final Path root = sourceDir.getRoot().toPath();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".class")) {
                    final Path relativize = root.relativize(file);
                    newFile(relativize.toString(), fileInZip -> {
                        try {
                            ResourceGroovyMethods.setBytes(fileInZip, ResourceGroovyMethods.getBytes(file.toFile()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                return super.visitFile(file, attrs);
            }
        });
    }
}
