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

import java.util.Collection;

/**
 * 事务管理器实现的通用配置接口。
 * 提供 {@link TransactionExecutionListener} 的注册能力。
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see PlatformTransactionManager
 * @see ReactiveTransactionManager
 */
public interface ConfigurableTransactionManager extends TransactionManager {

	/**
	 * 设置本事务管理器的 begin/commit/rollback 回调
	 * 所用的事务执行监听器。
	 * @see #addListener
	 */
	void setTransactionExecutionListeners(Collection<TransactionExecutionListener> listeners);

	/**
	 * 返回本事务管理器已注册的事务执行监听器。
	 * @see #setTransactionExecutionListeners
	 */
	Collection<TransactionExecutionListener> getTransactionExecutionListeners();

	/**
	 * 便捷注册给定监听器，用于本事务管理器的
	 * begin/commit/rollback 回调。
	 * @see #getTransactionExecutionListeners()
	 */
	default void addListener(TransactionExecutionListener listener) {
		getTransactionExecutionListeners().add(listener);
	}

}
