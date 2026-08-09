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

import java.io.Flushable;

/**
 * 由能够返回内部 rollback-only 标记的事务对象实现的接口，
 * 该标记通常来自已参与并将之标记为 rollback-only 的其他事务。
 *
 * <p>由 {@link DefaultTransactionStatus} 自动检测，
 * 以便即使非当前 TransactionStatus 所致也能始终返回当前的 rollbackOnly 标志。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see DefaultTransactionStatus#isGlobalRollbackOnly()
 */
public interface SmartTransactionObject extends Flushable {

	/**
	 * 返回事务是否在内部被标记为 rollback-only。
	 * 例如可检查 JTA UserTransaction。
	 * <p>默认实现返回 {@code false}。
	 * @see jakarta.transaction.UserTransaction#getStatus
	 * @see jakarta.transaction.Status#STATUS_MARKED_ROLLBACK
	 */
	default boolean isRollbackOnly() {
		return false;
	}

	/**
	 * 若适用，将底层 Session flush 到数据存储：
	 * 例如所有受影响的 Hibernate/JPA Session。
	 * <p>默认实现为空，将 flush 视为无操作。
	 */
	@Override
	default void flush() {
	}

}
