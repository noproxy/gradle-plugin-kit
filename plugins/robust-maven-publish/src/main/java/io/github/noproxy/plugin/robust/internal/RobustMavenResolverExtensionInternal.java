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

package io.github.noproxy.plugin.robust.internal;

import io.github.noproxy.plugin.robust.api.RobustMavenResolverExtension;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocatorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface RobustMavenResolverExtensionInternal extends RobustMavenResolverExtension {
    @Nullable
    String getVersion();

    @NotNull
    VariantArtifactsLocatorFactory getLocatorFactory();

    @Nullable
    File getMapping();

    @NotNull
    File getMethodMap();

}
