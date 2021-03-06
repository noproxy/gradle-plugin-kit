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
import com.github.noproxy.gradle.test.api.ManifestIntegrator;
import groovy.lang.Closure;

import java.io.File;

class DefaultAndroidIntegrator implements AndroidIntegrator {
    private static final String ANDROID_MANIFEST_XML_PATH = "src/main/AndroidManifest.xml";
    private final FileIntegrator integrator;

    DefaultAndroidIntegrator(FileIntegrator dir) {
        this.integrator = dir;
    }

    @Override
    public void manifest(String content) {
        Actions.setText(content).execute(integrator.file(ANDROID_MANIFEST_XML_PATH));

        final File manifest = integrator.file(ANDROID_MANIFEST_XML_PATH);
        final ManifestIntegrator manifestIntegrator = Integrators.manifest(manifest, integrator);

        manifestIntegrator.setContent(content);
    }

    @Override
    public void manifest(Closure closure) {
        final File manifest = integrator.newFile(ANDROID_MANIFEST_XML_PATH);
        closure = (Closure) closure.clone();
        closure.setDelegate(Integrators.manifest(manifest, integrator));
        closure.call();
    }
}
