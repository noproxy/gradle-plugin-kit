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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.component.SoftwareComponentVariant;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.artifacts.configurations.Configurations;
import org.gradle.api.internal.attributes.AttributeContainerInternal;

import java.util.Set;

@NonNullApi
public abstract class ConfigurationSoftwareComponentVariant implements SoftwareComponentVariant {
    private DomainObjectSet<DependencyConstraint> dependencyConstraints;
    private Set<? extends Capability> capabilities;
    private Set<ExcludeRule> excludeRules;
    private AttributeContainer attributes;

    protected abstract Configuration getConfiguration();

    @Override
    public Set<? extends ModuleDependency> getDependencies() {
        return getConfiguration().getIncoming().getDependencies().withType(ModuleDependency.class);
    }

    @Override
    public String getName() {
        return getConfiguration().getName();
    }

    @Override
    public Set<? extends DependencyConstraint> getDependencyConstraints() {
        if (dependencyConstraints == null) {
            dependencyConstraints = getConfiguration().getIncoming().getDependencyConstraints();
        }
        return dependencyConstraints;
    }

    @Override
    public Set<? extends Capability> getCapabilities() {
        if (capabilities == null) {
            this.capabilities = ImmutableSet.copyOf(Configurations.collectCapabilities(getConfiguration(),
                    Sets.newHashSet(),
                    Sets.newHashSet()));
        }
        return capabilities;
    }

    @Override
    public Set<ExcludeRule> getGlobalExcludes() {
        if (excludeRules == null) {
            this.excludeRules = ImmutableSet.copyOf(((ConfigurationInternal) getConfiguration()).getAllExcludeRules());
        }
        return excludeRules;
    }

    @Override
    public AttributeContainer getAttributes() {
        if (attributes == null) {
            attributes = ((AttributeContainerInternal) getConfiguration().getAttributes()).asImmutable();
        }
        return attributes;
    }
}
