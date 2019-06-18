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

import com.github.noproxy.android.internal.api.ConstructorSelector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DefaultConstructorSelector implements ConstructorSelector {
    private boolean isPublicOrPackageScoped(Class<?> type, Constructor<?> constructor) {
        if (isPackagePrivate(type.getModifiers())) {
            return !Modifier.isPrivate(constructor.getModifiers()) && !Modifier.isProtected(constructor.getModifiers());
        } else {
            return Modifier.isPublic(constructor.getModifiers());
        }
    }

    private boolean isPackagePrivate(int modifiers) {
        return !Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPublic(modifiers);
    }

    @Override
    public <T> Constructor<T> selectConstructor(Class<T> type) {
        //noinspection unchecked
        Constructor<T>[] constructors = (Constructor<T>[]) type.getDeclaredConstructors();

        if (constructors.length == 1) {
            Constructor<T> constructor = constructors[0];
            if (constructor.getParameterTypes().length == 0 && isPublicOrPackageScoped(type, constructor)) {
                return constructor;
            }
            if (constructor.getAnnotation(Inject.class) != null) {
                return constructor;
            }
            if (constructor.getAnnotation(com.google.inject.Inject.class) != null) {
                throw new InvalidUsageException(String.format("The constructor for class %s is annotated with @com.google.inject.Inject, you should use @javax.inject.Inject", type.getName()));
            }
            if (constructor.getParameterTypes().length == 0) {
                throw new InvalidUsageException(String.format("The constructor for class %s should be public or package protected or annotated with @Inject.", type.getName()));
            } else {
                throw new InvalidUsageException(String.format("The constructor for class %s should be annotated with @Inject.", type.getName()));
            }
        }

        List<Constructor<T>> injectConstructors = new ArrayList<>();
        for (Constructor<T> constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                injectConstructors.add(constructor);
            }
        }

        if (injectConstructors.isEmpty()) {
            throw new InvalidUsageException(String.format("Class %s has no constructor that is annotated with @Inject.", type.getName()));
        }
        if (injectConstructors.size() > 1) {
            throw new InvalidUsageException(String.format("Class %s has multiple constructors that are annotated with @Inject.", type.getName()));
        }
        return injectConstructors.get(0);
    }
}
