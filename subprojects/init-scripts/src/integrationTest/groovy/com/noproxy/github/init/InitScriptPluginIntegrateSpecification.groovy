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

package com.noproxy.github.init

import com.github.noproxy.gradle.test.api.template.IntegrateSpecification
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle

abstract class InitScriptPluginIntegrateSpecification extends IntegrateSpecification {
    def setup() {
        settings """\
            buildscript {
                repositories {
                    maven { url '/System/Volumes/Data/Users/yiyazhou/Projects/gradle-plugin-kit/subprojects/init-scripts/build/tmp/test_repo' }
                    google()
                    jcenter()
                }
                dependencies {
                    classpath "io.github.noproxy:init-scripts:+"
                }
            }

            gradle.plugins.pluginManager.apply(${testedInitScriptPlugin.name})
            """
    }

    abstract Class<? extends Plugin<Gradle>> getTestedInitScriptPlugin()
}
