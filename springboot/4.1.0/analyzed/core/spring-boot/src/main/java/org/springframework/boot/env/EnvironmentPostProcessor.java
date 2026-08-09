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

package org.springframework.boot.env;

import org.springframework.boot.bootstrap.BootstrapContext;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 允许在应用上下文刷新之前定制应用的 {@link Environment}。
 * <p>
 * {@code EnvironmentPostProcessor} 实现须在 {@code META-INF/spring.factories} 中注册，
 * 以本类全限定名作为键。若需按特定顺序调用，可实现 {@link org.springframework.core.Ordered Ordered}
 * 接口或使用 {@link org.springframework.core.annotation.Order @Order} 注解。
 * <p>
 * 自 Spring Boot 2.4 起，{@code EnvironmentPostProcessor} 实现可选接受以下构造参数：
 * <ul>
 * <li>{@link DeferredLogFactory} — 可创建日志输出延迟到应用完全就绪后的 Logger 的工厂
 * （允许环境本身配置日志级别）。</li>
 * <li>{@link ConfigurableBootstrapContext} — 可用于存储创建成本较高或需共享的对象的引导上下文
 * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）。</li>
 * </ul>
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.3.0
 * @deprecated since 4.0.0 for removal in 4.2.0 in favor of
 * {@link org.springframework.boot.EnvironmentPostProcessor}
 */
@FunctionalInterface
@Deprecated(since = "4.0.0", forRemoval = true)
public interface EnvironmentPostProcessor {

	/**
	 * 后处理给定 {@code environment}。
	 *
	 * @param environment 要后处理的环境
	 * @param application 环境所属的应用
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment,
			org.springframework.boot.SpringApplication application);

}
