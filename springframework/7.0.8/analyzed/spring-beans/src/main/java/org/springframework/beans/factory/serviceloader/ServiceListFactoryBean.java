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

package org.springframework.beans.factory.serviceloader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * 通过 JDK 1.6 {@link java.util.ServiceLoader} 机制暴露已配置服务类的
 * <i>全部</i>服务实现，以 {@code List} 形式呈现的
 * {@link org.springframework.beans.factory.FactoryBean}。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public class ServiceListFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

	@Override
	protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
		List<Object> result = new ArrayList<>();
		// 遍历 ServiceLoader 发现的所有服务实现并收集到列表中
		for (Object loaderObject : serviceLoader) {
			result.add(loaderObject);
		}
		return result;
	}

	@Override
	public Class<?> getObjectType() {
		return List.class;
	}

}
