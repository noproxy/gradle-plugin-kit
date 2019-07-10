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

package com.github.noproxy.gradle.test.processor;

import com.github.noproxy.gradle.test.annotations.Flatten;
import com.github.noproxy.gradle.test.annotations.Template;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("groovyOutputDirectory")
public class TemplateProcessor extends AbstractProcessor {
    public TemplateProcessor() {

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Flatten.class.getName(), Template.class.getName());
    }

    private void info(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, msg);
    }

    private void warn(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        info("start process " + annotations);
        for (Element element : roundEnv.getElementsAnnotatedWith(Template.class)) {
            info("process " + element);
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalArgumentException("Annotation '" + Flatten.class.getName() + "' can only be used on class or interface.");
            }

            final TypeElement templateType = (TypeElement) element;

            final Template template = element.getAnnotation(Template.class);
            final String clazzName = template.value();
            final String packageName;
            if (template.pkg().isEmpty()) {
                packageName = ClassName.get(templateType).packageName();
            } else {
                packageName = template.pkg();
            }

            final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(clazzName)
                    .superclass(ClassName.get(templateType))
                    .addModifiers(Modifier.PUBLIC);

            processTemplate(classBuilder, templateType);
            final JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                    .addFileComment("generated by TemplateProcess for @Flatten")
                    .build();

            switch (template.sourceType()) {
                case JAVA:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "write " + javaFile.packageName + "." + javaFile.typeSpec.name + ".java");
                    writeToFiler(javaFile);
                    break;
                case GROOVY:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "write " + javaFile.packageName + "." + javaFile.typeSpec.name + ".groovy");
                    writeToGroovyDirectory(javaFile);
                    break;
                default:
                    throw new IllegalArgumentException("not support sourceType: " + template.sourceType());
            }
        }

        //TODO
        return true;
    }


    private void writeToGroovyDirectory(JavaFile javaFile) {
        try {
            final String path = processingEnv.getOptions().get("groovyOutputDirectory");
            Assert.assertNotNull("required annotation process option 'groovyOutputDirectory' not set, but there are groovy sources need to write.", path);

            final File dir = new File(path);
            assert dir.mkdirs();

            writeTo(javaFile, dir.toPath(), "groovy");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write generated groovy source to groovyOutputDirectory: " + e.getMessage());
        }
    }

    private void writeToFiler(JavaFile javaFile) {
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fail to write generated java source to filer: " + e.getMessage());
        }
    }

    /**
     * Writes this to {@code directory} as UTF-8 using the standard directory structure.
     */
    private void writeTo(JavaFile javaFile, Path directory, String extension) throws IOException {
        Path outputDirectory = directory;
        final String packageName = javaFile.packageName;
        if (!packageName.isEmpty()) {
            for (String packageComponent : packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }

        Path outputPath = outputDirectory.resolve(javaFile.typeSpec.name + "." + extension);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(outputPath), UTF_8)) {
            javaFile.writeTo(writer);
        }
    }


    private Set<TypeMirror> collectAllInterfaces(TypeElement typeElement) {
        final Set<TypeMirror> result = Sets.newHashSet();

        collectAllInterfaces(result, typeElement);
        return result;
    }

    private void collectAllInterfaces(Set<TypeMirror> collector, TypeElement typeElement) {
        final List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        interfaces.forEach(anInterface -> {
            collector.add(anInterface);

            collectAllInterfaces(collector, (TypeElement) processingEnv.getTypeUtils().asElement(anInterface));
        });
    }

    private TypeElement toElement(TypeMirror typeMirror) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);
    }

    private TypeElement toElement(ClassName className) {
        return processingEnv.getElementUtils().getTypeElement(className.reflectionName());
    }

    private void fail(String msg) {

    }

    private void processTemplate(TypeSpec.Builder classBuilder, TypeElement templateType) {
        final Set<MethodSpec> methodSpecs = Sets.newHashSet();

        final Set<TypeMirror> flattenInterfaceHistory = Sets.newHashSet();
        final List<Element> flattenHistory = Lists.newArrayList();
        templateType.getEnclosedElements().stream().filter(e -> e.getAnnotation(Flatten.class) != null)
                .forEach(e -> {
                    //TODO assert flattenType is interface
                    if (e.getKind() != INTERFACE) {
                        fail("@Flatten must be annotated on interface, but on " + e);
                    }
                    if (flattenHistory.contains(e)) {
                        fail("You cannot annotate @Flatten on two same type. We found: " + e);
                    }
                    flattenHistory.add(e);


                    final Flatten flatten = e.getAnnotation(Flatten.class);
                    final TypeMirror flattenType = extractType(e);
                    final CharSequence getter = extractName(e);

                    classBuilder.addSuperinterface(ClassName.get(flattenType));

                    final Set<TypeMirror> allInterfacesByThisType = collectAllInterfaces(toElement(flattenType));
                    allInterfacesByThisType.add(flattenType);
                    // simply ignore all duplicated
                    allInterfacesByThisType.removeAll(flattenInterfaceHistory);

                    final Set<MethodSpec> methodSpecsByInterfaces =
                            allInterfacesByThisType.stream().map(this::toElement)
                                    .flatMap(typeElement -> typeElement.getEnclosedElements()
                                            .stream()
                                            .filter(element -> element.getKind() == METHOD)
                                            .map(ExecutableElement.class::cast)
                                            .map(executableElement -> {
                                                //TODO handle same methods from different flatten type

                                                final MethodSpec toCall = MethodSpec.overriding(executableElement).build();
                                                if (executableElement.getReturnType() instanceof NoType) {
                                                    return MethodSpec.overriding(executableElement)
                                                            .addCode(getter + "." + Poetry.call(toCall) + ";\n")
                                                            .build();
                                                } else {
                                                    return MethodSpec.overriding(executableElement)
                                                            .addCode("return " + getter + "." + Poetry.call(toCall) + ";\n")
                                                            .build();
                                                }
                                            })).collect(HashSet::new, (objects, methodSpec) -> {
                                List<MethodSpec> toRemoved = Lists.newArrayList();
                                for (MethodSpec exist : objects) {
                                    // ignore the same
                                    if (exist.equals(methodSpec)) {
                                        return;
                                    }

                                    if (Objects.equals(exist.name, methodSpec.name)
                                            && Objects.equals(exist.parameters, methodSpec.parameters)) {
                                        if (!exist.returnType.equals(methodSpec.returnType)) {
                                            fail("Found two methods have same name and parameters but different return value, it's:\n" + exist + "\n" + methodSpec);
                                            return;
                                        }
                                        final List<TypeName> newExs = methodSpec.exceptions;
                                        final List<TypeName> existExs = exist.exceptions;
                                        if (isExOverride(existExs, newExs)) {
                                            return;
                                        } else if (isExOverride(newExs, existExs)) {
                                            toRemoved.add(exist);
                                        } else {
                                            fail("Found two methods have same name and parameters but different exception, it's:\n" + exist + "\n" + methodSpec);
                                        }
                                    }
                                }
                                objects.removeAll(toRemoved);
                                objects.add(methodSpec);
                            }, (objects, objects2) -> {
                                throw new IllegalArgumentException();
                            });
                    flattenInterfaceHistory.addAll(allInterfacesByThisType);
                    methodSpecs.addAll(methodSpecsByInterfaces);
                });


        methodSpecs.forEach(classBuilder::addMethod);
    }

    private boolean isExOverride(List<TypeName> overrider, List<TypeName> overridden) {
        if (overrider.equals(overridden)) {
            return true;
        }

        if (overrider.isEmpty()) {
            return true;
        }

        if (overrider.stream().allMatch(ex -> {
            // ex is child of one of the overridden
            return overridden.stream().anyMatch(parent -> {
                if (ex.equals(parent)) {
                    return true;
                }

                final TypeElement exE = toElement((ClassName) ex);
                final TypeElement parentE = toElement((ClassName) parent);
                return processingEnv.getTypeUtils().isAssignable(exE.asType(), parentE.asType());
            });
        })) {
            return true;
        }

        return false;
    }

    private CharSequence extractName(Element e) {
        switch (e.getKind()) {
            case FIELD:
                return ((VariableElement) e).getSimpleName();

            case METHOD:
                return ((ExecutableElement) e).getSimpleName() + "()";
            default:
                throw new IllegalArgumentException("@Flatten can only used on field and getter method. But on: " + e);
        }
    }

    private TypeMirror extractType(Element e) {
        switch (e.getKind()) {
            case FIELD:
                return e.asType();
            case METHOD:
                return extractTypeFromMethod((ExecutableElement) e);
            default:
                throw new IllegalArgumentException("@Flatten can only used on field and getter method. But on: " + e);
        }
    }

    private TypeMirror extractTypeFromMethod(ExecutableElement executableElement) {
        final TypeMirror returnType = executableElement.getReturnType();
        if (// no return value
                returnType instanceof NoType ||
                        // static method, constructor of non-inner class, initializer
                        executableElement.getReceiverType() instanceof NoType ||
                        // require parameters
                        !executableElement.getParameters().isEmpty() ||
                        // require type parameters
                        !executableElement.getTypeParameters().isEmpty()) {
            throw new IllegalArgumentException("@Flatten can only used on 'getter' method. But on: " + executableElement);
        }

        return returnType;
    }

    private void checkProperties(List<? extends Element> properties) {
        properties.forEach(e -> {
            if (e.getKind() == ElementKind.FIELD) {
                return;
            }

            if (e.getKind() == METHOD) {
                final ExecutableElement executableElement = (ExecutableElement) e;
                if (// no return value
                        executableElement.getReturnType() instanceof NoType ||
                                // static method, constructor of non-inner class, initializer
                                executableElement.getReceiverType() instanceof NoType ||
                                // require parameters
                                !executableElement.getParameters().isEmpty() ||
                                // require type parameters
                                !executableElement.getTypeParameters().isEmpty()) {
                    throw new IllegalArgumentException("@Flatten can only used on 'getter' method. But on: " + executableElement);
                }
            }
        });
    }
}