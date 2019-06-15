/*
 *    Copyright 2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.noproxy.android.plugin;

import com.android.build.gradle.api.AndroidBasePlugin;
import com.github.noproxy.android.plugin.internal.AndroidPluginKitExtensionInternal;
import com.github.noproxy.android.plugin.internal.AndroidSdkProvider;
import com.github.noproxy.android.plugin.internal.AndroidSdkProviderFactory;
import com.github.noproxy.android.plugin.internal.DefaultAndroidPluginKitExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class AndroidPluginKitPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        final AndroidPluginKitExtensionInternal extension = (AndroidPluginKitExtensionInternal) project.getExtensions().
                create(AndroidPluginKitExtension.class, "androidPluginKit", DefaultAndroidPluginKitExtension.class);

        configureAndroidSdkForTests(project, extension);
    }

    /**
     * android plugin find sdk in this order:
     * 1. sdk.dir in local.properties
     * 2. android.dir in local.properties
     * 3. ANDROID_SDK_ROOT by System.getenv
     * 4. ANDROID_HOME by System.getenv
     * 5. android.home by System.getProperty
     */
    private void configureAndroidSdkForTests(Project project, AndroidPluginKitExtensionInternal extension) {
        final AndroidSdkProvider sdkProvider = new AndroidSdkProviderFactory(extension).createSdkProvider();
        project.getPlugins().withType(AndroidBasePlugin.class, androidBasePlugin -> {
            project.getTasks().withType(Test.class).all(test -> {
                final String sdkHome = sdkProvider.getSdkHome();
                test.systemProperty("android.home", sdkHome);
            });
        });
    }
}
