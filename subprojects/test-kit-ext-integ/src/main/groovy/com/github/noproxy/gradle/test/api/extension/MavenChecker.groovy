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

package com.github.noproxy.gradle.test.api.extension

import com.github.noproxy.gradle.test.api.FileIntegrator
import com.github.noproxy.gradle.test.api.template.IntegrateSpecification
import com.github.noproxy.gradle.test.internal.DefaultFileIntegrator
import com.google.common.base.Joiner
import com.google.common.collect.Lists
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.jetbrains.annotations.NotNull

//TODO 定义checker 谁来close，什么时候close，hide文件由谁创建
class MavenChecker {
    static class MavenContext {
        private final File root

        MavenContext(File root) {
            this.root = root
        }

        void module(String notation, @DelegatesTo(ModuleContext) Closure closure) {
            //TODO
        }

        void module(String groupId, String artifactId, String version, @DelegatesTo(ModuleContext) Closure closure) {
            def context = new ModuleContext(root, groupId, artifactId, version)
            closure.delegate = context
            closure()
        }
    }

    static class RegularFileContext {
        @Delegate(parameterAnnotations = true)
        private final File file

        RegularFileContext(File file) {
            this.file = file
        }

        void assertExists() {
            assert exists()
        }

        void assertNotExists() {
            assert !exists()
        }

        String getText() {
            return file.text
        }
    }

    static class ModuleContext {
        private final File moduleRoot
        private final String groupId
        private final String artifactId
        private final String version
        @Delegate(parameterAnnotations = true)
        private final FileIntegrator integrator
        private final List<String> checkedFiles = Lists.newArrayList()

        private String checked(String filename) {
            checkedFiles.add(filename)
            checkedFiles.add(filename + ".md5")
            checkedFiles.add(filename + ".sha1")
            return filename
        }

        ModuleContext(File repoRoot, String groupId, String artifactId, String version) {
            this.version = version
            this.artifactId = artifactId
            this.groupId = groupId
            this.moduleRoot = new File(repoRoot, groupId.replace('.', File.separator) + File.separator + artifactId + File.separator + version)
            integrator = new DefaultFileIntegrator(moduleRoot)
        }

        void component(String classifier = null, @NotNull String type,
                       @ClosureParams(value = SimpleType, options = "java.io.File")
                       @DelegatesTo(RegularFileContext) Closure closure) {
            def component = component(classifier, type)
            def clonedClosure = closure.clone() as Closure
            clonedClosure.delegate = new RegularFileContext(component)
            clonedClosure(component)
        }

        File component(String classifier = null, @NotNull String type) {
            return file(checked(componentFilename(classifier, type)))
        }

        private String componentFilename(String classifier = null, @NotNull String type) {
            return Joiner.on('-').skipNulls().join(artifactId, version, classifier) + "." + type
        }

        File getPom() {
            return component("pom")
        }

        void pom(@ClosureParams(value = SimpleType, options = "java.io.File")
                 @DelegatesTo(RegularFileContext) Closure closure) {
            component("pom", closure)
        }

        File getMetadata() {
            return component("module")
        }

        void metadata(@ClosureParams(value = SimpleType, options = "java.io.File")
                      @DelegatesTo(RegularFileContext) Closure closure) {
            component("module", closure)
        }

        void jar(@ClosureParams(value = SimpleType, options = "java.io.File")
                 @DelegatesTo(RegularFileContext) Closure closure) {
            component("jar", closure)
        }

        File getJar() {
            return component("jar")
        }

        void aar(@ClosureParams(value = SimpleType, options = "java.io.File")
                 @DelegatesTo(RegularFileContext) Closure closure = Closure.IDENTITY) {
            component("aar", closure)
        }

        File getAar() {
            return component("aar")
        }

        void noOtherComponents() {
            assert others.isEmpty()
        }

        List<File> getOthers() {
            def lefts = moduleRoot.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File dir, String name) {
                    return !checkedFiles.contains(name)
                }
            })
            assert lefts != null
            return lefts.toList()
        }
    }

    static <T> T buildRepo(IntegrateSpecification self, @DelegatesTo(MavenContext) Closure<T> closure) {
        return repo(self, 'build/repo', closure)
    }

    static <T> T repo(IntegrateSpecification self, String path, @DelegatesTo(MavenContext) Closure<T> closure) {
        return repo(self, self.file(path), closure)
    }

    static <T> T repo(IntegrateSpecification self, File root, @DelegatesTo(MavenContext) Closure<T> closure) {
        closure.delegate = new MavenContext(root)
        return closure()
    }

    static <T> T mavenLocal(IntegrateSpecification self, @DelegatesTo(MavenContext) Closure<T> closure) {

    }
}
