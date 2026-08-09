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

package org.springframework.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

/**
 * AOP 联盟 {@link org.aopalliance.intercept.MethodInvocation} 接口的扩展，允许访问方法调用所通过的代理。 <p> 有助于在
 * 必要时用代理替换返回值，例如，如果调用目标返回自身。
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 1.1.3
 * @see org.springframework.aop.framework.ReflectiveMethodInvocation
 * @see org.springframework.aop.support.DelegatingIntroductionInterceptor
 */
public interface ProxyMethodInvocation extends MethodInvocation {

	/**
	* 返回此方法调用所通过的代理。
	* @return 原始代理对象
	*/
	Object getProxy();

	/**
	* 创建该对象的克隆。如果在对此对象调用 {@code proceed()} 之前完成克隆，则每次克隆都可以调用 {@code proceed()} 一次，以多次调用连接点（以及建
	* 议链的其余部分）。
	* @return 此调用的可调用克隆。每个克隆可以调用 {@code proceed()} 一次。
	*/
	MethodInvocation invocableClone();

	/**
	* 创建该对象的克隆。如果在对此对象调用 {@code proceed()} 之前完成克隆，则每次克隆都可以调用 {@code proceed()} 一次，以多次调用连接点（以及建
	* 议链的其余部分）。
	* @param arguments 克隆调用应该使用的参数，覆盖原始参数
	* @return 此调用的可调用克隆。每个克隆可以调用 {@code proceed()} 一次。
	*/
	MethodInvocation invocableClone(@Nullable Object... arguments);

	/**
	* 设置要在此链中的任何建议中的后续调用中使用的参数。
	* @param arguments 参数数组
	*/
	void setArguments(@Nullable Object... arguments);

	/**
	* 将具有给定值的指定用户属性添加到此调用。 <p>此类属性在AOP框架本身内不使用。它们只是作为调用对象的一部分保留，以供特殊拦截器使用。
	* @param key 属性的名称
	* @param value 属性的值，或 {@code null} 来重置它
	*/
	void setUserAttribute(String key, @Nullable Object value);

	/**
	 * 返回指定用户属性的值。
	 * @param key 属性的名称
	 * @return {@code null}
	 * @see #setUserAttribute
	 */
	@Nullable Object getUserAttribute(String key);

}
