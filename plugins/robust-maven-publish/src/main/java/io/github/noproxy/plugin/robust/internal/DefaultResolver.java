/*
 * Copyright 2020 the original author or authors.
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
import com.google.common.base.Preconditions;
import io.github.noproxy.plugin.robust.api.Resolver;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocator;
import io.github.noproxy.plugin.robust.api.VariantArtifactsLocatorFactory;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

public class DefaultResolver implements Resolver {
    private final VariantArtifactsLocatorFactory locatorFactory;
    private final RobustMavenPublishExtensionInternal publishExtension;
    private final Project project;
    private final RobustMavenResolverExtensionInternal resolverExtension;

    public DefaultResolver(Project project, RobustMavenResolverExtensionInternal resolverExtension,
                           RobustMavenPublishExtensionInternal publishExtension) {
        this.locatorFactory = resolverExtension.getLocatorFactory();
        this.publishExtension = publishExtension;
        this.resolverExtension = resolverExtension;
        this.project = project;
    }

    private static <T> T assertSingleton(Set<T> collections) {
        return assertSingleton(collections, null);
    }

    private static <T> T assertSingleton(Set<T> collections, String msg) {
        Preconditions.checkArgument(collections.size() == 1, msg);
        return collections.stream().findFirst().get();
    }

    @Override
    @Nullable
    public File resolveMapping(ApplicationVariant variant) {
        final VariantArtifactsLocator resolveLocator = locatorFactory.createLocator(project, publishExtension, resolverExtension, variant);
        Configuration classpath = createResourceClasspath(variant, resolveLocator);

        final Set<File> mappings;
        if (resolveLocator instanceof MavenVariantArtifactsLocator) {
            final Set<ResolvedArtifact> artifacts = classpath.getResolvedConfiguration().getLenientConfiguration()
                    .getArtifacts(resolveLocator.getDependencySpec(ArtifactType.MAPPING));
            mappings = artifacts.stream().filter(resolveLocator.getResolvedArtifactSpec(ArtifactType.MAPPING))
                    .map(ResolvedArtifact::getFile).collect(Collectors.toSet());
        } else {
            mappings = classpath.getResolvedConfiguration().getLenientConfiguration()
                    .getFiles(resolveLocator.getDependencySpec(ArtifactType.MAPPING));
        }

        if (mappings.isEmpty()) {
            return null;
        }

        return assertSingleton(mappings);
    }

    // use separate configuration to resolve apk, because for other file, we use lenientConfiguration to ignore resolve error.
    // but for the apk, we want gradle throw exception
    private Configuration createResourceClasspath(ApplicationVariant variant, VariantArtifactsLocator resolveLocator) {
        final String variantName = capitalize(variant.getName());

        return maybeCreate("tinkerResolve" + variantName + "Classpath", files -> {
            files.setCanBeConsumed(false);
            files.setVisible(false);
            files.setDescription("Configuration to resolve base version of mapping.txt and R.txt files.");

            final Object symbol = resolveLocator.getDependencyNotation(ArtifactType.METHOD_MAPPING);
            if (symbol != null) {
                project.getDependencies().add(files.getName(), symbol);
            }
            final Object mapping = resolveLocator.getDependencyNotation(ArtifactType.MAPPING);
            if (mapping != null) {
                project.getDependencies().add(files.getName(), mapping);
            }
        });
    }

    private Configuration maybeCreate(String name, Action<? super Configuration> action) {
        Configuration created = project.getConfigurations().findByName(name);
        if (created == null) {
            created = project.getConfigurations().create(name, action);
        }

        return created;
    }

    @Override
    @NotNull
    public File resolveMethodMapping(ApplicationVariant variant) {
        final VariantArtifactsLocator resolveLocator = locatorFactory.createLocator(project, publishExtension, resolverExtension, variant);

        final String variantName = capitalize(variant.getName());
        final Configuration tinkerResolveApkClasspath = maybeCreate("tinkerResolve" + variantName + "ApkClasspath", files -> {
            files.setCanBeConsumed(false);
            files.setVisible(false);
            files.setDescription("Configuration to resolve base version of apk files.");

            project.getDependencies().add(files.getName(), Objects.requireNonNull(resolveLocator.getDependencyNotation(ArtifactType.METHOD_MAPPING)));
        });


        final Set<File> apk = tinkerResolveApkClasspath.getResolvedConfiguration().getFiles(resolveLocator.getDependencySpec(ArtifactType.METHOD_MAPPING));

        if (apk.isEmpty()) {
            return null;
        }

        return assertSingleton(apk, "Cannot find singleton apk file in Maven repository, we found: " + apk + ", ");
    }
}
