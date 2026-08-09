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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.log.LogMessage;

/**
 * 通过 {@link ConfigDataLocationResolver 解析} 并 {@link ConfigDataLoader 加载} 位置来导入 {@link ConfigData}。
 * 跟踪 {@link ConfigDataResource 资源} 以避免重复导入。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class ConfigDataImporter {

	private final Log logger;

	private final ConfigDataLocationResolvers resolvers;

	private final ConfigDataLoaders loaders;

	private final ConfigDataNotFoundAction notFoundAction;

	private final Set<ConfigDataResource> loaded = new HashSet<>();

	private final Set<ConfigDataLocation> loadedLocations = new HashSet<>();

	private final Set<ConfigDataLocation> optionalLocations = new HashSet<>();

	/**
	 * 创建新的 {@link ConfigDataImporter} 实例。
	 *
	 * @param logFactory 日志工厂
	 * @param notFoundAction 找不到位置时的处理动作
	 * @param resolvers 配置数据位置解析器
	 * @param loaders 配置数据加载器
	 */
	ConfigDataImporter(DeferredLogFactory logFactory, ConfigDataNotFoundAction notFoundAction,
			ConfigDataLocationResolvers resolvers, ConfigDataLoaders loaders) {
		this.logger = logFactory.getLog(getClass());
		this.resolvers = resolvers;
		this.loaders = loaders;
		this.notFoundAction = notFoundAction;
	}

	/**
	 * 解析并加载给定位置列表，过滤已加载项。
	 *
	 * @param activationContext 激活上下文
	 * @param locationResolverContext 位置解析器上下文
	 * @param loaderContext 加载器上下文
	 * @param locations 要解析的位置
	 * @return 已加载位置与数据的映射
	 */
	Map<ConfigDataResolutionResult, ConfigData> resolveAndLoad(@Nullable ConfigDataActivationContext activationContext,
			ConfigDataLocationResolverContext locationResolverContext, ConfigDataLoaderContext loaderContext,
			List<ConfigDataLocation> locations) {
		try {
			Profiles profiles = (activationContext != null) ? activationContext.getProfiles() : null;
			List<ConfigDataResolutionResult> resolved = resolve(locationResolverContext, profiles, locations);
			return load(loaderContext, resolved);
		}
		catch (IOException ex) {
			throw new IllegalStateException("IO error on loading imports from " + locations, ex);
		}
	}

	private List<ConfigDataResolutionResult> resolve(ConfigDataLocationResolverContext locationResolverContext,
			@Nullable Profiles profiles, List<ConfigDataLocation> locations) {
		List<ConfigDataResolutionResult> resolved = new ArrayList<>(locations.size());
		for (ConfigDataLocation location : locations) {
			resolved.addAll(resolve(locationResolverContext, profiles, location));
		}
		return Collections.unmodifiableList(resolved);
	}

	private List<ConfigDataResolutionResult> resolve(ConfigDataLocationResolverContext locationResolverContext,
			@Nullable Profiles profiles, ConfigDataLocation location) {
		try {
			return this.resolvers.resolve(locationResolverContext, location, profiles);
		}
		catch (ConfigDataNotFoundException ex) {
			handle(ex, location, null);
			return Collections.emptyList();
		}
	}

	private Map<ConfigDataResolutionResult, ConfigData> load(ConfigDataLoaderContext loaderContext,
			List<ConfigDataResolutionResult> candidates) throws IOException {
		Map<ConfigDataResolutionResult, ConfigData> result = new LinkedHashMap<>();
		for (int i = candidates.size() - 1; i >= 0; i--) {
			ConfigDataResolutionResult candidate = candidates.get(i);
			ConfigDataLocation location = candidate.getLocation();
			ConfigDataResource resource = candidate.getResource();
			this.logger.trace(LogMessage.format("Considering resource %s from location %s", resource, location));
			if (resource.isOptional()) {
				this.optionalLocations.add(location);
			}
			if (this.loaded.contains(resource)) {
				this.logger
					.trace(LogMessage.format("Already loaded resource %s ignoring location %s", resource, location));
				this.loadedLocations.add(location);
			}
			else {
				try {
					ConfigData loaded = this.loaders.load(loaderContext, resource);
					if (loaded != null) {
						this.logger.trace(LogMessage.format("Loaded resource %s from location %s", resource, location));
						this.loaded.add(resource);
						this.loadedLocations.add(location);
						result.put(candidate, loaded);
					}
				}
				catch (ConfigDataNotFoundException ex) {
					handle(ex, location, resource);
				}
			}
		}
		return Collections.unmodifiableMap(result);
	}

	private void handle(ConfigDataNotFoundException ex, ConfigDataLocation location,
			@Nullable ConfigDataResource resource) {
		if (ex instanceof ConfigDataResourceNotFoundException notFoundException) {
			ex = notFoundException.withLocation(location);
		}
		getNotFoundAction(location, resource).handle(this.logger, ex);
	}

	private ConfigDataNotFoundAction getNotFoundAction(ConfigDataLocation location,
			@Nullable ConfigDataResource resource) {
		if (location.isOptional() || (resource != null && resource.isOptional())) {
			return ConfigDataNotFoundAction.IGNORE;
		}
		return this.notFoundAction;
	}

	Set<ConfigDataLocation> getLoadedLocations() {
		return this.loadedLocations;
	}

	Set<ConfigDataLocation> getOptionalLocations() {
		return this.optionalLocations;
	}

}
