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

package org.springframework.beans.factory.aot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * AOT 服务集合，可通过 {@link Loader} 从
 * {@link SpringFactoriesLoader} 加载，或从 {@link ListableBeanFactory} 获取。
 *
 * @author Phillip Webb
 * @since 6.0
 * @param <T> 服务类型
 */
public final class AotServices<T> implements Iterable<T> {

	/**
	 * 查找 AOT 工厂配置的资源位置。
	 */
	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring/aot.factories";

	/** 已排序的 AOT 服务列表。 */
	private final List<T> services;

	/** 从 Bean 工厂加载的服务，按 bean 名称索引。 */
	private final Map<String, T> beans;

	/** 各服务实例的来源映射。 */
	private final Map<T, Source> sources;


	private AotServices(List<T> loaded, Map<String, T> beans) {
		this.services = collectServices(loaded, beans);
		this.sources = collectSources(loaded, beans.values());
		this.beans = beans;
	}

	private List<T> collectServices(List<T> loaded, Map<String, T> beans) {
		List<T> services = new ArrayList<>();
		services.addAll(beans.values());
		services.addAll(loaded);
		AnnotationAwareOrderComparator.sort(services);
		return Collections.unmodifiableList(services);
	}

	private Map<T, Source> collectSources(Collection<T> loaded, Collection<T> beans) {
		Map<T, Source> sources = new IdentityHashMap<>();
		loaded.forEach(service -> sources.put(service, Source.SPRING_FACTORIES_LOADER));
		beans.forEach(service -> sources.put(service, Source.BEAN_FACTORY));
		return Collections.unmodifiableMap(sources);
	}

	/**
	 * 创建新的 {@link Loader}，从 {@value #FACTORIES_RESOURCE_LOCATION} 获取 AOT 服务。
	 * @return 新的 {@link Loader} 实例
	 */
	public static Loader factories() {
		return factories((ClassLoader) null);
	}

	/**
	 * 创建新的 {@link Loader}，从 {@value #FACTORIES_RESOURCE_LOCATION} 获取 AOT 服务。
	 * @param classLoader 用于加载工厂资源的类加载器
	 * @return 新的 {@link Loader} 实例
	 */
	public static Loader factories(@Nullable ClassLoader classLoader) {
		return factories(getSpringFactoriesLoader(classLoader));
	}

	/**
	 * 创建新的 {@link Loader}，从给定的 {@link SpringFactoriesLoader} 获取 AOT 服务。
	 * @param springFactoriesLoader Spring 工厂加载器
	 * @return 新的 {@link Loader} 实例
	 */
	public static Loader factories(SpringFactoriesLoader springFactoriesLoader) {
		Assert.notNull(springFactoriesLoader, "'springFactoriesLoader' must not be null");
		return new Loader(springFactoriesLoader, null);
	}

	/**
	 * 创建新的 {@link Loader}，从 {@value #FACTORIES_RESOURCE_LOCATION} 以及
	 * 给定的 {@link ListableBeanFactory} 获取 AOT 服务。
	 * @param beanFactory bean 工厂
	 * @return 新的 {@link Loader} 实例
	 */
	public static Loader factoriesAndBeans(ListableBeanFactory beanFactory) {
		ClassLoader classLoader = (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory ?
				configurableBeanFactory.getBeanClassLoader() : null);
		return factoriesAndBeans(getSpringFactoriesLoader(classLoader), beanFactory);
	}

	/**
	 * 创建新的 {@link Loader}，从给定的 {@link SpringFactoriesLoader} 和
	 * {@link ListableBeanFactory} 获取 AOT 服务。
	 * @param springFactoriesLoader Spring 工厂加载器
	 * @param beanFactory bean 工厂
	 * @return 新的 {@link Loader} 实例
	 */
	public static Loader factoriesAndBeans(SpringFactoriesLoader springFactoriesLoader, ListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "'beanFactory' must not be null");
		Assert.notNull(springFactoriesLoader, "'springFactoriesLoader' must not be null");
		return new Loader(springFactoriesLoader, beanFactory);
	}

	private static SpringFactoriesLoader getSpringFactoriesLoader(
			@Nullable ClassLoader classLoader) {
		return SpringFactoriesLoader.forResourceLocation(FACTORIES_RESOURCE_LOCATION,
				classLoader);
	}

	@Override
	public Iterator<T> iterator() {
		return this.services.iterator();
	}

	/**
	 * 返回 AOT 服务的 {@link Stream}。
	 * @return 服务流
	 */
	public Stream<T> stream() {
		return this.services.stream();
	}

	/**
	 * 将 AOT 服务作为 {@link List} 返回。
	 * @return 服务列表
	 */
	public List<T> asList() {
		return this.services;
	}

	/**
	 * 查找为给定 bean 名称加载的 AOT 服务。
	 * @param beanName bean 名称
	 * @return AOT 服务，或 {@code null}
	 */
	public @Nullable T findByBeanName(String beanName) {
		return this.beans.get(beanName);
	}

	/**
	 * 获取给定服务的来源。
	 * @param service 服务实例
	 * @return 服务的来源
	 */
	public Source getSource(T service) {
		Source source = this.sources.get(service);
		Assert.state(source != null,
				() -> "Unable to find service " + ObjectUtils.identityToString(source));
		return source;
	}


	/**
	 * 用于实际加载服务的加载器类。
	 */
	public static class Loader {

		private final SpringFactoriesLoader springFactoriesLoader;

		private final @Nullable ListableBeanFactory beanFactory;


		Loader(SpringFactoriesLoader springFactoriesLoader, @Nullable ListableBeanFactory beanFactory) {
			this.springFactoriesLoader = springFactoriesLoader;
			this.beanFactory = beanFactory;
		}


		/**
		 * 加载给定类型的所有 AOT 服务。
		 * @param <T> 服务类型
		 * @param type 服务类型
		 * @return 新的 {@link AotServices} 实例
		 */
		public <T> AotServices<T> load(Class<T> type) {
			return new AotServices<>(this.springFactoriesLoader.load(type), loadBeans(type));
		}

		private <T> Map<String, T> loadBeans(Class<T> type) {
			return (this.beanFactory != null ?
					BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type, true, false) :
					Collections.emptyMap());
		}

	}

	/**
	 * 服务来源枚举。
	 */
	public enum Source {

		/**
		 * 通过 {@link SpringFactoriesLoader} 加载的 AOT 服务。
		 */
		SPRING_FACTORIES_LOADER,

		/**
		 * 从 {@link BeanFactory} 加载的 AOT 服务。
		 */
		BEAN_FACTORY

	}

}
