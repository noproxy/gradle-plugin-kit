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

import com.android.build.gradle.BasePlugin;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.scope.CodeShrinker;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.github.noproxy.android.api.AndroidHideApi;
import com.google.inject.Provider;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.NonNullApi;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.lang.reflect.Field;

@NonNullApi
public class DefaultAndroidHideApi implements AndroidHideApi {
    private final Provider<BasePlugin> basePluginProvider;
    private GlobalScope globalScopeCache;

    @Inject
    public DefaultAndroidHideApi(Provider<BasePlugin> basePluginProvider) {
        this.basePluginProvider = basePluginProvider;
    }

    @Override
    public BaseVariantData getVariantData(@NotNull BaseVariant variant) {
        return (BaseVariantData) InvokerHelper.invokeMethod(variant, "getVariantData", null);
    }

    @Override
    public GlobalScope getGlobalScope() {
        if (globalScopeCache == null) {
            try {
                final Field globalScopeField = BasePlugin.class.getDeclaredField("globalScope");
                globalScopeField.setAccessible(true);
                globalScopeCache = (GlobalScope) globalScopeField.get(basePluginProvider.get());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new InternalErrorException("fail to reflective get globeScope from BasePlugin", e);
            }
        }

        return globalScopeCache;
    }

    @Override
    public VariantScope getVariantScope(@NotNull BaseVariant variant) {
        return getVariantData(variant).getScope();
    }

    @Override
    public VariantManager getVariantManager() {
        return basePluginProvider.get().getVariantManager();
    }

    @Override
    public boolean isCodeShrinkerEnable(BaseVariant variant) {
        final VariantScope variantScope = getVariantScope(variant);
        return variantScope.getCodeShrinker() != null || variantScope.getType().isFeatureSplit();
    }

    @Override
    public CodeShrinker getCodeShrinker(BaseVariant variant) {
        return getVariantScope(variant).getCodeShrinker();
    }
}
