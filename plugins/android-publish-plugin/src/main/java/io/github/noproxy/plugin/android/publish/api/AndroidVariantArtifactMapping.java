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

package io.github.noproxy.plugin.android.publish.api;

import com.android.build.gradle.api.LibraryVariant;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;

@NonNullApi
public interface AndroidVariantArtifactMapping {
    /**
     * Change the extension or classifier of the artifact to make every artifact different.
     *
     * @param artifact       to configure
     * @param androidVariant of the artifact
     * @param scopeMapping   of the artifact
     * @implNote You can change only the extension and classifier. Changing other properties of artifacts makes no differences.
     */
    void execute(ConfigurablePublishArtifact artifact, LibraryVariant androidVariant, ScopeMapping scopeMapping);

    default Action<ConfigurablePublishArtifact> toAction(LibraryVariant androidVariant, ScopeMapping scopeMapping) {
        return artifact -> execute(artifact, androidVariant, scopeMapping);
    }
}
