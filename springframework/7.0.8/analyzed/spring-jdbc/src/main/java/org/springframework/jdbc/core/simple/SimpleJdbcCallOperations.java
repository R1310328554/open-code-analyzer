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

package org.springframework.jdbc.core.simple;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * 定义 {@link SimpleJdbcCall} 实现的 Simple JDBC Call API 接口。
 * 本接口不常直接使用，但可增强可测试性，
 * 因其易于 mock 或 stub。
 *
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @since 2.5
 */
public interface SimpleJdbcCallOperations {

	/**
	 * 指定要使用的存储过程名称——表示将调用存储过程。
	 * @param procedureName 存储过程名称
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withProcedureName(String procedureName);

	/**
	 * 指定要使用的函数名称——表示将调用存储函数。
	 * @param functionName 存储函数名称
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withFunctionName(String functionName);

	/**
	 * 可选地指定包含存储过程的 schema 名称。
	 * @param schemaName schema 名称
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withSchemaName(String schemaName);

	/**
	 * 可选地指定包含存储过程的 catalog 名称。
	 * <p>为与 Oracle DatabaseMetaData 保持一致，
	 * 若过程作为包的一部分声明，则用于指定包名。
	 * @param catalogName catalog 或包名
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withCatalogName(String catalogName);

	/**
	 * 指示过程的返回值应包含在返回结果中。
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withReturnValue();

	/**
	 * 按需指定一个或多个参数。这些参数将补充从数据库元数据获取的参数信息。
	 * <p>注意：仅声明为 {@code SqlParameter} 和 {@code SqlInOutParameter} 的参数
	 * 用于提供输入值。这与 {@code StoredProcedure} 类不同——
	 * 后者出于向后兼容允许为声明为 {@code SqlOutParameter} 的参数提供输入值。
	 * @param sqlParameters 要使用的参数
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations declareParameters(SqlParameter... sqlParameters);

	/** 尚未使用。 */
	SimpleJdbcCallOperations useInParameterNames(String... inParameterNames);

	/**
	 * 用于指定存储过程返回 ResultSet 且需由 {@link RowMapper} 映射时。
	 * 结果将使用指定的参数名称返回。多个 ResultSet 须按正确顺序声明。
	 * <p>若所用数据库使用 ref cursor，则指定名称须与
	 * 数据库中为过程声明的参数名称匹配。
	 * @param parameterName 返回结果的名称和/或 ref cursor 参数名称
	 * @param rowMapper 映射每行返回数据的 RowMapper 实现
	 * */
	SimpleJdbcCallOperations returningResultSet(String parameterName, RowMapper<?> rowMapper);

	/**
	 * 关闭通过 JDBC 获取的参数元数据信息的任何处理。
	 * @return 此 SimpleJdbcCall 实例
	 */
	SimpleJdbcCallOperations withoutProcedureColumnMetaDataAccess();

	/**
	 * 指示参数应按名称绑定。
	 * @return 此 SimpleJdbcCall 实例
	 * @since 4.2
	 */
	SimpleJdbcCallOperations withNamedBinding();


	/**
	 * 执行存储函数并以指定返回类型的 Object 返回结果。
	 * @param returnType 要返回值的类型
	 * @param args 可选数组，包含调用中使用的入参值。
	 * 参数值须与存储过程定义的参数顺序一致。
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, Object... args);

	/**
	 * 执行存储函数并以指定返回类型的 Object 返回结果。
	 * @param returnType 要返回值的类型
	 * @param args 包含调用中使用的参数值的 Map
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, Map<String, ?> args);

	/**
	 * 执行存储函数并以指定返回类型的 Object 返回结果。
	 * @param returnType 要返回值的类型
	 * @param args 包含调用中使用的参数值的 MapSqlParameterSource
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, SqlParameterSource args);

	/**
	 * 执行存储过程并以指定返回类型的 Object 返回单个出参。
	 * 若有多个出参，返回第一个，其余忽略。
	 * @param returnType 要返回值的类型
	 * @param args 可选数组，包含调用中使用的入参值。
	 * 参数值须与存储过程定义的参数顺序一致。
	 */
	<T> @Nullable T executeObject(Class<T> returnType, Object... args);

	/**
	 * 执行存储过程并以指定返回类型的 Object 返回单个出参。
	 * 若有多个出参，返回第一个，其余忽略。
	 * @param returnType 要返回值的类型
	 * @param args 包含调用中使用的参数值的 Map
	 */
	<T> @Nullable T executeObject(Class<T> returnType, Map<String, ?> args);

	/**
	 * 执行存储过程并以指定返回类型的 Object 返回单个出参。
	 * 若有多个出参，返回第一个，其余忽略。
	 * @param returnType 要返回值的类型
	 * @param args 包含调用中使用的参数值的 MapSqlParameterSource
	 */
	<T> @Nullable T executeObject(Class<T> returnType, SqlParameterSource args);

	/**
	 * 执行存储过程并返回出参 Map，键为参数声明中的名称。
	 * @param args 可选数组，包含调用中使用的入参值。
	 * 参数值须与存储过程定义的参数顺序一致。
	 * @return 出参 Map
	 */
	Map<String, @Nullable Object> execute(Object... args);

	/**
	 * 执行存储过程并返回出参 Map，键为参数声明中的名称。
	 * @param args 包含调用中使用的参数值的 Map
	 * @return 出参 Map
	 */
	Map<String, @Nullable Object> execute(Map<String, ?> args);

	/**
	 * 执行存储过程并返回出参 Map，键为参数声明中的名称。
	 * @param args 包含调用中使用的参数值的 SqlParameterSource
	 * @return 出参 Map
	 */
	Map<String, @Nullable Object> execute(SqlParameterSource args);

}
