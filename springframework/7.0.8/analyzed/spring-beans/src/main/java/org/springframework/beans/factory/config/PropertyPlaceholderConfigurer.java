/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.SpringProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

/**
 * {@link PlaceholderConfigurerSupport} 子类，针对 {@link #setLocation 本地}
 * {@link #setProperties 属性}、系统属性及环境变量解析 ${...} 占位符。
 *
 * <p>在以下情形仍适合使用 {@link PropertyPlaceholderConfigurer}：
 * <ul>
 * <li>没有 {@code spring-context} 模块（即使用 Spring 的 {@code BeanFactory} API
 * 而非 {@code ApplicationContext}）。
 * <li>现有配置使用了 {@link #setSystemPropertiesMode(int) "systemPropertiesMode"}
 * 和/或 {@link #setSystemPropertiesModeName(String) "systemPropertiesModeName"} 属性。
 * 建议用户逐步弃用这些设置，改为通过容器的 {@code Environment} 配置属性源搜索顺序；
 * 但若需完全保持原有行为，可继续使用 {@code PropertyPlaceholderConfigurer}。
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @since 02.10.2003
 * @see #setSystemPropertiesModeName
 * @see PlaceholderConfigurerSupport
 * @see PropertyOverrideConfigurer
 * @deprecated 自 5.2 起已弃用，将在 8.0 中移除；
 * 请改用 {@code org.springframework.context.support.PropertySourcesPlaceholderConfigurer}，
 * 它通过 {@link org.springframework.core.env.Environment} 与
 * {@link org.springframework.core.env.PropertySource} 机制更加灵活。
 */
@Deprecated(since = "5.2", forRemoval = true)
public class PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport {

	/** 从不检查系统属性 */
	public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

	/**
	 * 在指定属性中无法解析时检查系统属性。
	 * 这是默认模式。
	 */
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

	/**
	 * 先检查系统属性，再尝试指定属性。
	 * 这样系统属性可覆盖任何其他属性源。
	 */
	public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;


	/** 本类定义的系统属性模式常量名称到常量值的映射 */
	private static final Map<String, Integer> constants = Map.of(
			"SYSTEM_PROPERTIES_MODE_NEVER", SYSTEM_PROPERTIES_MODE_NEVER,
			"SYSTEM_PROPERTIES_MODE_FALLBACK", SYSTEM_PROPERTIES_MODE_FALLBACK,
			"SYSTEM_PROPERTIES_MODE_OVERRIDE", SYSTEM_PROPERTIES_MODE_OVERRIDE
		);


	/** 系统属性检查模式，默认为 FALLBACK */
	private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;

	/** 未找到匹配的系统属性时，是否搜索系统环境变量 */
	private boolean searchSystemEnvironment =
			!SpringProperties.getFlag(AbstractEnvironment.IGNORE_GETENV_PROPERTY_NAME);


	/**
	 * 通过对应常量的名称设置系统属性模式，例如 "SYSTEM_PROPERTIES_MODE_OVERRIDE"。
	 * @param constantName 常量名称
	 * @see #setSystemPropertiesMode
	 */
	public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer mode = constants.get(constantName);
		Assert.notNull(mode, "Only system properties mode constants allowed");
		this.systemPropertiesMode = mode;
	}

	/**
	 * 设置如何检查系统属性：作为后备、作为覆盖，或从不检查。
	 * 例如，将把 ${user.dir} 解析为 "user.dir" 系统属性。
	 * <p>默认为 "fallback"：若无法用指定属性解析占位符，则尝试系统属性。
	 * "override" 先检查系统属性，再尝试指定属性。"never" 完全不检查系统属性。
	 * @see #SYSTEM_PROPERTIES_MODE_NEVER
	 * @see #SYSTEM_PROPERTIES_MODE_FALLBACK
	 * @see #SYSTEM_PROPERTIES_MODE_OVERRIDE
	 * @see #setSystemPropertiesModeName
	 */
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}

	/**
	 * 设置在未找到匹配的系统属性时，是否搜索匹配的系统环境变量。
	 * 仅在 "systemPropertyMode" 生效时（即 "fallback" 或 "override"）应用，
	 * 且紧接在检查 JVM 系统属性之后。
	 * <p>默认为 {@code true}。关闭此设置将永不针对系统环境变量解析占位符。
	 * 通常建议将外部值作为 JVM 系统属性传入：即使在启动脚本中也可轻松实现，
	 * 包括已有环境变量的情况。
	 * @see #setSystemPropertiesMode
	 * @see System#getProperty(String)
	 * @see System#getenv(String)
	 */
	public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
		this.searchSystemEnvironment = searchSystemEnvironment;
	}

	/**
	 * 使用给定属性解析占位符，并按给定模式执行系统属性检查。
	 * <p>默认实现在系统属性检查之前/之后委托给
	 * {@code resolvePlaceholder(placeholder, props)}。
	 * <p>子类可重写以实现自定义解析策略，包括自定义系统属性检查时机。
	 * @param placeholder 待解析的占位符
	 * @param props 本配置器合并后的属性
	 * @param systemPropertiesMode 系统属性模式，对应本类中的常量
	 * @return 解析后的值，若无则为 null
	 * @see #setSystemPropertiesMode
	 * @see System#getProperty
	 * @see #resolvePlaceholder(String, java.util.Properties)
	 */
	protected @Nullable String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String propVal = null;
		if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
			propVal = resolveSystemProperty(placeholder);
		}
		if (propVal == null) {
			propVal = resolvePlaceholder(placeholder, props);
		}
		if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
			propVal = resolveSystemProperty(placeholder);
		}
		return propVal;
	}

	/**
	 * 使用给定属性解析占位符。
	 * 默认实现仅检查对应的属性键。
	 * <p>子类可重写以实现自定义占位符到键的映射或自定义解析策略，
	 * 也可能仅将给定属性作为后备。
	 * <p>注意：根据系统属性模式，仍会在调用本方法之前或之后检查系统属性。
	 * @param placeholder 待解析的占位符
	 * @param props 本配置器合并后的属性
	 * @return 解析后的值，若无则为 {@code null}
	 * @see #setSystemPropertiesMode
	 */
	protected @Nullable String resolvePlaceholder(String placeholder, Properties props) {
		return props.getProperty(placeholder);
	}

	/**
	 * 将给定键作为 JVM 系统属性解析；若未找到匹配的系统属性，
	 * 还可选地作为系统环境变量解析。
	 * @param key 作为系统属性键解析的占位符
	 * @return 系统属性值，未找到则为 {@code null}
	 * @see #setSearchSystemEnvironment
	 * @see System#getProperty(String)
	 * @see System#getenv(String)
	 */
	protected @Nullable String resolveSystemProperty(String key) {
		try {
			String value = System.getProperty(key);
			if (value == null && this.searchSystemEnvironment) {
				value = System.getenv(key);
			}
			return value;
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not access system property '" + key + "': " + ex);
			}
			return null;
		}
	}


	/**
	 * 遍历给定 Bean 工厂中的每个 Bean 定义，尝试用给定属性中的值替换 ${...} 属性占位符。
	 */
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {

		StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
		doProcessProperties(beanFactoryToProcess, valueResolver);
	}


	/** 解析占位符的 StringValueResolver 实现 */
	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(
					placeholderPrefix, placeholderSuffix, valueSeparator,
					escapeCharacter, ignoreUnresolvablePlaceholders);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		@Override
		public @Nullable String resolveStringValue(String strVal) throws BeansException {
			String resolved = this.helper.replacePlaceholders(strVal, this.resolver);
			if (trimValues) {
				resolved = resolved.trim();
			}
			return (resolved.equals(nullValue) ? null : resolved);
		}
	}


	/** 委托给外部类 resolvePlaceholder 方法的 PlaceholderResolver */
	private final class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

		private final Properties props;

		private PropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		@Override
		public @Nullable String resolvePlaceholder(String placeholderName) {
			return PropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName,
					this.props, systemPropertiesMode);
		}
	}

}
