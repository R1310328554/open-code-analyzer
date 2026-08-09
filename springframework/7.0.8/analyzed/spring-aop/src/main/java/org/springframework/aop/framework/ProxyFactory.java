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

import org.aopalliance.intercept.Interceptor;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.util.ClassUtils;

/**
 * 用于编程式（而非 Bean 工厂声明式配置）创建 AOP 代理的工厂。
 * 本类提供在用户代码中获取并配置 AOP 代理实例的简便方式。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 14.03.2003
 */
@SuppressWarnings("serial")
public class ProxyFactory extends ProxyCreatorSupport {

	/**
	 * 创建新的 ProxyFactory。
	 */
	public ProxyFactory() {
	}

	/**
	 * 创建新的 ProxyFactory。
	 * <p>将代理给定目标实现的所有接口。
	 * @param target 要代理的目标对象
	 */
	public ProxyFactory(Object target) {
		setTarget(target);
		setInterfaces(ClassUtils.getAllInterfaces(target));
	}

	/**
	 * 创建新的 ProxyFactory。
	 * <p>无目标，仅接口。须添加拦截器。
	 * @param proxyInterfaces 代理应实现的接口
	 */
	public ProxyFactory(Class<?>... proxyInterfaces) {
		setInterfaces(proxyInterfaces);
	}

	/**
	 * 为给定接口与拦截器创建新的 ProxyFactory。
	 * <p>便捷方法：为单一拦截器创建代理，
	 * 假设拦截器自行处理所有调用而非委托目标（如远程代理场景）。
	 * @param proxyInterface 代理应实现的接口
	 * @param interceptor 代理应调用的拦截器
	 */
	public ProxyFactory(Class<?> proxyInterface, Interceptor interceptor) {
		addInterface(proxyInterface);
		addAdvice(interceptor);
	}

	/**
	 * 为指定 {@code TargetSource} 创建 ProxyFactory，
	 * 使代理实现指定接口。
	 * @param proxyInterface 代理应实现的接口
	 * @param targetSource 代理应调用的 TargetSource
	 */
	public ProxyFactory(Class<?> proxyInterface, TargetSource targetSource) {
		addInterface(proxyInterface);
		setTargetSource(targetSource);
	}


	/**
	 * 根据本工厂设置创建新代理。
	 * <p>可重复调用。增删接口时效果不同。可增删拦截器。
	 * <p>使用默认类加载器：通常为线程上下文类加载器
	 *（代理创建需要时）。
	 * @return 代理对象
	 */
	public Object getProxy() {
		return createAopProxy().getProxy();
	}

	/**
	 * 根据本工厂设置创建新代理。
	 * <p>可重复调用。增删接口时效果不同。可增删拦截器。
	 * <p>使用给定类加载器（代理创建需要时）。
	 * @param classLoader 创建代理所用的类加载器
	 *（或 {@code null} 表示底层代理设施的默认值）
	 * @return 代理对象
	 */
	public Object getProxy(@Nullable ClassLoader classLoader) {
		return createAopProxy().getProxy(classLoader);
	}

	/**
	 * 根据本工厂设置确定代理类。
	 * @param classLoader 创建代理类所用的类加载器
	 *（或 {@code null} 表示底层代理设施的默认值）
	 * @return 代理类
	 * @since 6.0
	 */
	public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
		return createAopProxy().getProxyClass(classLoader);
	}


	/**
	 * 为给定接口与拦截器创建新代理。
	 * <p>便捷方法：为单一拦截器创建代理，
	 * 假设拦截器自行处理所有调用而非委托目标（如远程代理场景）。
	 * @param proxyInterface 代理应实现的接口
	 * @param interceptor 代理应调用的拦截器
	 * @return 代理对象
	 * @see #ProxyFactory(Class, org.aopalliance.intercept.Interceptor)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, Interceptor interceptor) {
		return (T) new ProxyFactory(proxyInterface, interceptor).getProxy();
	}

	/**
	 * 为指定 {@code TargetSource} 创建代理，实现指定接口。
	 * @param proxyInterface 代理应实现的接口
	 * @param targetSource 代理应调用的 TargetSource
	 * @return 代理对象
	 * @see #ProxyFactory(Class, org.springframework.aop.TargetSource)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, TargetSource targetSource) {
		return (T) new ProxyFactory(proxyInterface, targetSource).getProxy();
	}

	/**
	 * 为指定 {@code TargetSource} 创建代理，
	 * 扩展 {@code TargetSource} 的目标类。
	 * @param targetSource 代理应调用的 TargetSource
	 * @return 代理对象
	 */
	public static Object getProxy(TargetSource targetSource) {
		if (targetSource.getTargetClass() == null) {
			throw new IllegalArgumentException("Cannot create class proxy for TargetSource with null target class");
		}
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(targetSource);
		proxyFactory.setProxyTargetClass(true);
		return proxyFactory.getProxy();
	}

}
