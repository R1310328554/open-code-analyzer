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

import org.springframework.transaction.TransactionDefinition;

/**
 * {@link TransactionDefinition} 的扩展变体，表示资源事务，
 * 并特别指出事务资源是否可进行本地优化。
 *
 * @author Juergen Hoeller
 * @since 5.1
 * @see ResourceTransactionManager
 */
public interface ResourceTransactionDefinition extends TransactionDefinition {

	/**
	 * 判断事务资源是否可进行本地优化。
	 * @return 若资源已知完全局限于当前事务、不影响事务范围外任何操作则返回 {@code true}
	 * @see #isReadOnly()
	 */
	boolean isLocalResource();

}
