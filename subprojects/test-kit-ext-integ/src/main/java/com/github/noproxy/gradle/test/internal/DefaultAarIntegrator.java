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
import com.github.noproxy.gradle.test.api.AndroidIntegrator;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;

import java.io.File;

class DefaultAarIntegrator extends DefaultZipIntegrator implements AarIntegrator {
    private final AndroidIntegrator androidIntegrator = Integrators.android(this);

    DefaultAarIntegrator(@NotNull File destZipFile, @NotNull FileIntegratorInternal parent) {
        super(destZipFile, parent);
    }

    @Override
    public File classes(Closure closure) {
        final File classes = file("classes.jar");

        closure = (Closure) closure.clone();
        closure.setDelegate(Integrators.jar(classes, this));
        closure.call();
        return classes;
    }

    // begin: AndroidIntegrator
    @Override
    public void manifest(String content) {
        androidIntegrator.manifest(content);
    }

    @Override
    public void manifest(Closure manifestConfigure) {
        androidIntegrator.manifest(manifestConfigure);
    }
}
