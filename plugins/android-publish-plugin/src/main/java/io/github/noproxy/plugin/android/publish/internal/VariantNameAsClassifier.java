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
import com.google.common.base.Strings;
import io.github.noproxy.plugin.android.publish.api.AndroidVariantArtifactMapping;
import io.github.noproxy.plugin.android.publish.api.ScopeMapping;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;

@NonNullApi
public class VariantNameAsClassifier implements AndroidVariantArtifactMapping {
    private static CharSequence mappingName(ScopeMapping scope) {
        switch (scope) {
            case compile:
                return "api";
            case runtime:
                return "runtime";
            case compile_optional:
                return "compileOptional";
            case runtime_optional:
                return "runtimeOptional";
            default:
                throw new UnsupportedOperationException("Not supported scopeMapping: " + scope);
        }
    }

    @Override
    public void execute(ConfigurablePublishArtifact artifact, LibraryVariant androidVariant, ScopeMapping scopeMapping) {
        if (!Strings.isNullOrEmpty(artifact.getClassifier())) {
            throw new UnsupportedOperationException(String.format("Artifact %s already has classifier %s ", artifact, artifact.getClassifier()));
        }
        artifact.setClassifier(androidVariant.getBaseName() + StringGroovyMethods.capitalize(mappingName(scopeMapping)));
    }
}
