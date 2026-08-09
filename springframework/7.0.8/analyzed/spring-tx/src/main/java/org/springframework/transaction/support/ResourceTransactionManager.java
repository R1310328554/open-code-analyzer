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

import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@link org.springframework.transaction.PlatformTransactionManager} 接口的扩展，
 * 表示在单一目标资源上运行的原生资源事务管理器。
 * 此类事务管理器与 JTA 事务管理器的区别在于：
 * 不使用 XA 事务登记任意数量的资源，
 * 而是专注于利用单一目标资源的原生能力与简洁性。
 *
 * <p>此接口主要用于对事务管理器进行抽象内省，
 * 向客户端提示其获得的事务管理器类型
 * 以及事务管理器所操作的具体资源。
 *
 * @author Juergen Hoeller
 * @since 2.0.4
 * @see TransactionSynchronizationManager
 */
public interface ResourceTransactionManager extends PlatformTransactionManager {

	/**
	 * 返回此事务管理器所操作的资源工厂，
	 * 例如 JDBC DataSource 或 JMS ConnectionFactory。
	 * <p>该目标资源工厂通常用作
	 * {@link TransactionSynchronizationManager} 按线程绑定资源的键。
	 * @return 目标资源工厂（永不为 {@code null}）
	 * @see TransactionSynchronizationManager#bindResource
	 * @see TransactionSynchronizationManager#getResource
	 */
	Object getResourceFactory();

}
