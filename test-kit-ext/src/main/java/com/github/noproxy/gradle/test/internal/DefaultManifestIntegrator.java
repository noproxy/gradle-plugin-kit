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

package com.github.noproxy.gradle.test.internal;

import com.github.noproxy.gradle.test.api.ManifestIntegrator;

import java.io.File;
import java.io.IOException;

import static com.github.noproxy.gradle.test.internal.Actions.setText;

public class DefaultManifestIntegrator implements ManifestIntegrator {
    private static final String MANIFEST_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n";
    private static final String MANIFEST_END = "</manifest>";
    private final File manifest;
    private String packageName;

    DefaultManifestIntegrator(File manifest) {
        this.manifest = manifest;
    }

    private static String getPackageLine(String packageName) {
        return "    package=\"" + packageName + "\">\n";
    }

    @Override
    public void close() throws IOException {
        setText(MANIFEST_START + getPackageLine(packageName) +
                "    <application\n" +
                "        android:label=\"Example App\"\n" +
                "        android:theme=\"@android:style/Theme.Material.Light\">\n" +
                "\n" +
                "        <activity\n" +
                "            android:name=\".MainActivity\"\n" +
                "            android:label=\"@string/app_name\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                "            </intent-filter>\n" +
                "        </activity>\n" +
                "    </application>\n"
                + MANIFEST_END).execute(manifest);
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
