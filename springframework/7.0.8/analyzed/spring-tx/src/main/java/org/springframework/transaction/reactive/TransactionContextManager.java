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

import java.util.ArrayDeque;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.transaction.NoTransactionException;

/**
 * 注册和获取事务上下文的委托类。
 *
 * <p>通常由拦截或编排事务流的组件使用，
 * 如 AOP 拦截器或事务操作符。
 *
 * @author Mark Paluch
 * @since 5.2
 * @see TransactionSynchronization
 */
public abstract class TransactionContextManager {

	private TransactionContextManager() {
	}


	/**
	 * 从订阅者上下文或事务上下文持有者获取当前 {@link TransactionContext}。
	 * 若未注册上下文或上下文持有者，获取将失败并抛出 NoTransactionException。
	 * @return 当前 {@link TransactionContext}
	 * @throws NoTransactionException 若订阅者上下文中未找到 TransactionContext
	 * 或持有者中无上下文
	 */
	public static Mono<TransactionContext> currentContext() {
		return Mono.deferContextual(ctx -> {
			if (ctx.hasKey(TransactionContext.class)) {
				return Mono.just(ctx.get(TransactionContext.class));
			}
			if (ctx.hasKey(TransactionContextHolder.class)) {
				TransactionContextHolder holder = ctx.get(TransactionContextHolder.class);
				if (holder.hasContext()) {
					return Mono.just(holder.currentContext());
				}
			}
			return Mono.error(new NoTransactionInContextException());
		});
	}

	/**
	 * 创建 {@link TransactionContext} 并在订阅者 {@link Context} 中注册。
	 * @return 函数式上下文注册。
	 * @throws IllegalStateException 若已关联事务上下文。
	 * @see Mono#contextWrite(Function)
	 * @see Flux#contextWrite(Function)
	 */
	public static Function<Context, Context> createTransactionContext() {
		return context -> context.put(TransactionContext.class, new TransactionContext());
	}

	/**
	 * 返回用于创建或关联新 {@link TransactionContext} 的 {@link Function}。
	 * 通过 {@link TransactionSynchronizationManager} 与事务资源交互
	 * 需要在订阅者上下文中注册 TransactionContext。
	 * @return 函数式上下文注册。
	 */
	public static Function<Context, Context> getOrCreateContext() {
		return context -> {
			TransactionContextHolder holder = context.get(TransactionContextHolder.class);
			if (holder.hasContext()) {
				return context.put(TransactionContext.class, holder.currentContext());
			}
			return context.put(TransactionContext.class, holder.createContext());
		};
	}

	/**
	 * 返回用于创建或关联新 {@link TransactionContextHolder} 的 {@link Function}。
	 * 响应式流内事务的创建与释放需要遵循自上而下执行方案的可变持有者。
	 * Reactor 订阅者上下文在变更可见性上采用自下而上方式。
	 * @return 函数式上下文注册。
	 */
	public static Function<Context, Context> getOrCreateContextHolder() {
		return context -> {
			if (!context.hasKey(TransactionContextHolder.class)) {
				return context.put(TransactionContextHolder.class, new TransactionContextHolder(new ArrayDeque<>()));
			}
			return context;
		};
	}


	/**
	 * 用于响应式流的 {@link NoTransactionException} 无堆栈变体。
	 */
	@SuppressWarnings("serial")
	private static class NoTransactionInContextException extends NoTransactionException {

		public NoTransactionInContextException() {
			super("No transaction in context");
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			// 无堆栈异常
			return this;
		}
	}

}
