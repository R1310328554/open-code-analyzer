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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * 简化编程式事务边界与事务异常处理的操作符类。
 *
 * <p>核心方法是 {@link #transactional}，支持对函数式序列代码进行事务包装。
 * 本操作符处理事务生命周期与可能的异常，
 * 使 ReactiveTransactionCallback 实现和调用代码均无需显式处理事务。
 *
 * <p>典型用法：编写使用数据库连接等资源但自身不感知事务的底层数据访问对象。
 * 它们可通过使用本类的高层应用服务处理的事务隐式参与，
 * 通过内部类回调对象调用底层服务。
 *
 * <p><strong>注意：</strong>事务 Publisher 应避免 Subscription 取消。
 * 详见 Spring Framework 参考中的
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-operator-cancel">Cancel Signals</a> 章节。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @author Enric Sala
 * @since 5.2
 * @see #execute
 * @see ReactiveTransactionManager
 */
public interface TransactionalOperator {

	/**
	 * 将给定 Flux 指定的函数式序列包装在事务内。
	 * @param flux 应在事务内执行的 Flux
	 * @return 回调返回的结果 Publisher，无则为 {@code null}
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 TransactionCallback 抛出
	 */
	default <T> Flux<T> transactional(Flux<T> flux) {
		return execute(it -> flux);
	}

	/**
	 * 将给定 Mono 指定的函数式序列包装在事务内。
	 * @param mono 应在事务内执行的 Mono
	 * @return 回调返回的结果 Publisher
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 TransactionCallback 抛出
	 */
	default <T> Mono<T> transactional(Mono<T> mono) {
		return execute(it -> mono).singleOrEmpty();
	}

	/**
	 * 在事务内执行给定回调对象指定的操作。
	 * <p>允许返回事务内创建的结果对象，即领域对象或领域对象集合。
	 * 回调抛出的 RuntimeException 视为强制回滚的致命异常，
	 * 并传播给模板调用方。
	 * @param action 指定事务操作的回调对象
	 * @return 回调返回的结果对象
	 * @throws TransactionException 初始化、回滚或系统错误时
	 * @throws RuntimeException 若 TransactionCallback 抛出
	 */
	<T> Flux<T> execute(TransactionCallback<T> action) throws TransactionException;


	// 静态构建方法

	/**
	 * 使用 {@link ReactiveTransactionManager} 创建新的 {@link TransactionalOperator}，
	 * 使用默认事务。
	 * @param transactionManager 要使用的事务管理策略
	 * @return 事务操作符
	 */
	static TransactionalOperator create(ReactiveTransactionManager transactionManager){
		return create(transactionManager, TransactionDefinition.withDefaults());
	}

	/**
	 * 使用 {@link ReactiveTransactionManager} 和 {@link TransactionDefinition}
	 * 创建新的 {@link TransactionalOperator}。
	 * @param transactionManager 要使用的事务管理策略
	 * @param transactionDefinition 要应用的事务定义
	 * @return 事务操作符
	 */
	static TransactionalOperator create(
			ReactiveTransactionManager transactionManager, TransactionDefinition transactionDefinition){

		return new TransactionalOperatorImpl(transactionManager, transactionDefinition);
	}

}
