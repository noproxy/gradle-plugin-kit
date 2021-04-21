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
import io.github.noproxy.plugin.robust.api.LocalFileVariantArtifactsLocator;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocator;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocatorFactory;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DefaultVariantArtifactsLocatorFactory implements VariantArtifactsLocatorFactory {
    @NotNull
    @Override
    public VariantArtifactsLocator createLocator(Project project, @NotNull RobustMavenPublishExtensionInternal extension, @NotNull RobustMavenResolverExtensionInternal resolverExtension, @NotNull ApplicationVariant variant) {
        final File methodMap = resolverExtension.getMethodMap();
        if (methodMap != null) {
            project.getLogger().info("use local method map for robust: " + methodMap);
            return new LocalFileVariantArtifactsLocator(project, methodMap, resolverExtension.getMapping());
        }

        project.getLogger().info("use maven resolve methodMap file for robust: " + methodMap);
        return new MavenVariantArtifactsLocator(variant, extension.getGroupId(), extension.getArtifactId(), resolverExtension.getVersion());
    }

    @NotNull
    @Override
    public MavenVariantArtifactsLocator createMavenLocator(@NotNull ApplicationVariant variant, @NotNull RobustMavenPublishExtensionInternal extension, String resolveVersion) {
        return new MavenVariantArtifactsLocator(variant, extension.getGroupId(), extension.getArtifactId(), resolveVersion);
    }
}
