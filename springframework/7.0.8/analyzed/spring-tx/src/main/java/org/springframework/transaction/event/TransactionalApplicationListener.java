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

package org.springframework.transaction.event;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.Ordered;

/**
 * 根据 {@link TransactionPhase} 调用的 {@link ApplicationListener}。
 * 这是 {@link TransactionalEventListener} 注解的编程式等价物。
 *
 * <p>在监听器实现上添加 {@link org.springframework.core.Ordered}
 * 可让该监听器在事务完成前后运行的其他监听器中优先执行。
 *
 * <p>自 6.1 起，事务事件监听器可与由
 * {@link org.springframework.transaction.PlatformTransactionManager} 管理的线程绑定事务
 * 以及由 {@link org.springframework.transaction.ReactiveTransactionManager} 管理的响应式事务配合工作。
 * 对于前者，监听器保证能看到当前线程绑定的事务。
 * 由于后者使用 Reactor 上下文而非线程局部变量，
 * 事务上下文需作为事件源包含在发布的事件实例中：
 * 参见 {@link org.springframework.transaction.reactive.TransactionalEventPublisher}。
 *
 * @author Juergen Hoeller
 * @author Oliver Drotbohm
 * @since 5.3
 * @param <E> 要监听的特定 {@code ApplicationEvent} 子类
 * @see TransactionalEventListener
 * @see TransactionalApplicationListenerAdapter
 * @see #forPayload
 */
public interface TransactionalApplicationListener<E extends ApplicationEvent>
		extends ApplicationListener<E>, Ordered {

	/**
	 * 返回事务同步内的执行顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。
	 * @see org.springframework.transaction.support.TransactionSynchronization#getOrder()
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * 事务同步监听器不支持异步执行，
	 * 仅其目标监听器（{@link #processEvent}）可能支持。
	 * @since 6.1
	 */
	@Override
	default boolean supportsAsyncExecution() {
		return false;
	}

	/**
	 * 返回监听器标识符以便单独引用。
	 * <p>特定完成回调实现可能需要提供特定 id，
	 * 其他场景下空字符串（常见默认值）也可接受。
	 * @see org.springframework.context.event.SmartApplicationListener#getListenerId()
	 * @see TransactionalEventListener#id
	 * @see #addCallback
	 */
	default String getListenerId() {
		return "";
	}

	/**
	 * 返回监听器将被调用的事务 {@link TransactionPhase}。
	 * <p>默认阶段为 {@link TransactionPhase#AFTER_COMMIT}。
	 */
	default TransactionPhase getTransactionPhase() {
		return TransactionPhase.AFTER_COMMIT;
	}

	/**
	 * 添加在事务同步内处理时调用的回调，
	 * 即在真实事务期间触发 {@link #processEvent} 时。
	 * @param callback 要应用的同步回调
	 */
	void addCallback(SynchronizationCallback callback);

	/**
	 * 立即处理给定的 {@link ApplicationEvent}。与
	 * {@link #onApplicationEvent(ApplicationEvent)} 不同，调用此方法将
	 * 直接处理给定事件，而非推迟到关联的
	 * {@link #getTransactionPhase() 事务阶段}。
	 * @param event 通过目标监听器实现处理的事件
	 */
	void processEvent(E event);


	/**
	 * 为给定 payload 消费者创建新的 {@code TransactionalApplicationListener}，
	 * 在默认阶段 {@link TransactionPhase#AFTER_COMMIT} 中应用。
	 * @param consumer 事件 payload 消费者
	 * @param <T> 事件 payload 的类型
	 * @return 对应的 {@code TransactionalApplicationListener} 实例
	 * @see PayloadApplicationEvent#getPayload()
	 * @see TransactionalApplicationListenerAdapter
	 */
	static <T> TransactionalApplicationListener<PayloadApplicationEvent<T>> forPayload(Consumer<T> consumer) {
		return forPayload(TransactionPhase.AFTER_COMMIT, consumer);
	}

	/**
	 * 为给定 payload 消费者创建新的 {@code TransactionalApplicationListener}。
	 * @param phase 调用监听器的事务阶段
	 * @param consumer 事件 payload 消费者
	 * @param <T> 事件 payload 的类型
	 * @return 对应的 {@code TransactionalApplicationListener} 实例
	 * @see PayloadApplicationEvent#getPayload()
	 * @see TransactionalApplicationListenerAdapter
	 */
	static <T> TransactionalApplicationListener<PayloadApplicationEvent<T>> forPayload(
			TransactionPhase phase, Consumer<T> consumer) {

		TransactionalApplicationListenerAdapter<PayloadApplicationEvent<T>> listener =
				new TransactionalApplicationListenerAdapter<>(event -> consumer.accept(event.getPayload()));
		listener.setTransactionPhase(phase);
		return listener;
	}


	/**
	 * 在同步驱动的事件处理时调用的回调，
	 * 包装目标监听器调用（{@link #processEvent}）。
	 *
	 * @see #addCallback
	 * @see #processEvent
	 */
	interface SynchronizationCallback {

		/**
		 * 在事务事件监听器调用前调用。
		 * @param event 事务同步即将处理的事件
		 */
		default void preProcessEvent(ApplicationEvent event) {
		}

		/**
		 * 在事务事件监听器调用后调用。
		 * @param event 事务同步已完成处理的事件
		 * @param ex 监听器调用期间发生的异常（若有）
		 */
		default void postProcessEvent(ApplicationEvent event, @Nullable Throwable ex) {
		}
	}

}
