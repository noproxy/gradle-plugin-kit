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

import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.android.build.gradle.api.LibraryVariant;
import com.github.noproxy.android.api.AndroidProvider;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;

@NonNullApi
public class DefaultAndroidProvider implements AndroidProvider {
    private final Project project;

    public DefaultAndroidProvider(Project project) {
        this.project = project;
    }

    @Override
    public NamedDomainObjectContainer<LibraryVariant> getAndroidVariants() {
        final NamedDomainObjectContainer<LibraryVariant> variants = project.container(LibraryVariant.class);
        project.getPlugins().withId("com.android.library", plugin ->
                ((LibraryExtension) ((LibraryPlugin) plugin).getExtension()).getLibraryVariants().all(variants::add));
        return variants;
    }
}
