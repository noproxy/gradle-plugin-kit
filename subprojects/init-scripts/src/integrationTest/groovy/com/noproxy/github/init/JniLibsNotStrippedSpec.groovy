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

import com.github.noproxy.gradle.test.api.extension.ScriptTemplate
import io.github.noproxy.init.JniLibsNotStrippedPlugin
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle

class JniLibsNotStrippedSpec extends InitScriptPluginIntegrateSpecification {
    @Override
    Class<? extends Plugin<Gradle>> getTestedInitScriptPlugin() {
        return JniLibsNotStrippedPlugin
    }

    def "plugin applied"() {
        when:
        run 'help'

        then:
        noExceptionThrown()
        output.contains("plugin 'JniLibsNotStrippedPlugin' applied against ")
    }

    def "plugin add addition options"() {
        given:
        use(ScriptTemplate) {
            androidApplication()
        }
        buildFile """\

            task showOptions {
                doLast {
                    project.android.packagingOptions.doNotStrip.each {
                        logger.quiet("{}", it)                    
                    }
                }
            }
            """

        when:
        run 'showOptions'

        then:
        noExceptionThrown()
        output.contains("plugin 'JniLibsNotStrippedPlugin' applied against ")

        and:
        output.contains("*/arm64-v8a/libdu.so")
        output.contains("*/armeabi/libdu.so")
        output.contains("*/armeabi-v7a/libdu.so")
        output.contains("*/x86/libdu.so")
        output.contains("*/x86_64/libdu.so")
    }
}
