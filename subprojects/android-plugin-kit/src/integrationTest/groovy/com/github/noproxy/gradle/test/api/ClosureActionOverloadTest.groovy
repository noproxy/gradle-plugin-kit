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

package com.github.noproxy.gradle.test.api

import com.github.noproxy.gradle.test.internal.DefaultFileIntegrator
import com.github.noproxy.gradle.test.internal.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method

class ClosureActionOverloadTest extends Specification {
    @Rule
    TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider()
    FileIntegrator tested = new DefaultFileIntegrator(testDirectoryProvider.root)

    File getRoot() {
        return testDirectoryProvider.getRoot()
    }

    def hasParameterSize(Collection<Method> methods, int parameterSize) {
        return methods.findAll {
            it.parameterCount == parameterSize
        }
    }

    def computeHasClosureOverloadMethods(Class<?> testType) {
        def methods = testType.methods
        def answer = methods.findAll {
            def params = it.parameterTypes
            if (params.length == 0) {
                return false
            }
            if (params[params.length - 1] == Closure) {
                def overloadParameterTypes = params.dropRight(1)
                def requiredName = it.name
                return methods.any {
                    it.name == requiredName && it.parameterTypes == overloadParameterTypes
                }
            }
            return false
        }
        return answer
    }


    @Unroll
    def "test closure overload2 #method.name#method.parameters"(Method method) {
        def types = method.parameterTypes
        def closure = Mock(Closure)
        def innerArguments = types.toList().dropRight(1).collect { anyInstanceFor(it) }
        def arguments = innerArguments + [closure]
        def instance = Spy(tested)
        def toConfigure = anyInstanceFor(method.returnType)

        when:
        def answer = instance.invokeMethod(method.name, arguments as Object[])

        then:
        1 * instance./${method.name}/(innerArguments[0], closure)

        and:
        1 * instance./${method.name}/(innerArguments[0]) >> toConfigure
        0 * instance._

        and:
        1 * closure.setDelegate(toConfigure)

        and:
        1 * closure.call(toConfigure)

        and:
        answer == toConfigure

        where:
        method << hasParameterSize(computeHasClosureOverloadMethods(FileIntegrator), 2)
    }

    private <T> T anyInstanceFor(Class<?> type) {
        if (type == File) {
            return new File("test") as T
        } else if (type == String) {
            return "test" as T
        }

        //noinspection GroovyAssignabilityCheck
        return GroovyStub(type)
    }

    def "test #method"(String method) {
        def expect = new File(root, 'test')
        Closure action = Mock()

        expect:
        expect == call(method, 'test')

        when:
        def result1 = call(method, ['test', action])

        then:
        result1 == expect

        and:
        1 * action.setDelegate(expect)
        1 * action.call(expect) >> null

        where:
        method << ["file", "newDir"]
    }

    def call(String methodName, Object... arguments) {
        return tested.invokeMethod(methodName, arguments)
    }
}
