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

package com.github.noproxy.android.plugin.internal.sdk

import com.github.noproxy.gradle.test.api.template.UnitSpecification
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

class DiscoveringAndroidSdkProviderTest extends UnitSpecification {
    public static final String TEST_ANDROID_HOME = "/test/android/home"
    @Rule
    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    Project getProject() {
        return ProjectBuilder.builder().withProjectDir(root).build()
    }

    @Unroll
    def "can discover from local properties #propertyName"(String propertyName) {
        def provider = new DiscoveringAndroidSdkProvider(project, Stub(Logger))

        given:
        newFile('local.properties') << "$propertyName=/test/android/home"

        expect:
        TEST_ANDROID_HOME == provider.computeSdkHome()

        where:
        propertyName  | _
        "sdk.dir"     | _
        "android.dir" | _
    }

    @Unroll
    @RestoreSystemProperties
    def "can discover from system property #propertyName"(String propertyName) {
        def provider = new DiscoveringAndroidSdkProvider(project, Stub(Logger))

        given:
        System.setProperty(propertyName, TEST_ANDROID_HOME)

        expect:
        TEST_ANDROID_HOME == provider.computeSdkHome()

        where:
        propertyName   | _
        "android.home" | _
    }

    @Unroll
    def "can discover from system env #propertyName"(String propertyName) {
        def provider = new DiscoveringAndroidSdkProvider(project, Stub(Logger))
        given:
        environmentVariables.set(propertyName, TEST_ANDROID_HOME)

        expect:
        TEST_ANDROID_HOME == provider.computeSdkHome()

        where:
        propertyName       | _
        "ANDROID_SDK_ROOT" | _
        "ANDROID_HOME"     | _
    }

    def "throw AndroidSdkNotFoundException when no sdk found"() {
        def logger = Mock(Logger)
        def provider = new DiscoveringAndroidSdkProvider(project, logger)

        when:
        provider.computeSdkHome()

        then:
        thrown(AndroidSdkNotFoundException)

        0 * logger._
    }
}
