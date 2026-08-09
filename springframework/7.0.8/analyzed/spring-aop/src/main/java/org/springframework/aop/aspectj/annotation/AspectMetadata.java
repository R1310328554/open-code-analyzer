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
 * AspectJ 切面类的元数据，以及每个子句的附加 Spring AOP 切入点。
 * <p>U 使用 AspectJ 5 AJType 反射 API，使我们能够使用不同的 AspectJ
 * 实例化模型，例如“singleton”、“pertarget”和“perthis”。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
@SuppressWarnings("serial")
public class AspectMetadata implements Serializable {

	/**
	 * Spring 中定义的该方面的名称（bean 名称）——允许我们确定两条建议是否来自同一方面，从而确定它们的相对优先级。
	 */
	private final String aspectName;

	/**
	 * 方面类，单独存储，用于在反序列化时重新解析相应的 AjType。
	 */
	private final Class<?> aspectClass;

	/**
	 * AspectJ 反射信息。 <p>重新解决了反序列化问题，因为它本身不可序列化。
	 */
	private transient AjType<?> ajType;

	/**
	 * Spring AOP切入点对应于aspect的per子句。如果是单例，则为 {@code Pointcut.TRUE} 规范实例，否则为
	 * AspectJExpressionPointcut。
	 */
	private final Pointcut perClausePointcut;


	/**
	 * 为给定的方面类创建一个新的 AspectMetadata 实例。
	 * @param aspectClass 方面类
	 * @param aspectName 方面的名称
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
	 * 返回AspectJ反射信息。
	 */
	public AjType<?> getAjType() {
		return this.ajType;
	}

	/**
	 * 返回方面类。
	 */
	public Class<?> getAspectClass() {
		return this.aspectClass;
	}

	/**
	 * 返回方面名称。
	 */
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 返回单例切面的 Spring 切入点表达式。 （例如，{@code Pointcut.TRUE}，如果它是单例）。
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}

	/**
	 * 返回方面是否定义为“perthis”或“pertarget”。
	 */
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
	}

	/**
	 * 返回方面是否定义为“pertypewithin”。
	 */
	public boolean isPerTypeWithin() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTYPEWITHIN);
	}

	/**
	 * 返回该方面是否需要延迟实例化。
	 */
	public boolean isLazilyInstantiated() {
		return (isPerThisOrPerTarget() || isPerTypeWithin());
	}


	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.ajType = AjTypeSystem.getAjType(this.aspectClass);
	}

}
