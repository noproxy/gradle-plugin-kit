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

package com.github.noproxy.gradle.test.internal

import com.github.noproxy.gradle.test.api.AppendableContext
import com.github.noproxy.gradle.test.api.HasRepositories

import static com.github.noproxy.gradle.test.internal.extension.DefaultMethods.wrap

class DefaultScriptContext implements ScriptContextInternal {
    @Delegate(parameterAnnotations = true, includeTypes = AppendableContext.class)
    private final TextContext textContext = new TextContext()

    @Delegate(parameterAnnotations = true, includeTypes = HasRepositories.class)
    private final DefaultHasRepositories hasRepositories = new DefaultHasRepositories()


    @Override
    void appendTo(AppendableContext anotherContext) {
        textContext.appendTo(anotherContext)
        wrap("repositories {",
                hasRepositories,
                "}")
                .appendTo(anotherContext)
    }

    @Override
    boolean isEmpty() {
        return textContext.empty && hasRepositories.empty
    }
}
