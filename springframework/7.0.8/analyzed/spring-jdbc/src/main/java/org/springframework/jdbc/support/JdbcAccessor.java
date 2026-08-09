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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.jdbc.core.JdbcTemplate} 和其他 JDBC 访问 DAO
 * 帮助程序的基类，定义常见属性，例如数据源和异常转换器。
 * <p> 不适合直接使用。请参阅 {@link org.springframework.jdbc.core.JdbcTemplate}。
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 28.11.2003
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class JdbcAccessor implements InitializingBean {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 来源相关状态（`dataSource`）。 */
	private @Nullable DataSource dataSource;

	/** 异常相关状态（`exceptionTranslator`）。 */
	private volatile @Nullable SQLExceptionTranslator exceptionTranslator;

	/** `true`：该类的成员状态。 */
	private boolean lazyInit = true;


	/**
	 * 设置从中获取连接的 JDBC 数据源。
	 */
	public void setDataSource(@Nullable DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 返回此模板使用的数据源。
	 */
	public @Nullable DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 获取实际使用的DataSource。
	 * @return 数据源（绝不是 {@code null}）
	 * @throws IllegalStateException 如果没有设置数据源
	 * @since 5.0
	 */
	protected DataSource obtainDataSource() {
		DataSource dataSource = getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return dataSource;
	}

	/**
	 * 指定此访问器使用的 {@code DataSource} 的数据库产品名称。这允许初始化 {@link
	 * SQLErrorCodeSQLExceptionTranslator}，而无需从 {@code DataSource} 获取 {@code Connection}
	 * 来获取元数据。
	 * @param dbName 标识错误代码条目的数据库产品名称
	 * @see #setExceptionTranslator
	 * @see SQLErrorCodeSQLExceptionTranslator#setDatabaseProductName
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
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
	 * 为此实例设置异常转换器。如果在类路径的根目录中找到用户提供的 `sql-error-codes.xml` 文件，则默认使用 <p>A {@link SQLErrorCodeSQ
	 * LExceptionTranslator}。否则，从 6.0 开始，{@link SQLExceptionSubclassTranslator} 将作为默认转换器。
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLExceptionSubclassTranslator
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
	 * 设置是否在第一次遇到 SQLException 时延迟初始化此访问器的
	 * SQLExceptionTranslator。默认为“true”；可以切换为“false”以在启动时进行初始化。 <p>早期初始化仅在调用 {@code
	 * afterPropertiesSet()} 时适用。
	 * @see #getExceptionTranslator()
	 * @see #afterPropertiesSet()
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * 返回是否延迟初始化此访问器的 SQLExceptionTranslator。
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
		if (getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}

}
