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

package com.github.noproxy.gradle.test.api;

import com.github.noproxy.gradle.test.internal.Actions;
import com.github.noproxy.gradle.test.internal.extension.DefaultMethods;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

import static com.github.noproxy.gradle.test.internal.Actions.execute;

public interface AppendableContext {
    static AppendableContext of(File file) {
        return text -> {
            if (text.length() == 0) {
                text = "\n";
            } else if (text.charAt(text.length() - 1) != '\n') {
                text = text + "\n";
            }
            execute(file, Actions.append(text));
        };
    }

    static AppendableContext indent(AppendableContext context, int steps) {
        return text -> {
            DefaultMethods.indent(text, steps);
        };
    }

    static AppendableContext skipDuplicate(AppendableContext context) {
        Set<CharSequence> appended = Sets.newHashSet();
        return text -> {
            if (!appended.contains(text)) {
                appended.add(text);
                context.append(text);
            }
        };
    }

    void append(@NotNull CharSequence text);
}
