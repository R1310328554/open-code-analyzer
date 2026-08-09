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

package org.springframework.boot.context;

import java.io.IOException;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * 提供从 {@link BeanFactory} 加载并自动应用于 {@code SpringBootApplication}
 * 扫描的排除 {@link TypeFilter TypeFilters}。也可直接配合 {@code @ComponentScan} 使用：
 * <pre class="code">
 * &#064;ComponentScan(excludeFilters = @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
 * </pre>
 * <p>
 * 实现类应提供注册到 {@link BeanFactory} 的子类并重写
 * {@link #match(MetadataReader, MetadataReaderFactory)}，同时实现有效的
 * {@link #hashCode() hashCode} 与 {@link #equals(Object) equals}，以便用于 Spring 测试的应用上下文缓存。
 * <p>
 * {@code TypeExcludeFilter} 在应用生命周期极早期初始化，通常不应依赖其他 Bean，
 * 主要用于内部支持 {@code spring-boot-test}。
 *
 * @author Phillip Webb
 * @since 1.4.0
 */
public class TypeExcludeFilter implements TypeFilter, BeanFactoryAware {

	@SuppressWarnings("NullAway.Init")
	private BeanFactory beanFactory;

	private @Nullable Collection<TypeExcludeFilter> delegates;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		if (this.beanFactory instanceof ListableBeanFactory && getClass() == TypeExcludeFilter.class) {
			for (TypeExcludeFilter delegate : getDelegates()) {
				if (delegate.match(metadataReader, metadataReaderFactory)) {
					return true;
				}
			}
		}
		return false;
	}

	private Collection<TypeExcludeFilter> getDelegates() {
		Collection<TypeExcludeFilter> delegates = this.delegates;
		if (delegates == null) {
			delegates = ((ListableBeanFactory) this.beanFactory).getBeansOfType(TypeExcludeFilter.class).values();
			this.delegates = delegates;
		}
		return delegates;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		throw new IllegalStateException("TypeExcludeFilter " + getClass() + " has not implemented equals");
	}

	@Override
	public int hashCode() {
		throw new IllegalStateException("TypeExcludeFilter " + getClass() + " has not implemented hashCode");
	}

}
