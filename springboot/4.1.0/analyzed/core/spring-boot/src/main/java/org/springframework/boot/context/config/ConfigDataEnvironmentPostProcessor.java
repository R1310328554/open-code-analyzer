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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * 加载并将 {@link ConfigData} 应用到 Spring {@link Environment} 的 {@link EnvironmentPostProcessor}。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Nguyen Bao Sach
 * @since 2.4.0
 */
public class ConfigDataEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/**
	 * 处理器的默认顺序。
	 */
	public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	/**
	 * 抛出 {@code ConfigDataLocationNotFoundException} 时决定采取何种动作的属性。
	 * @see ConfigDataNotFoundAction
	 */
	public static final String ON_LOCATION_NOT_FOUND_PROPERTY = ConfigDataEnvironment.ON_NOT_FOUND_PROPERTY;

	private final DeferredLogFactory logFactory;

	private final Log logger;

	private final ConfigurableBootstrapContext bootstrapContext;

	private final @Nullable ConfigDataEnvironmentUpdateListener environmentUpdateListener;

	public ConfigDataEnvironmentPostProcessor(DeferredLogFactory logFactory,
			ConfigurableBootstrapContext bootstrapContext) {
		this(logFactory, bootstrapContext, null);
	}

	private ConfigDataEnvironmentPostProcessor(DeferredLogFactory logFactory,
			ConfigurableBootstrapContext bootstrapContext,
			@Nullable ConfigDataEnvironmentUpdateListener environmentUpdateListener) {
		this.logFactory = logFactory;
		this.logger = logFactory.getLog(getClass());
		this.bootstrapContext = bootstrapContext;
		this.environmentUpdateListener = environmentUpdateListener;
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		postProcessEnvironment(environment, application.getResourceLoader(), application.getAdditionalProfiles());
	}

	void postProcessEnvironment(ConfigurableEnvironment environment, @Nullable ResourceLoader resourceLoader,
			Collection<String> additionalProfiles) {
		this.logger.trace("Post-processing environment to add config data");
		resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
		getConfigDataEnvironment(environment, resourceLoader, additionalProfiles).processAndApply();
	}

	ConfigDataEnvironment getConfigDataEnvironment(ConfigurableEnvironment environment, ResourceLoader resourceLoader,
			Collection<String> additionalProfiles) {
		return new ConfigDataEnvironment(this.logFactory, this.bootstrapContext, environment, resourceLoader,
				additionalProfiles, this.environmentUpdateListener);
	}

	/**
	 * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。
	 * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。
	 *
	 * @param environment 要应用 {@link ConfigData} 的环境
	 */
	public static void applyTo(ConfigurableEnvironment environment) {
		applyTo(environment, null, null, Collections.emptyList());
	}

	/**
	 * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。
	 * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。
	 *
	 * @param environment 要应用 {@link ConfigData} 的环境
	 * @param resourceLoader 要使用的资源加载器
	 * @param bootstrapContext 引导上下文；{@code null} 时使用临时上下文
	 * @param additionalProfiles 要额外应用的 profile
	 */
	public static void applyTo(ConfigurableEnvironment environment, ResourceLoader resourceLoader,
			@Nullable ConfigurableBootstrapContext bootstrapContext, String... additionalProfiles) {
		applyTo(environment, resourceLoader, bootstrapContext, Arrays.asList(additionalProfiles));
	}

	/**
	 * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。
	 * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。
	 *
	 * @param environment 要应用 {@link ConfigData} 的环境
	 * @param resourceLoader 要使用的资源加载器
	 * @param bootstrapContext 引导上下文；{@code null} 时使用临时上下文
	 * @param additionalProfiles 要额外应用的 profile
	 */
	public static void applyTo(ConfigurableEnvironment environment, @Nullable ResourceLoader resourceLoader,
			@Nullable ConfigurableBootstrapContext bootstrapContext, Collection<String> additionalProfiles) {
		DeferredLogFactory logFactory = Supplier::get;
		bootstrapContext = (bootstrapContext != null) ? bootstrapContext : new DefaultBootstrapContext();
		ConfigDataEnvironmentPostProcessor postProcessor = new ConfigDataEnvironmentPostProcessor(logFactory,
				bootstrapContext);
		postProcessor.postProcessEnvironment(environment, resourceLoader, additionalProfiles);
	}

	/**
	 * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。
	 * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。
	 *
	 * @param environment 要应用 {@link ConfigData} 的环境
	 * @param resourceLoader 要使用的资源加载器
	 * @param bootstrapContext 引导上下文；{@code null} 时使用临时上下文
	 * @param additionalProfiles 要额外应用的 profile
	 * @param environmentUpdateListener 可选的 {@link ConfigDataEnvironmentUpdateListener}，用于跟踪 {@link Environment} 更新
	 */
	public static void applyTo(ConfigurableEnvironment environment, @Nullable ResourceLoader resourceLoader,
			@Nullable ConfigurableBootstrapContext bootstrapContext, Collection<String> additionalProfiles,
			ConfigDataEnvironmentUpdateListener environmentUpdateListener) {
		DeferredLogFactory logFactory = Supplier::get;
		bootstrapContext = (bootstrapContext != null) ? bootstrapContext : new DefaultBootstrapContext();
		ConfigDataEnvironmentPostProcessor postProcessor = new ConfigDataEnvironmentPostProcessor(logFactory,
				bootstrapContext, environmentUpdateListener);
		postProcessor.postProcessEnvironment(environment, resourceLoader, additionalProfiles);
	}

}
