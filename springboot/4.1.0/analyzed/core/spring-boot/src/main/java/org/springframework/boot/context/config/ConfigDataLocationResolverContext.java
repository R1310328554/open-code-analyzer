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

package org.springframework.boot.context.config;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.Binder;

/**
 * 提供给 {@link ConfigDataLocationResolver} 方法的上下文。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public interface ConfigDataLocationResolverContext {

	/**
	 * 提供可用于获取先前已贡献值的绑定器。
	 *
	 * @return 绑定器实例
	 */
	Binder getBinder();

	/**
	 * 提供触发本次解析的父 {@link ConfigDataResource}；若无可用父资源则返回 {@code null}。
	 *
	 * @return 父资源
	 */
	@Nullable ConfigDataResource getParent();

	/**
	 * 提供所有 {@link EnvironmentPostProcessor EnvironmentPostProcessor} 共享的
	 * {@link ConfigurableBootstrapContext}。
	 *
	 * @return 引导上下文
	 */
	ConfigurableBootstrapContext getBootstrapContext();

}
