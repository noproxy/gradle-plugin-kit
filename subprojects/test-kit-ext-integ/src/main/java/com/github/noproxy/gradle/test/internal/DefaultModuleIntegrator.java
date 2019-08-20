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
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;

import java.io.File;

class DefaultModuleIntegrator extends DefaultFileIntegrator implements ModuleIntegrator {
    private final String groupId;
    private final String artifactId;
    private final String version;

    DefaultModuleIntegrator(File dir, FileIntegrator parent, String groupId, String artifactId, String version) {
        super(dir, parent);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    @Override
    @NotNull
    public String getGroupId() {
        return groupId;
    }

    @Override
    @NotNull
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    @NotNull
    public String getVersion() {
        return version;
    }

    @Override
    public void jar(String classifier, Closure closure) {
        final String filename = DefaultMavenIntegrator.artifactName(artifactId, version, classifier, "jar");
        Integrators.jar(file(filename), this).configure(closure);
    }

    @Override
    public void aar(String classifier, Closure closure) {
        final String filename = DefaultMavenIntegrator.artifactName(artifactId, version, classifier, "aar");
        Integrators.aar(file(filename), this).configure(closure);
    }

    @Override
    public void archive(String classifier, String ext, Closure closure) {
        final String filename = DefaultMavenIntegrator.artifactName(artifactId, version, classifier, ext);
        Integrators.zip(file(filename), this).configure(closure);
    }

    @Override
    public void pom(Closure closure) {
        final String filename = DefaultMavenIntegrator.artifactName(artifactId, version, "pom");
        Integrators.with(file(filename))
                .configure(closure);
    }

    @Override
    public void file(String classifier, String ext, Closure closure) {
        final String filename = DefaultMavenIntegrator.artifactName(artifactId, version, classifier, ext);
        Integrators.with(file(filename))
                .configure(closure);
    }

    @Override
    public void jar(Closure closure) {
        jar(null, closure);
    }

    @Override
    public void aar(Closure closure) {
        aar(null, closure);
    }

    @Override
    public void archive(String ext, Closure closure) {
        archive(null, ext, closure);
    }
}
