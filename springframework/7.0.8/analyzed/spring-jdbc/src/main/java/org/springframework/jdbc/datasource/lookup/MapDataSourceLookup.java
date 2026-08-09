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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 依赖映射进行查找的简单 {@link DataSourceLookup} 实现。
 *
 * <p>适用于测试环境，或需将任意 {@link String} 名称
 * 映射到目标 {@link DataSource} 对象的应用。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public class MapDataSourceLookup implements DataSourceLookup {

	private final Map<String, DataSource> dataSources = new HashMap<>(4);


	/**
	 * 创建 {@link MapDataSourceLookup} 的新实例。
	 */
	public MapDataSourceLookup() {
	}

	/**
	 * 创建 {@link MapDataSourceLookup} 的新实例。
	 * @param dataSources {@link DataSource DataSources} 的 {@link Map}；
	 * 键为 {@link String}，值为实际 {@link DataSource} 实例。
	 */
	public MapDataSourceLookup(Map<String, DataSource> dataSources) {
		setDataSources(dataSources);
	}

	/**
	 * 创建 {@link MapDataSourceLookup} 的新实例。
	 * @param dataSourceName 所供 {@link DataSource} 的注册名称
	 * @param dataSource 要添加的 {@link DataSource}
	 */
	public MapDataSourceLookup(String dataSourceName, DataSource dataSource) {
		addDataSource(dataSourceName, dataSource);
	}


	/**
	 * 设置 {@link DataSource DataSources} 的 {@link Map}；
	 * 键为 {@link String}，值为实际 {@link DataSource} 实例。
	 * <p>若所供 {@link Map} 为 {@code null}，此方法调用无实际效果。
	 * @param dataSources 上述 {@link DataSource DataSources} 的 {@link Map}
	 */
	public void setDataSources(@Nullable Map<String, DataSource> dataSources) {
		if (dataSources != null) {
			this.dataSources.putAll(dataSources);
		}
	}

	/**
	 * 获取本对象维护的 {@link DataSource DataSources} 的 {@link Map}。
	 * <p>返回的 {@link Map} 为 {@link Collections#unmodifiableMap(java.util.Map) 不可修改}。
	 * @return 上述 {@link DataSource DataSources} 的 {@link Map}（永不为 {@code null}）
	 */
	public Map<String, DataSource> getDataSources() {
		return Collections.unmodifiableMap(this.dataSources);
	}

	/**
	 * 将所供 {@link DataSource} 添加到本对象维护的
	 * {@link DataSource DataSources} 映射中。
	 * @param dataSourceName 所供 {@link DataSource} 的注册名称
	 * @param dataSource 要添加的 {@link DataSource}
	 */
	public void addDataSource(String dataSourceName, DataSource dataSource) {
		Assert.notNull(dataSourceName, "DataSource name must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		this.dataSources.put(dataSourceName, dataSource);
	}

	@Override
	public DataSource getDataSource(String dataSourceName) throws DataSourceLookupFailureException {
		Assert.notNull(dataSourceName, "DataSource name must not be null");
		DataSource dataSource = this.dataSources.get(dataSourceName);
		if (dataSource == null) {
			throw new DataSourceLookupFailureException(
					"No DataSource with name '" + dataSourceName + "' registered");
		}
		return dataSource;
	}

}
