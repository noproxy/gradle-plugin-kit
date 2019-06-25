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

package com.github.noproxy.android.internal;

import com.github.noproxy.android.internal.api.ArgumentsComputer;
import com.google.inject.Injector;

import java.lang.reflect.Executable;

public class DefaultArgumentsComputer implements ArgumentsComputer {
    private final Injector injector;

    public DefaultArgumentsComputer(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Object[] argumentsFor(Executable constructorOrMethod) {
        return new Object[0];
    }

    @Override
    public Object[] argumentsFor(Executable constructorOrMethod, Object... providedArguments) {
        return new Object[0];
    }
}
