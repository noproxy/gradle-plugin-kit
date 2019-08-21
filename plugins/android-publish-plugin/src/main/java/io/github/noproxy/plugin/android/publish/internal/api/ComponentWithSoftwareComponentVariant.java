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

import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;

import java.util.Set;


/**
 * The order of the {@link SoftwareComponentInternal#getUsages()} is important. And gradle will sort by
 * {@link MavenAwareSoftwareComponentVariant#getScopeMapping()} if we implement {@link AdhocComponentWithVariants}.
 *
 * @see DefaultMavenPublication#getSortedUsageContexts()
 */
@SuppressWarnings("JavadocReference")
@NonNullApi
public interface ComponentWithSoftwareComponentVariant extends SoftwareComponent, AdhocComponentWithVariants {
    @Override
    default void addVariantsFromConfiguration(Configuration outgoingConfiguration, Action<? super ConfigurationVariantDetails> action) {
        throw new UnsupportedOperationException("not used");
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean addSoftwareComponentVariant(MavenAwareSoftwareComponentVariant variant);

    Set<? extends MavenAwareSoftwareComponentVariant> getSoftwareComponentVariants();
}
