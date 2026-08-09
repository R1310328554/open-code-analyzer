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

import org.jspecify.annotations.Nullable;

/**
 * {@code TargetSource} 用于获取 AOP 调用的当前“目标”，如果没有周围建议选择结束拦截器链本身，则将通过反射调用该目标。
 * <p>如果 {@code TargetSource} 是“静态”的，它将始终返回相同的目标，从而允许在 AOP 框架中进行优化。动态目标源可以支持池化、热插拔等。
 * <p>应用程序开发人员通常不需要直接使用{@code TargetSources}：这是一个AOP框架接口。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * 返回此 {@link TargetSource} 返回的目标类型。 <p>C 可以返回 {@code null}，尽管 {@code TargetSource}
	 * 的某些方案可能只适用于预定的目标类型。
	 * @return {@link TargetSource} 返回的目标类型
	 */
	@Override
	@Nullable Class<?> getTargetClass();

	/**
	 * 对 {@link #getTarget()} 的所有调用都会返回相同的对象吗？ <p> 这样的话，就不需要调用 {@link
	 * #releaseTarget(Object)}，AOP 框架缓存可以 {@link #getTarget()} 的返回值。 <p> 默认实现返回 {@code false}。
	 * @return 如果目标是不可变的
	 * @see #getTarget
	 */
	default boolean isStatic() {
		return false;
	}

	/**
	* 返回一个目标实例。在 AOP 框架调用 AOP 方法调用的“目标”之前立即调用。
	* @return 包含连接点的目标对象，如果没有实际目标实例，则为 {@code null}
	* @throws Exception 如果目标对象无法解析
	*/
	@Nullable Object getTarget() throws Exception;

	/**
	 * 释放从 {@link #getTarget()} 方法获取的给定目标对象（如果有）。 <p>默认实现为空。
	 * @param target 通过调用 {@link #getTarget()} 获得的对象
	 * @throws Exception 如果对象无法被释放
	 */
	default void releaseTarget(Object target) throws Exception {
	}

}
