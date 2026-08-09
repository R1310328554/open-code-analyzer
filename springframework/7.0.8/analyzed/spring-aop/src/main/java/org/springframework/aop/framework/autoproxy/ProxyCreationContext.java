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

package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NamedThreadLocal;

/**
 * 当前代理创建上下文的持有者，由自动代理创建者（例如 {@link AbstractAdvisorAutoProxyCreator}）公开。
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 */
public final class ProxyCreationContext {

	/**
	 */
	private static final ThreadLocal<String> currentProxiedBeanName =
			new NamedThreadLocal<>("Name of currently proxied bean");


	/**
	 * 创建 `ProxyCreationContext` 的新实例。
	 */
	private ProxyCreationContext() {
	}


	/**
	 * 返回当前代理 bean 实例的名称。
	 * @return bean 的名称，如果没有可用的则为 {@code null}
	 */
	public static @Nullable String getCurrentProxiedBeanName() {
		return currentProxiedBeanName.get();
	}

	/**
	 * 设置当前代理的 Bean 实例的名称。
	 * @param beanName bean 的名称，或用于重置它的 {@code null}
	 */
	static void setCurrentProxiedBeanName(@Nullable String beanName) {
		if (beanName != null) {
			currentProxiedBeanName.set(beanName);
		}
		else {
			currentProxiedBeanName.remove();
		}
	}

}
