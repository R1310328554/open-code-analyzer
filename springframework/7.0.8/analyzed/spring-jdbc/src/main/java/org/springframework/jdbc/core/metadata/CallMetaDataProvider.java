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

import org.springframework.jdbc.core.SqlParameter;

/**
 * 提供调用元数据的类须实现的 API 接口。
 *
 * <p>供 Spring 的
 * {@link org.springframework.jdbc.core.simple.SimpleJdbcCall} 内部使用。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Giuseppe Milicia
 * @since 2.5
 */
public interface CallMetaDataProvider {

	/**
	 * 使用提供的 DatabaseMetaData 初始化。
	 * @param databaseMetaData 用于获取数据库特定信息
	 * @throws SQLException 初始化失败时
	 */
	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	/**
	 * 初始化数据库特定的存储过程列元数据管理。
	 * <p>仅对受支持的数据库调用；可通过指定不使用列元数据关闭。
	 * @param databaseMetaData 用于获取数据库特定信息
	 * @param catalogName 要使用的 catalog 名（无则 {@code null}）
	 * @param schemaName 要使用的 schema 名（无则 {@code null}）
	 * @param procedureName 存储过程名
	 * @throws SQLException 初始化失败时
	 * @see	org.springframework.jdbc.core.simple.SimpleJdbcCall#withoutProcedureColumnMetaDataAccess()
	 */
	void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String procedureName) throws SQLException;

	/**
	 * 获取当前使用的调用参数元数据。
	 * @return {@link CallParameterMetaData} 列表
	 */
	List<CallParameterMetaData> getCallParameterMetaData();

	/**
	 * 对传入的过程名做必要修改以匹配当前元数据。
	 * <p>可能包括调整大小写。
	 */
	@Nullable String procedureNameToUse(@Nullable String procedureName);

	/**
	 * 对传入的 catalog 名做必要修改以匹配当前元数据。
	 * <p>可能包括调整大小写。
	 */
	@Nullable String catalogNameToUse(@Nullable String catalogName);

	/**
	 * 对传入的 schema 名做必要修改以匹配当前元数据。
	 * <p>可能包括调整大小写。
	 */
	@Nullable String schemaNameToUse(@Nullable String schemaName);

	/**
	 * 对传入的 catalog 名做必要修改以匹配当前元数据。
	 * <p>返回值用于元数据查找；可能调整大小写或在未提供时使用默认 catalog。
	 */
	@Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) ;

	/**
	 * 对传入的 schema 名做必要修改以匹配当前元数据。
	 * <p>返回值用于元数据查找；可能调整大小写或在未提供时使用默认 schema。
	 */
	@Nullable String metaDataSchemaNameToUse(@Nullable String schemaName);

	/**
	 * 对传入的列名做必要修改以匹配当前元数据。
	 * <p>可能包括调整大小写。
	 * @param parameterName 参数或列名
	 */
	@Nullable String parameterNameToUse(@Nullable String parameterName);

	/**
	 * 返回用于绑定给定参数名的命名参数名。
	 * @param parameterName 待绑定参数名
	 * @return 用于绑定的命名参数名
	 * @since 6.1.2
	 */
	String namedParameterBindingToUse(@Nullable String parameterName);

	/**
	 * 根据提供的元数据创建默认 OUT 参数。
	 * <p>未显式声明参数时使用。
	 * @param parameterName 参数名
	 * @param meta 本次调用的元数据
	 * @return 配置好的 SqlOutParameter
	 */
	SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 根据提供的元数据创建默认 IN/OUT 参数。
	 * <p>未显式声明参数时使用。
	 * @param parameterName 参数名
	 * @param meta 本次调用的元数据
	 * @return 配置好的 SqlInOutParameter
	 */
	SqlParameter createDefaultInOutParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 根据提供的元数据创建默认 IN 参数。
	 * <p>未显式声明参数时使用。
	 * @param parameterName 参数名
	 * @param meta 本次调用的元数据
	 * @return 配置好的 SqlParameter
	 */
	SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 获取当前用户名，用于元数据查找等。
	 * @return 数据库连接上的当前用户名
	 */
	@Nullable String getUserName();

	/**
	 * 是否使用存储过程列元数据？
	 */
	boolean isProcedureColumnMetaDataUsed();

	/**
	 * 本数据库是否支持通过 JDBC 调用
	 * {@link java.sql.Statement#getResultSet()} 获取返回的 ResultSet？
	 */
	boolean isReturnResultSetSupported();

	/**
	 * 本数据库是否支持将 ResultSet 作为 ref cursor 返回，
	 * 并通过 {@link java.sql.CallableStatement#getObject(int)} 按列读取？
	 */
	boolean isRefCursorSupported();

	/**
	 * 若支持 ref cursor，返回以 ResultSet 形式返回的列的
	 * {@link java.sql.Types} 类型。
	 */
	int getRefCursorSqlType();

	/**
	 * 是否跳过指定名称的返回参数？
	 * <p>允许数据库特定实现跳过对调用返回的特定结果的处理。
	 */
	boolean byPassReturnParameter(String parameterName);

	/**
	 * 数据库是否支持在过程调用中使用 catalog 名？
	 */
	boolean isSupportsCatalogsInProcedureCalls();

	/**
	 * 数据库是否支持在过程调用中使用 schema 名？
	 */
	boolean isSupportsSchemasInProcedureCalls();

}
