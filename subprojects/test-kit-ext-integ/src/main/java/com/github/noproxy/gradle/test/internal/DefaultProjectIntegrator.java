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

import com.github.noproxy.gradle.test.api.AndroidIntegrator;
import com.github.noproxy.gradle.test.api.FileIntegrator;
import com.github.noproxy.gradle.test.api.ProjectIntegrator;
import com.github.noproxy.gradle.test.api.SrcIntegrator;
import groovy.lang.Closure;
import org.gradle.api.Action;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DefaultProjectIntegrator implements ProjectIntegrator {
    private final FileIntegratorInternal integrator;

    public DefaultProjectIntegrator(FileIntegrator integrator) {
        this.integrator = (FileIntegratorInternal) integrator;
        ((FileIntegratorInternal) integrator).addCloseable(this);
    }

    @Override
    public File buildFile() {
        return integrator.newFile("build.gradle");
    }

    @Override
    public void buildFile(String append) {
        Actions.appendText(append).execute(buildFile());
    }

    @Override
    public void buildFile(Closure closure) {
        final File buildFile = buildFile();
        closure.setDelegate(buildFile);
        closure.call();
    }

    @Override
    public File properties() {
        return integrator.newFile("gradle.properties");
    }

    @Override
    public File properties(Action<File> action) {
        return integrator.newFile("gradle.properties", action);
    }

    @Override
    public void property(Map<String, String> properties) {
        properties.forEach(this::property);
    }

    @Override
    public void property(String propertyKey, String propertyValue) {
        properties(Actions.appendText(propertyKey + "=" + propertyValue + "\n"));
    }


    @Override
    public void src(Closure srcConfigure) {
        final SrcIntegrator src = new DefaultSrcIntegrator(integrator);
        srcConfigure.setDelegate(src);
        srcConfigure.call();
    }

    @Override
    public void android(Closure androidConfigure) {
        final AndroidIntegrator android = new DefaultAndroidIntegrator(integrator);
        androidConfigure.setDelegate(android);
        androidConfigure.call();
    }

    @Override
    public void close() throws IOException {
        integrator.newFile("settings.gradle");
    }
}
