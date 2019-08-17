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

import java.io.File;

public interface HidingDirectoryProvider {
    String HIDING_DIRECTORY = ".tmp";
    String MAVEN_DEFAULTS_TYPE = "maven";
    String MAVEN_DEFAULTS_NAME = "defaults";
    String MAVEN_DEFAULS_PATH = HIDING_DIRECTORY + File.separator + MAVEN_DEFAULTS_TYPE + File.separator + MAVEN_DEFAULTS_NAME;

    File newTempDir(String type, String name);

    FileIntegrator newTempFile(String type, String name);

    FileIntegrator newFixDir(String type, String name);

    FileIntegrator mavenDefaults();
}
