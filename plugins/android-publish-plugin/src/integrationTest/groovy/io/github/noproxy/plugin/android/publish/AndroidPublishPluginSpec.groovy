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

package io.github.noproxy.plugin.android.publish

import com.github.noproxy.gradle.test.api.template.IntegrateSpecification

class AndroidPublishPluginSpec extends IntegrateSpecification {
    def "test publish aar to maven"() {
        given:
        buildFile """
plugins {
    id 'io.github.noproxy.android-publish-plugin'
    id 'com.android.application'
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
        run "publishAndroidLibraryPublicationToBuildDir"

        then:
        def apk = file("build/repo/org/tinker/app/org.example.app/2.3-release/org.example.app-2.3-release.apk")
        assert apk.exists()
        def pom = file("build/repo/org/tinker/app/org.example.app/2.3-release/org.example.app-2.3-release.pom")
        assert pom.exists()
    }

    def "test resolve apk from maven"() {
        given:
        buildFile """
plugins {
    id 'com.github.noproxy.tinker-maven-publish'
    id 'com.android.application'
    id 'com.tencent.tinker.patch'
}

repositories {
    jcenter()
    google()
    maven {
        name = 'BuildDir'
        url = rootProject.file("build/repo")
    }
}

android {
    defaultConfig {
        applicationId "org.example.app"
        compileSdkVersion 28
    }
}

tinkerPublish {
    version = "2.3"
}

tinkerResolver {
    version = "1.1"
}

tinkerPatch {
    buildConfig {
        tinkerId = "2.3"    
    }
    useSign = false
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex", "assets/secondary-dex-*.jar"]
        loader = ["com.tencent.tinker.loader.*"]
    }
    lib {
        pattern = ["lib/*/*.so"]
    }
    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = ["assets/*_meta.txt"]
        largeModSize = 100
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
        run "help"
        then:
        noExceptionThrown()

        when:
        run "tinkerPatchRelease"
        then:
        fail()
        assert output.contains("Could not resolve all files for configuration ':tinkerResolveApkClasspath'")
        assert output.contains("Could not find org.tinker.app:org.example.app:1.1-release.")

        when:
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk") << binaryApk()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.pom") << pom()

        systemExit.expectSystemExit()
        run "tinkerPatchRelease"

        then:
        fail()
        assert output.contains("")
        with(output) {
            contains "Can not find the R.txt file in Maven Repository, continue build without R file."
            contains "Tinker patch begin"
            contains "oldApk:${root}/build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk"
            contains "newApk:${root}/build/outputs/apk/release/${root.name}-release-unsigned.apk"

        }
    }

    def "test resolve apk mapping and r from maven"() {
        given:
        buildFile """
plugins {
    id 'com.github.noproxy.tinker-maven-publish'
    id 'com.android.application'
    id 'com.tencent.tinker.patch'
}

repositories {
    jcenter()
    google()
    maven {
        name = 'BuildDir'
        url = rootProject.file("build/repo")
    }
}

android {
    defaultConfig {
        applicationId "org.example.app"
        compileSdkVersion 28
    }
}

tinkerPublish {
    version = "2.3"
}

tinkerResolver {
    version = "1.1"
}

tinkerPatch {
    buildConfig {
        tinkerId = "2.3"    
    }
    useSign = false
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex", "assets/secondary-dex-*.jar"]
        loader = ["com.tencent.tinker.loader.*"]
    }
    lib {
        pattern = ["lib/*/*.so"]
    }
    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = ["assets/*_meta.txt"]
        largeModSize = 100
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
        run "help"
        then:
        noExceptionThrown()

        when:
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk") << binaryApk()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-mapping.txt") << mappingContent()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-r.txt") << rContent()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.pom") << pom()

        systemExit.expectSystemExit()
        run "tinkerPatchRelease"

        then:
        fail()
        assert !output.contains("Could not resolve all files for configuration ':tinkerResolveApkClasspath'")
        assert !output.contains("Could not find org.tinker.app:org.example.app:1.1-release.")

        with(output) {
            contains "we build ${root.name} apk with apply resource mapping file ${root}/build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-r.txt"
            contains "Tinker patch begin"
            contains "oldApk:${root}/build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk"
            contains "newApk:${root}/build/outputs/apk/release/${root.name}-release-unsigned.apk"
        }
    }

    def "test resolve apk mapping and r from maven with ProGuard"() {
        given:
        buildFile """
plugins {
    id 'com.github.noproxy.tinker-maven-publish'
    id 'com.android.application'
    id 'com.tencent.tinker.patch'
}

repositories {
    jcenter()
    google()
    maven {
        name = 'BuildDir'
        url = rootProject.file("build/repo")
    }
}

android {
    defaultConfig {
        applicationId "org.example.app"
        compileSdkVersion 28
    }
    
    buildTypes {
        release {
            minifyEnabled = true
            proguardFiles 'proguard-rules.pro'
        }
    }
}

tinkerPublish {
    version = "2.3"
}

tinkerResolver {
    version = "1.1"
}

tinkerPatch {
    buildConfig {
        tinkerId = "2.3"    
    }
    useSign = false
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex", "assets/secondary-dex-*.jar"]
        loader = ["com.tencent.tinker.loader.*"]
    }
    lib {
        pattern = ["lib/*/*.so"]
    }
    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = ["assets/*_meta.txt"]
        largeModSize = 100
    }
}

"""
        newFile("proguard-rules.pro") << "-ignorewarnings"
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
        run "help"
        then:
        noExceptionThrown()

        when:
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk") << binaryApk()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-mapping.txt") << mappingContent()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-r.txt") << rContent()
        newFile("build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.pom") << pom()

        systemExit.expectSystemExit()
        run "tinkerPatchRelease"

        then:
        fail()
        assert !output.contains("Could not resolve all files for configuration ':tinkerResolveApkClasspath'")
        assert !output.contains("Could not find org.tinker.app:org.example.app:1.1-release.")
        assert output.contains("Warning: org.example.app.MainActivity is not being kept as 'org.example.app.MainActivity', but remapped to 'test.a'")


        with(output) {
            contains "we build ${root.name} apk with apply resource mapping file ${root}/build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release-r.txt"
            contains "Tinker patch begin"
            contains "oldApk:${root}/build/repo/org/tinker/app/org.example.app/1.1-release/org.example.app-1.1-release.apk"
            contains "newApk:${root}/build/outputs/apk/release/${root.name}-release-unsigned.apk"
        }
    }

    static String mappingContent() {
        return "org.example.app.MainActivity -> test.a:\n"
    }

    static String rContent() {
        return "int string app_name 0x7f0f0000\n"
    }

    static byte[] binaryApk() {
        //noinspection SpellCheckingInspection
        return Base64.decoder.decode("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")
    }

    static String pom() {
        """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.tinker.app</groupId>
  <artifactId>org.example.app</artifactId>
  <version>1.1-release</version>
  <packaging>apk</packaging>
</project>
"""
    }
}
