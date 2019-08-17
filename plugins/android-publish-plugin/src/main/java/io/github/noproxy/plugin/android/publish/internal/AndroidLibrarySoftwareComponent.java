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

import com.android.build.gradle.api.LibraryVariant;
import org.gradle.api.component.ComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * It should implement:
 * 1. SoftwareComponentInternal, because MavenPublishPlugin cast it and read usageContext (aka SoftwareComponentVariant)
 * to build runtime and compile variant dependencies for pom. See {@link DefaultMavenPublication#from(org.gradle.api.component.SoftwareComponent)};
 * 2. ComponentWithVariants, because it can activate Gradle Module Metadata Feature. See {@link DefaultMavenPublication#canPublishModuleMetadata()}.
 */
public class AndroidLibrarySoftwareComponent implements ComponentWithVariants, SoftwareComponentInternal {
    private Map<SoftwareComponent, LibraryVariant> variantComponents = new HashMap<>();

    void forEach(BiConsumer<SoftwareComponent, LibraryVariant> action) {
        variantComponents.forEach(action);
    }

    void addVariant(SoftwareComponent component, LibraryVariant variant) {
        variantComponents.put(component, variant);
    }

    @NotNull
    @Override
    public Set<? extends SoftwareComponent> getVariants() {
        return variantComponents.keySet();
    }

    @NotNull
    @Override
    public String getName() {
        return "androidLibrary";
    }

    @Override
    public Set<? extends UsageContext> getUsages() {
//        return variantComponents.values().stream().map(variant -> {
//            final LazyPublishArtifact aar = new LazyPublishArtifact(variant.getPackageLibraryProvider());
//            final Set<PublishArtifact> artifacts = Collections.singleton(aar);
//            return new DefaultUsageContext(variant.getName(), variant.getRuntimeConfiguration().getAttributes(),
//                    artifacts, variant.getRuntimeConfiguration());
//        }).collect(Collectors.toSet());
        return Collections.emptySet();
    }
}
