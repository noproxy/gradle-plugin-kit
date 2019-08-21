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

package com.github.noproxy.android.api;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.BasePlugin;
import com.android.build.gradle.LibraryPlugin;
import com.github.noproxy.android.internal.DefaultAndroidProvider;
import org.gradle.api.Action;
import org.gradle.api.Project;

public class AndroidKits {
    public static AndroidProvider provider(Project project) {
        return new DefaultAndroidProvider(project);
    }

    public static void withAppPlugin(Project project, Action<AppPlugin> action) {
        //noinspection unchecked
        project.getPlugins().withId("com.android.application", (Action) action);
    }

    public static void withLibraryPlugin(Project project, Action<LibraryPlugin> action) {
        //noinspection unchecked
        project.getPlugins().withId("com.android.library", (Action) action);
    }

    public static void afterAndroidEvaluate(Project project, Action<BasePlugin> action) {
        withAppPlugin(project, plugin -> project.afterEvaluate(ignored -> action.execute(plugin)));
        withLibraryPlugin(project, plugin -> project.afterEvaluate(ignored -> action.execute(plugin)));
    }
}
