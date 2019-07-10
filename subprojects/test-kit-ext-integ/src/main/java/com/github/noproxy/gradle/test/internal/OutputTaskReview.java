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

import com.github.noproxy.gradle.test.api.BuildRunner;
import com.github.noproxy.gradle.test.api.TaskReview;

//TODO use better implementation because broken when '--quiet'
public class OutputTaskReview implements TaskReview {
    private static final String TASK_UP_TO_DATE = "UP-TO-DATE";
    private static final String TASK_FROM_CACHE = "FROM-CACHE";
    private static final String TASK_NO_SOURCE = "NO-SOURCE";
    private final BuildRunner runner;

    public OutputTaskReview(BuildRunner runner) {
        this.runner = runner;
    }

    private String getOutput() {
        return runner.getOutput();
    }

    private String keywordFor(String task, String taskStatus) {
        if (task.startsWith(":")) {
            // for example, ':build', NO-SOURCE
            // match 'Task :build NO-SOURCE'
            // not match 'Task :app:build NO-SOURCE'
            return "Task " + task + " " + taskStatus;
        }
        // 'build', NO-SOURCE
        // match 'Task :build NO-SOURCE'
        // match 'Task :app:build NO-SOURCE'
        return ":" + task + " " + taskStatus;
    }

    private String keywordFor(String task) {
        if (task.startsWith(":")) {
            return "Task " + task;
        }
        return ":" + task;
    }

    @Override
    public boolean taskUpToDate(String task) {
        return getOutput().contains(keywordFor(task, TASK_UP_TO_DATE));
    }

    @Override
    public boolean taskFromCached(String task) {
        return getOutput().contains(keywordFor(task, TASK_FROM_CACHE));
    }

    @Override
    public boolean taskNoSource(String task) {
        return getOutput().contains(keywordFor(task, TASK_NO_SOURCE));
    }

    //  Task :checkstyleMain FAILED should considered as run
    @Override
    public boolean taskRun(String task) {
        return !taskNotExecuted(task)
                && !taskNotExecuted(task)
                && !taskFromCached(task)
                && !taskNoSource(task);
    }

    @Override
    public boolean taskNotExecuted(String task) {
        return !getOutput().contains(keywordFor(task));
    }

    @Override
    public boolean taskSkipped(String task) {
        return taskNotExecuted(task)
                || taskFromCached(task)
                || taskNoSource(task);
    }
}
