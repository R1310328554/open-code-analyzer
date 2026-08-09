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

package org.springframework.context.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.support.PropertySourceDescriptor;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.PropertySourceProcessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 配置类上已处理的 {@link PropertySource} 注册表。
 *
 * @author Stephane Nicoll
 * @since 6.0
 * @see PropertySourceDescriptor
 */
class PropertySourceRegistry {

	/** 实际加载属性源的处理器。 */
	private final PropertySourceProcessor propertySourceProcessor;

	/** 已处理的属性源描述符列表。 */
	private final List<PropertySourceDescriptor> descriptors;


	public PropertySourceRegistry(PropertySourceProcessor propertySourceProcessor) {
		this.propertySourceProcessor = propertySourceProcessor;
		this.descriptors = new ArrayList<>();
	}


	/**
	 * 处理给定的 {@code @PropertySource} 注解元数据。
	 * @param propertySource 找到的 {@code @PropertySource} 注解元数据
	 * @throws IOException 加载属性源失败时
	 */
	void processPropertySource(AnnotationAttributes propertySource) throws IOException {
		String name = propertySource.getString("name");
		if (!StringUtils.hasLength(name)) {
			name = null;
		}
		String encoding = propertySource.getString("encoding");
		if (!StringUtils.hasLength(encoding)) {
			encoding = null;
		}
		String[] locations = propertySource.getStringArray("value");
		Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
		boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");

		Class<? extends PropertySourceFactory> factoryClass = propertySource.getClass("factory");
		Class<? extends PropertySourceFactory> factoryClassToUse =
				(factoryClass != PropertySourceFactory.class ? factoryClass : null);
		PropertySourceDescriptor descriptor = new PropertySourceDescriptor(Arrays.asList(locations),
				ignoreResourceNotFound, name, factoryClassToUse, encoding);
		this.propertySourceProcessor.processPropertySource(descriptor);
		this.descriptors.add(descriptor);
	}

	/** 返回已注册描述符的不可变视图。 */
	public List<PropertySourceDescriptor> getDescriptors() {
		return Collections.unmodifiableList(this.descriptors);
	}

}
