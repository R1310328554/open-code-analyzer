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

package org.springframework.boot.logging.log4j2;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * 定制 Log4J2 默认配置的 Spring Boot {@link ConfigurationFactory}，用于：
 *
 * <ol>
 * <li>防止应用首次启动时打印 logger 警告。
 * </ol>
 *
 * 此工厂排序最后，由 classpath 资源 {@code log4j2.springboot}（打包在本 jar 中）触发。
 * 若 {@link Log4J2LoggingSystem} 处于活动状态，则返回 {@link DefaultConfiguration}，
 * 期望系统稍后使用正确的配置文件重新初始化 Log4J2。
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
@Plugin(name = "SpringBootConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(0)
public class SpringBootConfigurationFactory extends ConfigurationFactory {

	private static final String[] TYPES = { ".springboot" };

	@Override
	protected String[] getSupportedTypes() {
		return TYPES;
	}

	@Override
	public @Nullable Configuration getConfiguration(LoggerContext loggerContext, @Nullable ConfigurationSource source) {
		if (source == null || source == ConfigurationSource.NULL_SOURCE) {
			return null;
		}
		return new DefaultConfiguration();
	}

}
