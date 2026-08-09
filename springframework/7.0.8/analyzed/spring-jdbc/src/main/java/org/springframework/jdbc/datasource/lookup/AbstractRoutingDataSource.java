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

package org.springframework.jdbc.datasource.lookup;

import java.sql.Connection;
import java.sql.ConnectionBuilder;
import java.sql.SQLException;
import java.sql.ShardingKeyBuilder;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 抽象 {@link javax.sql.DataSource} 实现，根据查找键将 {@link #getConnection()}
 * 调用路由到多个目标 DataSource 之一。查找键通常（但不一定）
 * 由线程绑定的事务上下文决定。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see #determineCurrentLookupKey()
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements InitializingBean {

	private @Nullable Map<Object, Object> targetDataSources;

	private @Nullable Object defaultTargetDataSource;

	private boolean lenientFallback = true;

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private @Nullable Map<Object, DataSource> resolvedDataSources;

	private @Nullable DataSource resolvedDefaultDataSource;


	/**
	 * 指定目标 DataSource 映射，键为查找键。
	 * <p>映射值可以是 {@link javax.sql.DataSource} 实例，
	 * 或待 {@link #setDataSourceLookup DataSourceLookup} 解析的数据源名称字符串。
	 * <p>键可为任意类型；本类仅实现通用查找流程。
	 * 具体键表示由 {@link #resolveSpecifiedLookupKey(Object)} 与
	 * {@link #determineCurrentLookupKey()} 处理。
	 */
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

	/**
	 * 指定默认目标 DataSource（若有）。
	 * <p>映射值可以是 {@link javax.sql.DataSource} 实例，
	 * 或待 {@link #setDataSourceLookup DataSourceLookup} 解析的数据源名称字符串。
	 * <p>若无键控 {@link #setTargetDataSources targetDataSources}
	 * 匹配 {@link #determineCurrentLookupKey()} 当前查找键，则使用此 DataSource。
	 */
	public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
		this.defaultTargetDataSource = defaultTargetDataSource;
	}

	/**
	 * 指定当前查找键找不到对应 DataSource 时，是否宽松回退到默认 DataSource。
	 * <p>默认为 "true"，接受目标 DataSource 映射中无对应条目的查找键，
	 * 此时直接回退到默认 DataSource。
	 * <p>若希望仅在查找键为 {@code null} 时回退，可将此标志设为 "false"。
	 * 无 DataSource 条目的查找键将抛出 IllegalStateException。
	 * @see #setTargetDataSources
	 * @see #setDefaultTargetDataSource
	 * @see #determineCurrentLookupKey()
	 */
	public void setLenientFallback(boolean lenientFallback) {
		this.lenientFallback = lenientFallback;
	}

	/**
	 * 设置用于解析 {@link #setTargetDataSources targetDataSources} 映射中
	 * 数据源名称字符串的 DataSourceLookup 实现。
	 * <p>默认为 {@link JndiDataSourceLookup}，可直接指定应用服务器 DataSource 的 JNDI 名称。
	 */
	public void setDataSourceLookup(@Nullable DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}


	/**
	 * 委托 {@link #initialize()}。
	 */
	@Override
	public void afterPropertiesSet() {
		initialize();
	}

	/**
	 * 解析已配置的目标 DataSource，初始化本 {@code AbstractRoutingDataSource} 内部状态。
	 * @throws IllegalArgumentException 未配置目标 DataSource 时
	 * @since 6.1
	 * @see #setTargetDataSources(Map)
	 * @see #setDefaultTargetDataSource(Object)
	 * @see #getResolvedDataSources()
	 * @see #getResolvedDefaultDataSource()
	 */
	public void initialize() {
		if (this.targetDataSources == null) {
			throw new IllegalArgumentException("Property 'targetDataSources' is required");
		}
		this.resolvedDataSources = CollectionUtils.newHashMap(this.targetDataSources.size());
		this.targetDataSources.forEach((key, value) -> {
			Object lookupKey = resolveSpecifiedLookupKey(key);
			DataSource dataSource = resolveSpecifiedDataSource(value);
			this.resolvedDataSources.put(lookupKey, dataSource);
		});
		if (this.defaultTargetDataSource != null) {
			this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
		}
	}

	/**
	 * 将 {@link #setTargetDataSources targetDataSources} 映射中指定的查找键对象
	 * 解析为用于与 {@link #determineCurrentLookupKey()} 当前查找键匹配的
	 * 实际查找键。
	 * <p>默认实现直接返回给定键。
	 * @param lookupKey 用户指定的查找键对象
	 * @return 用于匹配的实际查找键
	 */
	protected Object resolveSpecifiedLookupKey(Object lookupKey) {
		return lookupKey;
	}

	/**
	 * 将指定数据源对象解析为 DataSource 实例。
	 * <p>默认实现处理 DataSource 实例与数据源名称
	 * （通过 {@link #setDataSourceLookup DataSourceLookup} 解析）。
	 * @param dataSourceObject {@link #setTargetDataSources targetDataSources}
	 * 映射中指定的数据源值对象
	 * @return 解析后的 DataSource（永不为 {@code null}）
	 * @throws IllegalArgumentException 值类型不受支持时
	 */
	protected DataSource resolveSpecifiedDataSource(Object dataSourceObject) throws IllegalArgumentException {
		if (dataSourceObject instanceof DataSource dataSource) {
			return dataSource;
		}
		else if (dataSourceObject instanceof String dataSourceName) {
			return this.dataSourceLookup.getDataSource(dataSourceName);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSourceObject);
		}
	}

	/**
	 * 返回本路由器管理的已解析目标 DataSource。
	 * @return 已解析查找键与 DataSource 的不可修改映射
	 * @throws IllegalStateException 目标 DataSource 尚未解析时
	 * @since 5.2.9
	 * @see #setTargetDataSources
	 */
	public Map<Object, DataSource> getResolvedDataSources() {
		Assert.state(this.resolvedDataSources != null, "DataSources not resolved yet - call afterPropertiesSet");
		return Collections.unmodifiableMap(this.resolvedDataSources);
	}

	/**
	 * 返回已解析的默认目标 DataSource（若有）。
	 * @return 默认 DataSource，若无或尚未解析则为 {@code null}
	 * @since 5.2.9
	 * @see #setDefaultTargetDataSource
	 */
	public @Nullable DataSource getResolvedDefaultDataSource() {
		return this.resolvedDefaultDataSource;
	}


	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	@Override
	public ConnectionBuilder createConnectionBuilder() throws SQLException {
		return determineTargetDataSource().createConnectionBuilder();
	}

	@Override
	public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
		return determineTargetDataSource().createShardingKeyBuilder();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return determineTargetDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
	}


	/**
	 * 获取当前目标 DataSource。确定 {@link #determineCurrentLookupKey()} 当前查找键，
	 * 在 {@link #setTargetDataSources targetDataSources} 映射中查找，
	 * 必要时回退到 {@link #setDefaultTargetDataSource 默认目标 DataSource}。
	 * @see #determineCurrentLookupKey()
	 */
	protected DataSource determineTargetDataSource() {
		Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
		Object lookupKey = determineCurrentLookupKey();
		DataSource dataSource = this.resolvedDataSources.get(lookupKey);
		if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
			dataSource = this.resolvedDefaultDataSource;
		}
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		return dataSource;
	}

	/**
	 * 确定当前查找键。通常实现为检查线程绑定的事务上下文。
	 * <p>允许任意键。返回的键需与 {@link #resolveSpecifiedLookupKey} 方法
	 * 解析后的存储查找键类型匹配。
	 */
	protected abstract @Nullable Object determineCurrentLookupKey();

}
