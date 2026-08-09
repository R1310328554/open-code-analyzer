/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.aspectj.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 可依据遵循 AspectJ 5 注解语法的类创建 Spring AOP 通知器的工厂抽象基类。
 *
 * <p>本类处理注解解析与校验功能。
 * 实际生成 Spring AOP 通知器的工作由子类完成。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

	private static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[] {
			Pointcut.class, Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class};

	private static final String AJC_MAGIC = "ajc$";

	/**
	 * 指示 Spring 在 Spring AOP 代理时忽略 ajc 编译切面的系统属性，
	 * 在同时启用织入与 AspectJ 自动代理的场景下恢复传统 Spring 行为。
	 * <p>默认为 "false"。若在给定构建配置中遇到切面重复执行，
	 * 可考虑设为 "true"。
	 * 仍建议重构 AspectJ 配置，从源头避免切面被双重暴露。
	 * @since 6.1.15
	 */
	public static final String IGNORE_AJC_PROPERTY_NAME = "spring.aop.ajc.ignore";

	private static final boolean shouldIgnoreAjcCompiledAspects =
			SpringProperties.getFlag(IGNORE_AJC_PROPERTY_NAME);


	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();


	@Override
	public boolean isAspect(Class<?> clazz) {
		return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null &&
				(!shouldIgnoreAjcCompiledAspects || !compiledByAjc(clazz)));
	}

	@Override
	public void validate(Class<?> aspectClass) throws AopConfigException {
		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
		if (!ajType.isAspect()) {
			throw new NotAnAtAspectException(aspectClass);
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
			throw new AopConfigException(aspectClass.getName() + " uses percflow instantiation model: " +
					"This is not supported in Spring AOP.");
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
			throw new AopConfigException(aspectClass.getName() + " uses percflowbelow instantiation model: " +
					"This is not supported in Spring AOP.");
		}
	}


	/**
	 * 查找并返回给定方法上的第一个 AspectJ 注解
	 * （理论上<i>应</i>只有一个...）。
	 */
	@SuppressWarnings("unchecked")
	protected static @Nullable AspectJAnnotation findAspectJAnnotationOnMethod(Method method) {
		for (Class<?> annotationType : ASPECTJ_ANNOTATION_CLASSES) {
			AspectJAnnotation annotation = findAnnotation(method, (Class<Annotation>) annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	private static @Nullable AspectJAnnotation findAnnotation(Method method, Class<? extends Annotation> annotationType) {
		Annotation annotation = AnnotationUtils.findAnnotation(method, annotationType);
		if (annotation != null) {
			return new AspectJAnnotation(annotation);
		}
		else {
			return null;
		}
	}

	private static boolean compiledByAjc(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().startsWith(AJC_MAGIC)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * AspectJ 注解类型枚举。
	 * @see AspectJAnnotation#getAnnotationType()
	 */
	protected enum AspectJAnnotationType {

		AtPointcut, AtAround, AtBefore, AtAfter, AtAfterReturning, AtAfterThrowing
	}


	/**
	 * 建模 AspectJ 注解的类，暴露其类型枚举与切点字符串。
	 */
	protected static class AspectJAnnotation {

		private static final String[] EXPRESSION_ATTRIBUTES = {"pointcut", "value"};

		private static final Map<Class<?>, AspectJAnnotationType> annotationTypeMap = Map.of(
				Pointcut.class, AspectJAnnotationType.AtPointcut, //
				Around.class, AspectJAnnotationType.AtAround, //
				Before.class, AspectJAnnotationType.AtBefore, //
				After.class, AspectJAnnotationType.AtAfter, //
				AfterReturning.class, AspectJAnnotationType.AtAfterReturning, //
				AfterThrowing.class, AspectJAnnotationType.AtAfterThrowing //
			);

		private final Annotation annotation;

		private final AspectJAnnotationType annotationType;

		private final String pointcutExpression;

		private final String argumentNames;

		public AspectJAnnotation(Annotation annotation) {
			this.annotation = annotation;
			this.annotationType = determineAnnotationType(annotation);
			try {
				this.pointcutExpression = resolvePointcutExpression(annotation);
				Object argNames = AnnotationUtils.getValue(annotation, "argNames");
				this.argumentNames = (argNames instanceof String names ? names : "");
			}
			catch (Exception ex) {
				throw new IllegalArgumentException(annotation + " is not a valid AspectJ annotation", ex);
			}
		}

		private AspectJAnnotationType determineAnnotationType(Annotation annotation) {
			AspectJAnnotationType type = annotationTypeMap.get(annotation.annotationType());
			if (type != null) {
				return type;
			}
			throw new IllegalStateException("Unknown annotation type: " + annotation);
		}

		private String resolvePointcutExpression(Annotation annotation) {
			for (String attributeName : EXPRESSION_ATTRIBUTES) {
				Object val = AnnotationUtils.getValue(annotation, attributeName);
				if (val instanceof String str && !str.isEmpty()) {
					return str;
				}
			}
			throw new IllegalStateException("Failed to resolve pointcut expression in: " + annotation);
		}

		public AspectJAnnotationType getAnnotationType() {
			return this.annotationType;
		}

		public Annotation getAnnotation() {
			return this.annotation;
		}

		public String getPointcutExpression() {
			return this.pointcutExpression;
		}

		public String getArgumentNames() {
			return this.argumentNames;
		}

		@Override
		public String toString() {
			return this.annotation.toString();
		}
	}


	/**
	 * 分析 AspectJ 注解级指定参数名的 ParameterNameDiscoverer 实现。
	 */
	private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

		private static final String[] EMPTY_ARRAY = new String[0];

		@Override
		public String @Nullable [] getParameterNames(Method method) {
			if (method.getParameterCount() == 0) {
				return EMPTY_ARRAY;
			}
			AspectJAnnotation annotation = findAspectJAnnotationOnMethod(method);
			if (annotation == null) {
				return null;
			}
			StringTokenizer nameTokens = new StringTokenizer(annotation.getArgumentNames(), ",");
			int numTokens = nameTokens.countTokens();
			if (numTokens > 0) {
				String[] names = new String[numTokens];
				for (int i = 0; i < names.length; i++) {
					names[i] = nameTokens.nextToken();
				}
				return names;
			}
			else {
				return null;
			}
		}

		@Override
		public @Nullable String @Nullable [] getParameterNames(Constructor<?> ctor) {
			throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
		}
	}

}
