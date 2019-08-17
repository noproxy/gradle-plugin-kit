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

import static com.github.noproxy.gradle.test.internal.Actions.appendText;
import static com.github.noproxy.gradle.test.internal.extension.DefaultMethods.indent;
import static com.github.noproxy.gradle.test.internal.extension.DefaultMethods.wrap;
import static org.codehaus.groovy.runtime.StringGroovyMethods.stripIndent;

// Only use for root project
class DefaultProjectIntegrator implements ProjectIntegrator {
    private final FileIntegratorInternal integrator;
    private final ScriptContextInternal buildFile = GroovyFactory.createScriptContext();
    private final ScriptContextInternal buildscript = GroovyFactory.createScriptContext();
    private final PluginsContextInternal plugins = GroovyFactory.createPluginsContext();

    DefaultProjectIntegrator(FileIntegrator integrator) {
        this.integrator = (FileIntegratorInternal) integrator;
    }

    private File getBuildGradleFile() {
        return integrator.newFile("build.gradle");
    }

    @Override
    public void buildFile(String append) {
        buildFile.append(append);
    }

    @Override
    public void buildFile(Closure closure) {
        closure = (Closure) closure.clone();
        closure.setDelegate(buildFile);
        closure.call();
    }

    @Override
    public void buildscript(Closure closure) {
        closure = (Closure) closure.clone();
        closure.setDelegate(buildscript);
        closure.call();
    }

    @Override
    public void plugins(Closure closure) {
        closure = (Closure) closure.clone();
        closure.setDelegate(plugins);
        closure.call();
    }

    @Override
    public File settings() {
        return integrator.newFile("settings.gradle");
    }

    @Override
    public void settings(String append) {
        appendText(stripIndent((CharSequence) append)).execute(settings());
    }

    @Override
    public void settings(Closure closure) {
        closure = (Closure) closure.clone();
        closure.setDelegate(settings());
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
        properties(appendText(propertyKey + "=" + propertyValue + "\n"));
    }

    @Override
    public void setRootProjectName(String name) {
        settings("\nrootProject.name = '" + name + "'\n");
    }

    @Override
    public void src(Closure srcConfigure) {
        final SrcIntegrator src = new DefaultSrcIntegrator(integrator);
        srcConfigure = (Closure) srcConfigure.clone();
        srcConfigure.setDelegate(src);
        srcConfigure.call();
    }

    @Override
    public void android(Closure androidConfigure) {
        final AndroidIntegrator android = new DefaultAndroidIntegrator(integrator);
        androidConfigure = (Closure) androidConfigure.clone();
        androidConfigure.setDelegate(android);
        androidConfigure.call();
    }

    @Override
    public void close() throws IOException {
        wrap("buildscript {",
                indent(buildscript, 1),
                "}").appendTo(getBuildGradleFile());

        wrap("plugins {",
                indent(plugins, 1),
                "}").appendTo(getBuildGradleFile());

        buildFile.appendTo(getBuildGradleFile());

        // ensure settings.gradle created
        settings();
    }
}
