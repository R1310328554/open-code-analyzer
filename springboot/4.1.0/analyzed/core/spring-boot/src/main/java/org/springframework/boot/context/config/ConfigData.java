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

package org.springframework.boot.context.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * 从 {@link ConfigDataResource} 加载的配置数据，最终可向 Spring {@link Environment}
 * 贡献 {@link PropertySource 属性源}。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 * @see ConfigDataLocationResolver
 * @see ConfigDataLoader
 */
public final class ConfigData {

	private final List<PropertySource<?>> propertySources;

	private final PropertySourceOptions propertySourceOptions;

	/**
	 * 不含任何数据的 {@link ConfigData} 实例。
	 */
	public static final ConfigData EMPTY = new ConfigData(Collections.emptySet());

	/**
	 * 创建新的 {@link ConfigData} 实例，对每个源应用相同选项。
	 *
	 * @param propertySources 按优先级升序排列的配置数据属性源
	 * @param options 应用于每个源的配置数据选项
	 * @see #ConfigData(Collection, PropertySourceOptions)
	 */
	public ConfigData(Collection<? extends PropertySource<?>> propertySources, Option... options) {
		this(propertySources, PropertySourceOptions.always(Options.of(options)));
	}

	/**
	 * 创建新的 {@link ConfigData} 实例，指定属性源选项。
	 *
	 * @param propertySources 按优先级升序排列的配置数据属性源
	 * @param propertySourceOptions 属性源选项
	 * @since 2.4.5
	 */
	public ConfigData(Collection<? extends PropertySource<?>> propertySources,
			PropertySourceOptions propertySourceOptions) {
		Assert.notNull(propertySources, "'propertySources' must not be null");
		Assert.notNull(propertySourceOptions, "'propertySourceOptions' must not be null");
		this.propertySources = Collections.unmodifiableList(new ArrayList<>(propertySources));
		this.propertySourceOptions = propertySourceOptions;
	}

	/**
	 * 按优先级升序返回配置数据属性源；若多个源含相同键，后出现的源优先。
	 *
	 * @return 配置数据属性源
	 */
	public List<PropertySource<?>> getPropertySources() {
		return this.propertySources;
	}

	/**
	 * 返回应用于给定源的 {@link Options 配置数据选项}。
	 *
	 * @param propertySource 要检查的属性源
	 * @return 适用的选项
	 * @since 2.4.5
	 */
	public Options getOptions(PropertySource<?> propertySource) {
		Options options = this.propertySourceOptions.get(propertySource);
		return (options != null) ? options : Options.NONE;
	}

	/**
	 * 为给定 {@link PropertySource} 提供 {@link Options} 的策略接口。
	 *
	 * @since 2.4.5
	 */
	@FunctionalInterface
	public interface PropertySourceOptions {

		/**
		 * 始终返回 {@link Options#NONE} 的 {@link PropertySourceOptions} 实例。
		 * @since 2.4.6
		 */
		PropertySourceOptions ALWAYS_NONE = new AlwaysPropertySourceOptions(Options.NONE);

		/**
		 * 返回应用于给定属性源的选项。
		 *
		 * @param propertySource 属性源
		 * @return 要应用的选项
		 */
		@Nullable Options get(PropertySource<?> propertySource);

		/**
		 * 创建始终返回相同选项（与属性源无关）的 {@link PropertySourceOptions} 实例。
		 *
		 * @param options 要返回的选项
		 * @return 新的 {@link PropertySourceOptions} 实例
		 */
		static PropertySourceOptions always(Option... options) {
			return always(Options.of(options));
		}

		/**
		 * Create a new {@link PropertySourceOptions} instance that always returns the
		 * same options regardless of the property source.
		 * @param options the options to return
		 * @return a new {@link PropertySourceOptions} instance
		 */
		static PropertySourceOptions always(Options options) {
			if (options == Options.NONE) {
				return ALWAYS_NONE;
			}
			return new AlwaysPropertySourceOptions(options);
		}

	}

	/**
	 * 始终返回相同结果的 {@link PropertySourceOptions}。
	 */
	private static class AlwaysPropertySourceOptions implements PropertySourceOptions {

		private final Options options;

		AlwaysPropertySourceOptions(Options options) {
			this.options = options;
		}

		@Override
		public Options get(PropertySource<?> propertySource) {
			return this.options;
		}

	}

	/**
	 * {@link Option} 标志集合。
	 *
	 * @since 2.4.5
	 */
	public static final class Options {

		/**
		 * 无选项。
		 */
		public static final Options NONE = new Options(Collections.emptySet());

		private final Set<Option> options;

		private Options(Set<Option> options) {
			this.options = Collections.unmodifiableSet(options);
		}

		Set<Option> asSet() {
			return this.options;
		}

		/**
		 * 判断本集合是否包含给定选项。
		 *
		 * @param option 要检查的选项
		 * @return 选项存在时为 {@code true}
		 */
		public boolean contains(Option option) {
			return this.options.contains(option);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Options other = (Options) obj;
			return this.options.equals(other.options);
		}

		@Override
		public int hashCode() {
			return this.options.hashCode();
		}

		@Override
		public String toString() {
			return this.options.toString();
		}

		/**
		 * 创建新 {@link Options} 实例，包含本集合中除给定选项外的所有选项。
		 *
		 * @param option 要排除的选项
		 * @return 新的 {@link Options} 实例
		 */
		public Options without(Option option) {
			return copy((options) -> options.remove(option));
		}

		/**
		 * 创建新 {@link Options} 实例，包含本集合及给定选项。
		 *
		 * @param option 要包含的选项
		 * @return 新的 {@link Options} 实例
		 */
		public Options with(Option option) {
			return copy((options) -> options.add(option));
		}

		private Options copy(Consumer<EnumSet<Option>> processor) {
			EnumSet<Option> options = (!this.options.isEmpty()) ? EnumSet.copyOf(this.options)
					: EnumSet.noneOf(Option.class);
			processor.accept(options);
			return new Options(options);
		}

		/**
		 * 使用给定 {@link Option} 值创建新实例。
		 *
		 * @param options 要包含的选项
		 * @return 新的 {@link Options} 实例
		 */
		public static Options of(Option... options) {
			Assert.notNull(options, "'options' must not be null");
			if (options.length == 0) {
				return NONE;
			}
			return new Options(EnumSet.copyOf(Arrays.asList(options)));
		}

	}

	/**
	 * 可应用的选项标志。
	 */
	public enum Option {

		/**
		 * 忽略源中所有 import 相关属性。
		 */
		IGNORE_IMPORTS,

		/**
		 * 忽略所有 profile 激活与 include 相关属性。
		 * @since 2.4.3
		 */
		IGNORE_PROFILES,

		/**
		 * 表示源为「profile 特定」，应在 profile 特定同级 import 之后包含。
		 * @since 2.4.5
		 */
		PROFILE_SPECIFIC

	}

}
