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

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;

/**
 * 可从带 AspectJ 注解语法的类创建 Spring AOP 通知器的工厂接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 */
public interface AspectJAdvisorFactory {

	/**
	 * 判断给定类是否为切面，
	 * 依据 AspectJ {@link org.aspectj.lang.reflect.AjTypeSystem} 的报告。
	 * <p>若所谓切面无效（如具体切面类的扩展），直接返回 {@code false}。
	 * 对 Spring AOP 无法处理的切面（如不支持的实例化模型）也可能返回 true。
	 * 必要时使用 {@link #validate} 处理这些情况。
	 * @param clazz 假定的注解风格 AspectJ 类
	 * @return 该类是否被 AspectJ 识别为切面类
	 */
	boolean isAspect(Class<?> clazz);

	/**
	 * 给定类是否为有效的 AspectJ 切面类？
	 * @param aspectClass 待校验的假定 AspectJ 注解风格类
	 * @throws AopConfigException 若类为无效切面（永不可合法）
	 * @throws NotAnAtAspectException 若类根本不是切面
	 * （是否合法取决于上下文）
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * 为指定切面实例上所有带 At-AspectJ 注解的方法
	 * 构建 Spring AOP 通知器。
	 * @param aspectInstanceFactory 切面实例工厂
	 * （非切面实例本身，以避免过早实例化）
	 * @return 本类的通知器列表
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	/**
	 * 为给定 AspectJ 通知方法构建 Spring AOP 通知器。
	 * @param candidateAdviceMethod 候选通知方法
	 * @param aspectInstanceFactory 切面实例工厂
	 * @param declarationOrder 切面内的声明顺序
	 * @param aspectName 切面名称
	 * @return 若方法不是 AspectJ 通知方法，
	 * 或为供其他通知使用但不单独创建 Spring 通知的切点，则返回 {@code null}
	 */
	@Nullable Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName);

	/**
	 * 为给定 AspectJ 通知方法构建 Spring AOP Advice。
	 * @param candidateAdviceMethod 候选通知方法
	 * @param expressionPointcut AspectJ 表达式切点
	 * @param aspectInstanceFactory 切面实例工厂
	 * @param declarationOrder 切面内的声明顺序
	 * @param aspectName 切面名称
	 * @return 若方法不是 AspectJ 通知方法，
	 * 或为供其他通知使用但不单独创建 Spring 通知的切点，则返回 {@code null}
	 * @see org.springframework.aop.aspectj.AspectJAroundAdvice
	 * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice
	 */
	@Nullable Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}
