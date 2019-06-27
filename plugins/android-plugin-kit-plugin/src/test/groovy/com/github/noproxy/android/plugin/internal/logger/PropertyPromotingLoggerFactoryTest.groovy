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

package com.github.noproxy.android.plugin.internal.logger

import com.github.noproxy.gradle.test.api.template.UnitSpecification
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import spock.lang.Unroll

class PropertyPromotingLoggerFactoryTest extends UnitSpecification {

    def "test promoting by property"() {
        final PropertyPromotingLoggerFactory factory = Spy(constructorArgs: [Stub(Project), "unused"], PropertyPromotingLoggerFactory) {
            1 * getPromotingMinLevel() >> LogLevel.QUIET
        } as PropertyPromotingLoggerFactory
        def delegateLogger = Mock(Logger)


        when:
        def promotedLogger = factory.promoting(delegateLogger)

        then:
        promotedLogger instanceof PromotingLogger
        def logger = promotedLogger as PromotingLogger

        logger.minLevel == LogLevel.QUIET


        when:
        logger.debug("test")

        then:
        1 * delegateLogger.log(LogLevel.QUIET, "test")
        0 * delegateLogger._

        when:
        logger.info("test")

        then:
        1 * delegateLogger.log(LogLevel.QUIET, "test")
        0 * delegateLogger._

        when:
        logger.warn("test")

        then:
        1 * delegateLogger.log(LogLevel.QUIET, "test")
        0 * delegateLogger._

        when:
        logger.lifecycle("test")

        then:
        1 * delegateLogger.log(LogLevel.QUIET, "test")
        0 * delegateLogger._
    }

    @Unroll
    def "log level promoted to #expectLevel if project property has value #propertyValue"(String propertyValue, LogLevel expectLevel) {
        final String LOG_SWITCH = "logSwitch"
//        property(LOG_SWITCH, propertyValue)

        final Project project = Mock(Project)

        when:
        def factory = new PropertyPromotingLoggerFactory(project, LOG_SWITCH)
        def level = factory.getPromotingMinLevel()

        then:
        expectLevel == level

        and:
        1 * project.hasProperty(LOG_SWITCH) >> true
        1 * project.property(LOG_SWITCH) >> propertyValue
        0 * _


        where:
        propertyValue | expectLevel
        "DEBUG"       | LogLevel.DEBUG
        "INFO"        | LogLevel.INFO
        "LIFECYCLE"   | LogLevel.LIFECYCLE
        "WARN"        | LogLevel.WARN
        "QUIET"       | LogLevel.QUIET
        "ERROR"       | LogLevel.ERROR

        "debug"       | LogLevel.DEBUG
        "info"        | LogLevel.INFO
        "lifecycle"   | LogLevel.LIFECYCLE
        "warn"        | LogLevel.WARN
        "quiet"       | LogLevel.QUIET
        "error"       | LogLevel.ERROR

        "true"        | LogLevel.QUIET
        "TRUE"        | LogLevel.QUIET
        "false"       | LogLevel.DEBUG
        "FALSE"       | LogLevel.DEBUG

        "max"         | LogLevel.QUIET
        "MAX"         | LogLevel.QUIET
        "min"         | LogLevel.DEBUG
        "MIN"         | LogLevel.DEBUG

        "all"         | LogLevel.QUIET
        "ALL"         | LogLevel.QUIET
        "none"        | LogLevel.DEBUG
        "NONE"        | LogLevel.DEBUG
    }

    def "InvalidLogLevelException if fail parsing property value"() {
        final String LOG_SWITCH = "logSwitch"

        given:
        def project = Mock(Project)
        def factory = new PropertyPromotingLoggerFactory(project, LOG_SWITCH)

        when:
        factory.getPromotingMinLevel()

        then:
        thrown(InvalidLogLevelException)

        and:
        1 * project.hasProperty(LOG_SWITCH) >> true
        1 * project.property(LOG_SWITCH) >> "unknown"
    }
}
