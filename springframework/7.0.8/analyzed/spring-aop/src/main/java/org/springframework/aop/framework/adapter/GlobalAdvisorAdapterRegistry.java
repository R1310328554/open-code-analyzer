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

package org.springframework.aop.framework.adapter;

/**
 * 单例：发布共享的 DefaultAdvisorAdapterRegistry 实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @see DefaultAdvisorAdapterRegistry
 */
public final class GlobalAdvisorAdapterRegistry {

	private GlobalAdvisorAdapterRegistry() {
	}


	/**
	 * 维护单一实例，以便返回给请求的类。
	 */
	private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

	/**
	 * 返回单例 {@link DefaultAdvisorAdapterRegistry} 实例。
	 */
	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}

	/**
	 * 重置单例 {@link DefaultAdvisorAdapterRegistry}，
	 * 移除所有 {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) 已注册}
	 * 的适配器。
	 */
	static void reset() {
		instance = new DefaultAdvisorAdapterRegistry();
	}

}
