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

package org.springframework.jdbc.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

/**
 * {@link FactoryBean} 实现采用位置字符串列表并创建 {@link Resource} 实例的排序数组。
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 3.0
 */
public class SortedResourcesFactoryBean extends AbstractFactoryBean<Resource[]> implements ResourceLoaderAware {

	/** `locations`：该类的成员状态。 */
	private final List<String> locations;

	/** 解析器相关状态（`resourcePatternResolver`）。 */
	private ResourcePatternResolver resourcePatternResolver;


	/**
	 * 创建 `SortedResourcesFactoryBean` 的新实例。
	 */
	public SortedResourcesFactoryBean(List<String> locations) {
		this.locations = locations;
		this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
	}

	/**
	 * 创建 `SortedResourcesFactoryBean` 的新实例。
	 */
	public SortedResourcesFactoryBean(ResourceLoader resourceLoader, List<String> locations) {
		this.locations = locations;
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}


	/**
	 * 设置 Resource Loader（`ResourceLoader`）。
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}


	/**
	 * 获取 Object Type（`ObjectType`）。
	 */
	@Override
	public Class<? extends Resource[]> getObjectType() {
		return Resource[].class;
	}

	/**
	 * 创建：Instance（方法 `createInstance`）。
	 */
	@Override
	protected Resource[] createInstance() throws Exception {
		List<Resource> result = new ArrayList<>();
		for (String location : this.locations) {
			Resource[] resources = this.resourcePatternResolver.getResources(location);

			// 缓存 URL 以避免排序期间重复 I/O
			Map<Resource, String> urlCache = new LinkedHashMap<>(resources.length);
			List<Resource> failingResources = new ArrayList<>();
			for (Resource resource : resources) {
				try {
					urlCache.put(resource, resource.getURL().toString());
				}
				catch (IOException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to resolve " + resource + " for sorting purposes: " + ex);
					}
					failingResources.add(resource);
				}
			}

			// 使用缓存的 URL 进行排序
			List<Resource> sortedResources = new ArrayList<>(urlCache.keySet());
			sortedResources.sort(Comparator.comparing(urlCache::get));

			result.addAll(sortedResources);
			result.addAll(failingResources);
		}
		return result.toArray(new Resource[0]);
	}

}
