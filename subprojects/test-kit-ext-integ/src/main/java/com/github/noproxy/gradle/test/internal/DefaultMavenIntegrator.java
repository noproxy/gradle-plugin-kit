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

import com.github.noproxy.gradle.test.api.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.internal.impldep.org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

class DefaultMavenIntegrator implements MavenIntegrator {

    private final FileIntegrator mavenRoot;
    private ListMultimap<Module, String> artifactVersions = ArrayListMultimap.create();

    DefaultMavenIntegrator(FileIntegrator mavenRoot) {
        this.mavenRoot = mavenRoot;
    }

    private static String toPath(String dotString) {
        return dotString.replace('.', File.separatorChar);
    }

    public static String artifactName(String artifact, String version, String ext) {
        return artifact + "-" + version + "." + ext;
    }

    public static String artifactName(String artifact, String version, @Nullable String classifier, String ext) {
        if (classifier != null && classifier.trim().length() > 0) {
            return artifact + "-" + version + "-" + classifier + "." + ext;
        }

        return artifact + "-" + version + "." + ext;
    }

    private static String mavenMetaData(String group, String artifact, Iterable<String> versions, String latestVersion) {
        final String start = String.format("<metadata>\n<groupId>%s</groupId>\n<artifactId>%s</artifactId>\n<versioning>\n<latest>%s</latest>\n<release>%s</release>\n<versions>",
                group, artifact, latestVersion, latestVersion);
        StringBuilder s = new StringBuilder(start);

        for (String version : versions) {
            s.append("<version>").append(version).append("</version>\n");
        }

        final String time = new SimpleDateFormat("YYYYMMDDSSSS").format(new Date());

        final String end = String.format("</versions>\n<lastUpdated>%s</lastUpdated>\n</versioning>\n</metadata>", time);
        s.append(end);
        return s.toString();
    }

    private static String findLatestVersion(List<String> versions) {
        return versions.stream()
                .map(ComparableVersion::new)
                .max(Comparator.naturalOrder())
                .map(ComparableVersion::toString).orElse("");
    }

    public FileIntegrator getMavenRoot() {
        return mavenRoot;
    }

    @Override
    public void javaModule(String group, String artifact, String version, Action<JarIntegrator> action) {
        this.module(group, artifact, version, "jar", dir -> {
            File jar = dir.file(artifactName(artifact, version, "jar"));
            final DefaultJarIntegrator context = Integrators.jar(jar, (FileIntegratorInternal) mavenRoot);
            action.execute(context);
        });
    }

    @Override
    public void androidModule(String group, String artifact, String version, Action<AarIntegrator> action) {
        this.module(group, artifact, version, "aar", dir -> {
            File aar = dir.file(artifactName(artifact, version, "aar"));
            final DefaultAarIntegrator context = Integrators.aar(aar, (FileIntegratorInternal) mavenRoot);
            action.execute(context);
        });
    }

    @Override
    public void archiveModule(String group, String artifact, String version, String packaging, Action<ZipIntegrator> action) {
        this.module(group, artifact, version, packaging, dir -> dir.newZip(artifactName(artifact, version, packaging), action));
    }

    @Override
    public void module(String group, String artifact, String version, String packaging, Action<ModuleIntegrator> action) {
        addArtifactVersion(group, artifact, version);

        // create pom
        final File versionDir = mavenRoot.file(FileIntegrator.join(toPath(group), artifact, version));
        ModuleIntegrator moduleIntegrator = Integrators.module(versionDir, mavenRoot, group, artifact, version);

        final String pom = String.format("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "<modelVersion>4.0.0</modelVersion>\n" +
                "<groupId>%s</groupId>\n" +
                "<artifactId>%s</artifactId>\n" +
                "<version>%s</version>\n" +
                "<packaging>aar</packaging>\n" +
                "<name>%s</name>\n" +
                "<description>Generated Module By DefaultMavenIntegrator</description>\n" +
                "</project>", group, artifact, version, artifact);
        moduleIntegrator.newFile(artifactName(artifact, version, "pom"), Actions.setText(pom));

        action.execute(moduleIntegrator);
    }

    @Override
    public void javaModule(String group, String artifact, String version,
                           @DelegatesTo(value = JarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        javaModule(group, artifact, version, Actions.of(closure));
    }

    @Override
    public void androidModule(String group, String artifact, String version,
                              @DelegatesTo(value = AarIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        androidModule(group, artifact, version, Actions.of(closure));
    }

    @Override
    public void archiveModule(String group, String artifact, String version, String packaging,
                              @DelegatesTo(value = ZipIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        archiveModule(group, artifact, version, packaging, Actions.of(closure));
    }

    @Override
    public void module(String group, String artifact, String version, String packaging,
                       @DelegatesTo(value = ModuleIntegrator.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        module(group, artifact, version, packaging, Actions.of(closure));
    }

    private void addArtifactVersion(String group, String artifact, String version) {
        artifactVersions.put(Module.of(group, artifact), version);
    }

    @Override
    public void close() {
        // create all maven metadata
        for (Module module : artifactVersions.keySet()) {
            //Note: artifactId don't need to replace dot with slash
            final String path = FileIntegrator.join(toPath(module.group), module.artifact, "maven-metadata.xml");
            final List<String> versions = artifactVersions.get(module);
            mavenRoot.newFile(path, Actions.setText(mavenMetaData(module.group, module.artifact, versions, findLatestVersion(versions))));
        }
    }

    private static class Module {
        final String group;
        final String artifact;

        private Module(@NotNull String group, @NotNull String artifact) {
            this.group = group;
            this.artifact = artifact;
        }

        static Module of(@NotNull String group, @NotNull String artifact) {
            return new Module(group, artifact);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Module) {
                return group.equals(((Module) obj).group) && artifact.equals(((Module) obj).artifact);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = group.hashCode();
            result = 31 * result + artifact.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Module[" + group + "," + artifact + ']';
        }
    }
}
