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
 * 可以从使用 AspectJ 注释语法注释的类创建 Spring AOP Advisor 的工厂接口。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 */
public interface AspectJAdvisorFactory {

	/**
	 * 确定给定的类是否是一个方面，如 AspectJ 的 {@link org.aspectj.lang.reflect.AjTypeSystem} 所报告的那样。如果假定的方面无效
	 * （例如具体方面类的扩展），<p> 将简单地返回 {@code false}。对于 Spring AOP 无法处理的某些方面，例如那些不支持实例化模型的方面，将返回 true。如
	 * 有必要，请使用 {@link #validate} 方法来处理这些情况。
	 * @param clazz 假定的注释样式 AspectJ 类
	 * @return 该类被 AspectJ 识别为切面类
	 */
	boolean isAspect(Class<?> clazz);

	/**
	 * 给定的类是有效的 AspectJ 方面类吗？
	 * @param aspectClass 假设要验证的 AspectJ 注释样式类
	 * @throws AopConfigException 如果该类是无效的方面（永远不可能合法）
	 * @throws NotAnAtAspectException 如果该类根本不是一个方面（这可能合法也可能不合法，具体取决于上下文）
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * 为指定方面实例上的所有带注释的 At-AspectJ 方法构建 Spring AOP Advisor。
	 * @param aspectInstanceFactory 方面实例工厂（不是方面实例本身，以避免急于实例化）
	 * @return 本课程的顾问名单
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	/**
	 * 为给定的 AspectJ 建议方法构建 Spring AOP Advisor。
	 * @param candidateAdviceMethod 候选人建议法
	 * @param aspectInstanceFactory 方面实例工厂
	 * @param declarationOrder 方面内的声明顺序
	 * @param aspectName 方面的名称
	 * @return null} 如果该方法不是 AspectJ 建议方法，或者它是一个将被其他建议使用但不会自行创建 Spring 建议的切入点
	 */
	@Nullable Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName);

	/**
	 * 为给定的 AspectJ 建议方法构建 Spring AOP 建议。
	 * @param candidateAdviceMethod 候选人建议法
	 * @param expressionPointcut AspectJ 表达式切入点
	 * @param aspectInstanceFactory 方面实例工厂
	 * @param declarationOrder 方面内的声明顺序
	 * @param aspectName 方面的名称
	 * @return null} 如果该方法不是 AspectJ 建议方法，或者它是一个将被其他建议使用但不会自行创建 Spring 建议的切入点
	 * @see org.springframework.aop.aspectj.AspectJAroundAdvice
	 * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice
	 */
	@Nullable Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}
