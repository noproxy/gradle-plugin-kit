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

package com.github.noproxy.gradle.test.internal.overload;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gradle.api.Action;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ConfigureOverloadTransformation implements ASTTransformation {
    private static final ClassNode CLASS_SPECIFICATION = ClassHelper.make(Specification.class);
    private static final ClassNode CLASS_ACTION = ClassHelper.make(Action.class);
    private static final ClassNode CLASS_CLOSURE = ClassHelper.make(Closure.class);
    private static final boolean DEBUG = true;

    private static List<ClassNode> getParameterTypes(MethodNode methodNode) {
        return Arrays.stream(methodNode.getParameters()).map(Parameter::getType).collect(Collectors.toList());
    }

    private void log(String format, Object... args) {
        if (DEBUG) {
            System.out.printf(format, args);
            System.out.println();
        }
    }

    @Override
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        log("global ast run against: %s", sourceUnit.getAST().getClasses().size(), sourceUnit.getName());
        sourceUnit.getAST().getClasses().stream()
                .filter(this::isSpecification)
                .forEach(spec -> {
                    log("process specification: " + spec.getName());
                    spec.getFields().stream()
                            .filter(this::isTestedField)
                            .forEach(fieldNode -> handleField(spec, fieldNode));
                });
    }

    private boolean isSpecification(ClassNode classNode) {
        return classNode.isDerivedFrom(CLASS_SPECIFICATION);
    }

    private boolean isTestedField(FieldNode fieldNode) {
        return getTestConfigurerAnnotation(fieldNode) != null;
    }

    private boolean canGenerateTestFor(List<MethodNode> allMethods, MethodNode methodNode) {
        final Parameter[] parameters = methodNode.getParameters();
        if (parameters.length <= 0) {
            return false;
        }

        final ClassNode lastParameterType = parameters[parameters.length - 1].getType();

        // arguments of configurer must end with Closure or Action
        if (!CLASS_CLOSURE.equals(lastParameterType) && !CLASS_ACTION.equals(lastParameterType)) {
            return false;
        }

        // configurer must have a corresponding method whose arguments don't end with closure or action
        return allMethods.stream().anyMatch(m -> methodMatch(m, methodNode));
    }

    private boolean methodMatch(MethodNode methodToCheck, MethodNode configurerMethod) {
        final boolean answer = methodToCheck.getName().equals(configurerMethod.getName())
                && getParameterTypes(methodToCheck).equals(getParameterTypes(configurerMethod))
                && methodToCheck.getReturnType().equals(configurerMethod.getReturnType());
//        log("check whether %s overload %s: %b", betterName(configurerMethod), betterName(methodToCheck), answer);
        return answer;
    }

    private void handleField(ClassNode specification, FieldNode field) {
        final ClassNode fieldType = field.getType();
        log("add tests for %s in %s", fieldType.getName(), specification.getName());
        final List<MethodNode> allMethods = fieldType.getMethods();
        allMethods.stream()
                .filter(method -> canGenerateTestFor(allMethods, method))
                .forEach(configurerMethod -> specification.addMethod(writeTestForMethod(field, configurerMethod)));
    }

    private String literalForArguments(Parameter[] parameters) {
        final StringBuilder stringBuilder = new StringBuilder();
        Arrays.asList(parameters).forEach(parameter -> {
            stringBuilder.append(parameter.getType().getNameWithoutPackage()).append(',');
        });
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private AnnotationNode getTestConfigurerAnnotation(FieldNode field) {
        final List<AnnotationNode> annotations = field.getAnnotations(ClassHelper.make(TestConfigurer.class));
        if (annotations.isEmpty()) {
            return null;
        }
        return annotations.get(0);
    }

    private String betterName(MethodNode methodNode) {
        final String methodDeclaredName = methodNode.getName() + "(" + literalForArguments(methodNode.getParameters()) + ")";
        return methodNode.getReturnType().getNameWithoutPackage() + " " + methodDeclaredName;
    }

    private MethodNode writeTestForMethod(FieldNode field, MethodNode testedMethod) {
        final String methodDeclaredName = testedMethod.getName() + "(" + literalForArguments(testedMethod.getParameters()) + ")";
        log("create test for %s.%s", field.getType().getName(), methodDeclaredName);
        final AnnotationNode testConfigurerAnnotation = getTestConfigurerAnnotation(field);
        final Statement stmt = stmt(callX(varX(field.getName()), "invokeMethod", args(constX(testedMethod.getName()),
                argumentsForMethod(testConfigurerAnnotation, testedMethod))));
        stmt.addStatementLabel("expect");

        final BlockStatement methodBody = block(stmt);
        final String generatedMethodName = "test " + methodDeclaredName;
        return new MethodNode(generatedMethodName,
                Modifier.PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodBody
        );
    }

    private ArrayExpression argumentsForMethod(AnnotationNode testConfigurerAnnotation, MethodNode method) {
        final List<Expression> arguments = Arrays.stream(method.getParameters()).map(parameter -> callProviderX(testConfigurerAnnotation, parameter.getType()))
                .collect(Collectors.toList());
        return new ArrayExpression(OBJECT_TYPE, arguments);
    }

    private Expression callProviderX(AnnotationNode params, ClassNode argumentType) {
        final Expression providerFieldName = params.getMember("value");


        if (// null when using default value
                providerFieldName == null
                        || providerFieldName.equals(constX("this"))
                        || providerFieldName.equals(constX(""))) {
            return callThisX("getArgumentForType", classX(argumentType));
        } else {
            return callX(varX(providerFieldName.getText()), "getArgumentForType", classX(argumentType));
        }
    }
}
