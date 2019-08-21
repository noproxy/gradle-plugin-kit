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

package io.github.noproxy.plugin.android.publish.api;

import com.android.build.gradle.api.LibraryVariant;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.specs.Spec;
import org.jetbrains.annotations.Nullable;

public interface AndroidPublishExtension {
    String DEFAULT_COMPATIBLE_PUBLISH_CONFIG = "release";

    void setLegacyPublish(boolean enable);

    void filterVariants(Spec<LibraryVariant> spec);

    void filterVariants(@ClosureParams(value = SimpleType.class, options = "com.android.build.gradle.api.LibraryVariant")
                        @DelegatesTo(LibraryVariant.class) Closure spec);

    void mapVariant(AndroidVariantArtifactMapping mapping);

    void mapVariant(
            @ClosureParams(value = SimpleType.class, options = {
                    "com.android.build.gradle.api.LibraryVariant",
                    "io.github.noproxy.plugin.android.publish.api.ScopeMapping"})
            @DelegatesTo(PublishArtifact.class) Closure closure);


    void setCompatiblePublishConfig(@Nullable String compatiblePublishConfig);

    void mapLegacyVariant(AndroidVariantModuleMapping mapping);

    void mapLegacyVariant(
            @ClosureParams(value = SimpleType.class, options = {
                    "com.android.build.gradle.api.LibraryVariant"})
            @DelegatesTo(ModuleCoordinate.class) Closure closure);
}
