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

import java.util.function.Consumer;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;

/**
 * 标准 {@link ApplicationListener} 接口的扩展变体，
 * 暴露更多元数据，例如支持的事件类型与源类型。
 *
 * <p>自 Spring Framework 4.2 起，本接口取代基于 Class 的
 * {@link SmartApplicationListener}，完整支持泛型事件类型。
 * 自 5.3.5 起正式继承 {@link SmartApplicationListener}，
 * 通过默认方法将 {@link #supportsEventType(Class)} 委托给
 * {@link #supportsEventType(ResolvableType)}。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.2
 * @see SmartApplicationListener
 * @see GenericApplicationListenerAdapter
 */
public interface GenericApplicationListener extends SmartApplicationListener {

	/**
	 * 重写 {@link SmartApplicationListener#supportsEventType(Class)}，
	 * 委托给 {@link #supportsEventType(ResolvableType)}。
	 */
	@Override
	default boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return supportsEventType(ResolvableType.forClass(eventType));
	}

	/**
	 * 判断本监听器是否实际支持给定事件类型。
	 * @param eventType 事件类型（永不为 {@code null}）
	 */
	boolean supportsEventType(ResolvableType eventType);


	/**
	 * 为给定事件类型创建新的 {@code ApplicationListener}。
	 * @param eventType 要监听的事件类型
	 * @param consumer 匹配事件触发时调用的消费者
	 * @param <E> 要监听的特定 {@code ApplicationEvent} 子类
	 * @return 对应的 {@code ApplicationListener} 实例
	 * @since 6.1.3
	 */
	static <E extends ApplicationEvent> GenericApplicationListener forEventType(Class<E> eventType, Consumer<E> consumer) {
		return new GenericApplicationListenerDelegate<>(eventType, consumer);
	}

}
