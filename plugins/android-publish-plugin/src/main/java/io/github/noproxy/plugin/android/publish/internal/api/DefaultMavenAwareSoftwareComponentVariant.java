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

package io.github.noproxy.plugin.android.publish.internal.api;

import com.google.common.collect.Sets;
import io.github.noproxy.plugin.android.publish.api.ScopeMapping;
import io.github.noproxy.plugin.android.publish.internal.ConfigurationSoftwareComponentVariant;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;

import java.util.Set;

@NonNullApi
public class DefaultMavenAwareSoftwareComponentVariant extends ConfigurationSoftwareComponentVariant implements MavenAwareSoftwareComponentVariant {
    private final PublishArtifact mainArtifact;
    private final ScopeMapping scopeMapping;
    private final Configuration configuration;

    public DefaultMavenAwareSoftwareComponentVariant(Configuration configuration, PublishArtifact artifact,
                                                        ScopeMapping scopeMapping) {
        this.configuration = configuration;
        mainArtifact = artifact;
        this.scopeMapping = scopeMapping;
    }

    @Override
    public Set<? extends PublishArtifact> getArtifacts() {
        return Sets.newHashSet(mainArtifact);
    }

    @Override
    protected Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public AttributeContainer getAttributes() {
        //TODO filter attrs
        return super.getAttributes();
    }

    @Override
    public ScopeMapping getScopeMapping() {
        return scopeMapping;
    }
}
