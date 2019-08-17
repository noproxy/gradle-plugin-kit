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

class AndroidPublishPluginSpec extends IntegrateSpecification {
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

    def "publish produces module and variants"() {
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
            """

        when:
        run "publish"

        then:
        success()

        and: 'module published'
        use(MavenChecker) {
            buildRepo {
                module("$GAV") {
                    pom {
                        assertExists()
                        assert text.contains('')
                    }
                    metadata {
                        assert exists()
                        text.contains('')
                    }
                    noOtherComponents()
                }
            }
        }
        and: 'variants published'
        use(MavenChecker) {
            buildRepo {
                ['debug', 'release'].every {
                    module("$GAV-debug") {
                        pom {
                            exists()
                        }
                        metadata {
                            !exists()
                        }
                        aar.exists()
                        noOtherComponents()
                    }
                }
            }
        }
    }

    def "module can be consumed"() {
        given:
        maven {
            module(GROUP, ARTIFACT, "$VERSION-debug", "aar") {
                aar {
                    classes {
                        newClass('org.example.ClassInDebug', """\
                            package org.example;

                            public class ClassInDebug {
                                public static void main(String[] args) {}
                            }
                            """.stripIndent()
                        )
                    }
                }
            }
            module(GROUP, ARTIFACT, "$VERSION-release", "aar") {
                aar {
                    classes {
                        newClass('org.example.ClassInRelease', """\
                            package org.example;
                            
                            public class ClassInRelease {
                                public static void main(String[] args) {}
                            }
                            """.stripIndent()
                        )
                    }
                }
            }
            module(GROUP, ARTIFACT, VERSION, "pom") {
                file(null, "module") {
                    text = MODULE_TEXT
                }
            }
        }
        buildFile {
            mavenDefaults()
            append """\
        dependencies {
            implementation '$GAV'
        }
        
        task resolve() {
            doLast {
                String conf = project.properties.get('configuration','no_option')
//                def files = configurations.getByName(conf).incoming.artifactView{ config ->
//                       config.attributes { container ->
//                               container.attribute(Attribute.of("artifactType", String.class), "classes")
//                           }
//                }.artifacts.artifactFiles.files

                def files = configurations.getByName(conf).allArtifacts.artifactFiles.files

                logger.quiet('{}:\\n {}', conf, files)
            }
        }
        """
        }

        when:
        run 'resolve', '-Pconfiguration=debugCompileClasspath', '--debug'

        then:
        success()

        output.contains('example')
    }

    public static def MODULE_TEXT = """{
  "formatVersion": "1.0",
  "component": {
    "group": "org.example",
    "module": "library",
    "version": "1.0.0",
    "attributes": {
      "org.gradle.status": "release"
    }
  },
  "createdBy": {
    "gradle": {
      "version": "5.6",
      "buildId": "cq7gm44l75b5xoymzhwaculthi"
    }
  },
  "variants": [
    {
      "name": "debugApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../library/1.0.0-debug/library-1.0.0-debug.module",
        "group": "org.example",
        "module": "library",
        "version": "1.0.0-debug"
      }
    },
    {
      "name": "debugRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../library/1.0.0-debug/library-1.0.0-debug.module",
        "group": "org.example",
        "module": "library",
        "version": "1.0.0-debug"
      }
    },
    {
      "name": "releaseApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../library/1.0.0-release/library-1.0.0-release.module",
        "group": "org.example",
        "module": "library",
        "version": "1.0.0-release"
      }
    },
    {
      "name": "releaseRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../library/1.0.0-release/library-1.0.0-release.module",
        "group": "org.example",
        "module": "library",
        "version": "1.0.0-release"
      }
    }
  ]
}
"""
}
