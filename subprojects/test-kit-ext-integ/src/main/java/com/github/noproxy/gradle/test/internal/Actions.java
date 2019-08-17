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

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Action;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.noproxy.gradle.test.internal.extension.DefaultMethods.indent;
import static org.codehaus.groovy.runtime.ResourceGroovyMethods.getText;

public class Actions {
    private static final Action ACTION_NOTHING = o -> {
    };

    public static <T> Action<T> nothing() {
        //noinspection unchecked
        return ACTION_NOTHING;
    }

    @SafeVarargs
    public static <T> void execute(T target, Action<T>... action) {
        composite(action).execute(target);
    }

    @SuppressWarnings("unchecked")
    public static <T> Action<T> composite(Action<T>... actions) {
        switch (actions.length) {
            case 0:
                return nothing();
            case 1:
                return actions[0];
            default:
                return t -> {
                    for (Action<T> action : actions) {
                        action.execute(t);
                    }
                };
        }
    }

    public static <T> Action<T> composite(Iterable<Action<T>> actions) {
        return t -> {
            for (Action<T> action : actions) {
                action.execute(t);
            }
        };
    }

    @SafeVarargs
    public static <T> Action<T> composite(Iterable<Action<T>> actions, Action<T>... others) {
        return t -> {
            for (Action<T> action : actions) {
                action.execute(t);
            }
            for (Action<T> action : others) {
                action.execute(t);
            }
        };
    }

    public static <T> Action<T> uncheckIO(UncheckIOAction<T> action) {
        return t -> {
            try {
                action.execute(t);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Deprecated
    public static Action<File> wrapText(int indentStep, String leading, String tailing) {
        return uncheckIO(file -> {
                    final String origin = ResourceGroovyMethods.getText(file, "UTF-8");
                    ResourceGroovyMethods.setText(file, "\n" + leading + "\n", "UTF-8");
                    ResourceGroovyMethods.append(file, indent(origin, indentStep));
                    ResourceGroovyMethods.append(file, "\n" + tailing);
                }
        );
    }

    public static Action<File> setText(String text) {
        return uncheckIO(file -> ResourceGroovyMethods.setText(file, text, "UTF-8"));
    }

    public static Action<File> append(Object text) {
        return uncheckIO(file -> ResourceGroovyMethods.append(file, text, "UTF-8"));
    }

    public static Action<File> appendText(String text) {
        return uncheckIO(file -> ResourceGroovyMethods.append(file, text, "UTF-8"));
    }

    public static <T> Action<T> of(Closure<?> closure) {
        return ClosureBackedAction.of(closure);
    }

    public static <T> Iterable<Action<T>> of(Iterable<Closure> closure) {
        final List result = StreamSupport.stream(closure.spliterator(), false)
                .map(ClosureBackedAction::of).collect(Collectors.toList());
        //noinspection unchecked
        return (Iterable<Action<T>>) result;
    }

    public static Action<File> createFile() {
        //noinspection ResultOfMethodCallIgnored
        return uncheckIO(File::createNewFile);
    }

    public static Action<File> mkdirs() {
        //noinspection ResultOfMethodCallIgnored
        return File::mkdirs;
    }

    public static Action<Closeable> close() {
        return uncheckIO(Closeable::close);
    }

    public static Action<File> restoreText(Action<File> action) {
        return uncheckIO(file -> {
            final String origin = getText(file, "UTF-8");
            ResourceGroovyMethods.setText(file, "");
            action.execute(file);
            ResourceGroovyMethods.append(file, origin);
        });
    }

    public interface UncheckIOAction<T> {
        void execute(T t) throws IOException;
    }
}
