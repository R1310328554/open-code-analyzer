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
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * 在 {@link SpringApplication} 启动过程中，{@link Environment} 首次可供检查与修改时发布的事件。
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class ApplicationEnvironmentPreparedEvent extends SpringApplicationEvent {

	private final ConfigurableBootstrapContext bootstrapContext;

	private final ConfigurableEnvironment environment;

	/**
	 * 创建新的 {@link ApplicationEnvironmentPreparedEvent} 实例。
	 *
	 * @param bootstrapContext 引导上下文
	 * @param application 当前应用
	 * @param args 应用运行参数
	 * @param environment 刚创建的环境
	 */
	public ApplicationEnvironmentPreparedEvent(ConfigurableBootstrapContext bootstrapContext,
			SpringApplication application, String[] args, ConfigurableEnvironment environment) {
		super(application, args);
		this.bootstrapContext = bootstrapContext;
		this.environment = environment;
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

	/**
	 * 返回环境。
	 *
	 * @return 环境
	 */
	public ConfigurableEnvironment getEnvironment() {
		return this.environment;
	}

}
