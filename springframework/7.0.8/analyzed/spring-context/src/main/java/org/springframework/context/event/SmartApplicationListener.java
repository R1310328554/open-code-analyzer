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

package org.springframework.context.event;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * 标准 {@link ApplicationListener} 接口的扩展变体，
 * 暴露更多元数据，例如支持的事件类型与源类型。
 *
 * <p>若需完整内省泛型事件类型，请考虑实现
 * {@link GenericApplicationListener} 接口。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see GenericApplicationListener
 * @see GenericApplicationListenerAdapter
 */
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

	/**
	 * 判断本监听器是否实际支持给定事件类型。
	 * @param eventType 事件类型（永不为 {@code null}）
	 */
	boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

	/**
	 * 判断本监听器是否实际支持给定源类型。
	 * <p>默认实现始终返回 {@code true}。
	 * @param sourceType 源类型，若无源则为 {@code null}
	 */
	default boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return true;
	}

	/**
	 * 确定本监听器在同一事件的监听器集合中的顺序。
	 * <p>默认实现返回 {@link #LOWEST_PRECEDENCE}。
	 */
	@Override
	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	/**
	 * 返回监听器的可选标识符。
	 * <p>默认值为空字符串。
	 * @since 5.3.5
	 * @see EventListener#id
	 * @see ApplicationEventMulticaster#removeApplicationListeners
	 */
	default String getListenerId() {
		return "";
	}

}
