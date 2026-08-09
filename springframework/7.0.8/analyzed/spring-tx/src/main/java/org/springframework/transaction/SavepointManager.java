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

package org.springframework.transaction;

/**
 * 以通用方式编程管理事务保存点的 API 接口。
 * 由 TransactionStatus 扩展，为特定事务暴露保存点管理功能。
 *
 * <p>注意，保存点仅在活动事务内有效。
 * 仅在高级需求时使用本编程式保存点处理；
 * 否则更宜使用 PROPAGATION_NESTED 的子事务。
 *
 * <p>本接口受 JDBC Savepoint 机制启发，
 * 但不依赖任何特定持久化技术。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionStatus
 * @see TransactionDefinition#PROPAGATION_NESTED
 * @see java.sql.Savepoint
 */
public interface SavepointManager {

	/**
	 * 创建新保存点。可通过 {@code rollbackToSavepoint} 回滚到特定保存点，
	 * 通过 {@code releaseSavepoint} 显式释放不再需要的保存点。
	 * <p>注意，多数事务管理器会在事务完成时自动释放保存点。
	 * @return 保存点对象，传入 {@link #rollbackToSavepoint} 或 {@link #releaseSavepoint}
	 * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时
	 * @throws TransactionException 无法创建保存点时，
	 * 例如事务状态不合适
	 * @see java.sql.Connection#setSavepoint
	 */
	Object createSavepoint() throws TransactionException;

	/**
	 * 回滚到给定保存点。
	 * <p>保存点之后<i>不会</i>自动释放。
	 * 可显式调用 {@link #releaseSavepoint(Object)} 或依赖事务完成时的自动释放。
	 * @param savepoint 要回滚到的保存点
	 * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时
	 * @throws TransactionException 回滚失败时
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	/**
	 * 显式释放给定保存点。
	 * <p>注意，多数事务管理器会在事务完成时自动释放保存点。
	 * <p>若事务完成时最终会正确清理资源，
	 * 实现应尽可能静默失败。
	 * @param savepoint 要释放的保存点
	 * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时
	 * @throws TransactionException 释放失败时
	 * @see java.sql.Connection#releaseSavepoint
	 */
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
