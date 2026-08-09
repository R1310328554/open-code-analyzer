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
 * 接口指定由提供调用元数据的类实现的 API。
 * <p> 这是供 Spring 的 {@link org.springframework.jdbc.core.simple.SimpleJdbcCall} 内部使用的。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Giuseppe Milicia
 * @since 2.5
 */
public interface CallMetaDataProvider {

	/**
	 * 使用提供的 DatabaseMetData 进行初始化。
	 * @param databaseMetaData 用于检索数据库特定信息
	 * @throws SQLException 如果初始化失败
	 */
	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;

	/**
	 * 初始化数据库特定的过程列元数据管理。 <p>仅针对支持的数据库调用。可以通过指定不应使用列元数据来关闭此初始化。
	 * @param databaseMetaData 用于检索数据库特定信息
	 * @param catalogName 要使用的目录名称（如果没有，则为 {@code null}）
	 * @param schemaName 要使用的模式名称的名称（如果没有，则为 {@code null}）
	 * @param procedureName 存储过程的名称
	 * @throws SQLException 如果初始化失败
	 * @see org.springframework.jdbc.core.simple.SimpleJdbcCall#withoutProcedureColumnMetaDataAccess()
	 */
	void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, @Nullable String catalogName,
			@Nullable String schemaName, @Nullable String procedureName) throws SQLException;

	/**
	 * 获取当前使用的呼叫参数元数据。
	 * @return {@link CallParameterMetaData} 列表
	 */
	List<CallParameterMetaData> getCallParameterMetaData();

	/**
	 * 提供对传入的过程名称的任何修改，以匹配当前使用的元数据。 <p>这可能包括更改大小写。
	 */
	@Nullable String procedureNameToUse(@Nullable String procedureName);

	/**
	 * 提供对传入的目录名称的任何修改，以匹配当前使用的元数据。 <p>这可能包括更改大小写。
	 */
	@Nullable String catalogNameToUse(@Nullable String catalogName);

	/**
	 * 提供对传入的架构名称的任何修改，以匹配当前使用的元数据。 <p>这可能包括更改大小写。
	 */
	@Nullable String schemaNameToUse(@Nullable String schemaName);

	/**
	 * 提供对传入的目录名称的任何修改，以匹配当前使用的元数据。 <p>返回的值将用于元数据查找。这可能包括更改所使用的案例或提供基本目录（如果未提供）。
	 */
	@Nullable String metaDataCatalogNameToUse(@Nullable String catalogName) ;

	/**
	 * 提供对传入的架构名称的任何修改，以匹配当前使用的元数据。 <p>返回的值将用于元数据查找。这可能包括更改所使用的情况或提供基本模式（如果未提供）。
	 */
	@Nullable String metaDataSchemaNameToUse(@Nullable String schemaName);

	/**
	 * 提供对传入的列名的任何修改，以匹配当前使用的元数据。 <p>这可能包括更改大小写。
	 * @param parameterName 列的参数名称
	 */
	@Nullable String parameterNameToUse(@Nullable String parameterName);

	/**
	 * 返回用于绑定给定参数名称的命名参数的名称。
	 * @param parameterName 要绑定的参数的名称
	 * @return 用于绑定给定参数名称的命名参数的名称
	 * @since 6.1.2
	 */
	String namedParameterBindingToUse(@Nullable String parameterName);

	/**
	 * 根据提供的元数据创建默认输出参数。 <p> 当没有进行显式参数声明时使用。
	 * @param parameterName 参数名称
	 * @param meta 用于此调用的元数据
	 * @return 配置的SqlOutParameter
	 */
	SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 根据提供的元数据创建默认输入/输出参数。 <p> 当没有进行显式参数声明时使用。
	 * @param parameterName 参数名称
	 * @param meta 用于此调用的元数据
	 * @return 配置的SqlInOut参数
	 */
	SqlParameter createDefaultInOutParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 根据提供的元数据创建默认参数。 <p> 当没有进行显式参数声明时使用。
	 * @param parameterName 参数名称
	 * @param meta 用于此调用的元数据
	 * @return 配置的Sql参数
	 */
	SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta);

	/**
	 * 获取当前用户的名称。对于元数据查找等很有用。
	 * @return 来自数据库连接的用户名
	 */
	@Nullable String getUserName();

	/**
	 * 我们是否使用过程列的元数据？
	 */
	boolean isProcedureColumnMetaDataUsed();

	/**
	 * 此数据库是否支持返回应使用 JDBC 调用检索的结果集：{@link java.sql.Statement#getResultSet()}？
	 */
	boolean isReturnResultSetSupported();

	/**
	 * 此数据库是否支持返回 ResultSet 作为引用游标，以便使用 {@link java.sql.CallableStatement#getObject(int)}
	 * 检索指定列？
	 */
	boolean isRefCursorSupported();

	/**
	 * 如果支持此功能，则获取返回 ResultSet 作为引用游标的列的 {@link java.sql.Types} 类型。
	 */
	int getRefCursorSqlType();

	/**
	 * 我们应该绕过指定名称的返回参数吗？ <p>这允许数据库特定实现跳过对数据库调用返回的特定结果的处理。
	 */
	boolean byPassReturnParameter(String parameterName);

	/**
	 * 数据库是否支持在过程调用中使用目录名称？
	 */
	boolean isSupportsCatalogsInProcedureCalls();

	/**
	 * 数据库是否支持在过程调用中使用架构名称？
	 */
	boolean isSupportsSchemasInProcedureCalls();

}
