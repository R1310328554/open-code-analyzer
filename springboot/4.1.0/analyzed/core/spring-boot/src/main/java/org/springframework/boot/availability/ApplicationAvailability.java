/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.availability;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationContext;

/**
 * 提供应用的 {@link AvailabilityState 可用性状态} 信息。
 * <p>
 * 组件可注入此类以获取当前状态信息。要更新应用状态，应直接向应用上下文
 * {@link ApplicationContext#publishEvent 发布} {@link AvailabilityChangeEvent}，
 * 或通过 {@link AvailabilityChangeEvent#publish} 发布。
 *
 * @author Brian Clozel
 * @author Phillip Webb
 * @since 2.3.0
 */
public interface ApplicationAvailability {

	/**
	 * 返回应用的 {@link LivenessState}。
	 *
	 * @return 存活状态
	 */
	default LivenessState getLivenessState() {
		return getState(LivenessState.class, LivenessState.BROKEN);
	}

	/**
	 * 返回应用的 {@link ReadinessState}。
	 *
	 * @return 就绪状态
	 */
	default ReadinessState getReadinessState() {
		return getState(ReadinessState.class, ReadinessState.REFUSING_TRAFFIC);
	}

	/**
	 * 返回应用的 {@link AvailabilityState} 信息。
	 *
	 * @param <S> 状态类型
	 * @param stateType 状态类型
	 * @param defaultState 尚未发布给定类型事件时返回的默认状态（不可为 {@code null}）
	 * @return 可用性状态
	 * @see #getState(Class)
	 */
	<S extends AvailabilityState> S getState(Class<S> stateType, S defaultState);

	/**
	 * 返回应用的 {@link AvailabilityState} 信息。
	 *
	 * @param <S> 状态类型
	 * @param stateType 状态类型
	 * @return 可用性状态；若尚未发布给定类型事件则为 {@code null}
	 * @see #getState(Class, AvailabilityState)
	 */
	<S extends AvailabilityState> @Nullable S getState(Class<S> stateType);

	/**
	 * 返回给定状态类型收到的最后一个 {@link AvailabilityChangeEvent}。
	 *
	 * @param <S> 状态类型
	 * @param stateType 状态类型
	 * @return 最后一次变更事件；若尚未发布给定类型事件则为 {@code null}
	 */
	<S extends AvailabilityState> @Nullable AvailabilityChangeEvent<S> getLastChangeEvent(Class<S> stateType);

}
