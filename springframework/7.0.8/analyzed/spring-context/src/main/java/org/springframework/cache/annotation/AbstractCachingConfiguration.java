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

package org.springframework.cache.annotation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

/**
 * 抽象基类 {@code @Configuration}，为启用 Spring 基于注解的缓存管理能力提供通用结构。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableCaching
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractCachingConfiguration implements ImportAware {

	/** {@code @EnableCaching} 注解属性。 */
	protected @Nullable AnnotationAttributes enableCaching;

	/** 缓存管理器供应者。 */
	@SuppressWarnings("NullAway.Init")
	protected Supplier<@Nullable CacheManager> cacheManager;

	/** 缓存解析器供应者。 */
	@SuppressWarnings("NullAway.Init")
	protected Supplier<@Nullable CacheResolver> cacheResolver;

	/** 键生成器供应者。 */
	@SuppressWarnings("NullAway.Init")
	protected Supplier<@Nullable KeyGenerator> keyGenerator;

	/** 缓存错误处理器供应者。 */
	@SuppressWarnings("NullAway.Init")
	protected Supplier<@Nullable CacheErrorHandler> errorHandler;


	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		// 从导入元数据读取 @EnableCaching 属性
		this.enableCaching = AnnotationAttributes.fromMap(
				importMetadata.getAnnotationAttributes(EnableCaching.class.getName()));
		if (this.enableCaching == null) {
			throw new IllegalArgumentException(
					"@EnableCaching is not present on importing class " + importMetadata.getClassName());
		}
	}

	@Autowired
	void setConfigurers(ObjectProvider<CachingConfigurer> configurers) {
		Supplier<@Nullable CachingConfigurer> configurer = () -> {
			// 收集所有 CachingConfigurer 候选
			List<CachingConfigurer> candidates = configurers.stream().toList();
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			// 仅允许唯一实现
			if (candidates.size() > 1) {
				throw new IllegalStateException(candidates.size() + " implementations of " +
						"CachingConfigurer were found when only 1 was expected. " +
						"Refactor the configuration such that CachingConfigurer is " +
						"implemented only once or not at all.");
			}
			return candidates.get(0);
		};
		useCachingConfigurer(new CachingConfigurerSupplier(configurer));
	}

	/**
	 * 从指定的 {@link CachingConfigurer} 提取配置。
	 */
	protected void useCachingConfigurer(CachingConfigurerSupplier cachingConfigurerSupplier) {
		this.cacheManager = cachingConfigurerSupplier.adapt(CachingConfigurer::cacheManager);
		this.cacheResolver = cachingConfigurerSupplier.adapt(CachingConfigurer::cacheResolver);
		this.keyGenerator = cachingConfigurerSupplier.adapt(CachingConfigurer::keyGenerator);
		this.errorHandler = cachingConfigurerSupplier.adapt(CachingConfigurer::errorHandler);
	}


	protected static class CachingConfigurerSupplier {

		private final SingletonSupplier<@Nullable CachingConfigurer> supplier;

		public CachingConfigurerSupplier(Supplier<@Nullable CachingConfigurer> supplier) {
			this.supplier = SingletonSupplier.ofNullable(supplier);
		}

		/**
		 * 将 {@link CachingConfigurer} 供应者适配为指定映射函数提供的另一供应者。
		 * 若底层 {@link CachingConfigurer} 为 {@code null}，则返回 {@code null} 且不调用映射函数。
		 * @param provider 用于适配供应者的提供者
		 * @param <T> 供应者类型
		 * @return 由指定函数映射的另一供应者
		 */
		public <T> Supplier<@Nullable T> adapt(Function<CachingConfigurer, @Nullable T> provider) {
			return () -> {
				CachingConfigurer cachingConfigurer = this.supplier.get();
				return (cachingConfigurer != null ? provider.apply(cachingConfigurer) : null);
			};
		}

	}

}
