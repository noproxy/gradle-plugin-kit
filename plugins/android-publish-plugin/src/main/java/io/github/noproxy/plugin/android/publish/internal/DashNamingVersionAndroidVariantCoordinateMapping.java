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

package io.github.noproxy.plugin.android.publish.internal;

import com.android.build.gradle.api.BaseVariant;
import com.google.common.base.Joiner;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;

import static com.google.common.base.Strings.emptyToNull;

@NonNullApi
public class DashNamingVersionAndroidVariantCoordinateMapping implements AndroidVariantCoordinateMapping {
    @Override
    public ModuleVersionIdentifier getVariantCoordinate(ModuleVersionIdentifier moduleCoordinate, BaseVariant androidVariant) {
        final String baseVersion = moduleCoordinate.getVersion();
        final String flavorName = androidVariant.getFlavorName();
        final String buildTypeName = androidVariant.getBuildType().getName();

        final String variantVersion = Joiner.on('-').skipNulls().join(baseVersion, emptyToNull(flavorName), emptyToNull(buildTypeName));
        return DefaultModuleVersionIdentifier.newId(moduleCoordinate.getModule(), variantVersion);
    }
}
