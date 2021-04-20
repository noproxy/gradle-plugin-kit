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

import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocator;
import org.apache.commons.lang3.ObjectUtils;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.specs.Spec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class MavenVariantArtifactsLocator implements VariantArtifactsLocator {
    private final BaseVariant variant;
    private final String groupId;
    private final String artifactId;
    private final String bareVersion;

    public MavenVariantArtifactsLocator(@NotNull ApplicationVariant variant,
                                        @Nullable String groupId,
                                        @Nullable String artifactId,
                                        @Nullable String bareVersion) {
        this.variant = variant;
        this.groupId = ObjectUtils.firstNonNull(groupId, "org.robust.meta");
        this.artifactId = ObjectUtils.firstNonNull(artifactId, variant.getApplicationId());
        this.bareVersion = Objects.requireNonNull(ObjectUtils.firstNonNull(bareVersion, variant.getVersionName()),
                "You must set a version to publish.");
    }

    @NotNull
    public String getGroupId() {
        return groupId;
    }

    @NotNull
    public String getArtifactId() {
        return artifactId;
    }

    @NotNull
    public String getVersion() {
        final StringBuilder version = new StringBuilder(bareVersion);
        final String flavorName = variant.getFlavorName();
        if (!flavorName.isEmpty()) {
            version.append("-").append(flavorName);
        }

        version.append("-").append(variant.getBuildType().getName());
        return version.toString();
    }

    @Nullable
    public String getClassifier(ArtifactType type) {
        switch (type) {
            case METHOD_MAPPING:
                return null;
            case MAPPING:
                return "mapping";
            default:
                throw new IllegalArgumentException("Unknown ArtifactType: " + type);
        }
    }

    @NotNull
    public String getExtension(ArtifactType type) {
        switch (type) {
            case METHOD_MAPPING:
                return "robust";
            case MAPPING:
                return "txt";
            default:
                throw new IllegalArgumentException("Unknown ArtifactType: " + type);
        }
    }

    @NotNull
    @Override
    public Object getDependencyNotation(ArtifactType type) {
        final String classifier = getClassifier(type);

        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() +
                (classifier == null ? "" : ":" + classifier) + "@" + getExtension(type);
    }

    @NotNull
    @Override
    public Spec<Dependency> getDependencySpec(ArtifactType type) {
        return dependency -> Objects.equals(dependency.getGroup(), getGroupId())
                && dependency.getName().equals(getArtifactId())
                && Objects.equals(dependency.getVersion(), getVersion());
    }

    @Override
    @NotNull
    public Predicate<ResolvedArtifact> getResolvedArtifactSpec(ArtifactType type) {
        return element -> Objects.equals(element.getClassifier(), getClassifier(type));
    }
}
