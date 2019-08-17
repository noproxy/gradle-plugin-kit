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

class GroovyFactory {
    private static final String IMPLEMENT_PACKAGE = GroovyFactory.class.getPackage().getName();
    private static final int METHOD_CURRENT = 0;
    private static final int METHOD_WHO_CALL_ME = 1;

    /**
     * @param offset 0 to get
     * @return current method name when offset = 0, previous method name when offset = 1
     */
    private static String getMethodName(int offset) {
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        // 1 is this method, aka 'getMethodName'
        // 2 is caller
        final StackTraceElement element = trace[2 + offset];
        return element.getMethodName();
    }

    private static String getImplementClassName(String methodName) {
        if (!methodName.startsWith("create")) {
            throw new AssertionError("the factory method name must start with 'create', but is '" + methodName + "'");
        }

        return methodName.replace("create", "Default");
    }

    private static <T> T createDefault() {
        final String implementClassName = getImplementClassName(getMethodName(METHOD_WHO_CALL_ME));
        try {
            //noinspection unchecked
            return (T) Class.forName(IMPLEMENT_PACKAGE + "." + implementClassName).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static ScriptContextInternal createScriptContext() {
        return createDefault();
    }

    static PluginsContextInternal createPluginsContext() {
        return createDefault();
    }
}
