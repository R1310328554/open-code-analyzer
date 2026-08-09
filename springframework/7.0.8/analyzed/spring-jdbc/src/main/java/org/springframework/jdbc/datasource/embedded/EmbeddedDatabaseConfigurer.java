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
 * {@code EmbeddedDatabaseConfigurer} 封装创建、连接和关闭
 * 特定类型嵌入式数据库（如 HSQL、H2 或 Derby）所需的配置。
 *
 * @author Keith Donald
 * @author Sam Brannen
 * @since 3.0
 */
public interface EmbeddedDatabaseConfigurer {

	/**
	 * 配置创建并连接嵌入式数据库所需的属性。
	 * @param properties 待配置的连接属性
	 * @param databaseName 嵌入式数据库名称
	 */
	void configureConnectionProperties(ConnectionProperties properties, String databaseName);

	/**
	 * 关闭支撑给定 {@link DataSource} 的嵌入式数据库实例。
	 * @param dataSource 对应的 {@link DataSource}
	 * @param databaseName 待关闭的数据库名称
	 */
	void shutdown(DataSource dataSource, String databaseName);

}
