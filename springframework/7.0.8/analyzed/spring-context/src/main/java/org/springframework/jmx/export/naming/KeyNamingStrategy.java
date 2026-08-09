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

package org.springframework.jmx.export.naming;

import java.io.IOException;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 根据传入 {@code MBeanExporter} 的 "beans" 映射中的键构建
 * {@code ObjectName} 实例的 {@code ObjectNamingStrategy} 实现。
 *
 * <p>还可检查以 {@code Properties} 或 {@code mappingLocations}
 * 属性文件形式提供的 ObjectName 映射。查找时使用 {@code MBeanExporter}
 * "beans" 映射中的键；若未找到映射，则直接使用该键构建 {@code ObjectName}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setMappings
 * @see #setMappingLocation
 * @see #setMappingLocations
 * @see org.springframework.jmx.export.MBeanExporter#setBeans
 */
public class KeyNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	/**
	 * 本类的 {@code Log} 实例。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 存储 Bean 键到 {@code ObjectName} 的映射。
	 */
	private @Nullable Properties mappings;

	/**
	 * 存储应加载到最终合并 {@code Properties} 集合中的 {@code Resource}，
	 * 用于 {@code ObjectName} 解析。
	 */
	private Resource @Nullable [] mappingLocations;

	/**
	 * 存储将 {@code mappings} {@code Properties} 与
	 * {@code mappingLocations} 所定义资源中的属性合并后的结果。
	 */
	private @Nullable Properties mergedMappings;


	/**
	 * 设置包含 ObjectName 映射的本地属性，例如通过 XML Bean 定义中的 "props" 标签。
	 * 可视为默认值，会被从文件加载的属性覆盖。
	 */
	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	/**
	 * 设置要加载的属性文件位置，其中包含 ObjectName 映射。
	 */
	public void setMappingLocation(Resource location) {
		this.mappingLocations = new Resource[] {location};
	}

	/**
	 * 设置要加载的属性文件位置列表，其中包含 ObjectName 映射。
	 */
	public void setMappingLocations(Resource... mappingLocations) {
		this.mappingLocations = mappingLocations;
	}


	/**
	 * 将 {@code mappings} 与 {@code mappingLocations} 中配置的
	 * {@code Properties} 合并为用于 {@code ObjectName} 解析的最终 {@code Properties} 实例。
	 */
	@Override
	public void afterPropertiesSet() throws IOException {
		this.mergedMappings = new Properties();
		CollectionUtils.mergePropertiesIntoMap(this.mappings, this.mergedMappings);

		if (this.mappingLocations != null) {
			for (Resource location : this.mappingLocations) {
				if (logger.isDebugEnabled()) {
					logger.debug("Loading JMX object name mappings file from " + location);
				}
				PropertiesLoaderUtils.fillProperties(this.mergedMappings, location);
			}
		}
	}


	/**
	 * 尝试通过给定键获取 {@code ObjectName}，优先在映射中查找对应值。
	 */
	@Override
	public ObjectName getObjectName(Object managedBean, @Nullable String beanKey) throws MalformedObjectNameException {
		Assert.notNull(beanKey, "KeyNamingStrategy requires bean key");
		String objectName = null;
		if (this.mergedMappings != null) {
			objectName = this.mergedMappings.getProperty(beanKey);
		}
		if (objectName == null) {
			objectName = beanKey;
		}
		return ObjectNameManager.getInstance(objectName);
	}

}
