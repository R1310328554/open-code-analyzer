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

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import org.springframework.util.Assert;

/**
 * JTA UserTransaction 句柄的适配器，接受 JTA
 * {@link jakarta.transaction.TransactionManager} 引用并为其创建
 * JTA {@link jakarta.transaction.UserTransaction} 句柄。
 *
 * <p>JTA UserTransaction 接口是 JTA TransactionManager 接口的精确子集。
 * 遗憾的是它并非 TransactionManager 的超接口，
 * 因此需要通过本类这样的适配器，才能以 UserTransaction 接口与 TransactionManager 句柄交互。
 *
 * <p>由 Spring 的 {@link JtaTransactionManager} 在特定场景内部使用。
 * 不供应用代码直接使用。
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 */
public class UserTransactionAdapter implements UserTransaction {

	private final TransactionManager transactionManager;


	/**
	 * 为给定 TransactionManager 创建新的 UserTransactionAdapter。
	 * @param transactionManager 要包装的 JTA TransactionManager
	 */
	public UserTransactionAdapter(TransactionManager transactionManager) {
		Assert.notNull(transactionManager, "TransactionManager must not be null");
		this.transactionManager = transactionManager;
	}

	/**
	 * 返回本适配器委托的 JTA TransactionManager。
	 */
	public final TransactionManager getTransactionManager() {
		return this.transactionManager;
	}


	@Override
	public void setTransactionTimeout(int timeout) throws SystemException {
		this.transactionManager.setTransactionTimeout(timeout);
	}

	@Override
	public void begin() throws NotSupportedException, SystemException {
		this.transactionManager.begin();
	}

	@Override
	public void commit()
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, SystemException {
		this.transactionManager.commit();
	}

	@Override
	public void rollback() throws SecurityException, SystemException {
		this.transactionManager.rollback();
	}

	@Override
	public void setRollbackOnly() throws SystemException {
		this.transactionManager.setRollbackOnly();
	}

	@Override
	public int getStatus() throws SystemException {
		return this.transactionManager.getStatus();
	}

}
