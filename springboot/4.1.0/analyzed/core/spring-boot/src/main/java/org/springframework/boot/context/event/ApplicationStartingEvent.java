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

package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * {@link SpringApplication} 启动后尽可能早地发布的事件——此时 {@link Environment} 与
 * {@link ApplicationContext} 尚不可用，但 {@link ApplicationListener} 已注册。
 * 事件源为 {@link SpringApplication} 本身；此早期阶段其内部状态仍可能被后续生命周期修改，
 * 不宜过度依赖。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class ApplicationStartingEvent extends SpringApplicationEvent {

	private final ConfigurableBootstrapContext bootstrapContext;

	/**
	 * 创建新的 {@link ApplicationStartingEvent} 实例。
	 *
	 * @param bootstrapContext 引导上下文
	 * @param application 当前应用
	 * @param args 应用运行参数
	 */
	public ApplicationStartingEvent(ConfigurableBootstrapContext bootstrapContext, SpringApplication application,
			String[] args) {
		super(application, args);
		this.bootstrapContext = bootstrapContext;
	}

	/**
	 * 返回引导上下文。
	 *
	 * @return 引导上下文
	 * @since 2.4.0
	 */
	public ConfigurableBootstrapContext getBootstrapContext() {
		return this.bootstrapContext;
	}

}
