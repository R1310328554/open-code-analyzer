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

package org.springframework.aop.scope;

import java.lang.reflect.Modifier;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.SimpleBeanTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 用于作用域对象的方便代理工厂 bean。
 * 使用此工厂 bean 创建的 <p>Proxies 是线程安全的单例，可以注入到共享对象中，具有透明的作用域行为。
 * <p>Proxies 由此类返回实现 {@link ScopedObject} 接口。目前，这允许从作用域中删除相应的对象，从而在下次访问时在作用域中无缝地创建新实例。
 * <p>请注意，默认情况下，该工厂创建的代理是基于 <i> 类的 </i> 代理。这可以通过将“proxyTargetClass”属性切换为“false”来自定义。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setProxyTargetClass
 */
@SuppressWarnings("serial")
public class ScopedProxyFactoryBean extends ProxyConfig
		implements FactoryBean<Object>, BeanFactoryAware, AopInfrastructureBean {

	/**
	 */
	private final SimpleBeanTargetSource scopedTargetSource = new SimpleBeanTargetSource();

	/**
	 */
	private @Nullable String targetBeanName;

	/**
	 */
	private @Nullable Object proxy;


	/**
	 * 创建一个新的 ScopedProxyFactoryBean 实例。
	 */
	public ScopedProxyFactoryBean() {
		setProxyTargetClass(true);
	}


	/**
	 * 设置要限定范围的 bean 的名称。
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
		this.scopedTargetSource.setTargetBeanName(targetBeanName);
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory cbf)) {
			throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
		}
		this.scopedTargetSource.setBeanFactory(beanFactory);

		ProxyFactory pf = new ProxyFactory();
		pf.copyFrom(this);
		pf.setTargetSource(this.scopedTargetSource);

		Assert.notNull(this.targetBeanName, "Property 'targetBeanName' is required");
		Class<?> beanType = beanFactory.getType(this.targetBeanName);
		if (beanType == null) {
			throw new IllegalStateException("Cannot create scoped proxy for bean '" + this.targetBeanName +
					"': Target type could not be determined at the time of proxy creation.");
		}
		if (!isProxyTargetClass() || beanType.isInterface() || Modifier.isPrivate(beanType.getModifiers())) {
			pf.setInterfaces(ClassUtils.getAllInterfacesForClass(beanType, cbf.getBeanClassLoader()));
		}

		// 添加仅实现 ScopedObject 上的方法的介绍。
		ScopedObject scopedObject = new DefaultScopedObject(cbf, this.scopedTargetSource.getTargetBeanName());
		pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject));

		// 添加 AopInfrastructureBean 标记以指示作用域代理
		// 本身不受自动代理的约束！只有它的目标 bean 是。
		pf.addInterface(AopInfrastructureBean.class);

		this.proxy = pf.getProxy(cbf.getBeanClassLoader());
	}


	/**
	 * 获取 Object（`Object`）。
	 */
	@Override
	public @Nullable Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	/**
	 * 获取 Object Type（`ObjectType`）。
	 */
	@Override
	public @Nullable Class<?> getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		return this.scopedTargetSource.getTargetClass();
	}

	/**
	 * 判断是否 Singleton。
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
