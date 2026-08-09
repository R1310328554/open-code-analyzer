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

package org.springframework.boot.support;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * {@link EnvironmentPostProcessorApplicationListener} 用于创建
 * {@link EnvironmentPostProcessor} 实例的工厂接口。
 *
 * @author Phillip Webb
 * @since 4.0.0
 */
@FunctionalInterface
public interface EnvironmentPostProcessorsFactory {

	/**
	 * 创建所有请求的 {@link EnvironmentPostProcessor} 实例。
	 * @param logFactory a deferred log factory 延迟日志工厂
	 * @param bootstrapContext a bootstrap context 引导上下文
	 * @return the post processor instances 后处理器实例列表
	 */
	List<EnvironmentPostProcessor> getEnvironmentPostProcessors(DeferredLogFactory logFactory,
			ConfigurableBootstrapContext bootstrapContext);

	/**
	 * 返回由 {@code spring.factories} 支持的 {@link EnvironmentPostProcessorsFactory}。
	 * @param classLoader the source class loader 源类加载器
	 * @return an {@link EnvironmentPostProcessorsFactory} instance 实例
	 */
	static EnvironmentPostProcessorsFactory fromSpringFactories(@Nullable ClassLoader classLoader) {
		return new SpringFactoriesEnvironmentPostProcessorsFactory(
				SpringFactoriesLoader.forDefaultResourceLocation(classLoader));
	}

	/**
	 * 返回通过反射从给定类创建后处理器的 {@link EnvironmentPostProcessorsFactory}。
	 * @param classes the post processor classes 后处理器类
	 * @return an {@link EnvironmentPostProcessorsFactory} instance 实例
	 */
	static EnvironmentPostProcessorsFactory of(Class<?>... classes) {
		return new ReflectionEnvironmentPostProcessorsFactory(classes);
	}

	/**
	 * 返回通过反射从给定类名创建后处理器的 {@link EnvironmentPostProcessorsFactory}。
	 * @param classNames the post processor class names 后处理器类名
	 * @return an {@link EnvironmentPostProcessorsFactory} instance 实例
	 */
	static EnvironmentPostProcessorsFactory of(String... classNames) {
		return of(null, classNames);
	}

	/**
	 * 返回通过反射从给定类名创建后处理器的 {@link EnvironmentPostProcessorsFactory}。
	 * @param classLoader the source class loader 源类加载器
	 * @param classNames the post processor class names 后处理器类名
	 * @return an {@link EnvironmentPostProcessorsFactory} instance 实例
	 */
	static EnvironmentPostProcessorsFactory of(@Nullable ClassLoader classLoader, String... classNames) {
		return new ReflectionEnvironmentPostProcessorsFactory(classLoader, classNames);
	}

}
