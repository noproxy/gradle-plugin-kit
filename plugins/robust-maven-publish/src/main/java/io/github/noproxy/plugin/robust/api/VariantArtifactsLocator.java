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

package io.github.noproxy.plugin.robust.api;

import io.github.noproxy.plugin.robust.internal.ArtifactType;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.specs.Spec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface VariantArtifactsLocator {
    @Nullable
    Object getDependencyNotation(ArtifactType type);

    @NotNull
    Spec<Dependency> getDependencySpec(ArtifactType type);

    @NotNull
    Predicate<ResolvedArtifact> getResolvedArtifactSpec(ArtifactType type);
}
