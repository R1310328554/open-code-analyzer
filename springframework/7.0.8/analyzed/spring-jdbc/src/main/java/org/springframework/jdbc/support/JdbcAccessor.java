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
 * {@link org.springframework.jdbc.core.JdbcTemplate} 及其他 JDBC 访问 DAO 辅助类的基类，
 * 定义 DataSource 和异常翻译器等公共属性。
 *
 * <p>不供直接使用，参见 {@link org.springframework.jdbc.core.JdbcTemplate}。
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 28.11.2003
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class JdbcAccessor implements InitializingBean {

	/** 供子类使用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable DataSource dataSource;

	private volatile @Nullable SQLExceptionTranslator exceptionTranslator;

	private boolean lazyInit = true;


	/**
	 * 设置用于获取连接的 JDBC DataSource。
	 */
	public void setDataSource(@Nullable DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 返回本模板使用的 DataSource。
	 */
	public @Nullable DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 获取实际使用的 DataSource。
	 * @return DataSource（永不为 {@code null}）
	 * @throws IllegalStateException 未设置 DataSource 时
	 * @since 5.0
	 */
	protected DataSource obtainDataSource() {
		DataSource dataSource = getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return dataSource;
	}

	/**
	 * 指定本访问器使用的 {@code DataSource} 的数据库产品名称。
	 * 无需从 {@code DataSource} 获取 {@code Connection} 读取元数据即可初始化 {@link SQLErrorCodeSQLExceptionTranslator}。
	 * @param dbName 标识错误码条目的数据库产品名称
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
	 * 设置本实例的异常翻译器。
	 * <p>若类路径根目录存在用户提供的 `sql-error-codes.xml`，默认使用 {@link SQLErrorCodeSQLExceptionTranslator}；
	 * 否则自 6.0 起默认使用 {@link SQLExceptionSubclassTranslator}。
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLExceptionSubclassTranslator
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
		if (getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}

}
