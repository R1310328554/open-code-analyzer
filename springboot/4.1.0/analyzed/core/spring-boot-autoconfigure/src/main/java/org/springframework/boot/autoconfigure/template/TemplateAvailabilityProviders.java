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

package org.springframework.boot.autoconfigure.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;

/**
 * {@link TemplateAvailabilityProvider} Bean 集合，用于检查哪个（若有）模板引擎支持给定视图。
 * 除非 {@code spring.template.provider.cache} 设为 {@code false}，否则缓存响应。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 1.4.0
 */
public class TemplateAvailabilityProviders {

	private final List<TemplateAvailabilityProvider> providers;

	private static final int CACHE_LIMIT = 1024;

	private static final TemplateAvailabilityProvider NONE = new NoTemplateAvailabilityProvider();

	/**
	 * 已解析的模板视图，无需全局锁即可返回已缓存实例。
	 */
	private final Map<String, TemplateAvailabilityProvider> resolved = new ConcurrentHashMap<>(CACHE_LIMIT);

	/**
	 * 从视图名到已解析模板提供者的映射，访问时同步。
	 */
	private final Map<String, TemplateAvailabilityProvider> cache = new LinkedHashMap<>(CACHE_LIMIT, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, TemplateAvailabilityProvider> eldest) {
			if (size() > CACHE_LIMIT) {
				TemplateAvailabilityProviders.this.resolved.remove(eldest.getKey());
				return true;
			}
			return false;
		}

	};

	/**
	 * 创建新的 {@link TemplateAvailabilityProviders} 实例。
	 * @param applicationContext 源应用上下文
	 */
	public TemplateAvailabilityProviders(ApplicationContext applicationContext) {
		this(getClassLoader(applicationContext));
	}

	private static ClassLoader getClassLoader(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "'applicationContext' must not be null");
		ClassLoader classLoader = applicationContext.getClassLoader();
		Assert.state(classLoader != null, "'classLoader' must not be null");
		return classLoader;
	}

	/**
	 * 创建新的 {@link TemplateAvailabilityProviders} 实例。
	 * @param classLoader 源类加载器
	 */
	public TemplateAvailabilityProviders(ClassLoader classLoader) {
		Assert.notNull(classLoader, "'classLoader' must not be null");
		this.providers = SpringFactoriesLoader.loadFactories(TemplateAvailabilityProvider.class, classLoader);
	}

	/**
	 * 创建新的 {@link TemplateAvailabilityProviders} 实例。
	 * @param providers 底层提供者
	 */
	protected TemplateAvailabilityProviders(Collection<? extends TemplateAvailabilityProvider> providers) {
		Assert.notNull(providers, "'providers' must not be null");
		this.providers = new ArrayList<>(providers);
	}

	/**
	 * 返回正在使用的底层提供者。
	 * @return 正在使用的提供者
	 */
	public List<TemplateAvailabilityProvider> getProviders() {
		return this.providers;
	}

	/**
	 * 获取可用于渲染给定视图的提供者。
	 * @param view 要渲染的视图
	 * @param applicationContext 应用上下文
	 * @return {@link TemplateAvailabilityProvider} 或 {@code null}
	 */
	public @Nullable TemplateAvailabilityProvider getProvider(String view, ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "'applicationContext' must not be null");
		ClassLoader classLoader = applicationContext.getClassLoader();
		Assert.state(classLoader != null, "'classLoader' must not be null");
		return getProvider(view, applicationContext.getEnvironment(), classLoader, applicationContext);
	}

	/**
	 * 获取可用于渲染给定视图的提供者。
	 * @param view 要渲染的视图
	 * @param environment 环境
	 * @param classLoader 类加载器
	 * @param resourceLoader 资源加载器
	 * @return {@link TemplateAvailabilityProvider} 或 {@code null}
	 */
	public @Nullable TemplateAvailabilityProvider getProvider(String view, Environment environment,
			ClassLoader classLoader, ResourceLoader resourceLoader) {
		Assert.notNull(view, "'view' must not be null");
		Assert.notNull(environment, "'environment' must not be null");
		Assert.notNull(classLoader, "'classLoader' must not be null");
		Assert.notNull(resourceLoader, "'resourceLoader' must not be null");
		Boolean useCache = environment.getProperty("spring.template.provider.cache", Boolean.class, true);
		if (!useCache) {
			return findProvider(view, environment, classLoader, resourceLoader);
		}
		TemplateAvailabilityProvider provider = this.resolved.get(view);
		if (provider == null) {
			synchronized (this.cache) {
				provider = findProvider(view, environment, classLoader, resourceLoader);
				provider = (provider != null) ? provider : NONE;
				this.resolved.put(view, provider);
				this.cache.put(view, provider);
			}
		}
		return (provider != NONE) ? provider : null;
	}

	private @Nullable TemplateAvailabilityProvider findProvider(String view, Environment environment,
			ClassLoader classLoader, ResourceLoader resourceLoader) {
		for (TemplateAvailabilityProvider candidate : this.providers) {
			if (candidate.isTemplateAvailable(view, environment, classLoader, resourceLoader)) {
				return candidate;
			}
		}
		return null;
	}

	private static final class NoTemplateAvailabilityProvider implements TemplateAvailabilityProvider {

		@Override
		public boolean isTemplateAvailable(String view, Environment environment, ClassLoader classLoader,
				ResourceLoader resourceLoader) {
			return false;
		}

	}

}
