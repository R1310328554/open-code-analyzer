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

package org.springframework.jmx.access;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.util.ClassUtils;

/**
 * 创建指向本地或远程运行的受管资源的代理。
 * {@code proxyInterface} 属性定义生成的代理应实现的接口；该接口应定义与
 * 待代理资源的管理接口中的操作和属性相对应的方法与属性。
 *
 * <p>受管资源不必实现代理接口，但这样做可能更方便。管理接口中的每个操作和属性
 * 并不要求在代理接口中都有对应的属性或方法。
 *
 * <p>尝试在代理接口上调用或访问与管理接口不对应的方法或属性时，
 * 将抛出 {@code InvalidInvocationException}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanClientInterceptor
 * @see InvalidInvocationException
 */
public class MBeanProxyFactoryBean extends MBeanClientInterceptor
		implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

	private @Nullable Class<?> proxyInterface;

	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private @Nullable Object mbeanProxy;


	/**
	 * 设置生成的代理将实现的接口。
	 * <p>通常是与目标 MBean 匹配的管理接口，为 MBean 属性暴露 bean 属性 setter/getter，
	 * 为 MBean 操作暴露常规 Java 方法。
	 * @see #setObjectName
	 */
	public void setProxyInterface(Class<?> proxyInterface) {
		this.proxyInterface = proxyInterface;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	/**
	 * 检查是否已指定 {@code proxyInterface}，然后为目标 MBean 生成代理。
	 */
	@Override
	public void afterPropertiesSet() throws MBeanServerNotFoundException, MBeanInfoRetrievalException {
		super.afterPropertiesSet();

		Class<?> interfaceToUse;
		if (this.proxyInterface == null) {
			interfaceToUse = getManagementInterface();
			if (interfaceToUse == null) {
				throw new IllegalArgumentException("Property 'proxyInterface' or 'managementInterface' is required");
			}
			this.proxyInterface = interfaceToUse;
		}
		else {
			interfaceToUse = this.proxyInterface;
			if (getManagementInterface() == null) {
				setManagementInterface(interfaceToUse);
			}
		}
		this.mbeanProxy = new ProxyFactory(interfaceToUse, this).getProxy(this.beanClassLoader);
	}


	@Override
	public @Nullable Object getObject() {
		return this.mbeanProxy;
	}

	@Override
	public @Nullable Class<?> getObjectType() {
		return this.proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
