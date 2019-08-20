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

package com.github.noproxy.gradle.test.api.extension

import com.github.noproxy.gradle.test.api.TemplateOptions
import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class ScriptTemplate {
    static void androidApplication(IntegrateSpecification self, String applicationId) {
        useAndroidApplicationPlugin(self)

        self.buildFile {
            append """\
            
            ${jcenter()}
            ${google()}
            
            android {
                defaultConfig {
                    applicationId "$applicationId"
                    compileSdkVersion 28
                }
            }"""
        }
    }

    static void androidLibrary(IntegrateSpecification self) {
        useAndroidLibraryPlugin(self)

        self.buildFile {
            append """\
            
            ${jcenter()}
            ${google()}
            
            android {
                defaultConfig {
                    compileSdkVersion 28
                }
            }"""
        }
    }

    static void usePortalPlugin(IntegrateSpecification self, String id, String version = null, boolean apply = true) {
        self.plugins {
            append "id '$id'"
            if (!apply) {
                append ", apply false"
            }
        }
    }

    static void usePlugin(IntegrateSpecification self, String id = null,
                          String classpath,
                          String... mavenUrls = []) {
        self.buildscript {
            if (mavenUrls && mavenUrls.length > 0) {
                append """repositories {"""
                mavenUrls.each {
                    append """\
                    maven {
                        url '$mavenUrl'
                    }""".stripIndent()
                }
                append '\n}'
            }
            append """\
                dependencies {
                    classpath '$classpath'
                }""".stripIndent()
        }
        if (id) {
            self.buildFile {
                append "\napply plugin: '$id'"
            }
        }
    }

    /*
        static void useAndroidLibraryPlugin(IntegrateSpecification self, String version = null, boolean apply = true) {
        def classpath = "com.android.tools.build:gradle:${version ?: TemplateOptions.getAndroidPluginVersion()}"
        def id = apply ? 'com.android.library' : null
        usePlugin(self, id, classpath)
    }
     */

    static void useAndroidLibraryPlugin(IntegrateSpecification self, String version = TemplateOptions.getAndroidPluginVersion(), boolean apply = true) {
        self.buildscript {
            google()
            jcenter()
        }
        usePlugin(self, apply ? 'com.android.library' : null, "com.android.tools.build:gradle:$version")
    }

    static void useAndroidApplicationPlugin(IntegrateSpecification self, String version = TemplateOptions.getAndroidPluginVersion(), boolean apply = true) {
        self.buildscript {
            google()
            jcenter()
        }
        usePlugin(self, apply ? 'com.android.application' : null, "com.android.tools.build:gradle:$version")
    }
}
