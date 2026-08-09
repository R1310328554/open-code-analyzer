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
 * 普通 {@link DataSourceTransactionManager} 的 {@link JdbcAccessor} 对齐子类，为提交和回滚步骤添加常见的 JDBC
 * 异常转换。通常与 {@link org.springframework.jdbc.core.JdbcTemplate} 结合使用，默认情况下应用相同的 {@link
 * SQLExceptionTranslator} 基础结构。
 * <p>Exception 转换与可序列化事务中的提交步骤（例如，在 Postgres 上）特别相关，其中在提交后期可能会发生并发失败。这允许向调用者抛出 {@link
 * org.springframework.dao.ConcurrencyFailureException} 而不是 {@link
 * org.springframework.transaction.TransactionSystemException}。
 * <p> 与 {@code HibernateTransactionManager} 和 {@code JpaTransactionManager} 类似，该事务管理器可能会从
 * {@link #commit} 中抛出 {@link DataAccessException}，也可能从 {@link #rollback} 中抛出 {@link
 * DataAccessException}。调用代码应该准备好在 {@link
 * org.springframework.transaction.TransactionException} 旁边处理此类异常，这通常是明智的，因为 {@code
 * TransactionSynchronization} 实现也可能在其 {@code flush} 和 {@code beforeCommit} 阶段抛出此类异常。
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 5.3
 * @see DataSourceTransactionManager
 * @see #setDataSource
 * @see #setExceptionTranslator
 */
@SuppressWarnings("serial")
public class JdbcTransactionManager extends DataSourceTransactionManager {

	/** 异常相关状态（`exceptionTranslator`）。 */
	private volatile @Nullable SQLExceptionTranslator exceptionTranslator;

	/** `true`：该类的成员状态。 */
	private boolean lazyInit = true;


	/**
	 * 创建一个新的 {@code JdbcTransactionManager} 实例。必须设置 {@code DataSource} 才能使用它。
	 * @see #setDataSource
	 */
	public JdbcTransactionManager() {
		super();
	}

	/**
	 * 创建一个新的 {@code JdbcTransactionManager} 实例。
	 * @param dataSource 用于管理事务的 JDBC 数据源
	 */
	public JdbcTransactionManager(DataSource dataSource) {
		this();
		setDataSource(dataSource);
		afterPropertiesSet();
	}


	/**
	 * 指定此事务管理器操作的 {@code DataSource} 的数据库产品名称。这允许初始化 {@link
	 * SQLErrorCodeSQLExceptionTranslator}，而无需从 {@code DataSource} 获取 {@code Connection}
	 * 来获取元数据。
	 * @param dbName 标识错误代码条目的数据库产品名称
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
	 * 为此事务管理器设置异常转换器。如果在类路径的根目录中找到用户提供的 `sql-error-codes.xml` 文件，则默认使用 <p>A {@link SQLErrorCod
	 * eSQLExceptionTranslator}。否则，从 6.0 开始，{@link SQLExceptionSubclassTranslator} 将作为默认转换器。
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLExceptionSubclassTranslator
	 * @see JdbcAccessor#setExceptionTranslator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * 返回用于此实例的异常转换器，并在必要时创建默认值。
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
	 * 设置是否在第一次遇到 SQLException 时延迟初始化此事务管理器的
	 * SQLExceptionTranslator。默认为“true”；可以切换为“false”以在启动时进行初始化。 <p>早期初始化仅在调用 {@code
	 * afterPropertiesSet()} 时适用。
	 * @see #getExceptionTranslator()
	 * @see #afterPropertiesSet()
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * 返回是否延迟初始化该事务管理器的 SQLExceptionTranslator。
	 * @see #getExceptionTranslator()
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	/**
	 * 如果需要，请立即初始化异常转换器，如果没有设置，则为指定的数据源创建一个默认转换器。
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}


	/**
	 * 此实现尝试使用 {@link SQLExceptionTranslator}，并回退到 {@link
	 * org.springframework.transaction.TransactionSystemException}。
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
