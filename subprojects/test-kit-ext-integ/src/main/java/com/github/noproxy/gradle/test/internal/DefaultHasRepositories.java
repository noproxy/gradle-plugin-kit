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

import com.github.noproxy.gradle.test.api.AppendableContext;
import com.github.noproxy.gradle.test.api.HasRepositories;

import static com.github.noproxy.gradle.test.api.AppendableContext.skipDuplicate;

public class DefaultHasRepositories extends TextContext implements HasRepositories {
    @Override
    public void appendTo(AppendableContext anotherContext) {
        super.appendTo(skipDuplicate(anotherContext));
    }

    private void internal(String named) {
        append(named + "()");
    }

    @Override
    public void mavenLocal() {
        internal("mavenLocal");
    }

    @Override
    public void mavenCentral() {
        internal("mavenCentral");
    }

    @Override
    public void jcenter() {
        internal("jcenter");
    }

    @Override
    public void google() {
        internal("google");
    }

    @Override
    public void maven(String name, String url) {
        String text = String.format("\n" +
                "maven { \n" +
                "    name '%s'\n" +
                "    url '%s'\n" +
                "}", name, url);
        append(text);
    }

    @Override
    public void maven(String url) {
        String text = String.format("\n" +
                "maven { \n" +
                "    url '%s'\n" +
                "}", url);
        append(text);
    }

    @Override
    public void mavenDefaults() {
        String text = String.format("\n" +
                "maven { \n" +
                "    url rootProject.file('%s')\n" +
                "}", HidingDirectoryProvider.MAVEN_DEFAULS_PATH);
        append(text);
    }
}
