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

package io.github.noproxy.plugin.robust

import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class RobustMavenPublishPluginSpec extends IntegrateSpecification {
    void createRobustXml(boolean proguard) {
        newFile('robust.xml') << """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <switch>
        <turnOnRobust>true</turnOnRobust>
        <manual>false</manual>

        <catchReflectException>true</catchReflectException>

        <patchLog>true</patchLog>
        <proguard>$proguard</proguard>

        <useAsm>true</useAsm>
    </switch>

    <packname name="hotfixPackage">
        <name>org.example</name>
    </packname>

    <!--不需要Robust插入代码的包名，Robust库不需要插入代码，如下的配置项请保留，还可以根据各个APP的情况执行添加-->
    <exceptPackname name="exceptPackage">
        <name>com.meituan.robust</name>
        <name>com.meituan.sample.extension</name>
        <name>com.meituan.sample.extension</name>
        <name>com.bytedance</name>
    </exceptPackname>

    <patchPackname name="patchPackname">
        <name>com.rx.rbs.patch</name>
    </patchPackname>
</resources>"""
    }

    def "test publish methodMapping to maven"() {
        given:
        createRobustXml(false)
        buildFile """
plugins {
    id 'io.github.noproxy.robust-maven-publish'
    id 'com.android.application'
    id 'robust'
}

repositories {
    jcenter()
    google()
}

android {
    defaultConfig {
        applicationId "org.example.app"
        compileSdkVersion 28
    }
}

robustPublish {
    version = "2.3"
}

publishing {
    repositories {
        maven {
            name = 'BuildDir'
            url = rootProject.file("build/repo")
        }
    }
}

"""
        newFile("src/main/java/org/example/app/MainActivity.java") << "package org.example.app;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "\n" +
                "public class MainActivity extends Activity {\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        System.out.println(getResources().getString(R.string.app_name));" +
                "    }\n" +
                "}"
        newFile("src/main/res/values/strings.xml") << """<resources>
    <string name="app_name">Example Demo</string>
</resources>
"""
        android {
            manifest {
                packageName = "org.example.app"
            }
        }

        when:
        run "assembleRelease", "publishRobustReleasePublicationToBuildDir"

        then:
        def methodMap = file("build/repo/org/robust/meta/org.example.app/2.3-release/org.example.app-2.3-release.robust")
        assert methodMap.exists()
        def pom = file("build/repo/org/robust/meta/org.example.app/2.3-release/org.example.app-2.3-release.pom")
        assert pom.exists()
    }

    def "test resolve methodMapping from maven"() {
        given:
        buildFile """
plugins {
    id 'io.github.noproxy.robust-maven-publish'
    id 'com.android.application'
    id 'auto-patch-plugin'
}

repositories {
    jcenter()
    google()
    maven {
        name = 'BuildDir'
        url = rootProject.file("build/repo")
    }
    maven {
        url 'https://maven.apuscn.com/nexus/content/repositories/android-xal-releases/'
        url 'https://maven.apuscn.com/nexus/content/repositories/android-xal-snapshots/'
    }
}

android {
    defaultConfig {
        applicationId "org.example.app"
        compileSdkVersion 28
        minSdkVersion 21
    }
}

robustPublish {
    version = "2.3"
}

robustResolver {
    version = "1.1"
}
dependencies {
    implementation("com.xal.robust:robust:1.0.0-beta01")
}
"""
        newFile("src/main/java/org/example/app/MainActivity.java") << "package org.example.app;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "\n" +
                "public class MainActivity extends Activity {\n" +
                "\n" +
                "    @Override\n" +
                "    @com.meituan.robust.patch.annotaion.Modify\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        System.out.println(getResources().getString(R.string.app_name)+\"mod\");" +
                "    }\n" +
                "}"
        newFile("src/main/res/values/strings.xml") << """<resources>
    <string name="app_name">Example Demo</string>
</resources>
"""
        android {
            manifest {
                packageName = "org.example.app"
            }
        }
        createRobustXml(false)

        when:
        run "help"
        then:
        noExceptionThrown()

        when:
        run "transformClassesWithAutoPatchTransformForRelease"
        then:
        fail()
        assert output.contains("Could not resolve all files for configuration ':robustResolveReleaseMethodMapClasspath'")
        assert output.contains("Could not find org.robust.meta:org.example.app:1.1-release.")

        when:
        newFile("build/repo/org/robust/meta/org.example.app/1.1-release/org.example.app-1.1-release.robust") << binaryMethodMap()
        newFile("build/repo/org/robust/meta/org.example.app/1.1-release/org.example.app-1.1-release.pom") << pom()
        fail()

        run "transformClassesWithAutoPatchTransformForRelease"

        then:
        success()
        assert output.contains("robust")
        assert output.contains("key is   org.example.app.MainActivity.onCreate(android.os.Bundle)")
    }

    static String mappingContent() {
        return "org.example.app.MainActivity -> test.a:\n"
    }

    static byte[] binaryMethodMap() {
        //noinspection SpellCheckingInspection
        return Base64.decoder.decode("H4sIAAAAAAAAAEWOK08DQRSFb5dseBSxIEDhwYxC1PFKGjahxRMEl52b3YHpzOTO7DJIDAgseASyP6KEX4BFETwagWGbQHqSc9R3km/8BalnWL/ABkUdlBZHylySPERfDdBtT4anmZ78JNA5gS4WBXl/zJI4MqzMPn90Ov/+8rp29jYHSR+WtEXZxyJYzmExVEy+slpGt7ML0yxfLbSbte0E6FkuBUUcOU0CnRMDVGavCKpR4VpYc8CEgTbRSLZKCuvFfm2kpi3/76HRlCI3gUri1c+n5++bu15rnUPaoK6p9c1m3LAenRPfjh83ug8f9wlAdFOPCL91riBHDwEAAA==")
    }

    static String pom() {
        """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.robust.meta</groupId>
  <artifactId>org.example.app</artifactId>
  <version>1.1-release</version>
  <packaging>robust</packaging>
</project>
"""
    }
}



