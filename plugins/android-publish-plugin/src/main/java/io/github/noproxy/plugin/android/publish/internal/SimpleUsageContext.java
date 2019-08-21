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

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.component.SoftwareComponentVariant;
import org.gradle.api.internal.component.MavenPublishingAwareContext;
import org.gradle.api.internal.component.UsageContext;

import java.util.Set;

@NonNullApi
class SimpleUsageContext implements UsageContext, MavenPublishingAwareContext {
    private final SoftwareComponentVariant delegate;
    private final io.github.noproxy.plugin.android.publish.api.ScopeMapping scopeMapping;

    SimpleUsageContext(SoftwareComponentVariant delegate, io.github.noproxy.plugin.android.publish.api.ScopeMapping scopeMapping) {
        this.delegate = delegate;
        this.scopeMapping = scopeMapping;
    }

    @Override
    public Set<? extends PublishArtifact> getArtifacts() {
        return delegate.getArtifacts();
    }

    @Override
    public Set<? extends ModuleDependency> getDependencies() {
        return delegate.getDependencies();
    }

    @Override
    public Set<? extends DependencyConstraint> getDependencyConstraints() {
        return delegate.getDependencyConstraints();
    }

    @Override
    public Set<? extends Capability> getCapabilities() {
        return delegate.getCapabilities();
    }

    @Override
    public Set<ExcludeRule> getGlobalExcludes() {
        return delegate.getGlobalExcludes();
    }

    @Override
    public AttributeContainer getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Usage getUsage() {
        throw new UnsupportedOperationException("This method has been deprecated, should never be called");
    }

    @Override
    public ScopeMapping getScopeMapping() {
        return ScopeMapping.valueOf(scopeMapping.name());
    }
}
