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

import java.util.ServiceLoader;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 基于 JDK 1.6 {@link java.util.ServiceLoader} 机制的 FactoryBean 抽象基类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean<Object>
		implements BeanClassLoaderAware {

	/** 目标服务类型（通常为服务的公共 API 接口）。 */
	private @Nullable Class<?> serviceType;

	/** 用于加载服务实现的类加载器。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * 设置所需的服务类型（通常为服务的公共 API）。
	 */
	public void setServiceType(@Nullable Class<?> serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * 返回所需的服务类型。
	 */
	public @Nullable Class<?> getServiceType() {
		return this.serviceType;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * 委托给 {@link #getObjectToExpose(java.util.ServiceLoader)} 创建实例。
	 * @return 要暴露的对象
	 */
	@Override
	protected Object createInstance() {
		Assert.state(getServiceType() != null, "Property 'serviceType' is required");
		return getObjectToExpose(ServiceLoader.load(getServiceType(), this.beanClassLoader));
	}

	/**
	 * 根据给定的 ServiceLoader 确定实际要暴露的对象。
	 * <p>由具体子类实现。
	 * @param serviceLoader 为已配置服务类创建的 ServiceLoader
	 * @return 要暴露的对象
	 */
	protected abstract Object getObjectToExpose(ServiceLoader<?> serviceLoader);

}
