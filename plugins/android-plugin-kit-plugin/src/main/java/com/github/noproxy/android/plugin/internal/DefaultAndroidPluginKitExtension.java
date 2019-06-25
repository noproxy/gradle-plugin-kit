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

import org.gradle.api.Project;
import org.jetbrains.annotations.Nullable;

import java.io.File;

class DefaultAndroidPluginKitExtension implements AndroidPluginKitExtension, AndroidPluginKitExtensionInternal {
    private final Project project;
    private File androidSdk;

    public DefaultAndroidPluginKitExtension(Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public File getTestAndroidSdk() {
        return androidSdk;
    }

    @Override
    public void setTestAndroidSdk(Object object) {
        androidSdk = project.file(object);
    }
}
