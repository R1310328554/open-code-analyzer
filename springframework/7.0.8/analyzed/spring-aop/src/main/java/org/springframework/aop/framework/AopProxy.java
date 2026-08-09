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
 * 配置的 AOP 代理的委托接口，允许创建实际的代理对象。
 * <p> 开箱即用的实现可用于 JDK 动态代理和 CGLIB 代理，如 {@link DefaultAopProxyFactory} 所应用的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DefaultAopProxyFactory
 */
public interface AopProxy {

	/**
	 * 创建一个新的代理对象。 <p>U使用AopProxy的默认类加载器（如果需要创建代理）：通常是线程上下文类加载器。
	 * @return 新代理对象（绝不是 {@code null}）
	 * @see Thread#getContextClassLoader()
	 */
	Object getProxy();

	/**
	 * 创建一个新的代理对象。 <p>U使用给定的类加载器（如果需要创建代理）。 {@code null} 将简单地向下传递，从而导致低级代理工具的默认值，该默认值通常与 AopPro
	 * xy 实现的 {@link #getProxy()} 方法选择的默认值不同。
	 * @param classLoader 用于创建代理的类加载器（或用于低级代理工具的默认值的 {@code null}）
	 * @return 新代理对象（绝不是 {@code null}）
	 */
	Object getProxy(@Nullable ClassLoader classLoader);

	/**
	 * 确定代理类。
	 * @param classLoader 用于创建代理类的类加载器（或用于低级代理工具的默认值的 {@code null}）
	 * @return 代理类
	 * @since 6.0
	 */
	Class<?> getProxyClass(@Nullable ClassLoader classLoader);

}
