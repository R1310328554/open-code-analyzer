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

package org.springframework.jdbc.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ConnectionBuilder;
import java.sql.SQLException;
import java.sql.ShardingKeyBuilder;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * JDBC {@link javax.sql.DataSource} 实现，将所有调用委托给给定的目标 {@link javax.sql.DataSource}。
 * <p> 该类旨在进行子类化，子类仅重写那些不应简单委托给目标数据源的方法（例如 {@link #getConnection()}）。
 * @author Juergen Hoeller
 * @since 1.1
 * @see #getConnection
 */
public class DelegatingDataSource implements DataSource, InitializingBean {

	/** 来源相关状态（`targetDataSource`）。 */
	private @Nullable DataSource targetDataSource;


	/**
	 * 创建一个新的 DelegatingDataSource。
	 * @see #setTargetDataSource
	 */
	public DelegatingDataSource() {
	}

	/**
	 * 创建一个新的 DelegatingDataSource。
	 * @param targetDataSource 目标数据源
	 */
	public DelegatingDataSource(DataSource targetDataSource) {
		setTargetDataSource(targetDataSource);
	}


	/**
	 * 设置此数据源应委托给的目标数据源。
	 */
	public void setTargetDataSource(@Nullable DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	/**
	 * 返回此数据源应委托给的目标数据源。
	 */
	public @Nullable DataSource getTargetDataSource() {
		return this.targetDataSource;
	}

	/**
	 * 获取实际使用的目标 {@code DataSource}（绝不是 {@code null}）。
	 * @since 5.0
	 */
	protected DataSource obtainTargetDataSource() {
		DataSource dataSource = getTargetDataSource();
		Assert.state(dataSource != null, "No 'targetDataSource' set");
		return dataSource;
	}

	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		if (getTargetDataSource() == null) {
			throw new IllegalArgumentException("Property 'targetDataSource' is required");
		}
	}


	/**
	 * 获取 Connection（`Connection`）。
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return obtainTargetDataSource().getConnection();
	}

	/**
	 * 获取 Connection（`Connection`）。
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return obtainTargetDataSource().getConnection(username, password);
	}

	/**
	 * 创建：Connection Builder（方法 `createConnectionBuilder`）。
	 */
	@Override
	public ConnectionBuilder createConnectionBuilder() throws SQLException {
		return obtainTargetDataSource().createConnectionBuilder();
	}

	/**
	 * 创建：Sharding Key Builder（方法 `createShardingKeyBuilder`）。
	 */
	@Override
	public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
		return obtainTargetDataSource().createShardingKeyBuilder();
	}

	/**
	 * 获取 Login Timeout（`LoginTimeout`）。
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return obtainTargetDataSource().getLoginTimeout();
	}

	/**
	 * 设置 Login Timeout（`LoginTimeout`）。
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		obtainTargetDataSource().setLoginTimeout(seconds);
	}

	/**
	 * 获取 Log Writer（`LogWriter`）。
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return obtainTargetDataSource().getLogWriter();
	}

	/**
	 * 设置 Log Writer（`LogWriter`）。
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		obtainTargetDataSource().setLogWriter(out);
	}

	/**
	 * 获取 Parent Logger（`ParentLogger`）。
	 */
	@Override
	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	/**
	 * 方法 `unwrap`：完成本类中与「unwrap」相关的职责。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return obtainTargetDataSource().unwrap(iface);
	}

	/**
	 * 判断是否 Wrapper For。
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || obtainTargetDataSource().isWrapperFor(iface));
	}

}
