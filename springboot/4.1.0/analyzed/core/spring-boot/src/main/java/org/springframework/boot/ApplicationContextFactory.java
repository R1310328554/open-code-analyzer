/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 为 {@link SpringApplication} 创建所用 {@link ConfigurableApplicationContext} 的策略接口。
 * 创建的上下文应以默认形式返回，由 {@code SpringApplication} 负责配置与刷新上下文。
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 2.4.0
 */
@FunctionalInterface
public interface ApplicationContextFactory {

	/**
	 * 默认 {@link ApplicationContextFactory} 实现，将根据 {@link WebApplicationType} 创建合适的上下文。
	 */
	ApplicationContextFactory DEFAULT = new DefaultApplicationContextFactory();

	/**
	 * 返回应设置在 {@link #create(WebApplicationType) 创建的} 应用上下文上的
	 * {@link Environment} 类型。此方法的结果可用于将现有环境实例转换为正确类型。
	 * @param webApplicationType Web 应用类型，或 {@code null}
	 * @return 期望的应用上下文类型，或 {@code null} 以使用默认值
	 * @since 2.6.14
	 */
	default @Nullable Class<? extends ConfigurableEnvironment> getEnvironmentType(
			@Nullable WebApplicationType webApplicationType) {
		return null;
	}

	/**
	 * 创建应设置在 {@link #create(WebApplicationType) 创建的} 应用上下文上的新 {@link Environment}。
	 * 此方法的结果必须与 {@link #getEnvironmentType(WebApplicationType)} 返回的类型一致。
	 * @param webApplicationType Web 应用类型，或 {@code null}
	 * @return 环境实例，或 {@code null} 以使用默认值
	 * @since 2.6.14
	 */
	default @Nullable ConfigurableEnvironment createEnvironment(@Nullable WebApplicationType webApplicationType) {
		return null;
	}

	/**
	 * 为 {@link SpringApplication} 创建 {@link ConfigurableApplicationContext 应用上下文}，
	 * 并尊重给定的 {@code webApplicationType}。
	 * @param webApplicationType Web 应用类型
	 * @return 新创建的应用上下文
	 */
	@Nullable ConfigurableApplicationContext create(@Nullable WebApplicationType webApplicationType);

	/**
	 * 创建 {@code ApplicationContextFactory}，通过主构造函数实例化给定 {@code contextClass} 来创建上下文。
	 * @param contextClass 上下文类
	 * @return 将实例化上下文类的工厂
	 * @see BeanUtils#instantiateClass(Class)
	 */
	static ApplicationContextFactory ofContextClass(Class<? extends ConfigurableApplicationContext> contextClass) {
		return of(() -> BeanUtils.instantiateClass(contextClass));
	}

	/**
	 * 创建 {@code ApplicationContextFactory}，通过调用给定 {@link Supplier} 来创建上下文。
	 * @param supplier 上下文供应者，例如 {@code AnnotationConfigApplicationContext::new}
	 * @return 将实例化上下文类的工厂
	 */
	static ApplicationContextFactory of(Supplier<ConfigurableApplicationContext> supplier) {
		return (webApplicationType) -> supplier.get();
	}

}
