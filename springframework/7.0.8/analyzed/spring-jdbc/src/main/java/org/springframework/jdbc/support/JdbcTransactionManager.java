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

package org.springframework.jdbc.support;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 与 {@link JdbcAccessor} 对齐的 {@link DataSourceTransactionManager} 子类，
 * 为 commit 和 rollback 步骤添加通用 JDBC 异常翻译。
 * 通常与 {@link org.springframework.jdbc.core.JdbcTemplate} 配合使用，
 * 后者默认应用相同的 {@link SQLExceptionTranslator} 基础设施。
 *
 * <p>异常翻译对可串行化事务的 commit 步骤尤为重要（如 Postgres），
 * 并发失败可能在 commit 时才出现，使调用方收到 {@link org.springframework.dao.ConcurrencyFailureException}
 * 而非 {@link org.springframework.transaction.TransactionSystemException}。
 *
 * <p>类似 {@code HibernateTransactionManager} 和 {@code JpaTransactionManager}，
 * 本事务管理器可能在 {@link #commit} 和 {@link #rollback} 时抛出 {@link DataAccessException}。
 * 调用代码应准备好处理此类异常以及 {@link org.springframework.transaction.TransactionException}，
 * 因为 {@code TransactionSynchronization} 实现也可能在 {@code flush} 和 {@code beforeCommit} 阶段抛出此类异常。
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 5.3
 * @see DataSourceTransactionManager
 * @see #setDataSource
 * @see #setExceptionTranslator
 */
@SuppressWarnings("serial")
public class JdbcTransactionManager extends DataSourceTransactionManager {

	private volatile @Nullable SQLExceptionTranslator exceptionTranslator;

	private boolean lazyInit = true;


	/**
	 * 创建 {@code JdbcTransactionManager} 新实例，使用前必须设置 {@code DataSource}。
	 * @see #setDataSource
	 */
	public JdbcTransactionManager() {
		super();
	}

	/**
	 * 创建 {@code JdbcTransactionManager} 新实例。
	 * @param dataSource 要管理事务的 JDBC DataSource
	 */
	public JdbcTransactionManager(DataSource dataSource) {
		this();
		setDataSource(dataSource);
		afterPropertiesSet();
	}


	/**
	 * 指定本事务管理器操作的 {@code DataSource} 的数据库产品名称。
	 * 无需从 {@code DataSource} 获取 {@code Connection} 读取元数据即可初始化 {@link SQLErrorCodeSQLExceptionTranslator}。
	 * @param dbName 标识错误码条目的数据库产品名称
	 * @see #setExceptionTranslator
	 * @see SQLErrorCodeSQLExceptionTranslator#setDatabaseProductName
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 * @see JdbcAccessor#setDatabaseProductName
	 */
	public void setDatabaseProductName(String dbName) {
		if (SQLErrorCodeSQLExceptionTranslator.hasUserProvidedErrorCodesFile()) {
			this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
		}
		else {
			this.exceptionTranslator = new SQLExceptionSubclassTranslator();
		}
	}

	/**
	 * 设置本事务管理器的异常翻译器。
	 * <p>若类路径根目录存在用户提供的 `sql-error-codes.xml`，默认使用 {@link SQLErrorCodeSQLExceptionTranslator}；
	 * 否则自 6.0 起默认使用 {@link SQLExceptionSubclassTranslator}。
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLExceptionSubclassTranslator
	 * @see JdbcAccessor#setExceptionTranslator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * 返回本实例使用的异常翻译器，必要时创建默认翻译器。
	 * @see #setExceptionTranslator
	 */
	public SQLExceptionTranslator getExceptionTranslator() {
		SQLExceptionTranslator exceptionTranslator = this.exceptionTranslator;
		if (exceptionTranslator != null) {
			return exceptionTranslator;
		}
		synchronized (this) {
			exceptionTranslator = this.exceptionTranslator;
			if (exceptionTranslator == null) {
				if (SQLErrorCodeSQLExceptionTranslator.hasUserProvidedErrorCodesFile()) {
					exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(obtainDataSource());
				}
				else {
					exceptionTranslator = new SQLExceptionSubclassTranslator();
				}
				this.exceptionTranslator = exceptionTranslator;
			}
			return exceptionTranslator;
		}
	}

	/**
	 * 设置是否在首次遇到 SQLException 时延迟初始化 SQLExceptionTranslator，默认 "true"；
	 * 设为 "false" 可在启动时初始化。
	 * <p>提前初始化仅在调用 {@code afterPropertiesSet()} 时生效。
	 * @see #getExceptionTranslator()
	 * @see #afterPropertiesSet()
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * 返回是否延迟初始化 SQLExceptionTranslator。
	 * @see #getExceptionTranslator()
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	/**
	 * 按需提前初始化异常翻译器，未设置时为指定 DataSource 创建默认翻译器。
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}


	/**
	 * 本实现尝试使用 {@link SQLExceptionTranslator}，
	 * 回退到 {@link org.springframework.transaction.TransactionSystemException}。
	 * @see #getExceptionTranslator()
	 * @see DataSourceTransactionManager#translateException
	 */
	@Override
	protected RuntimeException translateException(String task, SQLException ex) {
		DataAccessException dae = getExceptionTranslator().translate(task, null, ex);
		if (dae != null) {
			return dae;
		}
		return super.translateException(task, ex);
	}

}
