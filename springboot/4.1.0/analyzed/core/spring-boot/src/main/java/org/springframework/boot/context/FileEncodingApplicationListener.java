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

package org.springframework.boot.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 当系统文件编码与环境中的期望值不一致时中止应用启动的 {@link ApplicationListener}。
 * 默认无效果；若将 {@code spring.mandatory_file_encoding}（或其 camelCase/UPPERCASE 变体）
 * 设为字符编码名（如 "UTF-8"），则当 {@code file.encoding} 系统属性与其不一致时抛出异常。
 * <p>
 * {@code file.encoding} 通常由 JVM 根据 {@code LANG} 或 {@code LC_ALL} 环境变量设置，
 * 用于编码 JVM 参数及文件名/路径。多数情况下可在命令行覆盖该属性，
 * 也可将 {@code LANG} 设为明确的字符编码区域（如 "en_GB.UTF-8"）。
 *
 * @author Dave Syer
 * @author Madhura Bhave
 * @since 1.0.0
 */
public class FileEncodingApplicationListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	private static final Log logger = LogFactory.getLog(FileEncodingApplicationListener.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		String desired = environment.getProperty("spring.mandatory-file-encoding");
		if (desired == null) {
			return;
		}
		String encoding = System.getProperty("file.encoding");
		if (encoding != null && !desired.equalsIgnoreCase(encoding)) {
			if (logger.isErrorEnabled()) {
				logger.error("System property 'file.encoding' is currently '" + encoding + "'. It should be '" + desired
						+ "' (as defined in 'spring.mandatoryFileEncoding').");
				logger.error("Environment variable LANG is '" + System.getenv("LANG")
						+ "'. You could use a locale setting that matches encoding='" + desired + "'.");
				logger.error("Environment variable LC_ALL is '" + System.getenv("LC_ALL")
						+ "'. You could use a locale setting that matches encoding='" + desired + "'.");
			}
			throw new IllegalStateException("The Java Virtual Machine has not been configured to use the "
					+ "desired default character encoding (" + desired + ").");
		}
	}

}
