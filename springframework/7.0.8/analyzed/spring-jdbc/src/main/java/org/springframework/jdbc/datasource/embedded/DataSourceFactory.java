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

package org.springframework.jdbc.datasource.embedded;

import javax.sql.DataSource;

/**
 * {@code DataSourceFactory} 封装了特定 {@link DataSource} 实现的创建，例如 {@code HikariDataSource}
 * 形式的非池化 {@link org.springframework.jdbc.datasource.SimpleDriverDataSource} 或 HikariCP
 * 池设置。
 * <p> 在调用 {@link #getDataSource()} 实际获取配置的 {@code DataSource} 实例之前，先调用 {@link
 * #getConnectionProperties()} 配置规范化的 {@code DataSource} 属性。
 * @author Keith Donald
 * @author Sam Brannen
 * @since 3.0
 */
public interface DataSourceFactory {

	/**
	 * 获取要配置的 {@link #getDataSource DataSource} 的 {@linkplain ConnectionProperties connection
	 * properties}。
	 */
	ConnectionProperties getConnectionProperties();

	/**
	 * 获取应用了 {@linkplain #getConnectionProperties connection properties} 的 {@link DataSource}。
	 */
	DataSource getDataSource();

}
