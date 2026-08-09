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

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * {@link SpringApplication#run} 方法的监听器。
 * <p>
 * {@link SpringApplicationRunListener} 通过 {@link SpringFactoriesLoader} 加载，
 * 须声明接受 {@link SpringApplication} 实例与 {@code String[]} 参数的 public 构造器。
 * 每次 {@code run} 调用都会创建新实例。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Chris Bono
 * @since 1.0.0
 */
public interface SpringApplicationRunListener {

	/**
	 * {@code run} 方法刚启动时立即调用，可用于极早期初始化。
	 *
	 * @param bootstrapContext 引导上下文
	 */
	default void starting(ConfigurableBootstrapContext bootstrapContext) {
	}

	/**
	 * 环境准备完成后、{@link ApplicationContext} 创建前调用。
	 *
	 * @param bootstrapContext 引导上下文
	 * @param environment 环境
	 */
	default void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
			ConfigurableEnvironment environment) {
	}

	/**
	 * {@link ApplicationContext} 已创建并准备就绪、但尚未加载源时调用。
	 *
	 * @param context 应用上下文
	 */
	default void contextPrepared(ConfigurableApplicationContext context) {
	}

	/**
	 * 应用上下文已加载但尚未刷新时调用。
	 *
	 * @param context 应用上下文
	 */
	default void contextLoaded(ConfigurableApplicationContext context) {
	}

	/**
	 * 上下文已刷新且应用已启动，但尚未调用
	 * {@link CommandLineRunner CommandLineRunners} 与 {@link ApplicationRunner ApplicationRunners}。
	 *
	 * @param context 应用上下文
	 * @param timeTaken 启动耗时，未知时为 {@code null}
	 * @since 2.6.0
	 */
	default void started(ConfigurableApplicationContext context, @Nullable Duration timeTaken) {
	}

	/**
	 * {@code run} 方法即将结束前调用；此时上下文已刷新且所有
	 * {@link CommandLineRunner CommandLineRunners} 与 {@link ApplicationRunner ApplicationRunners}
	 * 均已执行完毕。
	 *
	 * @param context 应用上下文
	 * @param timeTaken 应用就绪耗时，未知时为 {@code null}
	 * @since 2.6.0
	 */
	default void ready(ConfigurableApplicationContext context, @Nullable Duration timeTaken) {
	}

	/**
	 * 应用运行失败时调用。
	 *
	 * @param context 应用上下文；若在上下文创建前失败则为 {@code null}
	 * @param exception 失败原因
	 * @since 2.0.0
	 */
	default void failed(@Nullable ConfigurableApplicationContext context, Throwable exception) {
	}

}
