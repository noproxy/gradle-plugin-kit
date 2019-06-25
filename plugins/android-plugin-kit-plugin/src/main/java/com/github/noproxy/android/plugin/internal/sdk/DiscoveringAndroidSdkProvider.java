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

package com.github.noproxy.android.plugin.internal.sdk;

import com.github.noproxy.android.plugin.internal.ExternalException;
import com.google.common.annotations.VisibleForTesting;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

class DiscoveringAndroidSdkProvider implements AndroidSdkProvider {
    private final Project project;
    private final Logger logger;
    private String androidSdkHome;

    DiscoveringAndroidSdkProvider(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    @Override
    public String getSdkHome() {
        if (androidSdkHome == null) {
            androidSdkHome = computeSdkHome();
        }

        return androidSdkHome;
    }

    @VisibleForTesting
    String computeSdkHome() {
        // sdk.dir in local.properties
        // android.dir in local.properties
        // ANDROID_SDK_ROOT by System.getenv
        // ANDROID_HOME by System.getenv
        // android.home by System.getProperty
        final Properties localProperties = loadLocalProperties();

        final String sdkDir = localProperties.getProperty("sdk.dir");
        if (sdkDir != null) {
            use("sdk.dir in local.properties");
            return sdkDir;
        }
        final String androidDir = localProperties.getProperty("android.dir");
        if (androidDir != null) {
            use("android.dir in local.properties");
            return androidDir;
        }

        final String androidSdkRoot = System.getenv().get("ANDROID_SDK_ROOT");
        if (androidSdkRoot != null) {
            use("ANDROID_SDK_ROOT in System env");
            return androidSdkRoot;
        }

        final String androidHome = System.getenv().get("ANDROID_HOME");
        if (androidHome != null) {
            use("ANDROID_HOME in System env");
            return androidHome;
        }

        final String androidHome2 = System.getProperty("android.home");
        if (androidHome2 != null) {
            use("android.home in System properties");
            return androidHome2;
        }

        throw new AndroidSdkNotFoundException();
    }

    private void use(String location) {
        logger.debug("use android sdk location from: {}", location);
    }

    @NotNull
    private Properties loadLocalProperties() {
        final Properties properties = new Properties();
        final File file = project.getRootProject().file("local.properties");
        if (file.exists() && file.isFile() && file.canRead()) {
            try (final FileReader reader = new FileReader(file)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new ExternalException("fail to read file '" + file + "'", e);
            }
        }

        return properties;
    }
}
