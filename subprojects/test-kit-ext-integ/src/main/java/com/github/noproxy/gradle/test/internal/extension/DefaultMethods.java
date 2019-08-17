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

package com.github.noproxy.gradle.test.internal.extension;

import com.github.noproxy.gradle.test.api.AppendableContext;
import com.github.noproxy.gradle.test.internal.Appender;
import com.github.noproxy.gradle.test.internal.DelegatingAppender;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.util.CharSequenceReader;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class DefaultMethods {
    public static String indent(CharSequence self, int steps) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iterator = IOGroovyMethods.iterator(new CharSequenceReader(self));
        while (iterator.hasNext()) {
            final String line = iterator.next();
            for (int i = 0; i < steps; i++) {
                builder.append("    ");
            }
            builder.append(line).append('\n');
        }

        if (self.charAt(self.length() - 1) != '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    public static Appender indent(Appender self, int steps) {
        return new IndentAppender(self, steps);
    }

    public static Appender wrap(String header, Appender self, String tail) {
        return wrap(header, self, tail, true);
    }

    public static Appender wrap(String header, Appender self, String tail, boolean skipWhenEmpty) {
        return new DelegatingAppender(self) {
            @Override
            public void appendTo(AppendableContext target) {
                if (skipWhenEmpty && isEmpty()) {
                    return;
                }

                target.append(header);
                getDelegate().appendTo(target);
                target.append(tail);
            }
        };
    }

    private static class IndentAppender implements Appender {
        private final Appender upstream;
        private final int steps;

        IndentAppender(Appender upstream, int steps) {
            this.upstream = upstream;
            this.steps = steps;
        }

        @Override
        public void appendTo(AppendableContext anotherContext) {
            upstream.appendTo(new IndentAppendableContext(anotherContext));
        }

        @Override
        public boolean isEmpty() {
            return upstream.isEmpty();
        }

        private class IndentAppendableContext implements AppendableContext {
            private final AppendableContext downstream;

            IndentAppendableContext(AppendableContext downstream) {
                this.downstream = downstream;
            }

            @Override
            public void append(@NotNull CharSequence text) {
                downstream.append(DefaultMethods.indent(text, steps));
            }
        }
    }
}
