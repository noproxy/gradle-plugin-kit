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
import groovy.lang.Closure;
import org.gradle.api.Action;

import java.io.File;

public class Integrators {
    static Integrator with(Object object) {
        return new AnyIntegrator(object);
    }

    static ManifestIntegrator manifest(File manifest, FileIntegrator closer) {
        final DefaultManifestIntegrator integrator = new DefaultManifestIntegrator(manifest);
        ((FileIntegratorInternal) closer).addCloseable(integrator);
        return integrator;
    }

    public static ProjectIntegrator project(FileIntegrator closer) {
        final DefaultProjectIntegrator integrator = new DefaultProjectIntegrator(closer);
        ((FileIntegratorInternal) closer).addCloseable(integrator);
        return integrator;
    }

    public static MavenIntegrator mavenDefaults(FileIntegrator closer) {
        final FileIntegratorInternal closerInternal = (FileIntegratorInternal) closer;
        final DefaultMavenIntegrator maven = new DefaultMavenIntegrator(closerInternal.mavenDefaults());
        closerInternal.addCloseable(maven);
        return maven;
    }

    static ZipIntegrator zip(File zipFile, FileIntegratorInternal closer) {
        //noinspection ResultOfMethodCallIgnored
        zipFile.getParentFile().mkdirs();

        final DefaultZipIntegrator zip = new DefaultZipIntegrator(zipFile, closer);
        closer.addCloseable(zip);
        return zip;
    }


    static DefaultJarIntegrator jar(File jarFile, FileIntegratorInternal closer) {
        //noinspection ResultOfMethodCallIgnored
        jarFile.getParentFile().mkdirs();

        final DefaultJarIntegrator jar = new DefaultJarIntegrator(jarFile, closer);
        closer.addCloseable(jar);
        return jar;
    }

    static DefaultAarIntegrator aar(File aarFile, FileIntegratorInternal closer) {
        //noinspection ResultOfMethodCallIgnored
        aarFile.getParentFile().mkdirs();

        final DefaultAarIntegrator aar = new DefaultAarIntegrator(aarFile, closer);
        closer.addCloseable(aar);
        return aar;
    }

    static ModuleIntegrator module(File dir, FileIntegrator closer, String group, String artifact, String version) {
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        ModuleIntegrator module = new DefaultModuleIntegrator(dir, closer, group, artifact, version);
        ((FileIntegratorInternal) closer).addCloseable(module);
        return module;
    }

    private static class AnyIntegrator implements Integrator {
        private final Object object;

        AnyIntegrator(Object object) {
            this.object = object;
        }

        @Override
        public void configure(Closure closure) {
            closure = (Closure) closure.clone();
            closure.setDelegate(object);
            closure.call();
        }

        @Override
        public <T> void configure(Action<T> action) {
            //noinspection unchecked
            action.execute((T) object);
        }
    }
}
