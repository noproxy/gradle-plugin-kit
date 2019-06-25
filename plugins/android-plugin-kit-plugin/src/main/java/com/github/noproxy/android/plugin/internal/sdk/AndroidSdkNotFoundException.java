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

package com.github.noproxy.android.plugin.internal.sdk;


import com.github.noproxy.android.plugin.internal.InvalidUsageException;

import org.gradle.initialization.BuildClientMetaData;
import org.gradle.internal.exceptions.Contextual;
import org.gradle.internal.exceptions.FailureResolutionAware;
import org.gradle.internal.logging.text.StyledTextOutput;

import static org.gradle.internal.logging.text.StyledTextOutput.Style.Header;
import static org.gradle.internal.logging.text.StyledTextOutput.Style.Normal;

@Contextual
public class AndroidSdkNotFoundException extends InvalidUsageException implements FailureResolutionAware {
    public AndroidSdkNotFoundException() {
        super("SDK location not found. Please refer the resolutions for how to define android SDK location.");
    }

    @Override
    public void appendResolution(StyledTextOutput output, BuildClientMetaData clientMetaData) {
        output.withStyle(Header).println("Please define android SDK location by:")
                .withStyle(Normal)
                .println("'sdk.dir' in the local.properties")
                .println("'ANDROID_HOME' in environment variable")
                .println("'testAndroidSdk' in 'androidPluginKit { }' block");
    }
}
