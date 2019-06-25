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

package com.github.noproxy.android.plugin.internal.logger;

import com.google.common.annotations.VisibleForTesting;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.slf4j.Marker;

import static org.gradle.api.logging.LogLevel.DEBUG;
import static org.gradle.api.logging.LogLevel.ERROR;
import static org.gradle.api.logging.LogLevel.INFO;
import static org.gradle.api.logging.LogLevel.LIFECYCLE;
import static org.gradle.api.logging.LogLevel.QUIET;
import static org.gradle.api.logging.LogLevel.WARN;

public class PromotingLogger implements Logger {
    private final Logger delegate;
    private final LogLevel minLevel;

    public PromotingLogger(Logger delegate, LogLevel minLevel) {
        this.delegate = delegate;
        this.minLevel = minLevel;
    }

    @VisibleForTesting
    public LogLevel getMinLevel() {
        return minLevel;
    }

    private LogLevel getPromotedLevel(LogLevel originLevel) {
        if (originLevel.compareTo(minLevel) > 0) {
            return originLevel;
        } else {
            return minLevel;
        }
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return delegate.isEnabled(getPromotedLevel(level));
    }

    @Override
    public void log(LogLevel level, String message) {
        delegate.log(getPromotedLevel(level), message);
    }

    @Override
    public void log(LogLevel level, String message, Object... objects) {
        delegate.log(getPromotedLevel(level), message, objects);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        delegate.log(getPromotedLevel(level), message, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(DEBUG);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isEnabled(DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(INFO);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isEnabled(INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(WARN);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isEnabled(WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(ERROR);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isEnabled(ERROR);
    }

    @Override
    public boolean isLifecycleEnabled() {
        return isEnabled(LIFECYCLE);
    }

    @Override
    public boolean isQuietEnabled() {
        return isEnabled(QUIET);
    }

    @Override
    public void debug(String message, Object... objects) {
        log(DEBUG, message, objects);
    }

    @Override
    public void info(String message, Object... objects) {
        log(INFO, message, objects);
    }

    @Override
    public void lifecycle(String message) {
        log(LIFECYCLE, message);
    }

    @Override
    public void lifecycle(String message, Object... objects) {
        log(LIFECYCLE, message, objects);
    }

    @Override
    public void lifecycle(String message, Throwable throwable) {
        log(LIFECYCLE, message, throwable);
    }

    @Override
    public void quiet(String message) {
        log(QUIET, message);
    }

    @Override
    public void quiet(String message, Object... objects) {
        log(QUIET, message, objects);
    }

    @Override
    public void quiet(String message, Throwable throwable) {
        log(QUIET, message, throwable);
    }


    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void debug(String s) {
        log(DEBUG, s);
    }

    @Override
    public void debug(String s, Object o) {
        log(DEBUG, s, o);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        log(DEBUG, s, o, o1);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        log(DEBUG, s, throwable);
    }

    @Override
    public void debug(Marker marker, String s) {
        log(DEBUG, s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        log(DEBUG, s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        log(DEBUG, s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        log(DEBUG, s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        log(DEBUG, s, throwable);
    }

    @Override
    public void info(String s) {
        log(INFO, s);
    }

    @Override
    public void info(String s, Object o) {
        log(INFO, s, o);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        log(INFO, s, o, o1);
    }

    @Override
    public void info(String s, Throwable throwable) {
        log(INFO, s, throwable);
    }

    @Override
    public void info(Marker marker, String s) {
        log(INFO, s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        log(INFO, s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        log(INFO, s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        log(INFO, s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        log(INFO, s, throwable);
    }

    @Override
    public void warn(String s) {
        log(WARN, s);
    }

    @Override
    public void warn(String s, Object o) {
        log(WARN, s, o);
    }

    @Override
    public void warn(String s, Object... objects) {
        log(WARN, s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        log(WARN, s, o, o1);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        log(WARN, s, throwable);
    }

    @Override
    public void warn(Marker marker, String s) {
        log(WARN, s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        log(WARN, s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        log(WARN, s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        log(WARN, s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        log(WARN, s, throwable);
    }

    @Override
    public void error(String s) {
        log(ERROR, s);
    }

    @Override
    public void error(String s, Object o) {
        log(ERROR, s, o);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        log(ERROR, s, o, o1);
    }

    @Override
    public void error(String s, Object... objects) {
        log(ERROR, s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        log(ERROR, s, throwable);
    }

    @Override
    public void error(Marker marker, String s) {
        log(ERROR, s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        log(ERROR, s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        log(ERROR, s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        log(ERROR, s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        log(ERROR, s, throwable);
    }


    // trace is not implemented in Gradle
    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {
    }

    @Override
    public void trace(String s, Object o) {
    }

    @Override
    public void trace(String s, Object o, Object o1) {
    }

    @Override
    public void trace(String s, Object... objects) {
    }

    @Override
    public void trace(String s, Throwable throwable) {
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String s) {
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
    }
}
