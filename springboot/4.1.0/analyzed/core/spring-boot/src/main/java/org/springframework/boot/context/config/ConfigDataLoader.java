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

import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.bootstrap.BootstrapContext;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.logging.DeferredLogFactory;

/**
 * 为给定 {@link ConfigDataResource} 加载 {@link ConfigData} 的策略类。
 * 实现应作为 {@code spring.factories} 条目注册，支持以下构造器参数类型：
 * <ul>
 * <li>{@link DeferredLogFactory} — 需要延迟日志时</li>
 * <li>{@link ConfigurableBootstrapContext} — 可存储创建成本高或需共享的对象
 * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）</li>
 * </ul>
 * <p>
 * 多个加载器不能声明同一资源。
 *
 * @param <R> 资源类型
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public interface ConfigDataLoader<R extends ConfigDataResource> {

	/**
	 * 判断本实例是否可加载指定资源。
	 *
	 * @param context 加载器上下文
	 * @param resource 要检查的资源
	 * @return 本加载器是否支持该资源
	 */
	default boolean isLoadable(ConfigDataLoaderContext context, R resource) {
		return true;
	}

	/**
	 * 为给定资源加载 {@link ConfigData}。
	 *
	 * @param context 加载器上下文
	 * @param resource 要加载的资源
	 * @return 已加载的配置数据；应跳过该位置时为 {@code null}
	 * @throws IOException IO 错误
	 * @throws ConfigDataResourceNotFoundException 找不到资源
	 */
	@Nullable ConfigData load(ConfigDataLoaderContext context, R resource)
			throws IOException, ConfigDataResourceNotFoundException;

}
