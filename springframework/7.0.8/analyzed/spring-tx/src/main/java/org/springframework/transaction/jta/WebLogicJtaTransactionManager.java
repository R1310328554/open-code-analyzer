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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;

/**
 * 适用于 Oracle WebLogic 15.1.1 及更高版本的特殊 {@link JtaTransactionManager} 变体。
 * 在 WebLogic 事务协调器上支持 Spring 事务定义的全部能力，<i>超越标准 JTA</i>：
 * 事务名称、每事务隔离级别，以及在所有情况下正确恢复事务。
 *
 * <p>使用 WebLogic 特殊的 {@code begin(name)} 方法启动 JTA 事务，
 * 以便<b>使 Spring 驱动的事务在 WebLogic 事务监视器中可见</b>。
 * 对于 Spring 声明式事务，暴露的名称（默认）为全限定类名 + "." + 方法名。
 *
 * <p>通过 WebLogic 对应的 JTA 事务属性 "ISOLATION LEVEL" 支持<b>每事务隔离级别</b>。
 * 这将把指定隔离级别（如 ISOLATION_SERIALIZABLE）应用于参与该事务的所有 JDBC Connection。
 *
 * <p>若标准 JTA resume 失败，则调用 WebLogic 特殊的 {@code forceResume} 方法，
 * 以<b>在目标事务被标记 rollback-only 时也能恢复</b>。
 * 若本就不依赖事务挂起的此特性，Spring 标准 JtaTransactionManager 也能正常工作。
 *
 * <p>默认情况下，JTA UserTransaction 和 TransactionManager 句柄
 * 直接从 WebLogic 的 {@code TransactionHelper} 获取。
 * 可通过指定 "userTransaction"/"userTransactionName" 和
 * "transactionManager"/"transactionManagerName" 覆盖，传入现有句柄
 * 或指定相应 JNDI 查找位置。
 *
 * <p>注意：本类在 Spring Framework 6.0 中曾移除，
 * 在 WebLogic 15.1.1 发布（最终提供 Jakarta EE 9 兼容性）后重新引入。
 * 自 Spring Framework 6.2.16 起，可再次手动配置——作为标准 {@link JtaTransactionManager} 的替代。
 *
 * @author Juergen Hoeller
 * @since 6.2.16
 * @see org.springframework.transaction.TransactionDefinition#getName()
 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
 */
@SuppressWarnings("serial")
public class WebLogicJtaTransactionManager extends JtaTransactionManager {

	private static final String USER_TRANSACTION_CLASS_NAME = "weblogic.transaction.UserTransaction";

	private static final String CLIENT_TRANSACTION_MANAGER_CLASS_NAME = "weblogic.transaction.ClientTransactionManager";

	private static final String TRANSACTION_CLASS_NAME = "weblogic.transaction.Transaction";

	private static final String TRANSACTION_HELPER_CLASS_NAME = "weblogic.transaction.TransactionHelper";

	private static final String ISOLATION_LEVEL_KEY = "ISOLATION LEVEL";


	private boolean weblogicUserTransactionAvailable;

	private @Nullable Method beginWithNameMethod;

	private @Nullable Method beginWithNameAndTimeoutMethod;

	private boolean weblogicTransactionManagerAvailable;

	private @Nullable Method forceResumeMethod;

	private @Nullable Method setPropertyMethod;

	private @Nullable Object transactionHelper;


	@Override
	public void afterPropertiesSet() throws TransactionSystemException {
		super.afterPropertiesSet();
		loadWebLogicTransactionClasses();
	}

	@Override
	protected @Nullable UserTransaction retrieveUserTransaction() throws TransactionSystemException {
		Object helper = loadWebLogicTransactionHelper();
		try {
			logger.trace("Retrieving JTA UserTransaction from WebLogic TransactionHelper");
			Method getUserTransactionMethod = helper.getClass().getMethod("getUserTransaction");
			return (UserTransaction) getUserTransactionMethod.invoke(this.transactionHelper);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebLogic's TransactionHelper.getUserTransaction() method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not invoke WebLogic's TransactionHelper.getUserTransaction() method", ex);
		}
	}

	@Override
	protected @Nullable TransactionManager retrieveTransactionManager() throws TransactionSystemException {
		Object helper = loadWebLogicTransactionHelper();
		try {
			logger.trace("Retrieving JTA TransactionManager from WebLogic TransactionHelper");
			Method getTransactionManagerMethod = helper.getClass().getMethod("getTransactionManager");
			return (TransactionManager) getTransactionManagerMethod.invoke(this.transactionHelper);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebLogic's TransactionHelper.getTransactionManager() method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not invoke WebLogic's TransactionHelper.getTransactionManager() method", ex);
		}
	}

	private Object loadWebLogicTransactionHelper() throws TransactionSystemException {
		Object helper = this.transactionHelper;
		if (helper == null) {
			try {
				Class<?> transactionHelperClass = getClass().getClassLoader().loadClass(TRANSACTION_HELPER_CLASS_NAME);
				Method getTransactionHelperMethod = transactionHelperClass.getMethod("getTransactionHelper");
				helper = getTransactionHelperMethod.invoke(null);
				this.transactionHelper = helper;
				logger.trace("WebLogic TransactionHelper found");
			}
			catch (InvocationTargetException ex) {
				throw new TransactionSystemException(
						"WebLogic's TransactionHelper.getTransactionHelper() method failed", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new TransactionSystemException(
						"Could not initialize WebLogicJtaTransactionManager because WebLogic API classes are not available",
						ex);
			}
		}
		return helper;
	}

	private void loadWebLogicTransactionClasses() throws TransactionSystemException {
		try {
			Class<?> userTransactionClass = getClass().getClassLoader().loadClass(USER_TRANSACTION_CLASS_NAME);
			this.weblogicUserTransactionAvailable = userTransactionClass.isInstance(getUserTransaction());
			if (this.weblogicUserTransactionAvailable) {
				this.beginWithNameMethod = userTransactionClass.getMethod("begin", String.class);
				this.beginWithNameAndTimeoutMethod = userTransactionClass.getMethod("begin", String.class, int.class);
				logger.debug("Support for WebLogic transaction names available");
			}
			else {
				logger.debug("Support for WebLogic transaction names not available");
			}

			// 获取 WebLogic ClientTransactionManager 接口。
			Class<?> transactionManagerClass =
					getClass().getClassLoader().loadClass(CLIENT_TRANSACTION_MANAGER_CLASS_NAME);
			logger.trace("WebLogic ClientTransactionManager found");

			this.weblogicTransactionManagerAvailable = transactionManagerClass.isInstance(getTransactionManager());
			if (this.weblogicTransactionManagerAvailable) {
				Class<?> transactionClass = getClass().getClassLoader().loadClass(TRANSACTION_CLASS_NAME);
				this.forceResumeMethod = transactionManagerClass.getMethod("forceResume", Transaction.class);
				this.setPropertyMethod = transactionClass.getMethod("setProperty", String.class, Serializable.class);
				logger.debug("Support for WebLogic forceResume available");
			}
			else {
				logger.debug("Support for WebLogic forceResume not available");
			}
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not initialize WebLogicJtaTransactionManager because WebLogic API classes are not available",
					ex);
		}
	}

	private TransactionManager obtainTransactionManager() {
		TransactionManager tm = getTransactionManager();
		Assert.state(tm != null, "No TransactionManager set");
		return tm;
	}


	@Override
	protected void doJtaBegin(JtaTransactionObject txObject, TransactionDefinition definition)
			throws NotSupportedException, SystemException {

		int timeout = determineTimeout(definition);

		// 将事务名称（若有）应用到 WebLogic 事务。
		if (this.weblogicUserTransactionAvailable && definition.getName() != null) {
			try {
				if (timeout > TransactionDefinition.TIMEOUT_DEFAULT) {
					/*
					weblogic.transaction.UserTransaction wut = (weblogic.transaction.UserTransaction) ut;
					wut.begin(definition.getName(), timeout);
					*/
					Assert.state(this.beginWithNameAndTimeoutMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameAndTimeoutMethod.invoke(txObject.getUserTransaction(), definition.getName(), timeout);
				}
				else {
					/*
					weblogic.transaction.UserTransaction wut = (weblogic.transaction.UserTransaction) ut;
					wut.begin(definition.getName());
					*/
					Assert.state(this.beginWithNameMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameMethod.invoke(txObject.getUserTransaction(), definition.getName());
				}
			}
			catch (InvocationTargetException ex) {
				throw new TransactionSystemException(
						"WebLogic's UserTransaction.begin() method failed", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new TransactionSystemException(
						"Could not invoke WebLogic's UserTransaction.begin() method", ex);
			}
		}
		else {
			// 无 WebLogic UserTransaction 或未指定事务名称
			// -> 使用标准 JTA begin 调用。
			applyTimeout(txObject, timeout);
			txObject.getUserTransaction().begin();
		}

		// 若有隔离级别，通过相应 WebLogic 事务属性指定。
		if (this.weblogicTransactionManagerAvailable) {
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				try {
					Transaction tx = obtainTransactionManager().getTransaction();
					Integer isolationLevel = definition.getIsolationLevel();
					/*
					weblogic.transaction.Transaction wtx = (weblogic.transaction.Transaction) tx;
					wtx.setProperty(ISOLATION_LEVEL_KEY, isolationLevel);
					*/
					Assert.state(this.setPropertyMethod != null, "WebLogic JTA API not initialized");
					this.setPropertyMethod.invoke(tx, ISOLATION_LEVEL_KEY, isolationLevel);
				}
				catch (InvocationTargetException ex) {
					throw new TransactionSystemException(
							"WebLogic's Transaction.setProperty(String, Serializable) method failed", ex.getTargetException());
				}
				catch (Exception ex) {
					throw new TransactionSystemException(
							"Could not invoke WebLogic's Transaction.setProperty(String, Serializable) method", ex);
				}
			}
		}
		else {
			applyIsolationLevel(txObject, definition.getIsolationLevel());
		}
	}

	@Override
	protected void doJtaResume(@Nullable JtaTransactionObject txObject, Object suspendedTransaction)
			throws InvalidTransactionException, SystemException {

		try {
			obtainTransactionManager().resume((Transaction) suspendedTransaction);
		}
		catch (InvalidTransactionException ex) {
			if (!this.weblogicTransactionManagerAvailable) {
				throw ex;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Standard JTA resume threw InvalidTransactionException: " + ex.getMessage() +
					" - trying WebLogic JTA forceResume");
			}
			/*
			weblogic.transaction.TransactionManager wtm =
					(weblogic.transaction.TransactionManager) getTransactionManager();
			wtm.forceResume(suspendedTransaction);
			*/
			try {
				Assert.state(this.forceResumeMethod != null, "WebLogic JTA API not initialized");
				this.forceResumeMethod.invoke(getTransactionManager(), suspendedTransaction);
			}
			catch (InvocationTargetException ex2) {
				throw new TransactionSystemException(
						"WebLogic's TransactionManager.forceResume(Transaction) method failed", ex2.getTargetException());
			}
			catch (Exception ex2) {
				throw new TransactionSystemException(
						"Could not access WebLogic's TransactionManager.forceResume(Transaction) method", ex2);
			}
		}
	}

	@Override
	public Transaction createTransaction(@Nullable String name, int timeout) throws NotSupportedException, SystemException {
		if (this.weblogicUserTransactionAvailable && name != null) {
			try {
				if (timeout >= 0) {
					Assert.state(this.beginWithNameAndTimeoutMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameAndTimeoutMethod.invoke(getUserTransaction(), name, timeout);
				}
				else {
					Assert.state(this.beginWithNameMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameMethod.invoke(getUserTransaction(), name);
				}
			}
			catch (InvocationTargetException ex) {
				if (ex.getTargetException() instanceof NotSupportedException) {
					throw (NotSupportedException) ex.getTargetException();
				}
				else if (ex.getTargetException() instanceof SystemException) {
					throw (SystemException) ex.getTargetException();
				}
				else if (ex.getTargetException() instanceof RuntimeException) {
					throw (RuntimeException) ex.getTargetException();
				}
				else {
					throw new SystemException(
							"WebLogic's begin() method failed with an unexpected error: " + ex.getTargetException());
				}
			}
			catch (Exception ex) {
				throw new SystemException("Could not invoke WebLogic's UserTransaction.begin() method: " + ex);
			}
			return new ManagedTransactionAdapter(obtainTransactionManager());
		}

		else {
			// 未指定名称 - 标准 JTA 已足够。
			return super.createTransaction(name, timeout);
		}
	}

}
