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

package org.springframework.boot.logging;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 表示 {@link LoggingSystem} 中某个 Logger 配置的不变类。
 *
 * @author Ben Hale
 * @author Phillip Webb
 * @since 1.5.0
 */
public final class LoggerConfiguration {

	private final String name;

	private final @Nullable LevelConfiguration levelConfiguration;

	private final LevelConfiguration inheritedLevelConfiguration;

	/**
	 * 创建新的 {@link LoggerConfiguration} 实例。
	 *
	 * @param name Logger 名称
	 * @param configuredLevel 已配置的级别
	 * @param effectiveLevel 有效级别
	 */
	public LoggerConfiguration(String name, @Nullable LogLevel configuredLevel, LogLevel effectiveLevel) {
		Assert.notNull(name, "'name' must not be null");
		Assert.notNull(effectiveLevel, "'effectiveLevel' must not be null");
		this.name = name;
		this.levelConfiguration = (configuredLevel != null) ? LevelConfiguration.of(configuredLevel) : null;
		this.inheritedLevelConfiguration = LevelConfiguration.of(effectiveLevel);
	}

	/**
	 * 创建新的 {@link LoggerConfiguration} 实例。
	 *
	 * @param name Logger 名称
	 * @param levelConfiguration 级别配置
	 * @param inheritedLevelConfiguration 继承的级别配置
	 * @since 2.7.13
	 */
	public LoggerConfiguration(String name, @Nullable LevelConfiguration levelConfiguration,
			LevelConfiguration inheritedLevelConfiguration) {
		Assert.notNull(name, "'name' must not be null");
		Assert.notNull(inheritedLevelConfiguration, "'inheritedLevelConfiguration' must not be null");
		this.name = name;
		this.levelConfiguration = levelConfiguration;
		this.inheritedLevelConfiguration = inheritedLevelConfiguration;
	}

	/**
	 * 返回 Logger 名称。
	 *
	 * @return the name of the logger Logger 名称
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回 Logger 已配置的级别。
	 *
	 * @return the configured level of the logger 已配置级别
	 * @see #getLevelConfiguration(ConfigurationScope)
	 */
	public @Nullable LogLevel getConfiguredLevel() {
		LevelConfiguration configuration = getLevelConfiguration(ConfigurationScope.DIRECT);
		return (configuration != null) ? configuration.getLevel() : null;
	}

	/**
	 * 返回 Logger 的有效级别。
	 *
	 * @return the effective level of the logger 有效级别
	 * @see #getLevelConfiguration(ConfigurationScope)
	 */
	public LogLevel getEffectiveLevel() {
		return getLevelConfiguration().getLevel();
	}

	/**
	 * 返回考虑继承 Logger 后的级别配置。
	 *
	 * @return the level configuration 级别配置
	 * @since 2.7.13
	 */
	public LevelConfiguration getLevelConfiguration() {
		LevelConfiguration result = getLevelConfiguration(ConfigurationScope.INHERITED);
		Assert.state(result != null, "Inherited level configuration must not be null");
		return result;
	}

	/**
	 * 返回给定作用域的级别配置。
	 *
	 * @param scope 配置作用域
	 * @return the level configuration or {@code null} 级别配置；
	 * {@link ConfigurationScope#DIRECT} 且无直接配置时为 {@code null}
	 * @since 2.7.13
	 */
	public @Nullable LevelConfiguration getLevelConfiguration(ConfigurationScope scope) {
		return (scope != ConfigurationScope.DIRECT) ? this.inheritedLevelConfiguration : this.levelConfiguration;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		LoggerConfiguration other = (LoggerConfiguration) obj;
		return ObjectUtils.nullSafeEquals(this.name, other.name)
				&& ObjectUtils.nullSafeEquals(this.levelConfiguration, other.levelConfiguration)
				&& ObjectUtils.nullSafeEquals(this.inheritedLevelConfiguration, other.inheritedLevelConfiguration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.levelConfiguration, this.inheritedLevelConfiguration);
	}

	@Override
	public String toString() {
		return "LoggerConfiguration [name=" + this.name + ", levelConfiguration=" + this.levelConfiguration
				+ ", inheritedLevelConfiguration=" + this.inheritedLevelConfiguration + "]";
	}

	/**
	 * 支持的 Logger 配置作用域。
	 *
	 * @since 2.7.13
	 */
	public enum ConfigurationScope {

		/**
		 * 仅返回直接应用的配置，常称为“已配置”或“已分配”配置。
		 */
		DIRECT,

		/**
		 * 可能返回父 Logger 应用的配置，常称为“有效”配置。
		 */
		INHERITED

	}

	/**
	 * Logger 级别配置。
	 *
	 * @since 2.7.13
	 */
	public static final class LevelConfiguration {

		private final String name;

		private final @Nullable LogLevel logLevel;

		private LevelConfiguration(String name, @Nullable LogLevel logLevel) {
			this.name = name;
			this.logLevel = logLevel;
		}

		/**
		 * 返回级别名称。
		 *
		 * @return the level name 级别名称
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * 若可能则返回实际级别值。
		 *
		 * @return the level value 级别值
		 * @throws IllegalStateException if this is a {@link #isCustom() custom} level 自定义级别时
		 */
		public LogLevel getLevel() {
			Assert.state(this.logLevel != null, () -> "Unable to provide LogLevel for '" + this.name + "'");
			return this.logLevel;
		}

		/**
		 * 返回是否为无法用 {@link LogLevel} 表示的自定义级别。
		 *
		 * @return if this is a custom level 是否为自定义级别
		 */
		public boolean isCustom() {
			return this.logLevel == null;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			LevelConfiguration other = (LevelConfiguration) obj;
			return this.logLevel == other.logLevel && ObjectUtils.nullSafeEquals(this.name, other.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.logLevel, this.name);
		}

		@Override
		public String toString() {
			return "LevelConfiguration [name=" + this.name + ", logLevel=" + this.logLevel + "]";
		}

		/**
		 * 根据给定 {@link LogLevel} 创建新的 {@link LevelConfiguration} 实例。
		 *
		 * @param logLevel 日志级别
		 * @return a new {@link LevelConfiguration} instance 新实例
		 */
		public static LevelConfiguration of(LogLevel logLevel) {
			Assert.notNull(logLevel, "'logLevel' must not be null");
			return new LevelConfiguration(logLevel.name(), logLevel);
		}

		/**
		 * 为自定义级别名创建新的 {@link LevelConfiguration} 实例。
		 *
		 * @param name 日志级别名称
		 * @return a new {@link LevelConfiguration} instance 新实例
		 */
		public static LevelConfiguration ofCustom(String name) {
			Assert.hasText(name, "'name' must not be empty");
			return new LevelConfiguration(name, null);
		}

	}

}
