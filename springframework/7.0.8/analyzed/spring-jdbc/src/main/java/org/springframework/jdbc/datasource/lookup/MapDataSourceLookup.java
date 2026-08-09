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
 * 简单的 {@link DataSourceLookup} 实现依赖于映射来进行查找。
 * <p> 适用于测试需要将任意 {@link String} 名称与目标 {@link DataSource} 对象相匹配的环境或应用程序。
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public class MapDataSourceLookup implements DataSourceLookup {

	private final Map<String, DataSource> dataSources = new HashMap<>(4);


	/**
	 * 创建 {@link MapDataSourceLookup} 类的新实例。
	 */
	public MapDataSourceLookup() {
	}

	/**
	 * 创建 {@link MapDataSourceLookup} 类的新实例。
	 * @param dataSources {@link Map} 或 {@link DataSource DataSources}；键是 {@link String Strings}，值是实际的 {@link DataSource} 实例。
	 */
	public MapDataSourceLookup(Map<String, DataSource> dataSources) {
		setDataSources(dataSources);
	}

	/**
	 * 创建 {@link MapDataSourceLookup} 类的新实例。
	 * @param dataSourceName 要添加提供的 {@link DataSource} 的名称
	 * @param dataSource 要添加的 {@link DataSource}
	 */
	public MapDataSourceLookup(String dataSourceName, DataSource dataSource) {
		addDataSource(dataSourceName, dataSource);
	}


	/**
	 * 设置{@link Map}为{@link DataSource DataSources}；键是 {@link String Strings}，值是实际的 {@link
	 * DataSource} 实例。 <p>如果提供的 {@link Map} 是 {@code null}，则此方法调用实际上无效。
	 * @param dataSources {@link Map} 表示 {@link DataSource DataSources}
	 */
	public void setDataSources(@Nullable Map<String, DataSource> dataSources) {
		if (dataSources != null) {
			this.dataSources.putAll(dataSources);
		}
	}

	/**
	 * 获取该对象维护的 {@link DataSource DataSources} 的 {@link Map}。 <p>返回的{@link Map}是{@link
	 * Collections#unmodifiableMap(java.util.Map) unmodifiable}。
	 * @return {@link Map} 或 {@link DataSource DataSources}（绝不是 {@code null}）
	 */
	public Map<String, DataSource> getDataSources() {
		return Collections.unmodifiableMap(this.dataSources);
	}

	/**
	 * 将提供的 {@link DataSource} 添加到此对象维护的 {@link DataSource DataSources} 映射中。
	 * @param dataSourceName 要添加提供的 {@link DataSource} 的名称
	 * @param dataSource 如此添加 {@link DataSource}
	 */
	public void addDataSource(String dataSourceName, DataSource dataSource) {
		Assert.notNull(dataSourceName, "DataSource name must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		this.dataSources.put(dataSourceName, dataSource);
	}

	/**
	 * 获取 Data Source（`DataSource`）。
	 */
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
