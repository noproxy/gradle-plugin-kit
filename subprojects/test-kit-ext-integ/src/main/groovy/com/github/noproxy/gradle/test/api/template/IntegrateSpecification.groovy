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

package com.github.noproxy.gradle.test.api.template

import com.github.noproxy.gradle.test.api.*
import com.github.noproxy.gradle.test.internal.*
import org.junit.Rule
import spock.lang.Specification

@CleanupTestDirectory
class IntegrateSpecification extends Specification {
    @Rule
    TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider()

    @Delegate(parameterAnnotations = true)
    private FileIntegrator integrator = new DefaultFileIntegrator(testDirectoryProvider.getTestDirectory())

    @Delegate(parameterAnnotations = true)
    private ProjectIntegrator project = Integrators.project(integrator)

    @Closer
    @Delegate(parameterAnnotations = true)
    private BuildRunner runner = new DefaultBuildRunner(integrator)

    @Delegate(parameterAnnotations = true)
    private TaskReview taskReview = new OutputTaskReview(runner)

    private final MavenIntegrator mavenDefaults = Integrators.mavenDefaults(integrator)

    void maven(@DelegatesTo(MavenIntegrator) Closure closure) {
        closure.delegate = mavenDefaults
        closure()
    }
}
