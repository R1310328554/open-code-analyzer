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
 * {@code TargetSource} 用于获取 AOP 调用的当前「目标」对象；
 * 若没有环绕通知自行终止拦截器链，
 * 该目标将通过反射被调用。
 *
 * <p>若 {@code TargetSource} 为「静态」，则始终返回同一目标，
 * 便于 AOP 框架优化。动态 TargetSource 可支持池化、热替换等。
 *
 * <p>应用开发者通常无需直接使用 {@code TargetSource}：
 * 这是 AOP 框架接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * 返回本 {@link TargetSource} 所返回目标的类型。
	 * <p>可返回 {@code null}，
	 * 尽管某些 {@code TargetSource} 用法可能仅适用于预定目标类。
	 * @return 本 {@link TargetSource} 所返回目标的类型
	 */
	@Override
	@Nullable Class<?> getTargetClass();

	/**
	 * 对 {@link #getTarget()} 的所有调用是否都返回同一对象？
	 * <p>若是，则无需调用 {@link #releaseTarget(Object)}，
	 * AOP 框架可缓存 {@link #getTarget()} 的返回值。
	 * <p>默认实现返回 {@code false}。
	 * @return 若目标不可变则返回 {@code true}
	 * @see #getTarget
	 */
	default boolean isStatic() {
		return false;
	}

	/**
	 * 返回目标实例。在 AOP 框架调用 AOP 方法调用的「目标」之前立即调用。
	 * @return 包含连接点的目标对象；若无实际目标实例则返回 {@code null}
	 * @throws Exception 若无法解析目标对象
	 */
	@Nullable Object getTarget() throws Exception;

	/**
	 * 释放通过 {@link #getTarget()} 获取的目标对象（若有）。
	 * <p>默认实现为空。
	 * @param target 调用 {@link #getTarget()} 获得的对象
	 * @throws Exception 若无法释放该对象
	 */
	default void releaseTarget(Object target) throws Exception {
	}

}
