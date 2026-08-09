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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * 支持从属性资源（即 properties 文件）配置各个 Bean 的属性值。
 * 适用于面向系统管理员的自定义配置文件，用于覆盖应用上下文中已配置的 Bean 属性。
 *
 * <p>发行版中提供两个具体实现：
 * <ul>
 * <li>{@link PropertyOverrideConfigurer}：按 "beanName.property=value" 风格覆盖属性
 * （从 properties 文件<i>推送</i>值到 Bean 定义）
 * <li>{@link PropertyPlaceholderConfigurer}：替换 "${...}" 占位符
 * （从 properties 文件<i>拉取</i>值到 Bean 定义）
 * </ul>
 *
 * <p>读取属性值后可通过重写 {@link #convertPropertyValue} 方法进行转换。
 * 例如，可检测加密值并在处理前相应解密。
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport
		implements BeanFactoryPostProcessor, PriorityOrdered {

	/** 排序优先级，默认为 {@link Ordered#LOWEST_PRECEDENCE}（与非 Ordered 对象相同） */
	private int order = Ordered.LOWEST_PRECEDENCE;


	/**
	 * 设置本对象的排序值。
	 * @see PriorityOrdered
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	/**
	 * 对给定 Bean 工厂执行属性的 {@linkplain #mergeProperties 合并}、
	 * {@linkplain #convertProperties 转换}与 {@linkplain #processProperties 处理}。
	 * @throws BeanInitializationException 若无法加载任何属性
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			Properties mergedProps = mergeProperties();

			// 如有必要，转换合并后的属性
			convertProperties(mergedProps);

			// 交由子类处理属性
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties: " + ex.getMessage(), ex);
		}
	}

	/**
	 * 转换给定的合并属性，必要时转换属性值。转换结果随后将被处理。
	 * <p>默认实现为每个属性值调用 {@link #convertPropertyValue}，
	 * 用转换后的值替换原始值。
	 * @param props 待转换的 Properties
	 * @see #processProperties
	 */
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * 将属性源中的给定属性转换为应应用的值。
	 * <p>默认实现调用 {@link #convertPropertyValue(String)}。
	 * @param propertyName 该值所属属性的名称
	 * @param propertyValue 属性源中的原始值
	 * @return 用于处理的转换后值
	 * @see #convertPropertyValue(String)
	 */
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}

	/**
	 * 将属性源中的给定属性值转换为应应用的值。
	 * <p>默认实现直接返回原始值。子类可重写此方法，
	 * 例如检测加密值并相应解密。
	 * @param originalValue 属性源中的原始值（properties 文件或本地 "properties"）
	 * @return 用于处理的转换后值
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 * @see #convertProperty(String, String)
	 */
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}


	/**
	 * 将给定 Properties 应用到给定 BeanFactory。
	 * @param beanFactory 应用上下文使用的 BeanFactory
	 * @param props 待应用的 Properties
	 * @throws org.springframework.beans.BeansException 出错时
	 */
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
