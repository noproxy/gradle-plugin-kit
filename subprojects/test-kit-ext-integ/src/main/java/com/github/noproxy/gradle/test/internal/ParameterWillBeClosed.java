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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the annotated executable will close all incoming parameters with type {@link java.io.Closeable}.
 * <p>
 * When it's {@link ElementType#METHOD} or {@link ElementType#CONSTRUCTOR}, the closeable parameters will be close.
 */
@Target({
        ElementType.METHOD,
        ElementType.CONSTRUCTOR
})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterWillBeClosed {
}
