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
import com.github.noproxy.gradle.test.api.SrcIntegrator;
import org.gradle.api.NonNullApi;

import org.jetbrains.annotations.Nullable;
import java.io.File;

@NonNullApi
public class DefaultSrcIntegrator implements SrcIntegrator {
    private final FileIntegrator integrator;

    DefaultSrcIntegrator(FileIntegrator integrator) {
        this.integrator = integrator;
    }

    @Override
    public File java(String sourceSet, @Nullable String pathOrPackageName, String simpleClassName) {
        if (pathOrPackageName == null) {
            return integrator.newFile("src/" + sourceSet + "/java" + "/" + simpleClassName + ".java");
        } else {
            final String path = pathOrPackageName.replace('.', '/');
            return integrator.newFile("src/" + sourceSet + "/java" + "/" + path + "/" + simpleClassName + ".java");
        }
    }

    @Override
    public File java(@Nullable String pathOrPackageName, String simpleClassName) {
        return java("main", pathOrPackageName, simpleClassName);
    }

    @Override
    public File java(String className) {
        final int lastDot = className.lastIndexOf('.');
        if (lastDot == 0 || lastDot + 1 >= className.length()) {
            throw new IllegalArgumentException("illegal class name: " + className);
        }

        final String classSimpleName;
        @Nullable final String packageName;
        if (lastDot > 0) {
            classSimpleName = className.substring(lastDot + 1);
            packageName = className.substring(0, lastDot);
        } else {
            classSimpleName = className;
            packageName = null;
        }

        return java(packageName, classSimpleName);
    }
}
