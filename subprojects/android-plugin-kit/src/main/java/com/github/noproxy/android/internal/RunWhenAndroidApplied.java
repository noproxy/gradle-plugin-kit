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

package com.github.noproxy.android.internal;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.Nullable;

public class RunWhenAndroidApplied {
    private final Project project;
    private final Logger logger;

    public RunWhenAndroidApplied(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    public void runWhenAndroidPluginApplied(Action<? super Plugin> action) {
        final Class<? extends Plugin> androidBasePluginClass = getAndroidBasePluginClass();
        if (androidBasePluginClass != null) {
            project.getPlugins().withType(androidBasePluginClass, action);
        }
    }

    @Nullable
    private Class<? extends Plugin> getAndroidBasePluginClass() {
        try {
            //noinspection unchecked
            return (Class<? extends Plugin>) Class.forName("com.android.build.gradle.api.AndroidBasePlugin",
                    false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.debug("class AndroidBasePlugin not found, the classpath may not contains android plugin.");
            return null;
        }
    }
}
