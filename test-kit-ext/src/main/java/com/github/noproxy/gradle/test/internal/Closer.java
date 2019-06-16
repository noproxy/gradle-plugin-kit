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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the annotated item will take responsible for closing all incoming {@link java.io.Closeable} resources.
 * <p>
 * When the annotated is a {@link ElementType#METHOD}, every incoming {@link java.io.Closeable}s will be closed when the method called.
 * When the annotated is a {@link ElementType#TYPE}, every incoming {@link java.io.Closeable}s will be closed
 * either when one of the enclosing {@link Closer} method called,
 * or when another closer is closed if this closer is an incoming resource of any other closer.
 * <p>
 * When the annotated is a {@link ElementType#FIELD}, it means the field is the final closer to take responsible for closing resources.
 */

@Target({
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.FIELD
})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Closer {
}
