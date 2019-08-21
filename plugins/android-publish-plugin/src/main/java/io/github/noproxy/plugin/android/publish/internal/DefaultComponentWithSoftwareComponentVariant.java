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
import io.github.noproxy.plugin.android.publish.internal.api.ComponentWithSoftwareComponentVariant;
import io.github.noproxy.plugin.android.publish.internal.api.MavenAwareSoftwareComponentVariant;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.ComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentVariant;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Maven Pom is writen by {@link Usage}.
 * <p>
 * Gradle writes module metadata variants from these type:
 * <dl>
 *     <dt>{@link ComponentWithVariants} to write external variant modules;</dt>
 *     <dd><pre>
 * "variants": [{
 *   "name": "apiElements",
 *   "attributes": {...},
 *   "dependencies": [...],
 *   "available-at": {
 *       "url": "../../library/1.0.0-debug/library-1.0.0-debug.module",
 *       "group": "org.example",
 *       "module": "library",
 *       "version": "1.0.0-debug"
 *   }
 * }]</pre></dd>
 *      <dt>{@link SoftwareComponentVariant} to write variant files.</dt>
 *      <dd><pre>
 * "variants": [{
 *   "name": "apiElements",
 *   "attributes": {...},
 *   "dependencies": [...],
 *   "files": [{
 *       "name": "client-1.0-SNAPSHOT.jar",
 *       "url": "client-1.0-SNAPSHOT.jar",
 *       "size": 539,
 *       "sha1": "1f94fe53d33babdc9de537bb3a0108dbc0e25e4b",
 *       "md5": "6364cdd9923e1eda9b328bc80f93969c"
 *     }]
 * }]</pre></dd>
 * </dl>
 *
 * @implNote <ol>
 * <li>MavenPublishPlugin assumes any component is {@link SoftwareComponentInternal} and reads {@link SoftwareComponentVariant} by {@link SoftwareComponentInternal#getUsages()};</li>
 * <li>Gradle module metadata feature will be activated if type is {@link ComponentWithVariants}.</li>
 * </ol>
 * @see DefaultMavenPublication#from(org.gradle.api.component.SoftwareComponent
 */
public class DefaultComponentWithSoftwareComponentVariant implements
        ComponentWithVariants,
        SoftwareComponentInternal,
        ComponentWithSoftwareComponentVariant {
    private final NamedDomainObjectContainer<MavenAwareSoftwareComponentVariant> variants;
    private final String name;
    private boolean mutable = true;

    public DefaultComponentWithSoftwareComponentVariant(Project project, String name) {
        this.variants = project.container(MavenAwareSoftwareComponentVariant.class);
        this.name = name;
    }

    @Override
    public boolean addSoftwareComponentVariant(MavenAwareSoftwareComponentVariant variant) {
        assertMutable();
        assertNoDuplicatedArtifacts(variant);
        return variants.add(variant);
    }

    private void assertNoDuplicatedArtifacts(MavenAwareSoftwareComponentVariant variant) {
        if (variant.getArtifacts().stream().anyMatch(toCheck -> variants.stream().flatMap(v ->
                v.getArtifacts().stream()).anyMatch(artifactsEquals(toCheck)))) {
            PublishArtifact source = null;
            PublishArtifact duplicated = null;
            for (PublishArtifact artifact : variant.getArtifacts()) {
                source = artifact;
                duplicated = variants.stream().flatMap(v -> v.getArtifacts().stream())
                        .filter(artifactsEquals(artifact)).findFirst().orElseThrow(AssertionError::new);
                break;
            }

            throw new AssertionError(String.format("The artifact '%s' of variant '%s' is the same as the existing '%s'",
                    source, variant, duplicated));
        }

    }

    private Predicate<PublishArtifact> artifactsEquals(PublishArtifact another) {
        return artifact -> another.getExtension().equals(artifact.getExtension()) &&
                Objects.equals(another.getClassifier(), artifact.getClassifier());
    }

    private void assertMutable() {
        if (!mutable) {
            throw new UnsupportedOperationException();
        }
    }

    @NotNull
    @Override
    public Set<? extends SoftwareComponent> getVariants() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<? extends UsageContext> getUsages() {
        mutable = false;
        return variants.stream().map(delegate -> new SimpleUsageContext(delegate, delegate.getScopeMapping())).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<? extends MavenAwareSoftwareComponentVariant> getSoftwareComponentVariants() {
        return variants;
    }

}
