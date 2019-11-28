package io.github.noproxy.init


import com.android.xml.AndroidXPathFactory
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import java.util.stream.Collectors

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


@SuppressWarnings("unused")
class CheckManifestPlugin implements Plugin<Gradle> {
    final IOFileFilter manifestFilter = new NameFileFilter("AndroidManifest.xml");

    private NodeList getNodesByXPath(final Document doc, String expression) {
        try {
            return (NodeList) AndroidXPathFactory.newXPath().compile(expression).evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("xpath error: " + expression, e);
        }
    }

    private Document parseXml(File manifestFile) {
        final FileReader fileReader
        try {
            fileReader = new FileReader(manifestFile)
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            return builder.parse(new InputSource(fileReader))
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (fileReader != null) {
                fileReader.close()
            }
            throw new IOException("failed to read AndroidManifest.xml", e)
        }
    }

    private boolean shouldCheckManifestNetworkSecurityConfig(android, variant) {
        def targetSdkVersion = variant.mergedFlavor.targetSdkVersion

        int apiLevel
        if (targetSdkVersion == null) {
            try {
                apiLevel = Integer.parseInt(android.compileSdkVersion)
            } catch (NumberFormatException | NullPointerException ignore) {
                // compileSdkVersion not set, build will fail
                return false
            }
        } else {
            apiLevel = targetSdkVersion.apiLevel
        }

        return apiLevel >= 28
    }

    @Override
    void apply(Gradle gradle) {
        gradle.allprojects { Project project ->
            plugins.withId("com.android.application") {
                project.afterEvaluate {
                    def android = project.extensions.getByName("android")
                    android.applicationVariants.all {
                        if (!shouldCheckManifestNetworkSecurityConfig(android, delegate)) {
                            project.logger.quiet("skip check manifest because target sdk < 28: {}", name)
                            return
                        }

                        tasks.getByName("process${name.capitalize()}Manifest").doLast { Task task ->
                            def logger = task.logger
                            def dirs = task.outputs.files.filter {
                                it.exists() && it.isDirectory() && it.canRead()
                            }.files

                            final Set<File> manifestInDir = dirs.stream()
                                    .flatMap { dir -> Objects.requireNonNull(FileUtils.listFiles(dir, manifestFilter, TrueFileFilter.TRUE)).stream() }
                                    .collect(Collectors.toSet())

                            final Set<File> manifestInOutputs = task.getOutputs().getFiles().filter {
                                it.getName() == "AndroidManifest.xml"
                            }.getFiles()

                            manifestInDir.addAll(manifestInOutputs)

                            manifestInDir.each {
                                logger.lifecycle("check manifest {}", it)

                                // check target sdk 28
                                def doc = parseXml(it)
                                def usesCleartextTraffic = getNodesByXPath(doc, "/manifest/application/@usesCleartextTraffic")
                                if (usesCleartextTraffic.length == 1) {
                                    logger.quiet("find usesCleartextTraffic {}", usesCleartextTraffic.item(0))
                                    return
                                }

                                def networkSecurityConfig = getNodesByXPath(doc, "/manifest/application/@networkSecurityConfig")
                                if (networkSecurityConfig.length == 1) {
                                    logger.quiet("find networkSecurityConfig {}", networkSecurityConfig.item(0))
                                    return
                                }

                                throw new AssertionError("AndroidManifest.xml must explicitly declare 'usesCleartextTraffic' attribute if target sdk > 28. \n" +
                                        "Target sdk > 28时，http连接会被禁止，除非在AndroidManifest.xml中设定android:usesCleartextTraffic=\"true\"。\n 详情参考: https://developer.android.com/training/articles/security-config#base-config")
                            }
                        }
                    }
                }
            }
        }
    }
}