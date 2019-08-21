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

import com.android.build.gradle.api.LibraryVariant;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.github.noproxy.plugin.android.publish.api.AndroidPublishExtension;
import io.github.noproxy.plugin.android.publish.api.AndroidVariantArtifactMapping;
import io.github.noproxy.plugin.android.publish.api.AndroidVariantModuleMapping;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.specs.Spec;
import org.jetbrains.annotations.Nullable;

public class DefaultAndroidPublishExtension implements AndroidPublishExtension {
    private boolean legacyPublish = true;
    private Spec<LibraryVariant> variantSpec;
    private AndroidVariantArtifactMapping variantMapping;
    private AndroidVariantModuleMapping legacyVariantMapping;
    private String compatiblePublishConfig = DEFAULT_COMPATIBLE_PUBLISH_CONFIG;

    public boolean isLegacyPublish() {
        return legacyPublish;
    }

    @Override
    public void setLegacyPublish(boolean enable) {
        legacyPublish = enable;
    }

    @Override
    public void filterVariants(Spec<LibraryVariant> spec) {
        variantSpec = spec;
    }

    @Override
    public void filterVariants(@DelegatesTo(LibraryVariant.class) Closure closure) {
        variantSpec = element -> {
            closure.setDelegate(element);
            Object value = closure.call(element);
            return (Boolean) InvokerHelper.invokeMethod(value, "asBoolean", null);
        };
    }

    @Override
    public void mapVariant(AndroidVariantArtifactMapping mapping) {
        variantMapping = mapping;
    }

    @Override
    public void mapVariant(Closure closure) {
        variantMapping = (artifact, androidVariant, scopeMapping) -> {
            closure.setDelegate(artifact);
            closure.call(androidVariant, scopeMapping);
        };
    }

    @Nullable
    public Spec<LibraryVariant> getVariantSpec() {
        return variantSpec;
    }

    @Nullable
    public AndroidVariantArtifactMapping getVariantMapping() {
        return variantMapping;
    }

    @Nullable
    public String getCompatiblePublishConfig() {
        return compatiblePublishConfig;
    }

    @Override
    public void setCompatiblePublishConfig(String compatiblePublishConfig) {
        this.compatiblePublishConfig = compatiblePublishConfig;
    }

    @Override
    public void mapLegacyVariant(AndroidVariantModuleMapping mapping) {
        legacyVariantMapping = mapping;
    }

    @Override
    public void mapLegacyVariant(Closure closure) {
        legacyVariantMapping = (moduleCoordinate, androidVariant) -> {
            closure.setDelegate(moduleCoordinate);
            closure.call(androidVariant);
        };
    }

    @Nullable
    public AndroidVariantModuleMapping getLegacyVariantMapping() {
        return legacyVariantMapping;
    }
}
