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

class Template {
    static String simpleJava(String variant) {
        return """\
            package org.example;
            
            public class ClassIn${variant.capitalize()} {
                public static void main(String[] args) {}
            }
            """.stripIndent()
    }

    static String modulePom(String group, String artifact, String version) {
        """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do-not-remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>$group</groupId>
  <artifactId>$artifact</artifactId>
  <version>$version</version>
  <packaging>pom</packaging>
</project>"""
    }

    static String moduleText(String group, String artifact, String version, String selfVersion) {
        return """{
  "formatVersion": "1.0",
  "component": {
    "group": "$group",
    "module": "$artifact",
    "version": "$selfVersion",
    "attributes": {
      "org.gradle.status": "release"
    }
  },
  "createdBy": {
    "gradle": {
      "version": "5.6",
      "buildId": "cq7gm44l75b5xoymzhwaculthi"
    }
  },
  "variants": [
    {
      "name": "debugApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../$artifact/$version-debug/$artifact-$version-debug.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-debug"
      }
    },
    {
      "name": "debugRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../$artifact/$version-debug/$artifact-$version-debug.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-debug"
      }
    },
    {
      "name": "releaseApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../$artifact/$version-release/$artifact-$version-release.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-release"
      }
    },
    {
      "name": "releaseRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../$artifact/$version-release/$artifact-$version-release.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-release"
      }
    }
  ]
}
"""
    }


    static String moduleText(String group, String artifact, String version) {
        return """{
  "formatVersion": "1.0",
  "component": {
    "group": "$group",
    "module": "$artifact",
    "version": "$version",
    "attributes": {
      "org.gradle.status": "release"
    }
  },
  "createdBy": {
    "gradle": {
      "version": "5.6",
      "buildId": "cq7gm44l75b5xoymzhwaculthi"
    }
  },
  "variants": [
    {
      "name": "debugApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../$artifact/$version-debug/$artifact-$version-debug.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-debug"
      }
    },
    {
      "name": "debugRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "debug",
        "com.android.build.api.attributes.VariantAttr": "debug",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../$artifact/$version-debug/$artifact-$version-debug.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-debug"
      }
    },
    {
      "name": "releaseApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "available-at": {
        "url": "../../$artifact/$version-release/$artifact-$version-release.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-release"
      }
    },
    {
      "name": "releaseRuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "release",
        "com.android.build.api.attributes.VariantAttr": "release",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "available-at": {
        "url": "../../$artifact/$version-release/$artifact-$version-release.module",
        "group": "$group",
        "module": "$artifact",
        "version": "$version-release"
      }
    }
  ]
}
"""
    }

    static String variantPom(String group, String artifact, String version, String variant) {
        return """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do-not-remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>$group</groupId>
  <artifactId>$artifact</artifactId>
  <version>$version-$variant</version>
  <packaging>pom</packaging>
</project>"""
    }

    static String variantModuleText(String group, String artifact, String version, String variant) {
        return """{
  "formatVersion": "1.0",
  "component": {
    "url": "../../$artifact/$version/$artifact-${version}.module",
    "group": "$group",
    "module": "$artifact",
    "version": "0.0.9",
    "attributes": {
      "org.gradle.status": "release"
    }
  },
  "createdBy": {
    "gradle": {
      "version": "5.6",
      "buildId": "5j27mvm3rfgktkkvz4dymycw3a"
    }
  },
  "variants": [
    {
      "name": "${variant}ApiElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "$variant",
        "com.android.build.api.attributes.VariantAttr": "$variant",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-api"
      },
      "dependencies": [
        {
          "group": "org.apache.commons",
          "module": "commons-lang3",
          "version": {
            "requires": "3.9"
          }
        }
      ],
      "files": [
        {
          "name": "$artifact-$version-${variant}.aar",
          "url": "$artifact-$version-${variant}.aar",
          "size": 1112,
          "sha1": "4ac5624e6beeb508ee219911d751f55b6dc3b5df",
          "md5": "1ad40bcd1be55e0e26f67be508b18e4c"
        }
      ]
    },
    {
      "name": "${variant}RuntimeElements",
      "attributes": {
        "com.android.build.api.attributes.BuildTypeAttr": "$variant",
        "com.android.build.api.attributes.VariantAttr": "$variant",
        "com.android.build.gradle.internal.dependency.AndroidTypeAttr": "Aar",
        "org.gradle.usage": "java-runtime"
      },
      "dependencies": [
        {
          "group": "commons-io",
          "module": "commons-io",
          "version": {
            "requires": "2.4"
          }
        },
        {
          "group": "org.jetbrains",
          "module": "annotations",
          "version": {
            "requires": "13.0"
          }
        },
        {
          "group": "org.apache.commons",
          "module": "commons-lang3",
          "version": {
            "requires": "3.9"
          }
        }
      ],
      "files": [
        {
          "name": "$artifact-$version-${variant}.aar",
          "url": "$artifact-$version-${variant}.aar",
          "size": 1112,
          "sha1": "4ac5624e6beeb508ee219911d751f55b6dc3b5df",
          "md5": "1ad40bcd1be55e0e26f67be508b18e4c"
        }
      ]
    }
  ]
}
"""
    }
}
