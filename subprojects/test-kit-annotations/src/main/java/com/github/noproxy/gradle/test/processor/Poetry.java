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


import com.squareup.javapoet.*;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Copy from https://github.com/sormuras/poetry/blob/master/src/main/com/github/sormuras/poetry/Poetry.java#L187
 * <p>
 * Java reflection, Javax model and JavaPoet related utilities.
 *
 * @author Christian Stein
 */
public interface Poetry {

    /**
     * Create annotated type name instance from reflected annotated type.
     *
     * @param type annotated type to inspect
     * @return annotated type name
     * @since 1.8
     */
    static TypeName annotated(AnnotatedType type) {
        AnnotatedType annotated = type;
        while (annotated instanceof AnnotatedArrayType) {
            annotated = ((AnnotatedArrayType) annotated).getAnnotatedGenericComponentType();
        }
        return TypeName.get(type.getType()).annotated(annotations(annotated.getAnnotations()));
    }

    /**
     * Create annotated type name instance from type mirror.
     *
     * @param mirror annotated type mirror to inspect
     * @return annotated type name
     * @since 1.8
     */
    static TypeName annotated(TypeMirror mirror) {
        assert mirror instanceof AnnotatedConstruct;
        TypeMirror annotated = mirror;
        while (annotated.getKind() == TypeKind.ARRAY) {
            annotated = ((ArrayType) annotated).getComponentType();
        }
        return TypeName.get(mirror).annotated(annotations(annotated.getAnnotationMirrors()));
    }

    static AnnotationSpec annotation(Class<? extends Annotation> type, Object... values) {
        return annotation(ClassName.get(type), values);
    }

    static AnnotationSpec annotation(ClassName type, Object... values) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(type);
        for (Object value : values) {
            value(builder, "value", value);
        }
        return builder.build();
    }

    /**
     * Convert annotations to annotation spec list.
     *
     * @param annotations source collection
     * @return a list of annotation specs
     */
    static List<AnnotationSpec> annotations(Annotation... annotations) {
        return asList(annotations).stream().map(AnnotationSpec::get).collect(Collectors.toList());
    }

    /**
     * Convert annotation mirrors to annotation spec list.
     *
     * @param mirrors source collection
     * @return a list of annotation specs
     */
    static List<AnnotationSpec> annotations(List<? extends AnnotationMirror> mirrors) {
        return mirrors.stream().map(AnnotationSpec::get).collect(Collectors.toList());
    }

    static String binary(TypeName type) {
        return binary(type, new StringBuilder()).toString();
    }

    static StringBuilder binary(TypeName type, StringBuilder builder) {
        if (type instanceof TypeVariableName || type instanceof WildcardTypeName) {
            throw new UnsupportedOperationException(type.getClass() + " hasn't got a binary name!");
        }
        if (type instanceof ClassName) {
            ClassName className = (ClassName) type;
            builder.append(className.topLevelClassName());
            className.simpleNames().subList(1, className.simpleNames().size()).forEach(
                    name -> builder.append('$').append(name));
            return builder;
        }
        if (type instanceof ArrayTypeName) {
            TypeName component = ((ArrayTypeName) type).componentType;
            builder.append('[');
            if (component.isPrimitive()) {
                //CHECKSTYLE:OFF
                // @formatter:off
                if (component.equals(TypeName.BOOLEAN)) return builder.append('Z');
                if (component.equals(TypeName.BYTE)) return builder.append('B');
                if (component.equals(TypeName.CHAR)) return builder.append('C');
                if (component.equals(TypeName.DOUBLE)) return builder.append('D');
                if (component.equals(TypeName.FLOAT)) return builder.append('F');
                if (component.equals(TypeName.INT)) return builder.append('I');
                if (component.equals(TypeName.LONG)) return builder.append('J');
                if (component.equals(TypeName.SHORT)) return builder.append('S');
                // @formatter:on
                //CHECKSTYLE:ON
                throw new AssertionError(component + " is primitive and not handled?!");
            }
            builder.append(binary(component));
            if (component instanceof ClassName) {
                builder.append(';');
            }
            return builder;
        }
        if (type instanceof ParameterizedTypeName) {
            return builder.append(((ParameterizedTypeName) type).rawType);
        }
        return builder.append(type);
    }

    /**
     * Build method call statement string using its own parameter names.
     * <p>
     * Same as: {@code call(method, p -> p.name)}
     *
     * @param method method to call
     * @return method call statement
     */
    static String call(MethodSpec method) {
        return call(method, p -> p.name);
    }

    /**
     * Build method call statement string using its own parameter names.
     * <p>
     * If parameter names are provided {@link String#indexOf(String, int)} yields: {@code "indexOf(str, fromIndex)"}.
     *
     * @param method        method to call
     * @param parameterName calculates name of the parameter
     * @return method call statement
     */
    static String call(MethodSpec method, Function<ParameterSpec, String> parameterName) {
        if (method.parameters.isEmpty()) {
            return method.name + "()";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(method.name);
        builder.append('(');
        for (ParameterSpec parameter : method.parameters) {
            if (builder.length() > method.name.length() + 1) {
                builder.append(", ");
            }
            builder.append(parameterName.apply(parameter));
        }
        builder.append(')');
        return builder.toString();
    }

    /**
     * Create type spec builder for the interface spec provided.
     *
     * @param interfaceSpec the interface to implement
     * @param name          the class name
     * @return the class spec implementing all methods found in interfaceSpec
     */
    static TypeSpec.Builder implement(TypeSpec interfaceSpec, String interfacePackage, String name,
                                      Function<MethodSpec, CodeBlock> coder) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(interfacePackage, interfaceSpec.name));
        interfaceSpec.methodSpecs.forEach(m -> builder.addMethod(override(m, coder).build()));
        return builder;
    }

    /**
     * Create method spec builder for the given method spec.
     *
     * @param method the method to override
     * @param coder  the code block supplier
     * @return the method builder overriding the other method
     */
    static MethodSpec.Builder override(MethodSpec method, Function<MethodSpec, CodeBlock> coder) {
        if (method.isConstructor()) {
            throw new IllegalArgumentException("constructor not supported");
        }
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name)
                .addJavadoc(method.javadoc.toString())
                .addAnnotations(method.annotations)
                .addModifiers(Modifier.PUBLIC)
                .returns(method.returnType)
                .addTypeVariables(method.typeVariables)
                .addParameters(method.parameters)
                .addExceptions(method.exceptions);
        builder.addCode(coder.apply(method));
        return builder;
    }

    /**
     * <p>
     * Falls back to {@code "$L"} literal format if the class of the given {@code value} object is not supported.
     *
     * @param builder add value to that builder
     * @param member  member name, like {@code "value"}
     * @param value   object to assign to member
     * @return the {@code builder} passed as first argument
     */
    static AnnotationSpec.Builder value(AnnotationSpec.Builder builder, String member, Object value) {
        if (value instanceof Class<?>) {
            return builder.addMember(member, "$T.class", value);
        }
        if (value instanceof Enum) {
            return builder.addMember(member, "$T.$L", value.getClass(), ((Enum<?>) value).name());
        }
        if (value instanceof String) {
            return builder.addMember(member, "$S", value);
        }
        if (value instanceof Float) {
            return builder.addMember(member, "$Lf", value);
        }
        return builder.addMember(member, "$L", value);
    }

}