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

package org.springframework.transaction.jta;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * 根据指定事务特性创建 JTA {@link jakarta.transaction.Transaction} 对象的策略接口。
 *
 * <p>默认实现 {@link SimpleTransactionFactory} 简单包装标准 JTA
 * {@link jakarta.transaction.TransactionManager}。
 * 本策略接口允许更复杂的实现以适配厂商特定 JTA 扩展。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see jakarta.transaction.TransactionManager#getTransaction()
 * @see SimpleTransactionFactory
 * @see JtaTransactionManager
 */
public interface TransactionFactory {

	/**
	 * 根据给定名称和超时创建活动 Transaction 对象。
	 * @param name 事务名称（可为 {@code null}）
	 * @param timeout 事务超时（-1 表示默认超时）
	 * @return 活动 Transaction 对象（永不为 {@code null}）
	 * @throws NotSupportedException 若事务管理器不支持指定类型的事务
	 * @throws SystemException 若事务管理器创建事务失败
	 */
	Transaction createTransaction(@Nullable String name, int timeout) throws NotSupportedException, SystemException;

	/**
	 * 判断底层事务管理器是否支持由资源适配器管理的 XA 事务
	 * （即无需显式登记 XA 资源）。
	 * <p>通常为 {@code false}。由
	 * {@link org.springframework.jca.endpoint.AbstractMessageEndpointFactory} 检查，
	 * 以区分无效配置与有效的 ResourceAdapter 管理事务。
	 * @see jakarta.resource.spi.ResourceAdapter#endpointActivation
	 * @see jakarta.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted
	 */
	boolean supportsResourceAdapterManagedTransactions();

}
