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

package com.github.noproxy.android.internal;

import com.github.noproxy.android.internal.api.AnnotationAwareCallerInternal;
import com.github.noproxy.android.internal.api.ArgumentsComputer;
import com.github.noproxy.android.internal.api.DefaultArgumentsComputer;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NonNullApi;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@NonNullApi
public class DefaultAnnotationAwareCaller implements AnnotationAwareCallerInternal {
    private final ConstructorSelector constructorSelector = new DefaultConstructorSelector();
    private final ArgumentsComputer argumentsComputer = new DefaultArgumentsComputer();

    @Override
    public void call(Object hasActions) {
        final Method[] methods = hasActions.getClass().getDeclaredMethods();
        for (Method method : methods) {
            mayCall(method, hasActions);
        }
    }

    @Override
    public <T> T create(Class<T> clazzHasActions) {
        final Constructor<T> constructor = constructorSelector.selectConstructor(clazzHasActions);
        final Object[] arguments = argumentsComputer.argumentsFor(constructor);

        try {
            return constructor.newInstance(arguments);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new InvalidUserDataException("fail to instantiate class " + clazzHasActions);
        } catch (IllegalAccessException e) {
            throw new InternalErrorException("fail to instantiate class", e);
        }
    }

    @Override
    public <T> T createAndCall(Class<T> clazzHasActions) {
        final T instance = create(clazzHasActions);
        call(instance);
        return instance;
    }

    @Override
    @Nullable
    public Object mayCall(Method method, Object hasActions) {
        final Object[] arguments = argumentsComputer.argumentsFor(method);
        try {
            return method.invoke(hasActions, arguments);
        } catch (IllegalAccessException e) {
            throw new InternalErrorException("fail to invoke method", e);
        } catch (InvocationTargetException e) {
            throw new InvalidUserDataException("fail to invoke method", e);
        }
    }
}
