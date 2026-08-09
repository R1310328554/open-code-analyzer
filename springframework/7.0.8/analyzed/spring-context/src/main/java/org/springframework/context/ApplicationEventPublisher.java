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

/**
 * 封装事件发布功能的接口。
 *
 * <p>作为 {@link ApplicationContext} 的超接口。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.1.1
 * @see ApplicationContext
 * @see ApplicationEventPublisherAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.event.EventPublicationInterceptor
 * @see org.springframework.transaction.event.TransactionalApplicationListener
 */
@FunctionalInterface
public interface ApplicationEventPublisher {

	/**
	 * 通知本应用中所有<strong>匹配</strong>的已注册监听器：发生了应用事件。
	 * 事件可以是框架事件（如 ContextRefreshedEvent）或应用特定事件。
	 * <p>此类事件发布步骤实质上是移交给多播器，并不意味着同步/异步执行，
	 * 甚至不意味着立即执行。鼓励监听器尽可能高效，对耗时且可能阻塞的操作
	 * 各自使用异步执行。
	 * <p>在响应式调用栈中使用时，可将事件发布作为简单移交：
	 * {@code Mono.fromRunnable(() -> eventPublisher.publishEvent(...))}。
	 * 与任何异步执行一样，响应式监听方法中无法使用线程局部数据。
	 * 处理事件所需的全部状态须包含在事件实例本身中。
	 * <p>若要在响应式移交中便捷包含当前事务上下文，可考虑使用
	 * {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(java.util.function.Function)}。
	 * 对于线程绑定事务，无需如此，因为状态会通过线程局部存储隐式可用。
	 * @param event the event to publish
	 * @see #publishEvent(Object)
	 * @see ApplicationListener#supportsAsyncExecution()
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	/**
	 * 通知本应用中所有<strong>匹配</strong>的已注册监听器：发生了事件。
	 * <p>若指定的 {@code event} 不是 {@link ApplicationEvent}，
	 * 则包装为 {@link PayloadApplicationEvent}。
	 * <p>此类事件发布步骤实质上是移交给多播器，并不意味着同步/异步执行，
	 * 甚至不意味着立即执行。鼓励监听器尽可能高效，对耗时且可能阻塞的操作
	 * 各自使用异步执行。
	 * <p>若要在响应式移交中便捷包含当前事务上下文，可考虑使用
	 * {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(Object)}。
	 * 对于线程绑定事务，无需如此，因为状态会通过线程局部存储隐式可用。
	 * @param event the event to publish
	 * @since 4.2
	 * @see #publishEvent(ApplicationEvent)
	 * @see PayloadApplicationEvent
	 */
	void publishEvent(Object event);

}
