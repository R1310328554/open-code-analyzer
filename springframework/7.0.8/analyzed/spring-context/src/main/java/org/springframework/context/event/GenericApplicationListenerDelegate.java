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
import org.springframework.core.ResolvableType;

/**
 * 支持单一事件类型的 {@link GenericApplicationListener} 实现。
 *
 * @author Stephane Nicoll
 * @since 6.1.3
 * @param <E> 要监听的特定 {@code ApplicationEvent} 子类
 */
class GenericApplicationListenerDelegate<E extends ApplicationEvent> implements GenericApplicationListener {

	/** 本监听器支持的事件类型。 */
	private final Class<E> supportedEventType;

	/** 事件触发时调用的消费者。 */
	private final Consumer<E> consumer;


	GenericApplicationListenerDelegate(Class<E> supportedEventType, Consumer<E> consumer) {
		this.supportedEventType = supportedEventType;
		this.consumer = consumer;
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		this.consumer.accept(this.supportedEventType.cast(event));
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		return this.supportedEventType.isAssignableFrom(eventType.toClass());
	}

}
