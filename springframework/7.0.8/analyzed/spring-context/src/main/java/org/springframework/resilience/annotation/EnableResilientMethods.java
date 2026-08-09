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

package org.springframework.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用 Spring 方法调用的核心弹性特性：
 * {@link Retryable @Retryable} 与 {@link ConcurrencyLimit @ConcurrencyLimit}。
 *
 * <p>也可通过分别定义 {@link RetryAnnotationBeanPostProcessor} 或
 * {@link ConcurrencyLimitBeanPostProcessor} 单独启用这些注解。
 *
 * @author Juergen Hoeller
 * @since 7.0
 * @see RetryAnnotationBeanPostProcessor
 * @see ConcurrencyLimitBeanPostProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ResilientMethodsConfiguration.class)
public @interface EnableResilientMethods {

	/**
	 * 指示是否创建基于子类（CGLIB）的代理，而非标准 Java 接口代理。
	 * <p>默认为 {@code false}。
	 * <p>注意：将此属性设为 {@code true} 仅影响
	 * {@link RetryAnnotationBeanPostProcessor} 与
	 * {@link ConcurrencyLimitBeanPostProcessor}。
	 * <p>通常建议依赖全局默认代理配置，
	 * 对特定 Bean 的代理需求通过受影响 Bean 类上的
	 * {@link org.springframework.context.annotation.Proxyable} 注解表达。
	 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 指示 {@link RetryAnnotationBeanPostProcessor} 与
	 * {@link ConcurrencyLimitBeanPostProcessor} 的应用顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE - 1}，以便在除 {@code @EnableAsync}
	 * 外的所有常见后处理器之后运行。
	 * @see org.springframework.scheduling.annotation.EnableAsync#order()
	 */
	int order() default Ordered.LOWEST_PRECEDENCE - 1;

}
