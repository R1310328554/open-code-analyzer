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

import org.springframework.core.NamedThreadLocal;

/**
 * 包含用于获取有关当前 AOP 调用信息的静态方法的类。
 * <p> 如果 AOP 框架配置为公开当前代理（不是默认的），则 {@code currentProxy()} 方法可用。它返回正在使用的 AOP 代理。目标对象或建议可以使用它
 * 来进行建议调用，就像在 EJB 中使用 {@code getEJBObject()} 一样。他们还可以使用它来查找配置建议。
 * <p>Spring 的 AOP 框架默认不公开代理，因为这样做会带来性能成本。
 * <p> 此类中的功能可能由需要在调用时访问资源的目标对象使用。然而，当有合理的替代方案时，不应使用这种方法，因为它使应用程序代码依赖于 AOP 下的使用，特别是 Spring 
 * AOP 框架。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 */
public final class AopContext {

	/**
	 * 与该线程关联的 AOP 代理的 ThreadLocal 持有者。将包含 {@code null}，除非控制代理配置上的“exposeProxy”属性已设置为“true”。
	 * @see ProxyConfig#setExposeProxy
	 */
	private static final ThreadLocal<Object> currentProxy = new NamedThreadLocal<>("Current AOP proxy");


	/**
	 * 创建 `AopContext` 的新实例。
	 */
	private AopContext() {
	}


	/**
	 * 尝试返回当前的AOP代理。仅当通过 AOP 调用调用方法并且 AOP 框架已设置为公开代理时，此方法才可用。否则，此方法将抛出 IllegalStateException。
	 * @return 当前 AOP 代理（从不返回 {@code null}）
	 * @throws IllegalStateException 如果找不到代理，因为该方法是在 AOP 调用上下文之外调用的，或者因为 AOP 框架尚未配置为公开代理
	 */
	public static Object currentProxy() throws IllegalStateException {
		Object proxy = currentProxy.get();
		if (proxy == null) {
			throw new IllegalStateException(
					"Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available, and " +
							"ensure that AopContext.currentProxy() is invoked in the same thread as the AOP invocation context.");
		}
		return proxy;
	}

	/**
	 * 通过 {@code currentProxy()} 方法使给定代理可用。 <p>请注意，调用者应注意适当保留旧值。
	 * @param proxy 要公开的代理（或 {@code null} 来重置它）
	 * @return 旧代理，如果没有绑定，可能是 {@code null}
	 * @see #currentProxy()
	 */
	static @Nullable Object setCurrentProxy(@Nullable Object proxy) {
		Object old = currentProxy.get();
		if (proxy != null) {
			currentProxy.set(proxy);
		}
		else {
			currentProxy.remove();
		}
		return old;
	}

}
