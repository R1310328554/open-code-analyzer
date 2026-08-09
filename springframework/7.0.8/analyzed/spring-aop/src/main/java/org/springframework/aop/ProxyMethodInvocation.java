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
 * AOP Alliance {@link org.aopalliance.intercept.MethodInvocation} 接口的扩展，
 * 允许访问本次方法调用所经过的代理。
 *
 * <p>必要时可用代理替换返回值，
 * 例如调用目标返回自身时。
 *
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 1.1.3
 * @see org.springframework.aop.framework.ReflectiveMethodInvocation
 * @see org.springframework.aop.support.DelegatingIntroductionInterceptor
 */
public interface ProxyMethodInvocation extends MethodInvocation {

	/**
	 * 返回本次方法调用所经过的代理。
	 * @return 原始代理对象
	 */
	Object getProxy();

	/**
	 * 创建本对象的克隆。若在调用 {@code proceed()} 之前完成克隆，
	 * 则每个克隆可各调用一次 {@code proceed()}，
	 * 从而多次执行连接点（及后续通知链）。
	 * @return 本调用的可执行克隆；每个克隆可调用一次 {@code proceed()}。
	 */
	MethodInvocation invocableClone();

	/**
	 * 创建本对象的克隆。若在调用 {@code proceed()} 之前完成克隆，
	 * 则每个克隆可各调用一次 {@code proceed()}，
	 * 从而多次执行连接点（及后续通知链）。
	 * @param arguments 克隆调用应使用的参数，覆盖原始参数
	 * @return 本调用的可执行克隆；每个克隆可调用一次 {@code proceed()}。
	 */
	MethodInvocation invocableClone(@Nullable Object... arguments);

	/**
	 * 设置本链中后续任意通知调用将使用的参数。
	 * @param arguments 参数数组
	 */
	void setArguments(@Nullable Object... arguments);

	/**
	 * 向本调用添加指定名称与值的用户属性。
	 * <p>此类属性不在 AOP 框架内部使用，
	 * 仅作为调用对象的一部分保留，供特殊拦截器使用。
	 * @param key 属性名称
	 * @param value 属性值，或 {@code null} 表示重置
	 */
	void setUserAttribute(String key, @Nullable Object value);

	/**
	 * 返回指定用户属性的值。
	 * @param key 属性名称
	 * @return 属性值，未设置时返回 {@code null}
	 * @see #setUserAttribute
	 */
	@Nullable Object getUserAttribute(String key);

}
