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

import io.github.noproxy.plugin.robust.api.RobustMavenPublishExtension;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocatorFactory;
import org.jetbrains.annotations.NotNull;

public class DefaultRobustMavenPublishExtension implements RobustMavenPublishExtension, RobustMavenPublishExtensionInternal {
    private String baseVersion;
    private String groupId;
    private String artifactId;
    private VariantArtifactsLocatorFactory locatorFactory;

    @NotNull
    @Override
    public VariantArtifactsLocatorFactory getLocatorFactory() {
        if (locatorFactory == null) {
            return new DefaultVariantArtifactsLocatorFactory();
        }

        return locatorFactory;
    }

    @Override
    public void setLocatorFactory(VariantArtifactsLocatorFactory locatorFactory) {
        this.locatorFactory = locatorFactory;
    }

    @Override
    public void setVersion(String version) {
        baseVersion = version;
    }

    @Override
    public String getVersion() {
        return baseVersion;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
}
