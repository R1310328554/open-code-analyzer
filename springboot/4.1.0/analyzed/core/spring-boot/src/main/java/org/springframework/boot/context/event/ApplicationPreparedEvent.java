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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 在 {@link SpringApplication} 启动过程中，{@link ApplicationContext} 已完全准备就绪但尚未刷新时发布的事件。
 * 此阶段 Bean 定义已加载，{@link Environment} 可供使用。
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class ApplicationPreparedEvent extends SpringApplicationEvent {

	private final ConfigurableApplicationContext context;

	/**
	 * 创建新的 {@link ApplicationPreparedEvent} 实例。
	 *
	 * @param application 当前应用
	 * @param args 应用运行参数
	 * @param context 即将刷新的 ApplicationContext
	 */
	public ApplicationPreparedEvent(SpringApplication application, String[] args,
			ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	/**
	 * 返回应用上下文。
	 *
	 * @return 上下文
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

}
