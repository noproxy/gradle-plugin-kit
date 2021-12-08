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

package io.github.noproxy.plugin.android;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BasePlugin;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.pipeline.TransformTask;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;
import com.android.build.gradle.internal.scope.CodeShrinker;
import com.android.build.gradle.internal.transforms.ProguardConfigurable;
import com.github.noproxy.android.api.AndroidHideApi;
import com.github.noproxy.android.internal.DefaultAndroidHideApi;
import io.github.noproxy.plugin.android.internal.DefaultConsumerProguardFilteringExtension;
import io.github.noproxy.plugin.android.internal.ProguardExcludeRule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@SuppressWarnings("unused")
public class ConsumerProguardFilteringPlugin implements Plugin<Project> {

    private ConfigurableFileCollection getConfigurationFiles(ProguardConfigurable transform) {
        try {
            final Field field = ProguardConfigurable.class.getDeclaredField("configurationFiles");
            field.setAccessible(true);
            return (ConfigurableFileCollection) field.get(transform);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("cannot find field 'configurationFiles' in class ProguardConfigurable");
        }
    }

    private void setConfigurationFiles(ProguardConfigurable transform, FileCollection files) {
        try {
            final Field field = ProguardConfigurable.class.getDeclaredField("configurationFiles");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//            field.set(transform, files);
            final ConfigurableFileCollection value = (ConfigurableFileCollection) field.get(transform);
            value.setFrom(files);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("cannot find field 'configurationFiles' in class ProguardConfigurable");
        }
    }

    private FileCollection getExcludedRules(ApplicationVariant variant, Set<ProguardExcludeRule> excludeRules) {
        return variant.getRuntimeConfiguration().getIncoming().artifactView(view -> {
            view.componentFilter(componentIdentifier -> {
                if (componentIdentifier instanceof ModuleComponentIdentifier) {
                    ModuleComponentIdentifier identifier = (ModuleComponentIdentifier) componentIdentifier;
                    return excludeRules.stream().anyMatch(excludeRule -> {
                        if (excludeRule.getGroup() != null) {
                            if (!excludeRule.getGroup().equals(identifier.getGroup())) {
                                return false;
                            }
                        }

                        if (excludeRule.getModule() != null) {
                            if (!excludeRule.getModule().equals(identifier.getModule())) {
                                return false;
                            }
                        }

                        return true;
                    });
                }
                return false;
            });
            view.attributes(attributes -> {
                attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CONSUMER_PROGUARD_RULES.getType());
            });
        }).getFiles();
    }

    @Override
    public void apply(@NotNull Project project) {
        project.getPlugins().withId("com.android.application", plugin -> {
            final AndroidHideApi androidHideApi = new DefaultAndroidHideApi(() -> (BasePlugin) plugin);

            final AppExtension appExtension = project.getExtensions().getByType(AppExtension.class);
            final DefaultConsumerProguardFilteringExtension rules = (DefaultConsumerProguardFilteringExtension) ((ExtensionAware) appExtension).getExtensions().create(ConsumerProguardFilteringExtension.class,
                    "consumerProguardRules", DefaultConsumerProguardFilteringExtension.class);

            appExtension.getApplicationVariants().all(variant -> {
                if (!androidHideApi.isCodeShrinkerEnable(variant)) {
                    return;
                }

                final ProguardConfigurable proguardOrR8Transform;
                final CodeShrinker codeShrinker = androidHideApi.getCodeShrinker(variant);
                proguardOrR8Transform = (ProguardConfigurable) project.getTasks().withType(TransformTask.class)
                        .getByName("transformClassesAndResourcesWith" + capitalize((CharSequence) codeShrinker.toString().toLowerCase()) +
                                "For" + capitalize((CharSequence) variant.getName())
                        )
                        .getTransform();

                final ConfigurableFileCollection configurationFiles = getConfigurationFiles(proguardOrR8Transform);
                final Set<Object> originFiles = new HashSet<>(configurationFiles.getFrom());
                setConfigurationFiles(proguardOrR8Transform, project.files(originFiles).minus(getExcludedRules(variant, rules.getExcludeRules())));
            });
        });
    }
}
