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

package com.github.noproxy.gradle.test.internal;

import com.github.noproxy.gradle.test.api.FileIntegrator;
import com.github.noproxy.gradle.test.api.ProjectRunner;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

public class DefaultProjectRunner implements ProjectRunner {
    private final FileIntegrator integrator;
    private Project project;

    //    @WillCloseWhenClosed
    @ParameterWillBeClosed
    public DefaultProjectRunner(FileIntegrator integrator) {
        this.integrator = integrator;
    }


    @Override
    public Project getRootProject() {
        if (project == null) {
            Actions.close().execute(integrator);
            project = ProjectBuilder.builder()
                    .withProjectDir(integrator.getRoot())
                    .build();
        }
        return project;
    }

    @Override
    public Project getRootProject(String name) {
        if (project == null) {
            Actions.close().execute(integrator);
            project = ProjectBuilder.builder()
                    .withName(name)
                    .withProjectDir(integrator.getRoot())
                    .build();
        }
        return project;
    }
}
