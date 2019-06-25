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

import com.github.noproxy.android.plugin.internal.AndroidPluginKitExtensionInternal
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import spock.lang.Specification

class AndroidSdkProviderFactoryTest extends Specification {
    def "discovering useless user defined"() {
        def extension = Mock(AndroidPluginKitExtensionInternal)
        def factory = new AndroidSdkProviderFactory(Stub(Project), Stub(Logger), extension)

        when:
        def provider = factory.createSdkProvider()

        then:
        1 * extension.testAndroidSdk >> null
        0 * _

        and:
        provider instanceof DiscoveringAndroidSdkProvider

        when:
        def provider2 = factory.createSdkProvider()

        then:
        1 * extension.testAndroidSdk >> Stub(File)
        0 * _

        and:
        provider2 instanceof UserDefinedAndroidSdkProvider
    }
}
