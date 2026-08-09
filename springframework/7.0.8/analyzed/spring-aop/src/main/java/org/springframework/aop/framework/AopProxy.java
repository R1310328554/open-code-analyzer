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

/**
 * 已配置 AOP 代理的委托接口，用于创建实际代理对象。
 *
 * <p>{@link DefaultAopProxyFactory} 提供 JDK 动态代理与 CGLIB 代理的开箱即用实现。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DefaultAopProxyFactory
 */
public interface AopProxy {

	/**
	 * 创建新代理对象。
	 * <p>使用 AopProxy 的默认类加载器（创建代理必要时）：通常为线程上下文类加载器。
	 * @return 新代理对象（永不返回 {@code null}）
	 * @see Thread#getContextClassLoader()
	 */
	Object getProxy();

	/**
	 * 创建新代理对象。
	 * <p>使用给定类加载器（创建代理必要时）。
	 * {@code null} 会向下传递，从而使用底层代理设施的默认值，
	 * 通常与 AopProxy 实现 {@link #getProxy()} 方法的默认选择不同。
	 * @param classLoader 创建代理所用的类加载器
	 * （或 {@code null} 使用底层代理设施默认值）
	 * @return 新代理对象（永不返回 {@code null}）
	 */
	Object getProxy(@Nullable ClassLoader classLoader);

	/**
	 * 确定代理类。
	 * @param classLoader 创建代理类所用的类加载器
	 * （或 {@code null} 使用底层代理设施默认值）
	 * @return 代理类
	 * @since 6.0
	 */
	Class<?> getProxyClass(@Nullable ClassLoader classLoader);

}
