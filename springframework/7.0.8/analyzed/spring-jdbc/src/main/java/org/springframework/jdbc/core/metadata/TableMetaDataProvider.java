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
 * 接口指定由提供表元数据的类实现的 API。
 * <p>这是供简单 JDBC 类内部使用的。
 * @author Thomas Risberg
 * @author Sam Brannen
 * @since 2.5
 */
public interface TableMetaDataProvider {

	/**
	 * 使用提供的数据库元数据进行初始化。
	 * @param databaseMetaData 用于检索数据库特定信息
	 * @throws SQLException 如果初始化失败
	 */
	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	/**
	 * 使用提供的数据库元数据、表和列信息进行初始化。 <p> 可以通过指定不应使用列元数据来关闭此初始化。
	 * @param databaseMetaData 用于检索数据库特定信息
	 * @param catalogName 要使用的目录名称（如果没有，则为 {@code null}）
	 * @param schemaName 要使用的模式名称的名称（如果没有，则为 {@code null}）
	 * @param tableName 表名
	 * @throws SQLException 如果初始化失败
	 */
	void initializeWithTableColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String tableName) throws SQLException;

	/**
	 * 获取当前使用的表参数元数据。
	 * @return {@link TableParameterMetaData} 列表
	 */
	List<TableParameterMetaData> getTableParameterMetaData();

	/**
	 * 获取基于元数据信息格式化的表名。 <p>这可能包括更改大小写。
	 */
	@Nullable String tableNameToUse(@Nullable String tableName);

	/**
	 * 获取基于元数据信息格式化的列名称。 <p>这可能包括更改大小写。
	 * @since 6.1
	 */
	@Nullable String columnNameToUse(@Nullable String columnName);

	/**
	 * 获取基于元数据信息格式化的目录名称。 <p>这可能包括更改大小写。
	 */
	@Nullable String catalogNameToUse(@Nullable String catalogName);

	/**
	 * 获取基于元数据信息格式化的模式名称。 <p>这可能包括更改大小写。
	 */
	@Nullable String schemaNameToUse(@Nullable String schemaName);

	/**
	 * 提供对传入的目录名称的任何修改，以匹配当前使用的元数据。 <p>返回的值将用于元数据查找。 <p>这可能包括更改所使用的案例或提供基本目录（如果未提供）。
	 */
	@Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) ;

	/**
	 * 提供对传入的架构名称的任何修改，以匹配当前使用的元数据。 <p>返回的值将用于元数据查找。 <p>这可能包括更改使用的情况或提供基本模式（如果未提供）。
	 */
	@Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) ;

	/**
	 * 我们是否使用表列的元数据？
	 */
	boolean isTableColumnMetaDataUsed();

	/**
	 * 该数据库是否支持 JDBC 功能来检索生成的密钥？
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	boolean isGetGeneratedKeysSupported();

	/**
	 * 当不支持检索生成密钥的 JDBC 功能时，此数据库是否支持简单查询来检索生成的密钥？
	 * @see #isGetGeneratedKeysSupported()
	 * @see #getSimpleQueryForGetGeneratedKey(String, String)
	 */
	boolean isGetGeneratedKeysSimulated();

	/**
	 * 当不支持检索生成密钥的 JDBC 功能时，获取简单查询来检索生成的密钥。
	 * @see #isGetGeneratedKeysSimulated()
	 */
	@Nullable String getSimpleQueryForGetGeneratedKey(String tableName, String keyColumnName);

	/**
	 * 该数据库是否支持列名字符串数组来检索生成的键？
	 * @see java.sql.Connection#createStruct(String, Object[])
	 */
	boolean isGeneratedKeysColumnNameArraySupported();

	/**
	 * 获取用于引用 SQL 标识符的字符串。 <p> 如果不支持标识符引用，则此方法返回一个空格 ({@code " "})。
	 * @return 标识符引用字符串
	 * @since 6.1
	 * @see DatabaseMetaData#getIdentifierQuoteString()
	 */
	String getIdentifierQuoteString();

}
