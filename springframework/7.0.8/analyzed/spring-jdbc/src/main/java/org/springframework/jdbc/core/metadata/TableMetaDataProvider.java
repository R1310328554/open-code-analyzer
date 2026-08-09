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

package org.springframework.jdbc.core.metadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * 定义提供表元数据的类应实现的 API 接口。
 *
 * <p>供 Simple JDBC 类内部使用。
 *
 * @author Thomas Risberg
 * @author Sam Brannen
 * @since 2.5
 */
public interface TableMetaDataProvider {

	/**
	 * 使用提供的数据库元数据进行初始化。
	 * @param databaseMetaData 用于获取数据库特定信息
	 * @throws SQLException 初始化失败时抛出
	 */
	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	/**
	 * 使用提供的数据库元数据、表和列信息进行初始化。
	 * <p>可通过指定不使用列元数据来关闭此初始化。
	 * @param databaseMetaData 用于获取数据库特定信息
	 * @param catalogName 要使用的 catalog 名称（无则为 {@code null}）
	 * @param schemaName 要使用的 schema 名称（无则为 {@code null}）
	 * @param tableName 表名
	 * @throws SQLException 初始化失败时抛出
	 */
	void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String tableName) throws SQLException;

	/**
	 * 获取当前使用的表参数元数据。
	 * @return {@link TableParameterMetaData} 列表
	 */
	List<TableParameterMetaData> getTableParameterMetaData();

	/**
	 * 根据元数据信息获取格式化后的表名。
	 * <p>可能包括大小写转换。
	 */
	@Nullable String tableNameToUse(@Nullable String tableName);

	/**
	 * 根据元数据信息获取格式化后的列名。
	 * <p>可能包括大小写转换。
	 * @since 6.1
	 */
	@Nullable String columnNameToUse(@Nullable String columnName);

	/**
	 * 根据元数据信息获取格式化后的 catalog 名称。
	 * <p>可能包括大小写转换。
	 */
	@Nullable String catalogNameToUse(@Nullable String catalogName);

	/**
	 * 根据元数据信息获取格式化后的 schema 名称。
	 * <p>可能包括大小写转换。
	 */
	@Nullable String schemaNameToUse(@Nullable String schemaName);

	/**
	 * 对传入的 catalog 名称进行必要修改，以匹配当前使用的元数据。
	 * <p>返回值将用于元数据查找。
	 * <p>可能包括调整大小写，或在未提供时给出默认 catalog。
	 */
	@Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) ;

	/**
	 * 对传入的 schema 名称进行必要修改，以匹配当前使用的元数据。
	 * <p>返回值将用于元数据查找。
	 * <p>可能包括调整大小写，或在未提供时给出默认 schema。
	 */
	@Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) ;

	/**
	 * 是否正在使用表列的元数据？
	 */
	boolean isTableColumnMetaDataUsed();

	/**
	 * 此数据库是否支持 JDBC 获取生成键的特性？
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	boolean isGetGeneratedKeysSupported();

	/**
	 * 当 JDBC 不支持获取生成键时，此数据库是否支持通过简单查询获取生成键？
	 * @see #isGetGeneratedKeysSupported()
	 * @see #getSimpleQueryForGetGeneratedKey(String, String)
	 */
	boolean isGetGeneratedKeysSimulated();

	/**
	 * 获取在 JDBC 不支持获取生成键时用于检索生成键的简单查询。
	 * @see #isGetGeneratedKeysSimulated()
	 */
	@Nullable String getSimpleQueryForGetGeneratedKey(String tableName, String keyColumnName);

	/**
	 * 此数据库是否支持通过列名字符串数组获取生成键？
	 * @see java.sql.Connection#createStruct(String, Object[])
	 */
	boolean isGeneratedKeysColumnNameArraySupported();

	/**
	 * 获取用于引用 SQL 标识符的字符串。
	 * <p>若不支持标识符引用，则返回空格（{@code " "}）。
	 * @return 数据库标识符引用字符串
	 * @since 6.1
	 * @see DatabaseMetaData#getIdentifierQuoteString()
	 */
	String getIdentifierQuoteString();

}
