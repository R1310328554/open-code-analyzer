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

package org.springframework.jdbc.core.support;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.util.Assert;

/**
 * 基于 JDBC 的数据访问对象的方便超类。
 * <p>需要设置一个{@link javax.sql.DataSource}，通过{@link #getJdbcTemplate()}方法向子类提供基于它的{@link
 * org.springframework.jdbc.core.JdbcTemplate}。
 * <p>该基类主要用于 JdbcTemplate 使用，但也可以在直接使用 Connection 或使用 {@code
 * org.springframework.jdbc.object} 操作对象时使用。
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setDataSource
 * @see #getJdbcTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @deprecated 7.0，支持直接注入{@link JdbcTemplate}或{@link org.springframework.jdbc.core.simple.JdbcClient}
 */
@Deprecated(since = "7.0", forRemoval = true)
@SuppressWarnings("removal")
public abstract class JdbcDaoSupport extends DaoSupport {

	/** 模板相关状态（`jdbcTemplate`）。 */
	private @Nullable JdbcTemplate jdbcTemplate;


	/**
	 * 设置此 DAO 要使用的 JDBC 数据源。
	 */
	public final void setDataSource(DataSource dataSource) {
		if (this.jdbcTemplate == null || dataSource != this.jdbcTemplate.getDataSource()) {
			this.jdbcTemplate = createJdbcTemplate(dataSource);
			initTemplateConfig();
		}
	}

	/**
	 * 为给定的数据源创建 JdbcTemplate。仅当使用数据源引用填充 DAO 时才调用！ <p>可以在子类中重写，以提供具有不同配置的JdbcTemplate实例，或者自定义J
	 * dbcTemplate子类。
	 * @param dataSource 用于创建 JdbcTemplate 的 JDBC 数据源
	 * @return 新的 JdbcTemplate 实例
	 * @see #setDataSource
	 */
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	/**
	 * 返回此 DAO 使用的 JDBC 数据源。
	 */
	public final @Nullable DataSource getDataSource() {
		return (this.jdbcTemplate != null ? this.jdbcTemplate.getDataSource() : null);
	}

	/**
	 * 显式为此 DAO 设置 JdbcTemplate，作为指定数据源的替代方法。
	 */
	public final void setJdbcTemplate(@Nullable JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		initTemplateConfig();
	}

	/**
	 * 返回此 DAO 的 JdbcTemplate，使用 DataSource 预先初始化或显式设置。
	 */
	public final @Nullable JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * 初始化此 DAO 的基于模板的配置。在直接或通过数据源设置新的 JdbcTemplate 后调用。 <p>这个实现是空的。子类可以覆盖它以基于 JdbcTemplate 配置更
	 * 多对象。
	 * @see #getJdbcTemplate()
	 */
	protected void initTemplateConfig() {
	}

	/**
	 * 检查：Dao Config（方法 `checkDaoConfig`）。
	 */
	@Override
	protected void checkDaoConfig() {
		if (this.jdbcTemplate == null) {
			throw new IllegalArgumentException("'dataSource' or 'jdbcTemplate' is required");
		}
	}


	/**
	 * 返回此 DAO 的 JdbcTemplate 的 SQLExceptionTranslator，用于转换自定义 JDBC 访问代码中的 SQLException。
	 * @see org.springframework.jdbc.core.JdbcTemplate#getExceptionTranslator()
	 */
	protected final SQLExceptionTranslator getExceptionTranslator() {
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		Assert.state(jdbcTemplate != null, "No JdbcTemplate set");
		return jdbcTemplate.getExceptionTranslator();
	}

	/**
	 * 从当前事务或新事务获取 JDBC 连接。
	 * @return JDBC连接
	 * @throws CannotGetJdbcConnectionException 如果尝试获取连接失败
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection(javax.sql.DataSource)
	 */
	protected final Connection getConnection() throws CannotGetJdbcConnectionException {
		DataSource dataSource = getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return DataSourceUtils.getConnection(dataSource);
	}

	/**
	 * 如果给定的 JDBC 连接（通过此 DAO 的数据源创建）未绑定到线程，则关闭该连接。
	 * @param con 要关闭的连接
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	protected final void releaseConnection(Connection con) {
		DataSourceUtils.releaseConnection(con, getDataSource());
	}

}
