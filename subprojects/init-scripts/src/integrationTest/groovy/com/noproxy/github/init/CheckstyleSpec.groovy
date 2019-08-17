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

class CheckstyleSpec extends InitScriptIntegrateSpecification {
    @Override
    protected String getTestedScriptName() {
        return "checkstyle.gradle"
    }

    def "can use against empty project"() {
        when:
        configure()

        then:
        success()
        output.contains("Checkstyle use config: '$root/generatedConfig/checkstyle/checkstyle-0.1.xml'")
    }

    def "can use against java project"() {
        given:
        buildFile """\
            plugins {
                id 'java'
            }
            """
        buildFile {
            jcenter()
        }

        src {
            java("org.example.Main") << """\
                package org.example;
                public class Main {
                    public static void foo(){}
                }
                """
        }

        when:
        run 'checkstyleMain'

        then:
        success()
        taskRun('checkstyleMain')
        output.contains("Checkstyle use config: '$root/generatedConfig/checkstyle/checkstyle-0.1.xml'")
    }
}
