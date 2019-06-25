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

package com.github.noproxy.android.plugin.internal;

import com.github.noproxy.android.plugin.AndroidPluginKitExtension;
import com.github.noproxy.android.plugin.internal.logger.PropertyPromotingLoggerFactory;
import com.github.noproxy.android.plugin.internal.sdk.AndroidSdkProvider;
import com.github.noproxy.android.plugin.internal.sdk.AndroidSdkProviderFactory;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.Test;


public class AndroidPluginKitPluginImpl {
    private static final String ANDROID_HOME_SYSTEM_PROPERTY = "android.home";
    private static final String PROPERTY_NAMING_SPACE = "com.github.noproxy.android-plugin-kit-plugin";
    private static final String PROPERTY_PROMOTING_LOG = PROPERTY_NAMING_SPACE + ".log";
    private final Project project;
    private final Logger logger;
    private final AndroidPluginKitExtensionInternal extension;


    public AndroidPluginKitPluginImpl(Project project) {
        this.project = project;
        this.logger = createLogger();
        this.extension = createExtension();
    }

    private Logger createLogger() {
        return new PropertyPromotingLoggerFactory(project, PROPERTY_PROMOTING_LOG).promoting(project.getLogger());
    }

    private AndroidPluginKitExtensionInternal createExtension() {
        return (AndroidPluginKitExtensionInternal) project.getExtensions().
                create(AndroidPluginKitExtension.class, "androidPluginKit", DefaultAndroidPluginKitExtension.class, project);
    }

    /**
     * android plugin find sdk in this order:
     * 1. sdk.dir in local.properties
     * 2. android.dir in local.properties
     * 3. ANDROID_SDK_ROOT by System.getenv
     * 4. ANDROID_HOME by System.getenv
     * 5. android.home by System.getProperty
     */
    public void configureAndroidSdkForTests() {
        final AndroidSdkProviderFactory androidSdkProviderFactory = new AndroidSdkProviderFactory(project, logger, extension);
        project.afterEvaluate(ignored -> {
            // wait for extension evaluated
            final AndroidSdkProvider sdkProvider = androidSdkProviderFactory.createSdkProvider();
            project.getTasks().withType(Test.class).configureEach(test -> {
                final String sdkHome = sdkProvider.getSdkHome();
                logger.debug("add system property '{}={}' to test task '{}'", ANDROID_HOME_SYSTEM_PROPERTY, sdkHome, test.getName());
                test.systemProperty(ANDROID_HOME_SYSTEM_PROPERTY, sdkHome);
            });
        });

    }
}
