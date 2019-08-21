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

package io.github.noproxy.plugin.android.publish

import com.github.noproxy.gradle.test.api.extension.MavenChecker
import com.github.noproxy.gradle.test.api.extension.ScriptTemplate
import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class LegacyPublishSpec extends IntegrateSpecification {
    private static final String GROUP = 'org.example'
    private static final String ARTIFACT = 'library'
    private static final String VERSION = '1.0.0'
    private static final String GAV = "$GROUP:$ARTIFACT:$VERSION"
    private static final String PACKAGE_NAME = "$GROUP.$ARTIFACT"

    def setup() {
        use(ScriptTemplate) {
            androidLibrary()
            usePortalPlugin('io.github.noproxy.android-publish-plugin')

            buildFile """
                group = '$GROUP'
                version = '$VERSION'
                
                publishing {
                    repositories {
                        maven {
                            name = 'BuildDir'
                            url = rootProject.file("build/repo")
                        }
                    }
                }
                """
        }
        android { manifest { packageName = PACKAGE_NAME } }
        rootProjectName = ARTIFACT
    }

    def "legacy publish produces all variants with different version suffix"() {
        given:
        buildFile """
            dependencies {
                implementation 'org.jetbrains:annotations:13.0'
                api 'org.apache.commons:commons-lang3:3.9'
                
            // TODO use example library
                debugImplementation "commons-io:commons-io:2.4"
            //    releaseImplementation 'org.apache.commons:commons-lang3:3.9'
            //
            //    debugApi 'org.apache.commons:commons-lang3:3.9'
            //    releaseApi 'org.apache.commons:commons-lang3:3.9'
            }
            
            androidPublishing {
                legacyPublish = true
            }
            """

        when:
        run "publish"

        then:
        success()

        and:
        use(MavenChecker) {
            buildRepo {
                module("$GAV-debug") {
                    pom {
                        assertExists()
                        assert text.contains('')
                    }
                    !metadata.exists()
                    noOtherComponents()
                }
            }
        }
    }
}
