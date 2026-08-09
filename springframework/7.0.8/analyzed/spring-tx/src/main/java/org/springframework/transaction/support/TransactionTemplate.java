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

import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;

/**
 * 简化编程式事务边界与事务异常处理的模板类。
 *
 * <p>核心方法是 {@link #execute}，支持实现 {@link TransactionCallback} 接口
 * 的事务代码。此模板处理事务生命周期及可能出现的异常，
 * 使 TransactionCallback 实现与调用代码均无需显式处理事务。
 *
 * <p>典型用法：编写使用 JDBC DataSource 等资源、
 * 但自身无事务感知的底层数据访问对象。
 * 它们可通过内部类回调对象调用底层服务，
 * 隐式参与使用此类的高层应用服务所管理的事务。
 *
 * <p>可在服务实现中直接实例化并传入事务管理器引用使用，
 * 或在应用上下文中配置后以 Bean 引用传给服务。
 * 注意：事务管理器应始终在应用上下文中配置为 Bean：
 * 第一种情况直接交给服务，第二种情况交给已配置的模板。
 *
 * <p>支持按名称设置传播行为与隔离级别，
 * 便于在上下文定义中配置。
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see #execute
 * @see #setTransactionManager
 * @see org.springframework.transaction.PlatformTransactionManager
 */
@SuppressWarnings("serial")
public class TransactionTemplate extends DefaultTransactionDefinition
		implements TransactionOperations, InitializingBean {

	/** 子类可用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable PlatformTransactionManager transactionManager;


	/**
	 * 构造供 Bean 使用的 TransactionTemplate。
	 * <p>注意：在任何 {@code execute} 调用前须设置 PlatformTransactionManager。
	 * @see #setTransactionManager
	 */
	public TransactionTemplate() {
	}

	/**
	 * 使用给定事务管理器构造 TransactionTemplate。
	 * @param transactionManager 要使用的事务管理策略
	 */
	public TransactionTemplate(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * 使用给定事务管理器构造 TransactionTemplate，
	 * 默认设置取自给定事务定义。
	 * @param transactionManager 要使用的事务管理策略
	 * @param transactionDefinition 复制默认设置来源的事务定义。
	 * 仍可通过本地属性覆盖值。
	 */
	public TransactionTemplate(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
		super(transactionDefinition);
		this.transactionManager = transactionManager;
	}


	/**
	 * 设置要使用的事务管理策略。
	 */
	public void setTransactionManager(@Nullable PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * 返回要使用的事务管理策略。
	 */
	public @Nullable PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("Property 'transactionManager' is required");
		}
	}


	@Override
	public <T extends @Nullable Object> T execute(TransactionCallback<T> action) throws TransactionException {
		Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");

		if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager cpptm) {
			return cpptm.execute(this, action);
		}
		else {
			TransactionStatus status = this.transactionManager.getTransaction(this);
			T result;
			try {
				result = action.doInTransaction(status);
			}
			catch (RuntimeException | Error ex) {
				// 事务代码抛出应用异常 -> 回滚
				rollbackOnException(status, ex);
				throw ex;
			}
			catch (Throwable ex) {
				// 事务代码抛出意外异常 -> 回滚
				rollbackOnException(status, ex);
				throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
			}
			this.transactionManager.commit(status);
			return result;
		}
	}

	/**
	 * 执行回滚，并正确处理回滚异常。
	 * @param status 表示事务的对象
	 * @param ex 抛出的应用异常或错误
	 * @throws TransactionException 回滚出错时
	 */
	private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
		Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");

		logger.debug("Initiating transaction rollback on application exception", ex);
		try {
			this.transactionManager.rollback(status);
		}
		catch (TransactionSystemException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			ex2.initApplicationException(ex);
			throw ex2;
		}
		catch (RuntimeException | Error ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (super.equals(other) && (!(other instanceof TransactionTemplate template) ||
				getTransactionManager() == template.getTransactionManager())));
	}

}
