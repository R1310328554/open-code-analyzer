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

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * 属性资源配置器，用于覆盖应用上下文定义中的 Bean 属性值。
 * 它将 properties 文件中的值<i>推送</i>到 Bean 定义中。
 *
 * <p>配置行格式如下：
 *
 * <pre class="code">beanName.property=value</pre>
 *
 * 示例 properties 文件：
 *
 * <pre class="code">
 * dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * <p>与 {@link PropertyPlaceholderConfigurer} 不同，原始定义中此类 Bean 属性
 * 可以有默认值，也可以完全没有值。若覆盖用的 properties 文件没有某属性的条目，
 * 则使用上下文定义中的默认值。
 *
 * <p>注意：上下文定义<i>并不知道</i>自己被覆盖；因此查看 XML 定义文件时这一点并不明显。
 * 此外，指定的覆盖值始终是<i>字面值</i>，不会转换为 Bean 引用。
 * 即使 XML Bean 定义中的原始值指定了 Bean 引用，也适用此规则。
 *
 * <p>若有多个 PropertyOverrideConfigurer 为同一 Bean 属性定义了不同值，
 * <i>最后一个</i>将生效（由于覆盖机制）。
 *
 * <p>读取属性值后可通过重写 {@code convertPropertyValue} 方法进行转换。
 * 例如，可检测加密值并在处理前相应解密。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 12.03.2003
 * @see #convertPropertyValue
 * @see PropertyPlaceholderConfigurer
 */
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

	/** 默认的 Bean 名称分隔符 */
	public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";


	/** Bean 名称与属性路径之间的分隔符，默认为点号（"."） */
	private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

	/** 是否忽略无效键，默认为 {@code false} */
	private boolean ignoreInvalidKeys = false;

	/** 包含已被覆盖的 Bean 名称 */
	private final Set<String> beanNames = ConcurrentHashMap.newKeySet(16);


	/**
	 * 设置 Bean 名称与属性路径之间期望的分隔符。
	 * 默认为点号（"."）。
	 */
	public void setBeanNameSeparator(String beanNameSeparator) {
		this.beanNameSeparator = beanNameSeparator;
	}

	/**
	 * 设置是否忽略无效键。默认为 {@code false}。
	 * <p>若忽略无效键，不符合 'beanName.property' 格式
	 * （或引用无效 Bean 名称或属性）的键将仅以 debug 级别记录。
	 * 这样 properties 文件中可以包含任意其他键。
	 */
	public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
		this.ignoreInvalidKeys = ignoreInvalidKeys;
	}


	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {

		for (Enumeration<?> names = props.propertyNames(); names.hasMoreElements();) {
			String key = (String) names.nextElement();
			try {
				processKey(beanFactory, key, props.getProperty(key));
			}
			catch (BeansException ex) {
				String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
				if (!this.ignoreInvalidKeys) {
					throw new BeanInitializationException(msg, ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug(msg, ex);
				}
			}
		}
	}

	/**
	 * 将给定键作为 'beanName.property' 条目处理。
	 */
	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
			throws BeansException {

		int separatorIndex = key.indexOf(this.beanNameSeparator);
		if (separatorIndex == -1) {
			throw new BeanInitializationException("Invalid key '" + key +
					"': expected 'beanName" + this.beanNameSeparator + "property'");
		}
		String beanName = key.substring(0, separatorIndex);
		String beanProperty = key.substring(separatorIndex + 1);
		this.beanNames.add(beanName);
		applyPropertyValue(factory, beanName, beanProperty, value);
		if (logger.isDebugEnabled()) {
			logger.debug("Property '" + key + "' set to value [" + value + "]");
		}
	}

	/**
	 * 将给定属性值应用到对应 Bean。
	 */
	protected void applyPropertyValue(
			ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

		BeanDefinition bd = factory.getBeanDefinition(beanName);
		BeanDefinition bdToUse = bd;
		while (bd != null) {
			bdToUse = bd;
			bd = bd.getOriginatingBeanDefinition();
		}
		PropertyValue pv = new PropertyValue(property, value);
		pv.setOptional(this.ignoreInvalidKeys);
		bdToUse.getPropertyValues().addPropertyValue(pv);
	}


	/**
	 * 该 Bean 是否存在覆盖？仅在至少处理过一次后才有效。
	 * @param beanName 要查询状态的 Bean 名称
	 * @return 指定 Bean 是否存在属性覆盖
	 */
	public boolean hasPropertyOverridesFor(String beanName) {
		return this.beanNames.contains(beanName);
	}

}
