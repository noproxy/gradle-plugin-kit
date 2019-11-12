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

class CheckManifestSpec extends InitScriptIntegrateSpecification {
    @Override
    protected String getTestedScriptName() {
        return "initscript.gradle"
    }

    def "can use against empty project"() {
        when:
        configure()

        then:
        success()
        output.contains("init script 'checkManifest.gradle' applied. version: 0")
    }

    def "android project without network traffic attribute will break"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
        }
        android {
            manifest {
                packageName = "org.example.app"
            }
        }
        buildFile """\
            android {
                defaultConfig {
                    targetSdkVersion 28
                }
            }
            """.stripIndent()

        when:
        run 'processDebugManifest'

        then:
        fail()
        taskRun('processDebugManifest')
        output.contains("AssertionError: AndroidManifest.xml must explicitly declare 'usesCleartextTraffic' attribute.")
    }

    def "android project with android:usesCleartextTraffic attribute will pass"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
        }
        android {
            manifest """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="org.example.app">
    <application
        android:label="Example App"
        android:usesCleartextTraffic="false"
        android:theme="@android:style/Theme.Material.Light">

    </application>
</manifest>"""
        }
        buildFile """\
            android {
                defaultConfig {
                    targetSdkVersion 28
                }
            }
            """.stripIndent()

        when:
        run 'processDebugManifest'

        then:
        success()
        taskRun('processDebugManifest')
        output.contains("find usesCleartextTraffic android:usesCleartextTraffic=\"false\"")
    }

    def "android project with android:networkSecurityConfig attribute will pass"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
        }
        android {
            manifest """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="org.example.app">
    <application
        android:label="Example App"
        android:networkSecurityConfig="@xml/network_security_config"
        android:theme="@android:style/Theme.Material.Light">

    </application>
</manifest>"""
        }
        buildFile """\
            android {
                defaultConfig {
                    targetSdkVersion 28
                }
            }
            """.stripIndent()

        when:
        run 'processDebugManifest'

        then:
        success()
        taskRun('processDebugManifest')
        output.contains("find networkSecurityConfig android:networkSecurityConfig=\"@xml/network_security_config\"")
    }

    def "android project with target < 28 will pass"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
        }
        android {
            manifest {
                packageName = "org.example.app"
            }
        }
        buildFile """\
            android {
                defaultConfig {
                    targetSdkVersion 27
                }
            }
            """.stripIndent()

        when:
        run 'processDebugManifest'

        then:
        success()
        taskRun('processDebugManifest')
        output.contains("skip check manifest because target sdk < 28")
    }
}
