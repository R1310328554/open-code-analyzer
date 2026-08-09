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

import org.springframework.boot.bootstrap.BootstrapContext;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 允许在应用上下文刷新之前自定义应用的 {@link Environment}。
 * <p>
 * {@code EnvironmentPostProcessor} 实现须在 {@code META-INF/spring.factories} 中注册，
 * 以本类全限定名作为键。若需按特定顺序调用，可实现
 * {@link org.springframework.core.Ordered Ordered} 接口或使用
 * {@link org.springframework.core.annotation.Order @Order} 注解。
 * <p>
 * 实现类可选地接受以下构造器参数：
 * <ul>
 * <li>{@link DeferredLogFactory} — 用于创建延迟输出日志的工厂，
 * 直到应用完全准备就绪后才输出（允许环境本身配置日志级别）。</li>
 * <li>{@link ConfigurableBootstrapContext} — 引导上下文，
 * 用于存储创建成本较高或需要共享的对象
 * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）。</li>
 * </ul>
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 4.0.0
 */
@FunctionalInterface
public interface EnvironmentPostProcessor {

	/**
	 * 后处理给定的 {@code environment}。
	 *
	 * @param environment 要后处理的环境
	 * @param application 所属的应用
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application);

}
