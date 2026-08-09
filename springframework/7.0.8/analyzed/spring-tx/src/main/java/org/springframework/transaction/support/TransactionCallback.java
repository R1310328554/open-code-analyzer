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

package org.springframework.transaction.support;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionStatus;

/**
 * 事务代码的回调接口。与 {@link TransactionTemplate} 的
 * {@code execute} 方法配合使用，常在方法实现中以匿名类形式出现。
 *
 * <p>通常用于将多个对无事务感知数据访问服务的调用
 * 组装到带事务边界的高层服务方法中。
 * 也可考虑声明式事务边界（例如通过 Spring 的
 * {@link org.springframework.transaction.annotation.Transactional} 注解）。
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @param <T> 结果类型
 * @see TransactionTemplate
 * @see CallbackPreferringPlatformTransactionManager
 */
@FunctionalInterface
public interface TransactionCallback<T extends @Nullable Object> {

	/**
	 * 在事务上下文中由 {@link TransactionTemplate#execute} 调用。
	 * 本身无需关心事务，但可通过给定 status 对象获取并影响当前事务状态，
	 * 例如设置 rollback-only。
	 * <p>允许返回在事务内创建的结果对象，即领域对象或领域对象集合。
	 * 回调抛出的 RuntimeException 视为强制回滚的应用异常。
	 * 此类异常会传播给模板调用方，除非回滚出现问题，
	 * 此时将抛出 TransactionException。
	 * @param status 关联的事务状态
	 * @return 结果对象，或 {@code null}
	 * @see TransactionTemplate#execute
	 * @see CallbackPreferringPlatformTransactionManager#execute
	 */
	T doInTransaction(TransactionStatus status);

}
