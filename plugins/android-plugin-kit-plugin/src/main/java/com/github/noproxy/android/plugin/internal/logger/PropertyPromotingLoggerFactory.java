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

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PropertyPromotingLoggerFactory {
    private static final List<String> QUIET_VALUES = Arrays.asList("true", "max", "all", "yes", "y");
    private static final List<String> NO_PROMOTING_VALUES = Arrays.asList("false", "min", "none", "no", "n");
    private static final LogLevel MIN_LEVEL = LogLevel.DEBUG;
    @NotNull
    private final Project project;
    @NotNull
    private final String promotingPropertyName;

    public PropertyPromotingLoggerFactory(@NotNull Project project, @NotNull String promotingPropertyName) {
        this.project = project;
        this.promotingPropertyName = promotingPropertyName;
    }

    private LogLevel parseLogLevel(@NotNull String value) {
        if (QUIET_VALUES.stream().anyMatch(value::equalsIgnoreCase)) {
            return LogLevel.QUIET;
        }
        if (NO_PROMOTING_VALUES.stream().anyMatch(value::equalsIgnoreCase)) {
            return MIN_LEVEL;
        }

        try {
            return LogLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidLogLevelException(value);
        }
    }

    @VisibleForTesting
    public LogLevel getPromotingMinLevel() {
        if (project.hasProperty(promotingPropertyName)) {
            final String value = (String) project.property(promotingPropertyName);
            if (value != null) {
                return parseLogLevel(value);
            }
        }
        // not promoted
        return MIN_LEVEL;
    }

    @NotNull
    public Logger promoting(@NotNull Logger originLogger) {
        if (originLogger instanceof PromotingLogger) {
            return originLogger;
        }

        return new PromotingLogger(originLogger, getPromotingMinLevel());
    }
}
