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

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 从源码级元数据读取 {@code ObjectName} 的 {@link ObjectNamingStrategy} 接口实现。
 * 若源码级元数据中找不到 {@code ObjectName}，则回退到 Bean 键（Bean 名称）。
 *
 * <p>使用 {@link JmxAttributeSource} 策略接口，可通过任意支持的实现读取元数据。
 * 开箱即用，{@link org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource}
 * 内省 Spring 自带的一组明确定义的注解。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see ObjectNamingStrategy
 * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource
 */
public class MetadataNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	private static final char[] QUOTABLE_CHARS = new char[] {',', '=', ':', '"'};


	/**
	 * 用于读取元数据的 {@code JmxAttributeSource} 实现。
	 */
	private @Nullable JmxAttributeSource attributeSource;

	private @Nullable String defaultDomain;


	/**
	 * 创建新的 {@code MetadataNamingStrategy}，需通过 {@link #setAttributeSource} 方法配置。
	 */
	public MetadataNamingStrategy() {
	}

	/**
	 * 为给定 {@code JmxAttributeSource} 创建新的 {@code MetadataNamingStrategy}。
	 * @param attributeSource 要使用的 JmxAttributeSource
	 */
	public MetadataNamingStrategy(JmxAttributeSource attributeSource) {
		Assert.notNull(attributeSource, "JmxAttributeSource must not be null");
		this.attributeSource = attributeSource;
	}


	/**
	 * 设置读取源码级元数据时使用的 {@code JmxAttributeSource} 接口实现。
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		Assert.notNull(attributeSource, "JmxAttributeSource must not be null");
		this.attributeSource = attributeSource;
	}

	/**
	 * 指定未提供源码级元数据时生成 ObjectName 所用的默认域。
	 * <p>默认使用 Bean 名称中指定的域（若 Bean 名称遵循 JMX ObjectName 语法）；
	 * 否则使用受管 Bean 类的包名。
	 */
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.attributeSource == null) {
			throw new IllegalArgumentException("Property 'attributeSource' is required");
		}
	}


	/**
	 * 从与受管资源 {@code Class} 关联的源码级元数据中读取 {@code ObjectName}。
	 */
	@Override
	public ObjectName getObjectName(Object managedBean, @Nullable String beanKey) throws MalformedObjectNameException {
		Assert.state(this.attributeSource != null, "No JmxAttributeSource set");
		Class<?> managedClass = AopUtils.getTargetClass(managedBean);
		ManagedResource mr = this.attributeSource.getManagedResource(managedClass);

		// Check that an object name has been specified.
		if (mr != null && StringUtils.hasText(mr.getObjectName())) {
			return ObjectNameManager.getInstance(mr.getObjectName());
		}
		else {
			Assert.state(beanKey != null, "No ManagedResource attribute and no bean key specified");
			try {
				return ObjectNameManager.getInstance(beanKey);
			}
			catch (MalformedObjectNameException ex) {
				String domain = this.defaultDomain;
				if (domain == null) {
					domain = ClassUtils.getPackageName(managedClass);
				}
				Hashtable<String, String> properties = new Hashtable<>();
				properties.put("type", ClassUtils.getShortName(managedClass));
				properties.put("name", quoteIfNecessary(beanKey));
				return ObjectNameManager.getInstance(domain, properties);
			}
		}
	}

	private static String quoteIfNecessary(String value) {
		return shouldQuote(value) ? ObjectName.quote(value) : value;
	}

	private static boolean shouldQuote(String value) {
		for (char quotableChar : QUOTABLE_CHARS) {
			if (value.indexOf(quotableChar) != -1) {
				return true;
			}
		}
		return false;
	}

}
