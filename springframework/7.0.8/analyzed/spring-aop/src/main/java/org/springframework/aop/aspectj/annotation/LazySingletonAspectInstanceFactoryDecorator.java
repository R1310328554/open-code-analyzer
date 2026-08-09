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

package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 装饰器使 {@link MetadataAwareAspectInstanceFactory} 仅实例化一次。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {

	/** `maaif`：该类的成员状态。 */
	private final MetadataAwareAspectInstanceFactory maaif;

	/** `materialized`：该类的成员状态。 */
	private volatile @Nullable Object materialized;


	/**
	 * 为给定的 AspectInstanceFactory 创建一个新的延迟初始化装饰器。
	 * @param maaif 要装饰的 MetadataAwareAspectInstanceFactory
	 */
	public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory maaif) {
		Assert.notNull(maaif, "AspectInstanceFactory must not be null");
		this.maaif = maaif;
	}


	/**
	 * 获取 Aspect Instance（`AspectInstance`）。
	 */
	@Override
	public Object getAspectInstance() {
		Object aspectInstance = this.materialized;
		if (aspectInstance == null) {
			Object mutex = this.maaif.getAspectCreationMutex();
			if (mutex == null) {
				aspectInstance = this.maaif.getAspectInstance();
				this.materialized = aspectInstance;
			}
			else {
				synchronized (mutex) {
					aspectInstance = this.materialized;
					if (aspectInstance == null) {
						aspectInstance = this.maaif.getAspectInstance();
						this.materialized = aspectInstance;
					}
				}
			}
		}
		return aspectInstance;
	}

	/**
	 * 判断是否 Materialized。
	 */
	public boolean isMaterialized() {
		return (this.materialized != null);
	}

	/**
	 * 获取 Aspect Class Loader（`AspectClassLoader`）。
	 */
	@Override
	public @Nullable ClassLoader getAspectClassLoader() {
		return this.maaif.getAspectClassLoader();
	}

	/**
	 * 获取 Aspect Metadata（`AspectMetadata`）。
	 */
	@Override
	public AspectMetadata getAspectMetadata() {
		return this.maaif.getAspectMetadata();
	}

	/**
	 * 获取 Aspect Creation Mutex（`AspectCreationMutex`）。
	 */
	@Override
	public @Nullable Object getAspectCreationMutex() {
		return this.maaif.getAspectCreationMutex();
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		return this.maaif.getOrder();
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "LazySingletonAspectInstanceFactoryDecorator: decorating " + this.maaif;
	}

}
