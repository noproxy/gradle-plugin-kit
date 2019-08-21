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


import com.github.noproxy.gradle.test.api.extension.ScriptTemplate
import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class MetadataConsumingSpec extends IntegrateSpecification {
    private static final String GROUP = 'org.example'
    private static final String ARTIFACT = 'library'
    private static final String VERSION = '1.0.0'
    private static final String GAV = "$GROUP:$ARTIFACT:$VERSION"
    private static final String PACKAGE_NAME = "$GROUP.$ARTIFACT"

    private void createMavenModule(String group, String artifact, String baseVersion) {
        maven {
            module(group, artifact, "$baseVersion-debug", "aar") {
                aar {
                    classes {
                        newClass('org.example.ClassInDebug', Template.simpleJava("debug"))
                    }
                }
                file(null, "module") {
                    append Template.variantModuleText(group, artifact, baseVersion, "debug")
                }
                pom {
                    text = Template.variantPom(group, artifact, baseVersion, "debug")
                }
            }
            module(group, artifact, "$baseVersion-release", "aar") {
                aar {
                    classes {
                        newClass('org.example.ClassInRelease', Template.simpleJava("release"))
                    }
                }
                file(null, "module") {
                    append Template.variantModuleText(group, artifact, baseVersion, "release")
                }
                pom {
                    text = Template.variantPom(group, artifact, baseVersion, "release")
                }
            }
            module(group, artifact, '0.0.9', "pom") {
                file(null, "module") {
                    text = Template.moduleText(group, artifact, VERSION, '0.0.9')
                }
                file(null, "pom") {
                    text = Template.modulePom(group, artifact, '0.0.9')
                }
            }
        }
    }

    def "module can be consumed by library"() {
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
        rootProjectName = "app"

        given:
        createMavenModule(GROUP, ARTIFACT, VERSION)
        buildFile {
//            mavenDefaults()
            append """\
        repositories {
            maven {
                url rootProject.file('.tmp/maven/defaults')
                metadataSources {
                    gradleMetadata()
                    mavenPom()
                }
            }
        }

        dependencies {
            implementation '$GROUP:$ARTIFACT:0.0.9'
        }
        
        task resolve() {
            doLast {
                String conf = project.properties.get('configuration','no_option')
//                def files = configurations.getByName(conf).incoming.artifactView{ config ->
//                       config.attributes { container ->
//                               container.attribute(Attribute.of("artifactType", String.class), "android-classes")
//                           }
//                }.artifacts.artifactFiles.files

                def files = configurations.getByName(conf).allArtifacts.artifactFiles.files

                logger.quiet('{}:\\n {}', conf, files)
            }
        }
        """
        }
        makeCompileAvailable()

        when:
//        run 'resolve', '-Pconfiguration=debugCompileClasspath' , '--debug'
        run 'assembleDebug'

        then:
        success()

        output.contains('example')
    }

    def "module can be consumed by application"() {
        use(ScriptTemplate) {
            androidApplication("${GROUP}.app")
        }
        rootProjectName = "app"

        given:
        createMavenModule(GROUP, ARTIFACT, VERSION)
        buildFile {
//            mavenDefaults()
            append """\
        repositories {
            maven {
                url rootProject.file('.tmp/maven/defaults')
                metadataSources {
                    gradleMetadata()
                    mavenPom()
                }
            }
        }
       android {
           compileOptions {
               sourceCompatibility 1.8
               targetCompatibility 1.8
           }
       }

        dependencies {
            implementation '$GROUP:$ARTIFACT:0.0.9'
        }
        
        task resolve() {
            doLast {
                String conf = project.properties.get('configuration','no_option')
//                def files = configurations.getByName(conf).incoming.artifactView{ config ->
//                       config.attributes { container ->
//                               container.attribute(Attribute.of("artifactType", String.class), "android-classes")
//                           }
//                }.artifacts.artifactFiles.files

                def files = configurations.getByName(conf).allArtifacts.artifactFiles.files

                logger.quiet('{}:\\n {}', conf, files)
            }
        }
        """.stripIndent()
        }
        makeCompileAvailable()

        when:
//        run 'resolve', '-Pconfiguration=debugCompileClasspath' , '--debug'
        run 'assembleDebug'//, '--debug'
//        run 'dependencies'//, '--configuration', 'debugCompileClasspath'

        then:
        success()

        output.contains('example')
        fail()
    }

    private void makeCompileAvailable() {
        android {
            src {
                res('values', 'colors.xml') << """\
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <color name="colorPrimary">#3F51B5</color>
                    </resources>
                """.stripIndent()
            }
        }
        android { manifest { packageName = "${GROUP}.app" } }
    }

}
