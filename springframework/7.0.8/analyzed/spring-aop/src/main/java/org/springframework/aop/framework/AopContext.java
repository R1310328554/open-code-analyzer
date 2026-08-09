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
 * 包含用于获取当前 AOP 调用信息的静态方法的类。
 *
 * <p>若 AOP 框架配置为暴露当前代理（非默认），则 {@code currentProxy()} 可用。
 * 它返回正在使用的 AOP 代理。目标对象或 advice 可借此发起带通知的调用，
 * 类似 EJB 中的 {@code getEJBObject()}，也可用于查找 advice 配置。
 *
 * <p>Spring AOP 默认不暴露代理，因存在性能开销。
 *
 * <p>需要访问调用上下文中资源的目标对象可能使用本类功能。
 * 但有合理替代方案时不应使用，以免应用代码依赖 AOP 及 Spring AOP 框架。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 */
public final class AopContext {

	/**
	 * 与本线程关联的 AOP 代理的 ThreadLocal 持有者。
	 * 除非控制代理配置的 "exposeProxy" 属性设为 "true"，否则为 {@code null}。
	 * @see ProxyConfig#setExposeProxy
	 */
	private static final ThreadLocal<Object> currentProxy = new NamedThreadLocal<>("Current AOP proxy");


	private AopContext() {
	}


	/**
	 * 尝试返回当前 AOP 代理。仅当调用方法通过 AOP 调用且框架已配置暴露代理时可用。
	 * 否则抛出 IllegalStateException。
	 * @return 当前 AOP 代理（永不返回 {@code null}）
	 * @throws IllegalStateException 若找不到代理，因方法在 AOP 调用上下文外调用，
	 * 或 AOP 框架未配置暴露代理
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
	 * 通过 {@code currentProxy()} 方法使给定代理可用。
	 * <p>注意：调用方应妥善保留旧值。
	 * @param proxy 要暴露的代理（或 {@code null} 以重置）
	 * @return 旧代理，未绑定时可为 {@code null}
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
