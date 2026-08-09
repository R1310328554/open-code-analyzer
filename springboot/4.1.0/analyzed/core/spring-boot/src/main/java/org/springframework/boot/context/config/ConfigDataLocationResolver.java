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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.bootstrap.BootstrapContext;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * 将 {@link ConfigDataLocation 配置位置} 解析为一个或多个 {@link ConfigDataResource 资源} 的策略接口。
 * 实现类应作为 {@code spring.factories} 条目注册。支持以下构造器参数类型：
 * <ul>
 * <li>{@link DeferredLogFactory} — 解析器需要延迟日志时使用</li>
 * <li>{@link Binder} — 解析器需从初始 {@link Environment} 获取值时使用</li>
 * <li>{@link ResourceLoader} — 解析器需要资源加载器时使用</li>
 * <li>{@link ConfigurableBootstrapContext} — 可用于存储创建成本较高或需共享的对象
 * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）</li>
 * </ul>
 * <p>
 * 解析器可实现 {@link Ordered} 或使用 {@link Order @Order} 注解。
 * 将使用第一个支持给定位置的解析器。
 *
 * @param <R> 资源类型
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public interface ConfigDataLocationResolver<R extends ConfigDataResource> {

	/**
	 * 返回此解析器是否能解析指定位置地址。
	 *
	 * @param context 位置解析器上下文
	 * @param location 待检查的位置
	 * @return 此解析器是否支持该位置
	 */
	boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location);

	/**
	 * 将 {@link ConfigDataLocation} 解析为一个或多个 {@link ConfigDataResource} 实例。
	 *
	 * @param context 位置解析器上下文
	 * @param location 待解析的位置
	 * @return 按优先级升序排列的 {@link ConfigDataResource 资源} 列表
	 * @throws ConfigDataLocationNotFoundException 非可选位置找不到时抛出
	 * @throws ConfigDataResourceNotFoundException 解析出的资源找不到时抛出
	 */
	List<R> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location)
			throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException;

	/**
	 * 根据可用 profile 将 {@link ConfigDataLocation} 解析为一个或多个 {@link ConfigDataResource} 实例。
	 * 在从已贡献值推断出 profile 后调用。默认返回空列表。
	 *
	 * @param context 位置解析器上下文
	 * @param location 待解析的位置
	 * @param profiles profile 信息
	 * @return 按优先级升序排列的已解析位置列表
	 * @throws ConfigDataLocationNotFoundException 非可选位置找不到时抛出
	 */
	default List<R> resolveProfileSpecific(ConfigDataLocationResolverContext context, ConfigDataLocation location,
			Profiles profiles) throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

}
