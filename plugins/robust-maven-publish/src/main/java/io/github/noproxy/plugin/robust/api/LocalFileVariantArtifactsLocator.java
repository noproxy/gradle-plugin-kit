package io.github.noproxy.plugin.robust.api;

import io.github.noproxy.plugin.robust.internal.ArtifactType;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.specs.Spec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.function.Predicate;

public class LocalFileVariantArtifactsLocator implements VariantArtifactsLocator {
    private final Project project;
    @NotNull
    private final File methodMap;
    @Nullable
    private final File mapping;

    public LocalFileVariantArtifactsLocator(Project project, @NotNull File methodMap, @Nullable File mapping) {
        this.project = project;
        this.methodMap = methodMap;
        this.mapping = mapping;
    }

    @Nullable
    @Override
    public Object getDependencyNotation(ArtifactType type) {
        final File file = getArtifactFile(type);
        if (file == null) {
            return null;
        }

        return project.files(file);
    }

    private File getArtifactFile(ArtifactType type) {
        switch (type) {
            case METHOD_MAPPING:
                return Objects.requireNonNull(methodMap, "method mapping file is null");
            case MAPPING:
                return mapping;
            default:
                return null;
        }
    }

    @NotNull
    @Override
    public Spec<Dependency> getDependencySpec(ArtifactType type) {
        return dependency -> (dependency instanceof SelfResolvingDependency) && ((SelfResolvingDependency) dependency).resolve().contains(getArtifactFile(type));
    }

    @NotNull
    @Override
    public Predicate<ResolvedArtifact> getResolvedArtifactSpec(ArtifactType type) {
        return resolvedArtifact -> resolvedArtifact.getFile().equals(getArtifactFile(type));
    }
}
