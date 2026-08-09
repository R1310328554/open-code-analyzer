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

package org.springframework.cache.concurrent;

import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * 在 Spring 容器内便捷配置 {@link ConcurrentMapCache} 的 {@link FactoryBean}。
 * 可通过 Bean 属性配置；未显式指定名称时使用 Bean 名称作为缓存名。
 *
 * <p>适用于测试或简单缓存场景，通常与
 * {@link org.springframework.cache.support.SimpleCacheManager} 配合使用，
 * 或通过 {@link ConcurrentMapCacheManager} 动态创建。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public class ConcurrentMapCacheFactoryBean
		implements FactoryBean<ConcurrentMapCache>, BeanNameAware, InitializingBean {

	/** 缓存逻辑名称；空字符串时由 {@link #setBeanName} 填充。 */
	private String name = "";

	/** 可选的预填充底层存储；{@code null} 时使用默认 {@link java.util.concurrent.ConcurrentHashMap}。 */
	private @Nullable ConcurrentMap<Object, Object> store;

	/** 是否允许 {@code null} 值（转换为内部占位对象）。 */
	private boolean allowNullValues = true;

	/** 初始化完成后持有的 {@link ConcurrentMapCache} 实例。 */
	private @Nullable ConcurrentMapCache cache;


	/**
	 * 指定缓存名称。
	 * <p>默认为 ""（空字符串）。
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 指定用作内部存储的 ConcurrentMap（可预填充数据）。
	 * <p>默认为标准 {@link java.util.concurrent.ConcurrentHashMap}。
	 */
	public void setStore(ConcurrentMap<Object, Object> store) {
		this.store = store;
	}

	/**
	 * 设置是否允许 {@code null} 值（转换为内部占位对象）。
	 * <p>默认为 {@code true}。
	 */
	public void setAllowNullValues(boolean allowNullValues) {
		this.allowNullValues = allowNullValues;
	}

	@Override
	public void setBeanName(String beanName) {
		if (!StringUtils.hasLength(this.name)) {
			setName(beanName);
		}
	}

	@Override
	public void afterPropertiesSet() {
		this.cache = (this.store != null ? new ConcurrentMapCache(this.name, this.store, this.allowNullValues) :
				new ConcurrentMapCache(this.name, this.allowNullValues));
	}


	@Override
	public @Nullable ConcurrentMapCache getObject() {
		return this.cache;
	}

	@Override
	public Class<?> getObjectType() {
		return ConcurrentMapCache.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
