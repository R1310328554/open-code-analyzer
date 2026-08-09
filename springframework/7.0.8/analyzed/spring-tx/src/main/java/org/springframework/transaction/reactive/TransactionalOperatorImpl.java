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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;

/**
 * 简化编程式事务边界与事务异常处理的操作符类。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @author Enric Sala
 * @since 5.2
 * @see #execute
 * @see ReactiveTransactionManager
 */
final class TransactionalOperatorImpl implements TransactionalOperator {

	private static final Log logger = LogFactory.getLog(TransactionalOperatorImpl.class);

	private final ReactiveTransactionManager transactionManager;

	private final TransactionDefinition transactionDefinition;


	/**
	 * 使用给定事务管理器构造新 TransactionTemplate，
	 * 从给定事务定义复制默认设置。仍可设置本地属性以更改值。
	 * @param transactionManager 要使用的事务管理策略
	 * @param transactionDefinition 复制默认设置来源的事务定义
	 */
	TransactionalOperatorImpl(ReactiveTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
		Assert.notNull(transactionManager, "ReactiveTransactionManager must not be null");
		Assert.notNull(transactionDefinition, "TransactionDefinition must not be null");
		this.transactionManager = transactionManager;
		this.transactionDefinition = transactionDefinition;
	}


	/**
	 * 返回要使用的事务管理策略。
	 */
	public ReactiveTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public <T> Flux<T> execute(TransactionCallback<T> action) throws TransactionException {
		return TransactionContextManager.currentContext().flatMapMany(context ->
			Flux.usingWhen(
				this.transactionManager.getReactiveTransaction(this.transactionDefinition),
				action::doInTransaction,
				this.transactionManager::commit,
				this::rollbackOnException,
				this.transactionManager::rollback)
			.onErrorMap(this::unwrapIfResourceCleanupFailure))
		.contextWrite(TransactionContextManager.getOrCreateContext())
		.contextWrite(TransactionContextManager.getOrCreateContextHolder());
	}

	/**
	 * 执行回滚，正确处理回滚异常。
	 * @param status 表示事务的对象
	 * @param ex 抛出的应用异常或错误
	 * @throws TransactionException 回滚错误时
	 */
	private Mono<Void> rollbackOnException(ReactiveTransaction status, Throwable ex) throws TransactionException {
		logger.debug("Initiating transaction rollback on application exception", ex);
		return this.transactionManager.rollback(status).onErrorMap(ex2 -> {
					logger.error("Application exception overridden by rollback exception", ex);
					if (ex2 instanceof TransactionSystemException tse) {
						tse.initApplicationException(ex);
					}
					else {
						ex2.addSuppressed(ex);
					}
					return ex2;
				}
		);
	}

	/**
	 * 若由 {@link Flux#usingWhen} 中异步资源清理失败产生，解包 throwable 的原因。
	 * @param ex 要尝试解包的 throwable
	 */
	private Throwable unwrapIfResourceCleanupFailure(Throwable ex) {
		if (ex instanceof RuntimeException && ex.getCause() != null) {
			String msg = ex.getMessage();
			if (msg != null && msg.startsWith("Async resource cleanup failed")) {
				return ex.getCause();
			}
		}
		return ex;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (super.equals(other) && (!(other instanceof TransactionalOperatorImpl toi) ||
				getTransactionManager() == toi.getTransactionManager())));
	}

	@Override
	public int hashCode() {
		return getTransactionManager().hashCode();
	}

}
