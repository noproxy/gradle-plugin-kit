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

package io.github.noproxy.processor.recording;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

class DefaultAnnotationsRecorder implements AnnotationsRecorder {
    private final Filer filer;

    DefaultAnnotationsRecorder(ProcessingEnvironment env) {
        this.filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> {
            record(annotation, roundEnv.getElementsAnnotatedWith(annotation));
        });

        if (roundEnv.processingOver()) {
            saveRecords();
            return true;
        }

        return false;
    }

    private void saveRecords() {
        //TODO write records to filer
    }

    private void record(TypeElement annotation, Set<? extends Element> annotatedElements) {
        annotatedElements.forEach(o -> {
            //TODO record annotations in some data structure, map or tree
            // Looks like enclosing class -> annotated element -> annotation -> value
        });
    }
}
