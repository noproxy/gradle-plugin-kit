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

package com.github.noproxy.built;

import org.gradle.api.artifacts.ComponentMetadataContext;
import org.gradle.api.artifacts.ComponentMetadataDetails;
import org.gradle.api.artifacts.ComponentMetadataRule;

import java.util.Arrays;
import java.util.List;

public class HamcrestCapability implements ComponentMetadataRule {
    private static final List<String> HAMCRESTS = Arrays.asList("hamcrest-core", "hamcrest-integration", "hamcrest-library");

    @Override
    public void execute(ComponentMetadataContext context) {
        final ComponentMetadataDetails details = context.getDetails();
        if (HAMCRESTS.contains(details.getId().getName())) {
            details.allVariants(variantMetadata ->
                    variantMetadata.withCapabilities(capabilitiesMetadata ->
                            capabilitiesMetadata.addCapability("org.hamcrest",
                                    "hamcrest", details.getId().getVersion() + "."
                                            + HAMCRESTS.indexOf(details.getId().getName()))));
        }
    }
}
