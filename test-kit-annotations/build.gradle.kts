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

plugins {
    `java-library`
}

dependencies {
    implementation("com.squareup:javapoet:1.11.1")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc4")
}

// test
dependencies {
    components.all(HamcrestCapability::class.java)
    api("org.hamcrest:hamcrest:2.1")
    api("net.bytebuddy:byte-buddy:1.9.2")
    api("org.objenesis:objenesis:3.0.1")
    api("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(group = "org.codehaus.groovy", module = "groovy-all")
    }
}

class HamcrestCapability : ComponentMetadataRule {
    companion object {
        val HAMCRESTS = listOf("hamcrest-core", "hamcrest-integration", "hamcrest-library")
    }

    override fun execute(context: ComponentMetadataContext) {
        context.details.let {
            if (it.id.name in HAMCRESTS) {
                it.allVariants {
                    withCapabilities {
                        addCapability("org.hamcrest", "hamcrest", it.id.version + "." + HAMCRESTS.indexOf(it.id.name))
                    }
                }
            }
        }
    }
}