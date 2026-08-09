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

package org.springframework.transaction.reactive;

import java.util.function.Function;

import reactor.core.publisher.Mono;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;

/**
 * 在响应式环境中发布事务事件的委托类。
 * 将当前 Reactor 管理的 {@link TransactionContext} 作为
 * 每个待发布 {@link ApplicationEvent} 的源对象。
 *
 * <p>本委托仅为便利。当前 {@link TransactionContext} 也可直接作为事件源，
 * 然后通过 {@link ApplicationEventPublisher}（如 Spring
 * {@link org.springframework.context.ApplicationContext}）发布：
 *
 * <pre class="code">
 * TransactionContextManager.currentContext()
 *     .map(source -> new PayloadApplicationEvent&lt;&gt;(source, "myPayload"))
 *     .doOnSuccess(this.eventPublisher::publishEvent)
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see #publishEvent(Function)
 * @see #publishEvent(Object)
 * @see ApplicationEventPublisher
 */
public class TransactionalEventPublisher {

	private final ApplicationEventPublisher eventPublisher;


	/**
	 * 创建用于在响应式环境中发布事务事件的新委托。
	 * @param eventPublisher 实际使用的事件发布器，
	 * 通常为 Spring {@link org.springframework.context.ApplicationContext}
	 */
	public TransactionalEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}


	/**
	 * 发布由给定函数创建的事件，该函数将事务源对象（{@link TransactionContext}）映射为事件实例。
	 * @param eventCreationFunction 将源对象映射为事件实例的函数，
	 * 例如 {@code source -> new PayloadApplicationEvent&lt;&gt;(source, "myPayload")}
	 * @return 事务事件发布的 Reactor {@link Mono}
	 */
	public Mono<Void> publishEvent(Function<TransactionContext, ApplicationEvent> eventCreationFunction) {
		return TransactionContextManager.currentContext().map(eventCreationFunction)
				.doOnSuccess(this.eventPublisher::publishEvent).then();
	}

	/**
	 * 发布为给定 payload 创建的事件。
	 * @param payload 作为事件发布的 payload
	 * @return 事务事件发布的 Reactor {@link Mono}
	 */
	public Mono<Void> publishEvent(Object payload) {
		if (payload instanceof ApplicationEvent) {
			return Mono.error(new IllegalArgumentException("Cannot publish ApplicationEvent with transactional " +
					"source - publish payload object or use publishEvent(Function<Object, ApplicationEvent>"));
		}
		return publishEvent(source -> new PayloadApplicationEvent<>(source, payload));
	}

}
