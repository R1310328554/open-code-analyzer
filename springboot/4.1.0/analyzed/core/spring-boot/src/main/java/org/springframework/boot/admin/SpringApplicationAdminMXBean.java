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

package org.springframework.boot.admin;

import org.jspecify.annotations.Nullable;

/**
 * 通过 JMX 控制与监控运行中 {@code SpringApplication} 的 MBean 契约。
 * <p>
 * <strong>仅供内部使用。</strong>
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 */
public interface SpringApplicationAdminMXBean {

	/**
	 * 应用是否已完全启动并就绪。
	 *
	 * @return 应用就绪时为 {@code true}
	 * @see org.springframework.boot.context.event.ApplicationReadyEvent
	 */
	boolean isReady();

	/**
	 * 应用是否运行在嵌入式 Web 容器中。
	 * <p>
	 * Web 应用未完全启动时可能返回 {@code false}，建议先等待 {@link #isReady() 就绪}。
	 *
	 * @return 运行在嵌入式 Web 容器时为 {@code true}
	 * @see #isReady()
	 */
	boolean isEmbeddedWebApplication();

	/**
	 * 从应用 {@link org.springframework.core.env.Environment Environment} 返回指定键的值。
	 *
	 * @param key 属性键
	 * @return 属性值，不存在时为 {@code null}
	 */
	@Nullable String getProperty(String key);

	/**
	 * 关闭应用。
	 *
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	void shutdown();

}
