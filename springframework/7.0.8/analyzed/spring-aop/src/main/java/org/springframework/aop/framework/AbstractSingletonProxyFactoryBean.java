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

package org.springframework.aop.framework;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * 用于生成单例范围代理对象的 {@link FactoryBean} 类型的便捷超类。
 * <p> 管理前置和后置拦截器（引用，而不是像 {@link ProxyFactoryBean} 中的拦截器名称）并提供一致的接口管理。
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig
		implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

	/** 目标相关状态（`target`）。 */
	private @Nullable Object target;

	/** 代理相关状态（`proxyInterfaces`）。 */
	private Class<?> @Nullable [] proxyInterfaces;

	/** 拦截器相关状态（`preInterceptors`）。 */
	private Object @Nullable [] preInterceptors;

	/** 拦截器相关状态（`postInterceptors`）。 */
	private Object @Nullable [] postInterceptors;

	/**
	 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/** 类相关状态（`proxyClassLoader`）。 */
	private transient @Nullable ClassLoader proxyClassLoader;

	/** 代理相关状态（`proxy`）。 */
	private @Nullable Object proxy;


	/**
	 * 设置目标对象，即要使用事务代理包装的 bean。 <p>目标可以是任何对象，在这种情况下将创建 SingletonTargetSource。如果它是
	 * TargetSource，则不会创建包装器 TargetSource：这允许使用池或原型 TargetSource 等。
	 * @see org.springframework.aop.TargetSource
	 * @see org.springframework.aop.target.SingletonTargetSource
	 * @see org.springframework.aop.target.LazyInitTargetSource
	 * @see org.springframework.aop.target.PrototypeTargetSource
	 * @see org.springframework.aop.target.CommonsPool2TargetSource
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * 指定被代理的接口集。 <p>如果未指定（默认），AOP基础结构通过分析目标、代理目标对象实现的所有接口来确定哪些接口需要代理。
	 */
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * 设置要在隐式事务拦截器之前应用的其他拦截器（或顾问程序），例如 PerformanceMonitorInterceptor。 <p>您可以指定任何AOP联盟方法拦截器或其他Sp
	 * ring AOP建议，以及Spring AOP顾问。
	 * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
	 */
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * 设置在隐式事务拦截器之后应用的附加拦截器（或顾问程序）。 <p>您可以指定任何AOP联盟方法拦截器或其他Spring AOP建议，以及Spring AOP顾问。
	 */
	public void setPostInterceptors(Object[] postInterceptors) {
		this.postInterceptors = postInterceptors;
	}

	/**
	 * 指定要使用的 AdvisorAdapterRegistry。默认是全局 AdvisorAdapterRegistry。
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置ClassLoader来生成代理类。<p>Default是bean的ClassLoader，即包含BeanFactory用来加载所有bean类的ClassLoader。对于
	 * 特定代理，可以在此处覆盖此设置。
	 */
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
	}

	/**
	 * 设置 Bean Class Loader（`BeanClassLoader`）。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = classLoader;
		}
	}


	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.target == null) {
			throw new IllegalArgumentException("Property 'target' is required");
		}
		if (this.target instanceof String) {
			throw new IllegalArgumentException("'target' needs to be a bean reference, not a bean name as value");
		}
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
		}

		ProxyFactory proxyFactory = new ProxyFactory();

		if (this.preInterceptors != null) {
			for (Object interceptor : this.preInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		// 添加主拦截器（通常是 Advisor）。
		proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));

		if (this.postInterceptors != null) {
			for (Object interceptor : this.postInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		proxyFactory.copyFrom(this);

		TargetSource targetSource = createTargetSource(this.target);
		proxyFactory.setTargetSource(targetSource);

		if (this.proxyInterfaces != null) {
			proxyFactory.setInterfaces(this.proxyInterfaces);
		}
		else if (!isProxyTargetClass()) {
			// 依靠 AOP 基础设施来告诉我们要代理哪些接口。
			Class<?> targetClass = targetSource.getTargetClass();
			if (targetClass != null) {
				proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
		}

		postProcessProxyFactory(proxyFactory);

		this.proxy = proxyFactory.getProxy(this.proxyClassLoader);
	}

	/**
	 * 确定给定目标（或 TargetSource）的 TargetSource。
	 * @param target 目标。如果这是 TargetSource 的实现，则将其用作我们的 TargetSource；否则它会被包装在 SingletonTargetSource 中。
	 * @return 该对象的目标源
	 */
	protected TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource targetSource) {
			return targetSource;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}

	/**
	 * 子类的挂钩，用于在使用 {@link ProxyFactory} 创建代理实例之前对 {@link ProxyFactory} 进行后处理。
	 * @param proxyFactory 即将使用的 AOP ProxyFactory
	 * @since 4.2
	 */
	protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
	}


	/**
	 * 获取 Object（`Object`）。
	 */
	@Override
	public Object getObject() {
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
		if (this.proxyInterfaces != null && this.proxyInterfaces.length == 1) {
			return this.proxyInterfaces[0];
		}
		if (this.target instanceof TargetSource targetSource) {
			return targetSource.getTargetClass();
		}
		if (this.target != null) {
			return this.target.getClass();
		}
		return null;
	}

	/**
	 * 判断是否 Singleton。
	 */
	@Override
	public final boolean isSingleton() {
		return true;
	}


	/**
	 * 为此代理工厂 bean 创建“主”拦截器。通常是顾问，但也可以是任何类型的建议。 <p>Pre-拦截器将在此拦截器之前应用，后拦截器将在该拦截器之后应用。
	 */
	protected abstract Object createMainInterceptor();

}
