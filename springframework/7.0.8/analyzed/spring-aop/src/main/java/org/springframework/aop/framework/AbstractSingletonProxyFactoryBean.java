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
 * 生产单例作用域代理对象的 {@link FactoryBean} 类型的便捷超类。
 *
 * <p>管理前置与后置拦截器（引用形式，而非 {@link ProxyFactoryBean} 中的拦截器名称），
 * 并提供一致的接口管理。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig
		implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

	private @Nullable Object target;

	private Class<?> @Nullable [] proxyInterfaces;

	private Object @Nullable [] preInterceptors;

	private Object @Nullable [] postInterceptors;

	/** 默认为全局 AdvisorAdapterRegistry。 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	private transient @Nullable ClassLoader proxyClassLoader;

	private @Nullable Object proxy;


	/**
	 * 设置目标对象，即待包装为事务代理的 Bean。
	 * <p>目标可以是任意对象，此时会创建 SingletonTargetSource。
	 * 若本身是 TargetSource，则不再包装：从而可使用池化或 prototype TargetSource 等。
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
	 * 指定被代理的接口集合。
	 * <p>未指定（默认）时，AOP 基础设施通过分析目标对象
	 * 确定需代理的接口，代理目标实现的所有接口。
	 */
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * 设置隐式事务拦截器之前应用的额外拦截器（或通知器），
	 * 例如 PerformanceMonitorInterceptor。
	 * <p>可指定任意 AOP Alliance MethodInterceptor、其他 Spring AOP Advice
	 * 或 Spring AOP Advisor。
	 * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
	 */
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * 设置隐式事务拦截器之后应用的额外拦截器（或通知器）。
	 * <p>可指定任意 AOP Alliance MethodInterceptor、其他 Spring AOP Advice
	 * 或 Spring AOP Advisor。
	 */
	public void setPostInterceptors(Object[] postInterceptors) {
		this.postInterceptors = postInterceptors;
	}

	/**
	 * 指定使用的 AdvisorAdapterRegistry。
	 * 默认为全局 AdvisorAdapterRegistry。
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置生成代理类所用的 ClassLoader。
	 * <p>默认为 Bean 的 ClassLoader，即容器 BeanFactory 加载所有 Bean 类所用的 ClassLoader。
	 * 可在此为特定代理覆盖。
	 */
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = classLoader;
		}
	}


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

		// 添加主拦截器（通常为 Advisor）。
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
			// 依赖 AOP 基础设施确定需代理的接口。
			Class<?> targetClass = targetSource.getTargetClass();
			if (targetClass != null) {
				proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
		}

		postProcessProxyFactory(proxyFactory);

		this.proxy = proxyFactory.getProxy(this.proxyClassLoader);
	}

	/**
	 * 为给定目标（或 TargetSource）确定 TargetSource。
	 * @param target 目标对象。若为实现 TargetSource 则直接使用；
	 * 否则包装为 SingletonTargetSource。
	 * @return 该对象的 TargetSource
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
	 * 供子类在创建代理实例前对 {@link ProxyFactory} 进行后处理的钩子。
	 * @param proxyFactory 即将使用的 AOP ProxyFactory
	 * @since 4.2
	 */
	protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
	}


	@Override
	public Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

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

	@Override
	public final boolean isSingleton() {
		return true;
	}


	/**
	 * 为本代理工厂 Bean 创建「主」拦截器。
	 * 通常为 Advisor，也可为任意类型的 Advice。
	 * <p>前置拦截器在其之前应用，后置拦截器在其之后应用。
	 */
	protected abstract Object createMainInterceptor();

}
