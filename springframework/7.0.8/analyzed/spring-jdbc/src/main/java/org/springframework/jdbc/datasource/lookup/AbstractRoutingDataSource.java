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
 * 抽象 {@link javax.sql.DataSource} 实现，根据查找键将 {@link #getConnection()} 调用路由到各种目标数据源之一。后者通常（但
 * 不一定）通过某些线程绑定的事务上下文来确定。
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see #determineCurrentLookupKey()
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements InitializingBean {

	/** 来源相关状态（`targetDataSources`）。 */
	private @Nullable Map<Object, Object> targetDataSources;

	/** 来源相关状态（`defaultTargetDataSource`）。 */
	private @Nullable Object defaultTargetDataSource;

	/** `true`：该类的成员状态。 */
	private boolean lenientFallback = true;

	/**
	 * 方法 `JndiDataSourceLookup`：完成本类中与「Jndi Data Source Lookup」相关的职责。
	 */
	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	/** 来源相关状态（`resolvedDataSources`）。 */
	private @Nullable Map<Object, DataSource> resolvedDataSources;

	/** 来源相关状态（`resolvedDefaultDataSource`）。 */
	private @Nullable DataSource resolvedDefaultDataSource;


	/**
	 * 指定目标数据源的映射，以查找键为键。 <p> 映射的值可以是相应的 {@link javax.sql.DataSource} 实例，也可以是数据源名称字符串（将通过
	 * {@link #setDataSourceLookup DataSourceLookup} 解析）。 <p>的key可以是任意类型；此类仅实现通用查找过程。具体的密钥表示将由
	 * {@link #resolveSpecifiedLookupKey(Object)} 和 {@link #determineCurrentLookupKey()} 处理。
	 */
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

	/**
	 * 指定默认目标数据源（如果有）。 <p> 映射的值可以是相应的 {@link javax.sql.DataSource} 实例，也可以是数据源名称字符串（将通过 {@link
	 * #setDataSourceLookup DataSourceLookup} 解析）。 <p> 如果没有键控的 {@link #setTargetDataSources
	 * targetDataSources} 与 {@link #determineCurrentLookupKey()} 当前查找键匹配，则此数据源将用作目标。
	 */
	public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
		this.defaultTargetDataSource = defaultTargetDataSource;
	}

	/**
	 * 指定如果找不到当前查找键的特定数据源，是否对默认数据源应用宽松的回退。 <p>Default 为“true”，接受在目标数据源映射中没有相应条目的查找键 - 在这种情况下只需回
	 * ​​退到默认数据源。 <p> 如果您希望仅在查找键为 {@code null} 时应用回退，请将此标志切换为“false”。没有 DataSource 条目的查找键将导致 Il
	 * legalStateException。
	 * @see #setTargetDataSources
	 * @see #setDefaultTargetDataSource
	 * @see #determineCurrentLookupKey()
	 */
	public void setLenientFallback(boolean lenientFallback) {
		this.lenientFallback = lenientFallback;
	}

	/**
	 * 设置 DataSourceLookup 实现以用于解析 {@link #setTargetDataSources targetDataSources}
	 * 映射中的数据源名称字符串。 <p>Default 是 {@link JndiDataSourceLookup}，允许直接指定应用程序服务器数据源的 JNDI 名称。
	 */
	public void setDataSourceLookup(@Nullable DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}


	/**
	 * {@link #initialize()} 的代表。
	 */
	@Override
	public void afterPropertiesSet() {
		initialize();
	}

	/**
	 * 通过解析配置的目标数据源来初始化此 {@code AbstractRoutingDataSource} 的内部状态。
	 * @throws IllegalArgumentException 如果目标数据源尚未配置
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
	 * 将 {@link #setTargetDataSources targetDataSources} 映射中指定的给定查找键对象解析为用于与 {@link
	 * #determineCurrentLookupKey() current lookup key} 匹配的实际查找键。 <p>的默认实现只是按原样返回给定的密钥。
	 * @param lookupKey 用户指定的查找关键对象
	 * @return 根据需要查找键进行匹配
	 */
	protected Object resolveSpecifiedLookupKey(Object lookupKey) {
		return lookupKey;
	}

	/**
	 * 将指定的数据源对象解析为DataSource实例。 <p> 默认实现处理数据源实例和数据源名称（通过 {@link #setDataSourceLookup DataSourc
	 * eLookup} 解析）。
	 * @param dataSourceObject {@link #setTargetDataSources targetDataSources} 映射中指定的数据源值对象
	 * @return 已解析的数据源（绝不是 {@code null}）
	 * @throws IllegalArgumentException 如果值类型不受支持
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
	 * 返回此路由器管理的已解析目标数据源。
	 * @return 解析的查找键和数据源的不可修改的映射
	 * @throws IllegalStateException 如果目标数据源尚未解析
	 * @since 5.2.9
	 * @see #setTargetDataSources
	 */
	public Map<Object, DataSource> getResolvedDataSources() {
		Assert.state(this.resolvedDataSources != null, "DataSources not resolved yet - call afterPropertiesSet");
		return Collections.unmodifiableMap(this.resolvedDataSources);
	}

	/**
	 * 返回已解析的默认目标数据源（如果有）。
	 * @return 默认数据源，或 {@code null}（如果没有或尚未解决）
	 * @since 5.2.9
	 * @see #setDefaultTargetDataSource
	 */
	public @Nullable DataSource getResolvedDefaultDataSource() {
		return this.resolvedDefaultDataSource;
	}


	/**
	 * 获取 Connection（`Connection`）。
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	/**
	 * 获取 Connection（`Connection`）。
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	/**
	 * 创建：Connection Builder（方法 `createConnectionBuilder`）。
	 */
	@Override
	public ConnectionBuilder createConnectionBuilder() throws SQLException {
		return determineTargetDataSource().createConnectionBuilder();
	}

	/**
	 * 创建：Sharding Key Builder（方法 `createShardingKeyBuilder`）。
	 */
	@Override
	public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
		return determineTargetDataSource().createShardingKeyBuilder();
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
		return determineTargetDataSource().unwrap(iface);
	}

	/**
	 * 判断是否 Wrapper For。
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
	}


	/**
	 * 检索当前目标数据源。确定 {@link #determineCurrentLookupKey() current lookup key}，在 {@link
	 * #setTargetDataSources targetDataSources} 映射中执行查找，如有必要，回退到指定的 {@link
	 * #setDefaultTargetDataSource default target DataSource}。
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
	 * 确定当前的查找键。这通常会被实现来检查线程绑定的事务上下文。 <p> 允许使用任意键。返回的键需要与存储的查找键类型匹配，由 {@link #resolveSpecifiedL
	 * ookupKey} 方法解析。
	 */
	protected abstract @Nullable Object determineCurrentLookupKey();

}
