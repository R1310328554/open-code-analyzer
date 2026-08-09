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
 * 指定 {@link SimpleJdbcCall} 实现的简单 JDBC 调用 API 的接口。该接口通常不直接使用，但提供了增强可测试性的选项，因为它可以轻松地被模拟或存根。
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @since 2.5
 */
public interface SimpleJdbcCallOperations {

	/**
	 * 指定要使用的过程名称 - 这意味着我们将调用存储过程。
	 * @param procedureName 存储过程的名称
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withProcedureName(String procedureName);

	/**
	 * 指定要使用的过程名称 - 这意味着我们将调用一个存储函数。
	 * @param functionName 存储的函数的名称
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withFunctionName(String functionName);

	/**
	 * （可选）指定包含存储过程的架构的名称。
	 * @param schemaName 模式的名称
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withSchemaName(String schemaName);

	/**
	 * （可选）指定包含存储过程的目录的名称。 <p> 为了提供与 Oracle 数据库元数据的一致性，如果过程被声明为包的一部分，则用于指定包名称。
	 * @param catalogName 目录或包名称
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withCatalogName(String catalogName);

	/**
	 * 指示过程的返回值应包含在返回的结果中。
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withReturnValue();

	/**
	 * 如果需要，指定一个或多个参数。这些参数将补充有从数据库元数据检索到的任何参数信息。 <p>请注意，只有声明为 {@code SqlParameter} 和 {@code Sql
	 * InOutParameter} 的参数才会用于提供输入值。这与 {@code StoredProcedure} 类不同，出于向后兼容性的原因，{@code StoredProc
	 * edure} 类允许为声明为 {@code SqlOutParameter} 的参数提供输入值。
	 * @param sqlParameters 要使用的参数
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations declareParameters(SqlParameter... sqlParameters);

	/**
	 */
	SimpleJdbcCallOperations useInParameterNames(String... inParameterNames);

	/**
	 * 用于指定存储过程返回 ResultSet 且您希望它由 {@link RowMapper} 映射的时间。将使用指定的参数名称返回结果。必须以正确的顺序声明多个结果集。 <p>如
	 * 果您使用的数据库使用引用游标，则指定的名称必须与为数据库中的过程声明的参数名称相匹配。
	 * @param parameterName 返回结果的名称和/或引用游标参数的名称
	 * @param rowMapper RowMapper 实现将映射每行返回的数据
	 */
	SimpleJdbcCallOperations returningResultSet(String parameterName, RowMapper<?> rowMapper);

	/**
	 * 关闭对通过 JDBC 获取的参数元数据信息的任何处理。
	 * @return 此 SimpleJdbcCall 的实例
	 */
	SimpleJdbcCallOperations withoutProcedureColumnMetaDataAccess();

	/**
	 * 指示参数应按名称绑定。
	 * @return 此 SimpleJdbcCall 的实例
	 * @since 4.2
	 */
	SimpleJdbcCallOperations withNamedBinding();


	/**
	 * 执行存储的函数并将获得的结果作为指定返回类型的对象返回。
	 * @param returnType 返回值的类型
	 * @param args 包含要在调用中使用的输入参数值的可选数组。参数值的提供顺序必须与为存储过程定义参数的顺序相同。
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, Object... args);

	/**
	 * 执行存储的函数并将获得的结果作为指定返回类型的对象返回。
	 * @param returnType 返回值的类型
	 * @param args 包含调用中要使用的参数值的 Map
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, Map<String, ?> args);

	/**
	 * 执行存储的函数并将获得的结果作为指定返回类型的对象返回。
	 * @param returnType 返回值的类型
	 * @param args 包含要在调用中使用的参数值的 MapSqlParameterSource
	 */
	<T> @Nullable T executeFunction(Class<T> returnType, SqlParameterSource args);

	/**
	 * 执行存储过程并将单个输出参数作为指定返回类型的对象返回。如果有多个输出参数，则返回第一个输出参数，并忽略其他输出参数。
	 * @param returnType 返回值的类型
	 * @param args 包含要在调用中使用的输入参数值的可选数组。参数值的提供顺序必须与为存储过程定义参数的顺序相同。
	 */
	<T> @Nullable T executeObject(Class<T> returnType, Object... args);

	/**
	 * 执行存储过程并将单个输出参数作为指定返回类型的对象返回。如果有多个输出参数，则返回第一个输出参数，并忽略其他输出参数。
	 * @param returnType 返回值的类型
	 * @param args 包含调用中要使用的参数值的 Map
	 */
	<T> @Nullable T executeObject(Class<T> returnType, Map<String, ?> args);

	/**
	 * 执行存储过程并将单个输出参数作为指定返回类型的对象返回。如果有多个输出参数，则返回第一个输出参数，并忽略其他输出参数。
	 * @param returnType 返回值的类型
	 * @param args 包含要在调用中使用的参数值的 MapSqlParameterSource
	 */
	<T> @Nullable T executeObject(Class<T> returnType, SqlParameterSource args);

	/**
	 * 执行存储过程并返回输出参数的映射，按参数声明中的名称键入。
	 * @param args 包含要在调用中使用的输入参数值的可选数组。参数值的提供顺序必须与为存储过程定义参数的顺序相同。
	 * @return 输出参数映射
	 */
	Map<String, @Nullable Object> execute(Object... args);

	/**
	 * 执行存储过程并返回输出参数的映射，按参数声明中的名称键入。
	 * @param args 包含调用中要使用的参数值的 Map
	 * @return 输出参数映射
	 */
	Map<String, @Nullable Object> execute(Map<String, ?> args);

	/**
	 * 执行存储过程并返回输出参数的映射，按参数声明中的名称键入。
	 * @param args 包含要在调用中使用的参数值的 SqlParameterSource
	 * @return 输出参数映射
	 */
	Map<String, @Nullable Object> execute(SqlParameterSource args);

}
