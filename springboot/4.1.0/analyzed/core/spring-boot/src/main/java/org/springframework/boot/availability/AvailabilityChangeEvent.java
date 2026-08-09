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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * 应用 {@link AvailabilityState} 发生变化时发送的 {@link ApplicationEvent}。
 * <p>
 * 任何应用组件均可发送此类事件以更新应用状态。
 *
 * @param <S> 可用性状态类型
 * @author Brian Clozel
 * @author Phillip Webb
 * @since 2.3.0
 */
public class AvailabilityChangeEvent<S extends AvailabilityState> extends PayloadApplicationEvent<S> {

	/**
	 * 创建新的 {@link AvailabilityChangeEvent} 实例。
	 *
	 * @param source 事件源
	 * @param state 可用性状态（不可为 {@code null}）
	 */
	public AvailabilityChangeEvent(Object source, S state) {
		super(source, state);
	}

	/**
	 * 返回变更后的可用性状态。
	 *
	 * @return 可用性状态
	 */
	public S getState() {
		return getPayload();
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), getStateType());
	}

	private Class<?> getStateType() {
		S state = getState();
		if (state instanceof Enum<?> enumState) {
			return enumState.getDeclaringClass();
		}
		return state.getClass();
	}

	/**
	 * 向给定应用上下文发布 {@link AvailabilityChangeEvent} 的便捷方法。
	 *
	 * @param <S> 可用性状态类型
	 * @param context 用于发布事件的上下文
	 * @param state 变更后的可用性状态
	 */
	public static <S extends AvailabilityState> void publish(ApplicationContext context, S state) {
		Assert.notNull(context, "'context' must not be null");
		publish(context, context, state);
	}

	/**
	 * 向给定应用上下文发布 {@link AvailabilityChangeEvent} 的便捷方法。
	 *
	 * @param <S> 可用性状态类型
	 * @param publisher 用于发布事件的发布器
	 * @param source 事件源
	 * @param state 变更后的可用性状态
	 */
	public static <S extends AvailabilityState> void publish(ApplicationEventPublisher publisher, Object source,
			S state) {
		Assert.notNull(publisher, "'publisher' must not be null");
		publisher.publishEvent(new AvailabilityChangeEvent<>(source, state));
	}

}
