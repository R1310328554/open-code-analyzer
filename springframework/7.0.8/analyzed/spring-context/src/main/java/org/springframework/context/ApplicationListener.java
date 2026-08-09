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

package org.springframework.context;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * 应用事件监听器应实现的接口。
 *
 * <p>基于观察者设计模式的标准 {@link java.util.EventListener} 接口。
 *
 * <p>{@code ApplicationListener} 可泛型声明其关注的事件类型。
 * 注册到 Spring {@code ApplicationContext} 后，事件会据此过滤，
 * 仅对匹配的事件对象调用该监听器。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @param <E> the specific {@code ApplicationEvent} subclass to listen to
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.event.SmartApplicationListener
 * @see org.springframework.context.event.GenericApplicationListener
 * @see org.springframework.context.event.EventListener
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	/**
	 * 处理应用事件。
	 * @param event the event to respond to
	 */
	void onApplicationEvent(E event);

	/**
	 * 返回本监听器是否支持异步执行。
	 * @return {@code true} if this listener instance can be executed asynchronously
	 * depending on the multicaster configuration (the default), or {@code false} if it
	 * needs to immediately run within the original thread which published the event
	 * @since 6.1
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster#setTaskExecutor
	 */
	default boolean supportsAsyncExecution() {
		return true;
	}


	/**
	 * 为给定载荷消费者创建新的 {@code ApplicationListener}。
	 * @param consumer the event payload consumer
	 * @param <T> the type of the event payload
	 * @return a corresponding {@code ApplicationListener} instance
	 * @since 5.3
	 * @see PayloadApplicationEvent
	 */
	static <T> ApplicationListener<PayloadApplicationEvent<T>> forPayload(Consumer<T> consumer) {
		return event -> consumer.accept(event.getPayload());
	}

}
