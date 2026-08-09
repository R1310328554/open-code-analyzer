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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.env.PropertySourceInfo;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 可直接或间接向 {@link Environment} 贡献配置数据的单个元素。
 * 存在多种 {@link Kind 类型} 的贡献者，均为不可变，import 处理过程中会被新版本替换。
 * <p>
 * 贡献者可提供待处理的 import 集合，最终转为子节点。import 分两个阶段：
 * <ul>
 * <li>{@link ImportPhase#BEFORE_PROFILE_ACTIVATION profile 激活前}</li>
 * <li>{@link ImportPhase#AFTER_PROFILE_ACTIVATION profile 激活后}</li>
 * </ul>
 * 每个阶段中，<em>所有</em> import 会先解析再加载。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Nan Chiu
 */
class ConfigDataEnvironmentContributor implements Iterable<ConfigDataEnvironmentContributor> {

	private static final ConfigData.Options EMPTY_LOCATION_OPTIONS = ConfigData.Options
		.of(ConfigData.Option.IGNORE_IMPORTS);

	private final @Nullable ConfigDataLocation location;

	private final @Nullable ConfigDataResource resource;

	private final boolean fromProfileSpecificImport;

	private final @Nullable PropertySource<?> propertySource;

	private final @Nullable ConfigurationPropertySource configurationPropertySource;

	private final @Nullable ConfigDataProperties properties;

	private final ConfigData.Options configDataOptions;

	private final Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children;

	private final Kind kind;

	private final ConversionService conversionService;

	/**
	 * 创建新的 {@link ConfigDataEnvironmentContributor} 实例。
	 *
	 * @param kind 贡献者类型
	 * @param location 本贡献者的位置
	 * @param resource 贡献数据的资源，或 {@code null}
	 * @param fromProfileSpecificImport 是否来自 profile 特定 import
	 * @param propertySource 数据的属性源，或 {@code null}
	 * @param configurationPropertySource 数据的配置属性源，或 {@code null}
	 * @param properties 配置数据属性，或 {@code null}
	 * @param configDataOptions 应应用的配置数据选项
	 * @param children 各 {@link ImportPhase} 下的子贡献者
	 * @param conversionService 要使用的转换服务
	 */
	ConfigDataEnvironmentContributor(Kind kind, @Nullable ConfigDataLocation location,
			@Nullable ConfigDataResource resource, boolean fromProfileSpecificImport,
			@Nullable PropertySource<?> propertySource,
			@Nullable ConfigurationPropertySource configurationPropertySource,
			@Nullable ConfigDataProperties properties, ConfigData.@Nullable Options configDataOptions,
			@Nullable Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children,
			ConversionService conversionService) {
		this.kind = kind;
		this.location = location;
		this.resource = resource;
		this.fromProfileSpecificImport = fromProfileSpecificImport;
		this.properties = properties;
		this.propertySource = propertySource;
		this.configurationPropertySource = configurationPropertySource;
		this.configDataOptions = (configDataOptions != null) ? configDataOptions : ConfigData.Options.NONE;
		this.children = (children != null) ? children : Collections.emptyMap();
		this.conversionService = conversionService;
	}

	/**
	 * 返回贡献者类型。
	 *
	 * @return 贡献者类型
	 */
	Kind getKind() {
		return this.kind;
	}

	@Nullable ConfigDataLocation getLocation() {
		return this.location;
	}

	/**
	 * 返回本贡献者当前是否活动。
	 *
	 * @param activationContext 激活上下文
	 * @return 贡献者是否活动
	 */
	boolean isActive(@Nullable ConfigDataActivationContext activationContext) {
		if (this.kind == Kind.UNBOUND_IMPORT) {
			return false;
		}
		return this.properties == null || this.properties.isActive(activationContext);
	}

	/**
	 * 返回贡献本实例的资源。
	 *
	 * @return 资源，或 {@code null}
	 */
	@Nullable ConfigDataResource getResource() {
		return this.resource;
	}

	/**
	 * 返回贡献者是否来自 profile 特定 import。
	 *
	 * @return 是否为 profile 特定
	 */
	boolean isFromProfileSpecificImport() {
		return this.fromProfileSpecificImport;
	}

	/**
	 * 返回本贡献者的属性源。
	 *
	 * @return 属性源，或 {@code null}
	 */
	@Nullable PropertySource<?> getPropertySource() {
		return this.propertySource;
	}

	/**
	 * 返回本贡献者的配置属性源。
	 *
	 * @return 配置属性源，或 {@code null}
	 */
	@Nullable ConfigurationPropertySource getConfigurationPropertySource() {
		return this.configurationPropertySource;
	}

	/**
	 * 返回贡献者是否具有指定配置数据选项。
	 *
	 * @param option 要检查的选项
	 * @return 选项存在时为 {@code true}
	 */
	boolean hasConfigDataOption(ConfigData.Option option) {
		return this.configDataOptions.contains(option);
	}

	ConfigDataEnvironmentContributor withoutConfigDataOption(ConfigData.Option option) {
		return new ConfigDataEnvironmentContributor(this.kind, this.location, this.resource,
				this.fromProfileSpecificImport, this.propertySource, this.configurationPropertySource, this.properties,
				this.configDataOptions.without(option), this.children, this.conversionService);
	}

	/**
	 * 返回本贡献者请求的所有 import。
	 *
	 * @return import 列表
	 */
	List<ConfigDataLocation> getImports() {
		return (this.properties != null) ? this.properties.getImports() : Collections.emptyList();
	}

	/**
	 * 若本贡献者在给定阶段仍有未处理的 import 则返回 {@code true}。
	 *
	 * @param importPhase import 阶段
	 * @return 是否存在未处理的 import
	 */
	boolean hasUnprocessedImports(ImportPhase importPhase) {
		if (getImports().isEmpty()) {
			return false;
		}
		return !this.children.containsKey(importPhase);
	}

	/**
	 * 返回给定阶段下本贡献者的子节点。
	 *
	 * @param importPhase import 阶段
	 * @return 子节点列表
	 */
	List<ConfigDataEnvironmentContributor> getChildren(ImportPhase importPhase) {
		return this.children.getOrDefault(importPhase, Collections.emptyList());
	}

	/**
	 * 按优先级顺序遍历本贡献者及其所有子节点的 {@link Stream}。
	 *
	 * @return 流
	 */
	Stream<ConfigDataEnvironmentContributor> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * 按优先级顺序遍历本贡献者及其所有子节点的 {@link Iterator}。
	 *
	 * @return 迭代器
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ConfigDataEnvironmentContributor> iterator() {
		return new ContributorIterator();
	}

	/**
	 * 创建已绑定 {@link ConfigDataProperties} 的新 {@link ConfigDataEnvironmentContributor}。
	 *
	 * @param contributors 用于绑定的贡献者
	 * @param activationContext 激活上下文
	 * @return 新贡献者实例
	 */
	ConfigDataEnvironmentContributor withBoundProperties(Iterable<ConfigDataEnvironmentContributor> contributors,
			@Nullable ConfigDataActivationContext activationContext) {
		ConfigurationPropertySource configurationPropertySource = getConfigurationPropertySource();
		Assert.state(configurationPropertySource != null, "'configurationPropertySource' must not be null");
		Iterable<ConfigurationPropertySource> sources = Collections.singleton(configurationPropertySource);
		PlaceholdersResolver placeholdersResolver = new ConfigDataEnvironmentContributorPlaceholdersResolver(
				contributors, activationContext, this, true, this.conversionService);
		Binder binder = new Binder(sources, placeholdersResolver, null, null, null);
		ConfigDataProperties properties = ConfigDataProperties.get(binder);
		if (properties != null && this.configDataOptions.contains(ConfigData.Option.IGNORE_IMPORTS)) {
			properties = properties.withoutImports();
		}
		return new ConfigDataEnvironmentContributor(Kind.BOUND_IMPORT, this.location, this.resource,
				this.fromProfileSpecificImport, this.propertySource, this.configurationPropertySource, properties,
				this.configDataOptions, null, this.conversionService);
	}

	/**
	 * 为给定阶段创建带有新子节点集合的新 {@link ConfigDataEnvironmentContributor} 实例。
	 *
	 * @param importPhase import 阶段
	 * @param children 新子节点
	 * @return 新贡献者实例
	 */
	ConfigDataEnvironmentContributor withChildren(ImportPhase importPhase,
			List<ConfigDataEnvironmentContributor> children) {
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> updatedChildren = new LinkedHashMap<>(this.children);
		updatedChildren.put(importPhase, children);
		if (importPhase == ImportPhase.AFTER_PROFILE_ACTIVATION) {
			moveProfileSpecific(updatedChildren);
		}
		return new ConfigDataEnvironmentContributor(this.kind, this.location, this.resource,
				this.fromProfileSpecificImport, this.propertySource, this.configurationPropertySource, this.properties,
				this.configDataOptions, updatedChildren, this.conversionService);
	}

	private void moveProfileSpecific(Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children) {
		List<ConfigDataEnvironmentContributor> before = children.get(ImportPhase.BEFORE_PROFILE_ACTIVATION);
		if (!hasAnyProfileSpecificChildren(before)) {
			return;
		}
		List<ConfigDataEnvironmentContributor> updatedBefore = new ArrayList<>(before.size());
		List<ConfigDataEnvironmentContributor> updatedAfter = new ArrayList<>();
		for (ConfigDataEnvironmentContributor contributor : before) {
			updatedBefore.add(moveProfileSpecificChildren(contributor, updatedAfter));
		}
		updatedAfter.addAll(children.getOrDefault(ImportPhase.AFTER_PROFILE_ACTIVATION, Collections.emptyList()));
		children.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, updatedBefore);
		children.put(ImportPhase.AFTER_PROFILE_ACTIVATION, updatedAfter);
	}

	private ConfigDataEnvironmentContributor moveProfileSpecificChildren(ConfigDataEnvironmentContributor contributor,
			List<ConfigDataEnvironmentContributor> removed) {
		for (ImportPhase importPhase : ImportPhase.values()) {
			List<ConfigDataEnvironmentContributor> children = contributor.getChildren(importPhase);
			List<ConfigDataEnvironmentContributor> updatedChildren = new ArrayList<>(children.size());
			for (ConfigDataEnvironmentContributor child : children) {
				if (child.hasConfigDataOption(ConfigData.Option.PROFILE_SPECIFIC)) {
					removed.add(child.withoutConfigDataOption(ConfigData.Option.PROFILE_SPECIFIC));
				}
				else {
					updatedChildren.add(child);
				}
			}
			contributor = contributor.withChildren(importPhase, updatedChildren);
		}
		return contributor;
	}

	@Contract("null -> false")
	private boolean hasAnyProfileSpecificChildren(@Nullable List<ConfigDataEnvironmentContributor> contributors) {
		if (CollectionUtils.isEmpty(contributors)) {
			return false;
		}
		for (ConfigDataEnvironmentContributor contributor : contributors) {
			for (ImportPhase importPhase : ImportPhase.values()) {
				if (contributor.getChildren(importPhase)
					.stream()
					.anyMatch((child) -> child.hasConfigDataOption(ConfigData.Option.PROFILE_SPECIFIC))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 创建替换现有子节点的新 {@link ConfigDataEnvironmentContributor} 实例。
	 *
	 * @param existing 应被替换的现有节点
	 * @param replacement 替换节点
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	ConfigDataEnvironmentContributor withReplacement(ConfigDataEnvironmentContributor existing,
			ConfigDataEnvironmentContributor replacement) {
		if (this == existing) {
			return replacement;
		}
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> updatedChildren = new LinkedHashMap<>(
				this.children.size());
		this.children.forEach((importPhase, contributors) -> {
			List<ConfigDataEnvironmentContributor> updatedContributors = new ArrayList<>(contributors.size());
			for (ConfigDataEnvironmentContributor contributor : contributors) {
				updatedContributors.add(contributor.withReplacement(existing, replacement));
			}
			updatedChildren.put(importPhase, Collections.unmodifiableList(updatedContributors));
		});
		return new ConfigDataEnvironmentContributor(this.kind, this.location, this.resource,
				this.fromProfileSpecificImport, this.propertySource, this.configurationPropertySource, this.properties,
				this.configDataOptions, updatedChildren, this.conversionService);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		buildToString("", builder);
		return builder.toString();
	}

	private void buildToString(String prefix, StringBuilder builder) {
		builder.append(prefix);
		builder.append(this.kind);
		builder.append(" ");
		builder.append(this.location);
		builder.append(" ");
		builder.append(this.resource);
		builder.append(" ");
		builder.append(this.configDataOptions);
		builder.append("\n");
		for (ConfigDataEnvironmentContributor child : this.children.getOrDefault(ImportPhase.BEFORE_PROFILE_ACTIVATION,
				Collections.emptyList())) {
			child.buildToString(prefix + "    ", builder);
		}
		for (ConfigDataEnvironmentContributor child : this.children.getOrDefault(ImportPhase.AFTER_PROFILE_ACTIVATION,
				Collections.emptyList())) {
			child.buildToString(prefix + "    ", builder);
		}
	}

	/**
	 * 创建 {@link Kind#ROOT 根} 贡献者的工厂方法。
	 *
	 * @param contributors 根的直系子节点
	 * @param conversionService 要使用的转换服务
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	static ConfigDataEnvironmentContributor of(List<ConfigDataEnvironmentContributor> contributors,
			ConversionService conversionService) {
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children = new LinkedHashMap<>();
		children.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, Collections.unmodifiableList(contributors));
		return new ConfigDataEnvironmentContributor(Kind.ROOT, null, null, false, null, null, null, null, children,
				conversionService);
	}

	/**
	 * 创建 {@link Kind#INITIAL_IMPORT 初始 import} 贡献者的工厂方法。
	 * 用于触发额外贡献者的初始 import，本身不贡献任何属性。
	 *
	 * @param initialImports 初始 import 位置（占位符已解析）
	 * @param conversionService 要使用的转换服务
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	static ConfigDataEnvironmentContributor ofInitialImports(List<ConfigDataLocation> initialImports,
			ConversionService conversionService) {
		ConfigDataProperties properties = new ConfigDataProperties(initialImports, null);
		return new ConfigDataEnvironmentContributor(Kind.INITIAL_IMPORT, null, null, false, null, null, properties,
				null, null, conversionService);
	}

	/**
	 * 创建包装 {@link Kind#EXISTING 现有} 属性源的贡献者的工厂方法。
	 * 提供对现有属性的访问，但不主动 import 额外贡献者。
	 *
	 * @param propertySource 要包装的属性源
	 * @param conversionService 要使用的转换服务
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	static ConfigDataEnvironmentContributor ofExisting(PropertySource<?> propertySource,
			ConversionService conversionService) {
		return new ConfigDataEnvironmentContributor(Kind.EXISTING, null, null, false, propertySource,
				asConfigurationPropertySource(propertySource), null, null, null, conversionService);
	}

	/**
	 * 创建 {@link Kind#UNBOUND_IMPORT 未绑定 import} 贡献者的工厂方法。
	 * 该贡献者由其他贡献者主动 import，后续可能继续 import 更多贡献者。
	 *
	 * @param location 本贡献者的位置
	 * @param resource 配置数据资源
	 * @param profileSpecific 是否来自 profile 特定 import
	 * @param configData 配置数据
	 * @param propertySourceIndex 应使用的属性源索引
	 * @param conversionService 要使用的转换服务
	 * @param environmentUpdateListener 环境更新监听器
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	static ConfigDataEnvironmentContributor ofUnboundImport(@Nullable ConfigDataLocation location,
			@Nullable ConfigDataResource resource, boolean profileSpecific, ConfigData configData,
			int propertySourceIndex, ConversionService conversionService,
			ConfigDataEnvironmentUpdateListener environmentUpdateListener) {
		PropertySource<?> propertySource = configData.getPropertySources().get(propertySourceIndex);
		ConfigData.Options options = configData.getOptions(propertySource);
		options = environmentUpdateListener.onConfigDataOptions(configData, propertySource, options);
		return new ConfigDataEnvironmentContributor(Kind.UNBOUND_IMPORT, location, resource, profileSpecific,
				propertySource, asConfigurationPropertySource(propertySource), null, options, null, conversionService);
	}

	private static @Nullable ConfigurationPropertySource asConfigurationPropertySource(
			PropertySource<?> propertySource) {
		ConfigurationPropertySource configurationPropertySource = ConfigurationPropertySource.from(propertySource);
		if (configurationPropertySource != null && propertySource instanceof PropertySourceInfo propertySourceInfo) {
			configurationPropertySource = configurationPropertySource.withPrefix(propertySourceInfo.getPrefix());
		}
		return configurationPropertySource;
	}

	/**
	 * 创建 {@link Kind#EMPTY_LOCATION 空位置} 贡献者的工厂方法。
	 *
	 * @param location 本贡献者的位置
	 * @param profileSpecific 是否来自 profile 特定 import
	 * @param conversionService 要使用的转换服务
	 * @return 新的 {@link ConfigDataEnvironmentContributor} 实例
	 */
	static ConfigDataEnvironmentContributor ofEmptyLocation(ConfigDataLocation location, boolean profileSpecific,
			ConversionService conversionService) {
		return new ConfigDataEnvironmentContributor(Kind.EMPTY_LOCATION, location, null, profileSpecific, null, null,
				null, EMPTY_LOCATION_OPTIONS, null, conversionService);
	}

	/**
	 * 贡献者的各种类型。
	 */
	enum Kind {

		/**
		 * 包含初始子节点集合的根贡献者。
		 */
		ROOT,

		/**
		 * 需要处理的初始 import。
		 */
		INITIAL_IMPORT,

		/**
		 * 贡献属性但不贡献 import 的现有属性源。
		 */
		EXISTING,

		/**
		 * 从其他贡献者 import 了 {@link ConfigData} 但尚未绑定的贡献者。
		 */
		UNBOUND_IMPORT,

		/**
		 * 从其他贡献者 import 了 {@link ConfigData} 且已完成绑定的贡献者。
		 */
		BOUND_IMPORT,

		/**
		 * 有效但无可加载内容的位置。
		 */
		EMPTY_LOCATION

	}

	/**
	 * 获取 import 时可用的 import 阶段。
	 */
	enum ImportPhase {

		/**
		 * profile 激活前的阶段。
		 */
		BEFORE_PROFILE_ACTIVATION,

		/**
		 * profile 激活后的阶段。
		 */
		AFTER_PROFILE_ACTIVATION;

		/**
		 * 根据给定激活上下文返回 {@link ImportPhase}。
		 *
		 * @param activationContext 激活上下文
		 * @return import 阶段
		 */
		static ImportPhase get(@Nullable ConfigDataActivationContext activationContext) {
			if (activationContext != null && activationContext.getProfiles() != null) {
				return AFTER_PROFILE_ACTIVATION;
			}
			return BEFORE_PROFILE_ACTIVATION;
		}

	}

	/**
	 * 遍历贡献者树的迭代器。
	 */
	private final class ContributorIterator implements Iterator<ConfigDataEnvironmentContributor> {

		private @Nullable ImportPhase phase;

		private Iterator<ConfigDataEnvironmentContributor> children;

		private Iterator<ConfigDataEnvironmentContributor> current;

		private @Nullable ConfigDataEnvironmentContributor next;

		private ContributorIterator() {
			this.phase = ImportPhase.AFTER_PROFILE_ACTIVATION;
			this.children = getChildren(this.phase).iterator();
			this.current = Collections.emptyIterator();
		}

		@Override
		public boolean hasNext() {
			return fetchIfNecessary() != null;
		}

		@Override
		public ConfigDataEnvironmentContributor next() {
			ConfigDataEnvironmentContributor next = fetchIfNecessary();
			if (next == null) {
				throw new NoSuchElementException();
			}
			this.next = null;
			return next;
		}

		private @Nullable ConfigDataEnvironmentContributor fetchIfNecessary() {
			if (this.next != null) {
				return this.next;
			}
			if (this.current.hasNext()) {
				this.next = this.current.next();
				return this.next;
			}
			if (this.children.hasNext()) {
				this.current = this.children.next().iterator();
				return fetchIfNecessary();
			}
			if (this.phase == ImportPhase.AFTER_PROFILE_ACTIVATION) {
				this.phase = ImportPhase.BEFORE_PROFILE_ACTIVATION;
				this.children = getChildren(this.phase).iterator();
				return fetchIfNecessary();
			}
			if (this.phase == ImportPhase.BEFORE_PROFILE_ACTIVATION) {
				this.phase = null;
				this.next = ConfigDataEnvironmentContributor.this;
				return this.next;
			}
			return null;
		}

	}

}
