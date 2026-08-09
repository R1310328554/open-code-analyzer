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

package org.springframework.boot.context.properties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * 记录并提供已绑定的 {@link ConfigurationProperties @ConfigurationProperties} 的 Bean。
 *
 * @author Madhura Bhave
 * @since 2.3.0
 */
public class BoundConfigurationProperties {

	private final Map<ConfigurationPropertyName, ConfigurationProperty> properties = new LinkedHashMap<>();

	/**
	 * 注册此类的 Bean 名称。
	 */
	private static final String BEAN_NAME = BoundConfigurationProperties.class.getName();

	void add(ConfigurationProperty configurationProperty) {
		this.properties.put(configurationProperty.getName(), configurationProperty);
	}

	/**
	 * 获取绑定到给定名称的配置属性。
	 *
	 * @param name 属性名
	 * @return 已绑定属性，或 {@code null}
	 */
	public @Nullable ConfigurationProperty get(ConfigurationPropertyName name) {
		return this.properties.get(name);
	}

	/**
	 * 获取所有已绑定属性。
	 *
	 * @return 所有已绑定属性的映射
	 */
	public Map<ConfigurationPropertyName, ConfigurationProperty> getAll() {
		return Collections.unmodifiableMap(this.properties);
	}

	/**
	 * 若可用，从给定 {@link ApplicationContext} 返回 {@link BoundConfigurationProperties}。
	 *
	 * @param context 要搜索的上下文
	 * @return {@link BoundConfigurationProperties} 实例，或 {@code null}
	 */
	public static @Nullable BoundConfigurationProperties get(ApplicationContext context) {
		return (!context.containsBeanDefinition(BEAN_NAME)) ? null
				: context.getBean(BEAN_NAME, BoundConfigurationProperties.class);
	}

	static void register(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "'registry' must not be null");
		if (!registry.containsBeanDefinition(BEAN_NAME)) {
			BeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(BoundConfigurationProperties.class)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();
			registry.registerBeanDefinition(BEAN_NAME, definition);
		}
	}

}
