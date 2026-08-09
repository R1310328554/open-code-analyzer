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

import org.reactivestreams.Publisher;

import org.springframework.transaction.ReactiveTransaction;

/**
 * 响应式事务代码的回调接口。与 {@link TransactionalOperator} 的
 * {@code execute} 方法配合使用，常在方法实现中作为匿名类。
 *
 * <p>通常用于将多个对事务无感知的数据访问服务调用
 * 组装到带事务边界的高层服务方法中。也可考虑声明式事务边界
 * （例如通过 Spring 的 {@link org.springframework.transaction.annotation.Transactional} 注解）。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @param <T> 结果类型
 * @see TransactionalOperator
 */
@FunctionalInterface
public interface TransactionCallback<T> {

	/**
	 * 在事务上下文中由 {@link TransactionalOperator} 调用。
	 * 无需自行管理事务，但可通过给定状态对象获取并影响当前事务状态，
	 * 例如设置 rollback-only。
	 * @param status 关联的事务状态
	 * @return 结果 Publisher
	 * @see TransactionalOperator#transactional
	 */
	Publisher<T> doInTransaction(ReactiveTransaction status);

}
