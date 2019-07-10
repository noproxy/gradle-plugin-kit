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

package com.github.noproxy.android.plugin

import com.github.noproxy.gradle.test.api.template.IntegrateSpecification
import spock.lang.PendingFeature

class AndroidPluginKitPluginSpec extends IntegrateSpecification {

    def setup() {
        buildArgument("-P", "com.github.noproxy.android-plugin-kit-plugin.log=QUIET")
        quiet()
    }

    //TODO 由于需要支持多次run，我们必须要将对buildFile的操作hook住，然后延迟到
    def "sdk configured for test task"() {
        buildFile """
plugins {
    id 'com.github.noproxy.android-plugin-kit-plugin'
    id 'java-library'
}

androidPluginKit {
    setTestAndroidSdk file('sdk')
}
"""

        when:
        printTestSystemProperties()

        then:
        output.contains("add system property 'android.home=${root}/sdk' to test task 'test'")
        output.contains("task task ':test' systemProperties: {android.home=${root}/sdk}")
    }

    def printTestSystemProperties() {
        buildFile """
task printTestSystemProperties() {
    doLast {
        throw new com.github.noproxy.android.plugin.internal.sdk.AndroidSdkNotFoundException()
        tasks.withType(Test).all {
            logger.quiet("task {} systemProperties: {}", delegate, systemProperties)
        }
    }
}
"""
        run "printTestSystemProperties"
    }

    @PendingFeature(reason = "delay throwing AndroidSdkNotFound to task executing in next version")
    def "not fail when configure if sdk not found"() {
        buildFile """
plugins {
    id 'com.github.noproxy.android-plugin-kit-plugin'
    id 'java-library'
}
"""

        when:
        configure()

        then:
        success()
        !output.contains("add system property 'android.home=${root}/sdk' to test task 'test'")
        !output.contains("android.home")
    }

    def "fail when test task executing if sdk not found"() {
        buildFile """
plugins {
    id 'com.github.noproxy.android-plugin-kit-plugin'
    id 'java-library'
}
"""

        when:
        printTestSystemProperties()

        then:
        fail()
        output.contains("SDK location not found. Please refer the resolutions for how to define android SDK location.")

        and: "has friendly resolution output"
        output.contains("Please define android SDK location by:")
        output.contains("'sdk.dir' in the local.properties")
        output.contains("'ANDROID_HOME' in environment variable")
        output.contains("'testAndroidSdk' in 'androidPluginKit { }' block")
    }
}
