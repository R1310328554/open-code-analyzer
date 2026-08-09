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
 * 基于 JDBC 的数据访问对象的便捷超类。
 *
 * <p>需要设置 {@link javax.sql.DataSource}，并通过 {@link #getJdbcTemplate()}
 * 向子类提供基于它的 {@link org.springframework.jdbc.core.JdbcTemplate}。
 *
 * <p>该基类主要用于 JdbcTemplate 场景，也可在直接使用 Connection
 * 或使用 {@code org.springframework.jdbc.object} 操作对象时使用。
 *
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setDataSource
 * @see #getJdbcTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @deprecated 自 7.0 起弃用，建议直接注入 {@link JdbcTemplate}
 * 或 {@link org.springframework.jdbc.core.simple.JdbcClient}
 */
@Deprecated(since = "7.0", forRemoval = true)
@SuppressWarnings("removal")
public abstract class JdbcDaoSupport extends DaoSupport {

	private @Nullable JdbcTemplate jdbcTemplate;


	/**
	 * 设置本 DAO 使用的 JDBC DataSource。
	 */
	public final void setDataSource(DataSource dataSource) {
		if (this.jdbcTemplate == null || dataSource != this.jdbcTemplate.getDataSource()) {
			this.jdbcTemplate = createJdbcTemplate(dataSource);
			initTemplateConfig();
		}
	}

	/**
	 * 为给定 DataSource 创建 JdbcTemplate。
	 * 仅在通过 DataSource 引用填充 DAO 时调用。
	 * <p>子类可覆盖以提供不同配置或自定义 JdbcTemplate 子类实例。
	 * @param dataSource 要为其创建 JdbcTemplate 的 JDBC DataSource
	 * @return 新的 JdbcTemplate 实例
	 * @see #setDataSource
	 */
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	/**
	 * 返回本 DAO 使用的 JDBC DataSource。
	 */
	public final @Nullable DataSource getDataSource() {
		return (this.jdbcTemplate != null ? this.jdbcTemplate.getDataSource() : null);
	}

	/**
	 * 显式设置本 DAO 的 JdbcTemplate，作为指定 DataSource 的替代方式。
	 */
	public final void setJdbcTemplate(@Nullable JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		initTemplateConfig();
	}

	/**
	 * 返回本 DAO 的 JdbcTemplate，
	 * 已通过 DataSource 预初始化或显式设置。
	 */
	public final @Nullable JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * 初始化本 DAO 基于模板的配置。
	 * 在直接设置或通过 DataSource 设置新 JdbcTemplate 后调用。
	 * <p>本实现为空。子类可覆盖以基于 JdbcTemplate 配置更多对象。
	 * @see #getJdbcTemplate()
	 */
	protected void initTemplateConfig() {
	}

	@Override
	protected void checkDaoConfig() {
		if (this.jdbcTemplate == null) {
			throw new IllegalArgumentException("'dataSource' or 'jdbcTemplate' is required");
		}
	}


	/**
	 * 返回本 DAO 的 JdbcTemplate 的 SQLExceptionTranslator，
	 * 用于在自定义 JDBC 访问代码中翻译 SQLException。
	 * @see org.springframework.jdbc.core.JdbcTemplate#getExceptionTranslator()
	 */
	protected final SQLExceptionTranslator getExceptionTranslator() {
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		Assert.state(jdbcTemplate != null, "No JdbcTemplate set");
		return jdbcTemplate.getExceptionTranslator();
	}

	/**
	 * 获取 JDBC Connection，来自当前事务或新建连接。
	 * @return JDBC Connection
	 * @throws CannotGetJdbcConnectionException 获取 Connection 失败时
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection(javax.sql.DataSource)
	 */
	protected final Connection getConnection() throws CannotGetJdbcConnectionException {
		DataSource dataSource = getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return DataSourceUtils.getConnection(dataSource);
	}

	/**
	 * 关闭通过本 DAO 的 DataSource 创建的 JDBC Connection（若未绑定到线程）。
	 * @param con 要关闭的 Connection
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	protected final void releaseConnection(Connection con) {
		DataSourceUtils.releaseConnection(con, getDataSource());
	}

}
