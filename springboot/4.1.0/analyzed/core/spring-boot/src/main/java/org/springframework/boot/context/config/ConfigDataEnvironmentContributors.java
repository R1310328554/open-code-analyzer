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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.ImportPhase;
import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.Kind;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.log.LogMessage;
import org.springframework.util.ObjectUtils;

/**
 * 用于处理 import 的 {@link ConfigDataEnvironmentContributor} 不可变树结构。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class ConfigDataEnvironmentContributors implements Iterable<ConfigDataEnvironmentContributor> {

	private static final Predicate<ConfigDataEnvironmentContributor> NO_CONTRIBUTOR_FILTER = (contributor) -> true;

	private final Log logger;

	private final ConfigDataEnvironmentContributor root;

	private final ConfigurableBootstrapContext bootstrapContext;

	private final ConversionService conversionService;

	private final ConfigDataEnvironmentUpdateListener environmentUpdateListener;

	/**
	 * 创建新的 {@link ConfigDataEnvironmentContributors} 实例。
	 *
	 * @param logFactory 日志工厂
	 * @param bootstrapContext 引导上下文
	 * @param contributors 初始贡献者集合
	 * @param conversionService 要使用的转换服务
	 * @param environmentUpdateListener 环境更新监听器
	 */
	ConfigDataEnvironmentContributors(DeferredLogFactory logFactory, ConfigurableBootstrapContext bootstrapContext,
			List<ConfigDataEnvironmentContributor> contributors, ConversionService conversionService,
			ConfigDataEnvironmentUpdateListener environmentUpdateListener) {
		this.logger = logFactory.getLog(getClass());
		this.bootstrapContext = bootstrapContext;
		this.root = ConfigDataEnvironmentContributor.of(contributors, conversionService);
		this.conversionService = conversionService;
		this.environmentUpdateListener = environmentUpdateListener;
	}

	private ConfigDataEnvironmentContributors(Log logger, ConfigurableBootstrapContext bootstrapContext,
			ConfigDataEnvironmentContributor root, ConversionService conversionService,
			ConfigDataEnvironmentUpdateListener environmentUpdateListener) {
		this.logger = logger;
		this.bootstrapContext = bootstrapContext;
		this.root = root;
		this.conversionService = conversionService;
		this.environmentUpdateListener = environmentUpdateListener;
	}

	/**
	 * 处理所有活动贡献者的 import 并返回新的 {@link ConfigDataEnvironmentContributors} 实例。
	 *
	 * @param importer 用于导入 {@link ConfigData} 的导入器
	 * @param activationContext 当前激活上下文；尚未创建时为 {@code null}
	 * @return 已处理所有相关 import 的 {@link ConfigDataEnvironmentContributors} 实例
	 */
	ConfigDataEnvironmentContributors withProcessedImports(ConfigDataImporter importer,
			@Nullable ConfigDataActivationContext activationContext) {
		ImportPhase importPhase = ImportPhase.get(activationContext);
		this.logger.trace(LogMessage.format("Processing imports for phase %s. %s", importPhase,
				(activationContext != null) ? activationContext : "no activation context"));
		ConfigDataEnvironmentContributors result = this;
		int processed = 0;
		while (true) {
			ConfigDataEnvironmentContributor contributor = getNextToProcess(result, activationContext, importPhase);
			if (contributor == null) {
				this.logger.trace(LogMessage.format("Processed imports for of %d contributors", processed));
				return result;
			}
			if (contributor.getKind() == Kind.UNBOUND_IMPORT) {
				ConfigDataEnvironmentContributor bound = contributor.withBoundProperties(result, activationContext);
				result = new ConfigDataEnvironmentContributors(this.logger, this.bootstrapContext,
						result.getRoot().withReplacement(contributor, bound), this.conversionService,
						this.environmentUpdateListener);
				continue;
			}
			ConfigDataLocationResolverContext locationResolverContext = new ContributorConfigDataLocationResolverContext(
					result, contributor, activationContext);
			ConfigDataLoaderContext loaderContext = new ContributorDataLoaderContext(this);
			List<ConfigDataLocation> imports = contributor.getImports();
			this.logger.trace(LogMessage.format("Processing imports %s", imports));
			Map<ConfigDataResolutionResult, ConfigData> imported = importer.resolveAndLoad(activationContext,
					locationResolverContext, loaderContext, imports);
			this.logger.trace(LogMessage.of(() -> getImportedMessage(imported.keySet())));
			ConfigDataEnvironmentContributor contributorAndChildren = contributor.withChildren(importPhase,
					asContributors(imported));
			result = new ConfigDataEnvironmentContributors(this.logger, this.bootstrapContext,
					result.getRoot().withReplacement(contributor, contributorAndChildren), this.conversionService,
					this.environmentUpdateListener);
			processed++;
		}
	}

	private CharSequence getImportedMessage(Set<ConfigDataResolutionResult> results) {
		if (results.isEmpty()) {
			return "Nothing imported";
		}
		StringBuilder message = new StringBuilder();
		message.append("Imported " + results.size() + " resource" + ((results.size() != 1) ? "s " : " "));
		message.append(results.stream().map(ConfigDataResolutionResult::getResource).toList());
		return message;
	}

	protected final ConfigurableBootstrapContext getBootstrapContext() {
		return this.bootstrapContext;
	}

	private @Nullable ConfigDataEnvironmentContributor getNextToProcess(ConfigDataEnvironmentContributors contributors,
			@Nullable ConfigDataActivationContext activationContext, ImportPhase importPhase) {
		for (ConfigDataEnvironmentContributor contributor : contributors.getRoot()) {
			if (contributor.getKind() == Kind.UNBOUND_IMPORT
					|| isActiveWithUnprocessedImports(activationContext, importPhase, contributor)) {
				return contributor;
			}
		}
		return null;
	}

	private boolean isActiveWithUnprocessedImports(@Nullable ConfigDataActivationContext activationContext,
			ImportPhase importPhase, ConfigDataEnvironmentContributor contributor) {
		return contributor.isActive(activationContext) && contributor.hasUnprocessedImports(importPhase);
	}

	private List<ConfigDataEnvironmentContributor> asContributors(
			Map<ConfigDataResolutionResult, ConfigData> imported) {
		List<ConfigDataEnvironmentContributor> contributors = new ArrayList<>(imported.size() * 5);
		imported.forEach((resolutionResult, data) -> {
			ConfigDataLocation location = resolutionResult.getLocation();
			ConfigDataResource resource = resolutionResult.getResource();
			boolean profileSpecific = resolutionResult.isProfileSpecific();
			if (data.getPropertySources().isEmpty()) {
				contributors.add(ConfigDataEnvironmentContributor.ofEmptyLocation(location, profileSpecific,
						this.conversionService));
			}
			else {
				for (int i = data.getPropertySources().size() - 1; i >= 0; i--) {
					contributors.add(ConfigDataEnvironmentContributor.ofUnboundImport(location, resource,
							profileSpecific, data, i, this.conversionService, this.environmentUpdateListener));
				}
			}
		});
		return Collections.unmodifiableList(contributors);
	}

	/**
	 * 返回根贡献者。
	 *
	 * @return 根贡献者
	 */
	ConfigDataEnvironmentContributor getRoot() {
		return this.root;
	}

	/**
	 * 返回由贡献者支持的 {@link Binder}。
	 *
	 * @param activationContext 激活上下文
	 * @param options 要应用的绑定器选项
	 * @return 绑定器实例
	 */
	Binder getBinder(@Nullable ConfigDataActivationContext activationContext, BinderOption... options) {
		return getBinder(activationContext, NO_CONTRIBUTOR_FILTER, options);
	}

	/**
	 * 返回由贡献者支持的 {@link Binder}。
	 *
	 * @param activationContext 激活上下文
	 * @param filter 用于限制贡献者的过滤器
	 * @param options 要应用的绑定器选项
	 * @return 绑定器实例
	 */
	Binder getBinder(@Nullable ConfigDataActivationContext activationContext,
			Predicate<ConfigDataEnvironmentContributor> filter, BinderOption... options) {
		return getBinder(activationContext, filter, asBinderOptionsSet(options));
	}

	private Set<BinderOption> asBinderOptionsSet(BinderOption... options) {
		return ObjectUtils.isEmpty(options) ? EnumSet.noneOf(BinderOption.class)
				: EnumSet.copyOf(Arrays.asList(options));
	}

	private Binder getBinder(@Nullable ConfigDataActivationContext activationContext,
			Predicate<ConfigDataEnvironmentContributor> filter, Set<BinderOption> options) {
		boolean failOnInactiveSource = options.contains(BinderOption.FAIL_ON_BIND_TO_INACTIVE_SOURCE);
		Iterable<ConfigurationPropertySource> sources = () -> getBinderSources(
				filter.and((contributor) -> failOnInactiveSource || contributor.isActive(activationContext)));
		PlaceholdersResolver placeholdersResolver = new ConfigDataEnvironmentContributorPlaceholdersResolver(this.root,
				activationContext, null, failOnInactiveSource, this.conversionService);
		BindHandler bindHandler = !failOnInactiveSource ? null : new InactiveSourceChecker(activationContext);
		return new Binder(sources, placeholdersResolver, null, null, bindHandler);
	}

	private Iterator<ConfigurationPropertySource> getBinderSources(Predicate<ConfigDataEnvironmentContributor> filter) {
		return this.root.stream()
			.filter(this::hasConfigurationPropertySource)
			.filter(filter)
			.map(ConfigDataEnvironmentContributor::getConfigurationPropertySource)
			.iterator();
	}

	private boolean hasConfigurationPropertySource(ConfigDataEnvironmentContributor contributor) {
		return contributor.getConfigurationPropertySource() != null;
	}

	@Override
	public Iterator<ConfigDataEnvironmentContributor> iterator() {
		return this.root.iterator();
	}

	/**
	 * {@link ConfigDataLocationResolverContext} for a contributor.
	 */
	/**
	 * 贡献者对应的 {@link ConfigDataLoaderContext}。
	 */
	private static class ContributorDataLoaderContext implements ConfigDataLoaderContext {

		private final ConfigDataEnvironmentContributors contributors;

		ContributorDataLoaderContext(ConfigDataEnvironmentContributors contributors) {
			this.contributors = contributors;
		}

		@Override
		public ConfigurableBootstrapContext getBootstrapContext() {
			return this.contributors.getBootstrapContext();
		}

	}

	/**
	 * {@link ConfigDataLocationResolverContext} for a contributor.
	 */
	/**
	 * 贡献者对应的 {@link ConfigDataLocationResolverContext}。
	 */
	private static class ContributorConfigDataLocationResolverContext implements ConfigDataLocationResolverContext {

		private final ConfigDataEnvironmentContributors contributors;

		private final ConfigDataEnvironmentContributor contributor;

		private final @Nullable ConfigDataActivationContext activationContext;

		private volatile @Nullable Binder binder;

		ContributorConfigDataLocationResolverContext(ConfigDataEnvironmentContributors contributors,
				ConfigDataEnvironmentContributor contributor, @Nullable ConfigDataActivationContext activationContext) {
			this.contributors = contributors;
			this.contributor = contributor;
			this.activationContext = activationContext;
		}

		@Override
		public Binder getBinder() {
			Binder binder = this.binder;
			if (binder == null) {
				binder = this.contributors.getBinder(this.activationContext);
				this.binder = binder;
			}
			return binder;
		}

		@Override
		public @Nullable ConfigDataResource getParent() {
			return this.contributor.getResource();
		}

		@Override
		public ConfigurableBootstrapContext getBootstrapContext() {
			return this.contributors.getBootstrapContext();
		}

	}

	private class InactiveSourceChecker implements BindHandler {

		private final @Nullable ConfigDataActivationContext activationContext;

		InactiveSourceChecker(@Nullable ConfigDataActivationContext activationContext) {
			this.activationContext = activationContext;
		}

		@Override
		public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
				Object result) {
			for (ConfigDataEnvironmentContributor contributor : ConfigDataEnvironmentContributors.this) {
				if (!contributor.isActive(this.activationContext)) {
					InactiveConfigDataAccessException.throwIfPropertyFound(contributor, name);
				}
			}
			return result;
		}

	}

	/**
	 * 可与 {@link ConfigDataEnvironmentContributors#getBinder(ConfigDataActivationContext, BinderOption...)} 配合使用的绑定器选项。
	 */
	enum BinderOption {

		/**
		 * 非活动贡献者包含绑定值时抛出异常。
		 */
		FAIL_ON_BIND_TO_INACTIVE_SOURCE

	}

}
