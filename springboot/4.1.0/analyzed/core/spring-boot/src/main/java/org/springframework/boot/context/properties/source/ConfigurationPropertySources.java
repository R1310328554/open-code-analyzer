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

package org.springframework.boot.context.properties.source;

import java.util.Collections;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySource.StubPropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;

/**
 * 提供对 {@link ConfigurationPropertySource 配置属性源} 的访问。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public final class ConfigurationPropertySources {

	/**
	 * {@link #attach(Environment) 适配器} 所用 {@link PropertySource} 的名称。
	 */
	private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

	private ConfigurationPropertySources() {
	}

	/**
	 * 创建新的 {@link PropertyResolver}，基于底层 {@link PropertySources} 解析属性值。
	 * 提供感知 {@link ConfigurationPropertySource} 且优于 {@link PropertySourcesPropertyResolver} 的实现。
	 *
	 * @param propertySources 要使用的 {@link PropertySource} 集合
	 * @return a {@link ConfigurablePropertyResolver} implementation 可配置属性解析器实现
	 * @since 2.5.0
	 */
	public static ConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
		return new ConfigurationPropertySourcesPropertyResolver(propertySources);
	}

	/**
	 * 判断给定 {@link PropertySource} 是否为已 {@link #attach(Environment) 附加} 到
	 * {@link Environment} 的 {@link ConfigurationPropertySource}。
	 *
	 * @param propertySource 要检测的属性源
	 * @return {@code true} if this is the attached {@link ConfigurationPropertySource} 若为已附加的配置属性源则为 {@code true}
	 */
	public static boolean isAttachedConfigurationPropertySource(PropertySource<?> propertySource) {
		return ATTACHED_PROPERTY_SOURCE_NAME.equals(propertySource.getName());
	}

	/**
	 * 为指定 {@link Environment} 附加 {@link ConfigurationPropertySource} 支持。
	 * 将环境管理的每个 {@link PropertySource} 适配为 {@link ConfigurationPropertySource}，
	 * 使经典 {@link PropertySourcesPropertyResolver} 调用可使用
	 * {@link ConfigurationPropertyName 配置属性名} 解析。
	 * <p>
	 * 附加的解析器会动态跟踪底层 {@link Environment} 属性源的增删。
	 *
	 * @param environment 源环境（必须是 {@link ConfigurableEnvironment} 实例）
	 * @see #get(Environment)
	 */
	public static void attach(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
		MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
		PropertySource<?> attached = getAttached(sources);
		if (!isUsingSources(attached, sources)) {
			attached = new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
					new SpringConfigurationPropertySources(sources));
		}
		sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);
		sources.addFirst(attached);
	}

	@Contract("null, _ -> false")
	private static boolean isUsingSources(@Nullable PropertySource<?> attached, MutablePropertySources sources) {
		return attached instanceof ConfigurationPropertySourcesPropertySource
				&& ((SpringConfigurationPropertySources) attached.getSource()).isUsingSources(sources);
	}

	static @Nullable PropertySource<?> getAttached(@Nullable MutablePropertySources sources) {
		return (sources != null) ? sources.get(ATTACHED_PROPERTY_SOURCE_NAME) : null;
	}

	/**
	 * 返回先前已 {@link #attach(Environment) 附加} 到 {@link Environment} 的
	 * {@link ConfigurationPropertySource} 实例集合。
	 *
	 * @param environment 源环境（必须是 {@link ConfigurableEnvironment} 实例）
	 * @return an iterable set of configuration property sources 可迭代的配置属性源集合
	 * @throws IllegalStateException if not configuration property sources have been attached 若尚未附加配置属性源
	 */
	public static Iterable<ConfigurationPropertySource> get(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
		MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
		ConfigurationPropertySourcesPropertySource attached = (ConfigurationPropertySourcesPropertySource) sources
			.get(ATTACHED_PROPERTY_SOURCE_NAME);
		if (attached == null) {
			return from(sources);
		}
		return attached.getSource();
	}

	/**
	 * 返回包含单个新 {@link ConfigurationPropertySource} 的 {@link Iterable}，
	 * 该实例由给定 Spring {@link PropertySource} 适配而来；无法适配时该元素可为 {@code null}。
	 *
	 * @param source 要适配的 Spring 属性源
	 * @return an {@link Iterable} containing a single newly adapted {@link SpringConfigurationPropertySource} 包含单个新适配实例的可迭代对象
	 */
	public static Iterable<@Nullable ConfigurationPropertySource> from(PropertySource<?> source) {
		return Collections.singleton(ConfigurationPropertySource.from(source));
	}

	/**
	 * 返回包含由给定 Spring {@link PropertySource PropertySources} 适配的新
	 * {@link ConfigurationPropertySource} 实例的 {@link Iterable}。
	 * <p>
	 * 此方法会扁平化嵌套属性源，并过滤所有 {@link StubPropertySource 桩属性源}。
	 * 底层源通过迭代器返回的属性源变化自动跟踪更新；底层源应线程安全，例如 {@link MutablePropertySources}。
	 *
	 * @param sources 要适配的 Spring 属性源
	 * @return an {@link Iterable} containing newly adapted {@link SpringConfigurationPropertySource} instances 包含新适配实例的可迭代对象
	 */
	public static Iterable<ConfigurationPropertySource> from(Iterable<PropertySource<?>> sources) {
		return new SpringConfigurationPropertySources(sources);
	}

	private static Stream<PropertySource<?>> streamPropertySources(PropertySources sources) {
		return sources.stream()
			.flatMap(ConfigurationPropertySources::flatten)
			.filter(ConfigurationPropertySources::isIncluded);
	}

	private static Stream<PropertySource<?>> flatten(PropertySource<?> source) {
		if (source.getSource() instanceof ConfigurableEnvironment configurableEnvironment) {
			return streamPropertySources(configurableEnvironment.getPropertySources());
		}
		return Stream.of(source);
	}

	private static boolean isIncluded(PropertySource<?> source) {
		return !(source instanceof StubPropertySource)
				&& !(source instanceof ConfigurationPropertySourcesPropertySource);
	}

}
