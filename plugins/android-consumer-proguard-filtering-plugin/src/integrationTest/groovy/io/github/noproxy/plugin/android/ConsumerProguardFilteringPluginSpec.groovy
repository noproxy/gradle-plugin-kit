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

package io.github.noproxy.plugin.android

import com.github.noproxy.gradle.test.api.extension.ScriptTemplate
import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class ConsumerProguardFilteringPluginSpec extends IntegrateSpecification {
    def "not exclude proguard rules from external library"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
            usePortalPlugin('io.github.noproxy.android-consumer-proguard-filtering-plugin')
            buildFile """\
android {
    buildTypes {
        debug {
            minifyEnabled true
            proguardFiles 'proguard.txt'
        }
    }

    consumerProguardRules {
        exclude group: 'org.example'
    }
}

dependencies {
    implementation 'org.example:lib:1.0'
    implementation 'org.example:lib2:1.0'
    implementation 'org.example2:lib:1.0'
}
"""
        }
        android {
            manifest {
                packageName = "org.example.app"
            }
        }
        buildFile {
            mavenDefaults()
        }
        newFile('proguard.txt') << """-printconfiguration build/proguard-final.txt"""

        maven {
            androidModule('org.example', 'lib', '1.0') {
                newFile('proguard.txt') << """\
-keep class org.example.lib
"""
            }
            androidModule('org.example', 'lib2', '1.0') {
                newFile('proguard.txt') << """\
-keep class org.example.lib2
"""
                androidModule('org.example2', 'lib', '1.0') {
                    newFile('proguard.txt') << """\
-keep class org.example2.lib
"""
                }
            }
        }
        when:
//        run 'mergeDebugConsumerProguardFiles'
        run 'assembleDebug'

        then:
        noExceptionThrown()
        def output = file('build/proguard-final.txt')
        output.exists()
        def text = output.text
        text.contains("-keep class org.example.lib")
        text.contains("-keep class org.example.lib2")
        text.contains("-keep class org.example2.lib")
// Warning: com.ironsource.unity.androidbridge.AndroidBridge: can't find referenced class com.ironsource.unity.androidbridge.BuildConfig

        when:
        buildFile """\
        android {
            consumerProguardRules {
                exclude group: 'org.example'
            }
        }
        """.stripIndent()


        run 'assembleDebug'
        then:
        def text2 = output.text
        !text2.contains("-keep class org.example.lib")
        !text2.contains("-keep class org.example.lib2")
        text2.contains("-keep class org.example2.lib")
    }

    def "exclude proguard rules from external library"() {
        use(ScriptTemplate) {
            androidApplication('org.example.app')
            usePortalPlugin('io.github.noproxy.android-consumer-proguard-filtering-plugin')
            buildFile """\
android {
    buildTypes {
        debug {
            minifyEnabled true
            proguardFiles 'proguard.txt'
        }
    }

    consumerProguardRules {
        exclude group: 'org.example'
    }
}

dependencies {
    implementation 'org.example:lib:1.0'
    implementation 'org.example:lib2:1.0'
    implementation 'org.example2:lib:1.0'
}
"""
        }
        android {
            manifest {
                packageName = "org.example.app"
            }
        }
        buildFile {
            mavenDefaults()
        }
        newFile('proguard.txt') << """-printconfiguration build/proguard-final.txt"""

        maven {
            androidModule('org.example', 'lib', '1.0') {
                newFile('proguard.txt') << """\
-keep class org.example.lib
"""
            }
            androidModule('org.example', 'lib2', '1.0') {
                newFile('proguard.txt') << """\
-keep class org.example.lib2
"""
                androidModule('org.example2', 'lib', '1.0') {
                    newFile('proguard.txt') << """\
-keep class org.example2.lib
"""
                }
            }
        }
        buildFile """\
        android {
            consumerProguardRules {
                exclude group: 'org.example'
            }
        }
        """.stripIndent()

        when:
        run 'assembleDebug'

        then:
        noExceptionThrown()
        def output = file('build/proguard-final.txt')
        output.exists()
        def text = output.text
        !text.contains("-keep class org.example.lib")
        !text.contains("-keep class org.example.lib2")
        text.contains("-keep class org.example2.lib")
    }

}
