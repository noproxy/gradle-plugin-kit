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

package com.github.noproxy.android.internal.api

import com.github.noproxy.android.internal.DefaultArgumentsComputer
import com.github.noproxy.gradle.test.api.template.UnitSpecification
import com.google.inject.Injector

import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class ArgumentsComputerTest extends UnitSpecification {
    private class User {}
    def injector = Mock(Injector)
    def computer = new DefaultArgumentsComputer(injector)
    def user = Stub(User)

    Executable mockExecutable(Class<?>... types) {
        return Mock(Executable) {
            getParameterTypes() >> types
            getParameters() >> params(types.length)
        }
    }

    def "select arguments from injector"() {
        when:
        def arguments = computer.argumentsFor(mockExecutable(String, int, User))

        then:
        arguments == ["test", 12, user].toArray()

        and:
        1 * injector.getInstance(String) >> "test"
        1 * injector.getInstance(int) >> 12
        1 * injector.getInstance(User) >> user
        0 * injector._
    }

    def "fill null arguments from injector"() {
        when:
        def arguments = computer.argumentsFor(mockExecutable(String, int, User), "test", 12, null)

        then:
        arguments == ["test", 12, user].toArray()

        and:
        1 * injector.getInstance(User) >> user
        0 * injector._
    }


    def "fill insufficient arguments from injector"() {
        when:
        def arguments = computer.argumentsFor(mockExecutable(String, int, User), "test", 12)

        then:
        arguments == ["test", 12, user].toArray()

        and:
        1 * injector.getInstance(User) >> user
        0 * injector._
    }

    def "intended null argument can use NULL"() {
        when:
        def arguments = computer.argumentsFor(mockExecutable(String, int, User),
                "test", 12, ArgumentsComputer.NULL)

        then:
        arguments == ["test", 12, null].toArray()

        and:
        0 * injector._
    }

    def "oversize arguments cause IllegalArgumentException"() {
        when:
        def arguments = computer.argumentsFor(mockExecutable(String, int, User),
                "test", 12, user, user)

        then:
        thrown(IllegalArgumentException)

        and:
        0 * injector._
    }

    def params(int size) {
        def p = new Parameter("name", 0, null, 0)
        return [p] * size
    }
}
