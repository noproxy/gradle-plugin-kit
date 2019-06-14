/*
 *    Copyright 2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.noproxy.gradle.test.internal;

import com.github.noproxy.gradle.test.api.BuildRunner;
import com.github.noproxy.gradle.test.api.FileIntegrator;

import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class DefaultBuildRunner implements BuildRunner {
    private final DefaultGradleRunner gradleRunner;
    private final List<String> appendArguments = Lists.newArrayList();
    private final FileIntegrator integrator;
    private String output;
    private BuildResult result;
    private Throwable throwable = null;


    public DefaultBuildRunner(FileIntegrator integrator) {
        this.integrator = integrator;
        gradleRunner = new DefaultGradleRunner();
        gradleRunner.withDebug(true)
                .withPluginClasspath()
//                .withGradleVersion("4.10.2")
                .withProjectDir(integrator.getRoot())
                .forwardOutput();
    }

    @Override
    public void run(String... arguments) {
        Actions.close().execute(integrator);

        // clean
        output = null;
        result = null;
        throwable = null;

        final ArrayList<String> computed = Lists.newArrayList(arguments);
        computed.addAll(appendArguments);
        if (!computed.contains("--stacktrace")) {
            computed.add("--stacktrace");
        }

        try {
            result = gradleRunner
                    .withArguments(computed)
                    .build();
        } catch (UnexpectedBuildFailure failure) {
            result = failure.getBuildResult();
            throwable = failure;
        }
        output = result.getOutput();
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public void fail() {
        Assert.assertNotNull(result);
        Assert.assertNotNull(throwable);
    }

    @Override
    public void success() {
        Assert.assertNotNull(result);
        Assert.assertNull(throwable);
    }


    @Override
    public void assemble() {
        run("assemble");
    }

    @Override
    public void configure() {
        run("help");
    }
}
