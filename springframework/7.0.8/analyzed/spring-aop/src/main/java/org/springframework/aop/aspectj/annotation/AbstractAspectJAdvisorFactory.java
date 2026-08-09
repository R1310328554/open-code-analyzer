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
 * 工厂的抽象基类，可以从遵循 AspectJ 5 注释语法的类中给定 AspectJ 类创建 Spring AOP Advisor。
 * <p>该类处理注释解析和验证功能。它实际上并不生成 Spring AOP Advisor，而是推迟到子类。
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
	 * 指示 Spring 忽略 Spring AOP 代理的 ajc 编译方面的系统属性，为同时启用编织和 AspectJ 自动代理的场景恢复传统的 Spring 行为。 <p>默认
	 * 为“false”。如果您在给定的构建设置中遇到方面的双重执行，请考虑将其切换为“true”。请注意，我们建议重构您的 AspectJ 配置，以避免 AspectJ 方面的这种双
	 * 重暴露。
	 * @since 6.1.15
	 */
	public static final String IGNORE_AJC_PROPERTY_NAME = "spring.aop.ajc.ignore";

	/** `shouldIgnoreAjcCompiledAspects`：该类的成员状态。 */
	private static final boolean shouldIgnoreAjcCompiledAspects =
			SpringProperties.getFlag(IGNORE_AJC_PROPERTY_NAME);


	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 方法 `AspectJAnnotationParameterNameDiscoverer`：完成本类中与「Aspect J Annotation Parameter Name Discoverer」相关的职责。
	 */
	protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();


	/**
	 * 判断是否 Aspect。
	 */
	@Override
	public boolean isAspect(Class<?> clazz) {
		return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null &&
				(!shouldIgnoreAjcCompiledAspects || !compiledByAjc(clazz)));
	}

	/**
	 * 校验（方法 `validate`）。
	 */
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
	 * 查找并返回给定方法上的第一个 AspectJ 注释（无论如何，<i> 应该 </i> 只能是一个......）。
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

	/**
	 * 查找：Annotation（方法 `findAnnotation`）。
	 */
	private static @Nullable AspectJAnnotation findAnnotation(Method method, Class<? extends Annotation> annotationType) {
		Annotation annotation = AnnotationUtils.findAnnotation(method, annotationType);
		if (annotation != null) {
			return new AspectJAnnotation(annotation);
		}
		else {
			return null;
		}
	}

	/**
	 * 方法 `compiledByAjc`：完成本类中与「compiled By Ajc」相关的职责。
	 */
	private static boolean compiledByAjc(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().startsWith(AJC_MAGIC)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * AspectJ 注释类型的枚举。
	 * @see AspectJAnnotation#getAnnotationType()
	 */
	protected enum AspectJAnnotationType {

		AtPointcut, AtAround, AtBefore, AtAfter, AtAfterReturning, AtAfterThrowing
	}


	/**
	 * 对 AspectJ 注释进行类建模，公开其类型枚举和切入点字符串。
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
	 * ParameterNameDiscoverer 实现，用于分析在 AspectJ 注释级别指定的参数名称。
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
