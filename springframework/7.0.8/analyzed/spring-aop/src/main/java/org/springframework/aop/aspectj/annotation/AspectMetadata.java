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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.ComposablePointcut;

/**
 * AspectJ 切面类的元数据，另含 per 子句对应的 Spring AOP 切点。
 *
 * <p>使用 AspectJ 5 AJType 反射 API，
 * 支持 singleton、pertarget、perthis 等不同 AspectJ 实例化模型。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
@SuppressWarnings("serial")
public class AspectMetadata implements Serializable {

	/**
	 * 本切面在 Spring 中的名称（Bean 名称）——
	 * 用于判断两条通知是否来自同一切面，从而确定相对优先级。
	 */
	private final String aspectName;

	/**
	 * 切面类，单独存储以便反序列化时重新解析对应 AjType。
	 */
	private final Class<?> aspectClass;

	/**
	 * AspectJ 反射信息。
	 * <p>反序列化时重新解析，因其本身不可序列化。
	 */
	private transient AjType<?> ajType;

	/**
	 * 切面对应 per 子句的 Spring AOP 切点。
	 * 单例时为 {@code Pointcut.TRUE} 规范实例，
	 * 否则为 AspectJExpressionPointcut。
	 */
	private final Pointcut perClausePointcut;


	/**
	 * 为给定切面类创建新的 AspectMetadata 实例。
	 * @param aspectClass 切面类
	 * @param aspectName 切面名称
	 */
	public AspectMetadata(Class<?> aspectClass, String aspectName) {
		this.aspectName = aspectName;

		Class<?> currClass = aspectClass;
		AjType<?> ajType = null;
		while (currClass != Object.class) {
			AjType<?> ajTypeToCheck = AjTypeSystem.getAjType(currClass);
			if (ajTypeToCheck.isAspect()) {
				ajType = ajTypeToCheck;
				break;
			}
			currClass = currClass.getSuperclass();
		}
		if (ajType == null) {
			throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
		}
		if (ajType.getDeclarePrecedence().length > 0) {
			throw new IllegalArgumentException("DeclarePrecedence not presently supported in Spring AOP");
		}
		this.aspectClass = ajType.getJavaClass();
		this.ajType = ajType;

		switch (this.ajType.getPerClause().getKind()) {
			case SINGLETON -> {
				this.perClausePointcut = Pointcut.TRUE;
			}
			case PERTARGET, PERTHIS -> {
				AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
				ajexp.setLocation(aspectClass.getName());
				ajexp.setExpression(findPerClause(aspectClass));
				ajexp.setPointcutDeclarationScope(aspectClass);
				this.perClausePointcut = ajexp;
			}
			case PERTYPEWITHIN -> {
				// 使用类型模式
				this.perClausePointcut = new ComposablePointcut(new TypePatternClassFilter(findPerClause(aspectClass)));
			}
			default -> throw new AopConfigException(
					"PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for " + aspectClass);
		}
	}

	/**
	 * 从 {@code pertarget(contents)} 形式的字符串中提取内容。
	 */
	private String findPerClause(Class<?> aspectClass) {
		Aspect ann = aspectClass.getAnnotation(Aspect.class);
		if (ann == null) {
			return "";
		}
		String value = ann.value();
		int beginIndex = value.indexOf('(');
		if (beginIndex < 0) {
			return "";
		}
		return value.substring(beginIndex + 1, value.length() - 1);
	}


	/**
	 * 返回 AspectJ 反射信息。
	 */
	public AjType<?> getAjType() {
		return this.ajType;
	}

	/**
	 * 返回切面类。
	 */
	public Class<?> getAspectClass() {
		return this.aspectClass;
	}

	/**
	 * 返回切面名称。
	 */
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 返回单例切面的 Spring 切点表达式
	 * （例如单例时为 {@code Pointcut.TRUE}）。
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}

	/**
	 * 返回切面是否定义为 "perthis" 或 "pertarget"。
	 */
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
	}

	/**
	 * 返回切面是否定义为 "pertypewithin"。
	 */
	public boolean isPerTypeWithin() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTYPEWITHIN);
	}

	/**
	 * 返回切面是否需要延迟实例化。
	 */
	public boolean isLazilyInstantiated() {
		return (isPerThisOrPerTarget() || isPerTypeWithin());
	}


	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.ajType = AjTypeSystem.getAjType(this.aspectClass);
	}

}
