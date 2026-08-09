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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.ConversionService;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * 流畅的 {@code JdbcClient}，具有常见的 JDBC 查询和更新操作，支持 JDBC 样式的位置以及 Spring 样式的命名参数，并为 JDBC {@code
 * PreparedStatement} 执行提供方便的统一外观。
 * <p> 用于将查询结果检索为 {@code java.util.Optional} 的示例： <pre class="code"> 可选<Integer> value =
 * client.sql("从客户中选择年龄，其中 ID = :id") .param("id", 3) .query(Integer.class) .optional();
 * </pre>
 * <p>委托给 {@link org.springframework.jdbc.core.JdbcTemplate} 和 {@link
 * org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate}。对于复杂的 JDBC
 * 操作——例如，批量插入和存储过程调用——您可以直接使用这些较低级别的模板类，也可以使用 {@link SimpleJdbcInsert} 和 {@link
 * SimpleJdbcCall}。
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 6.1
 * @see ResultSetExtractor
 * @see RowCallbackHandler
 * @see RowMapper
 * @see JdbcOperations
 * @see NamedParameterJdbcOperations
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
 */
public interface JdbcClient {

	/**
	 * 任何 JDBC 操作的起点：自定义 SQL 字符串。
	 * @param sql SQL 查询或更新语句作为字符串
	 * @return 链式语句规范
	 */
	StatementSpec sql(String sql);


	// 静态工厂方法

	/**
	 * 为给定的 {@link DataSource} 创建 {@code JdbcClient}。
	 * @param dataSource 从中获取连接的数据源
	 */
	static JdbcClient create(DataSource dataSource) {
		return new DefaultJdbcClient(dataSource);
	}

	/**
	 * 为给定的 {@link JdbcOperations} 委托创建 {@code JdbcClient}，通常是 {@link
	 * org.springframework.jdbc.core.JdbcTemplate}。 <p> 使用此工厂方法可以重用现有的 {@code JdbcTemplate}
	 * 配置，包括其 {@code DataSource}。
	 * @param jdbcTemplate 执行操作的委托
	 */
	static JdbcClient create(JdbcOperations jdbcTemplate) {
		return new DefaultJdbcClient(jdbcTemplate);
	}

	/**
	 * 为给定的 {@link NamedParameterJdbcOperations} 委托创建 {@code JdbcClient}，通常是 {@link
	 * org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate}。
	 * <p>使用此工厂方法可以重用现有的{@code NamedParameterJdbcTemplate}配置，包括其底层的{@code JdbcTemplate}和{@code
	 * DataSource}。
	 * @param jdbcTemplate 执行操作的委托
	 */
	static JdbcClient create(NamedParameterJdbcOperations jdbcTemplate) {
		return new DefaultJdbcClient(jdbcTemplate, null);
	}

	/**
	 * 为给定的 {@link NamedParameterJdbcOperations} 委托创建 {@code JdbcClient}，通常是 {@link
	 * org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate}。 <p> 使用此工厂方法可以重用现有的
	 * {@code NamedParameterJdbcTemplate} 配置，包括其底层 {@code JdbcTemplate} 和 {@code
	 * DataSource}，以及用于查询映射类的自定义 {@link ConversionService}。
	 * @param jdbcTemplate 执行操作的委托
	 * @param conversionService {@link ConversionService}，用于将获取的 JDBC 值转换为 {@link StatementSpec#query(Class)} 中的映射类
	 * @since 7.0
	 */
	static JdbcClient create(NamedParameterJdbcOperations jdbcTemplate, ConversionService conversionService) {
		return new DefaultJdbcClient(jdbcTemplate, conversionService);
	}


	/**
	 * 参数绑定和查询/更新执行的语句规范。
	 */
	interface StatementSpec {

		/**
		 * 将给定的提取大小应用于任何后续查询语句。
		 * @param fetchSize 获取大小
		 * @since 7.0
		 * @see org.springframework.jdbc.core.JdbcTemplate#setFetchSize
		 */
		StatementSpec withFetchSize(int fetchSize);

		/**
		 * 将给定的最大行数应用于任何后续查询语句。
		 * @param maxRows 最大行数
		 * @since 7.0
		 * @see org.springframework.jdbc.core.JdbcTemplate#setMaxRows
		 */
		StatementSpec withMaxRows(int maxRows);

		/**
		 * 将给定的查询超时应用于任何后续查询语句。
		 * @param queryTimeout 查询超时（以秒为单位）
		 * @since 7.0
		 * @see org.springframework.jdbc.core.JdbcTemplate#setQueryTimeout
		 */
		StatementSpec withQueryTimeout(int queryTimeout);

		/**
		 * 为“?”绑定位置 JDBC 语句参数通过参数值注册的隐式顺序解析占位符。 <p>这主要适用于带有单个参数或很少参数的语句，按照参数在SQL语句中出现的顺序注册每个参数值。
		 * @param value 要绑定的参数值
		 * @return 语句规范（用于链接）
		 * @see java.sql.PreparedStatement#setObject(int, Object)
		 */
		StatementSpec param(@Nullable Object value);

		/**
		 * 为“?”绑定位置 JDBC 语句参数通过显式 JDBC 语句参数索引进行占位符解析。
		 * @param jdbcIndex JDBC 样式索引（从 1 开始）
		 * @param value 要绑定的参数值
		 * @return 语句规范（用于链接）
		 * @see java.sql.PreparedStatement#setObject(int, Object)
		 */
		StatementSpec param(int jdbcIndex, @Nullable Object value);

		/**
		 * 为“?”绑定位置 JDBC 语句参数通过显式 JDBC 语句参数索引进行占位符解析。
		 * @param jdbcIndex JDBC 样式索引（从 1 开始）
		 * @param value 要绑定的参数值
		 * @param sqlType 关联的 SQL 类型（请参阅 {@link java.sql.Types}）
		 * @return 语句规范（用于链接）
		 * @see java.sql.PreparedStatement#setObject(int, Object, int)
		 */
		StatementSpec param(int jdbcIndex, @Nullable Object value, int sqlType);

		/**
		 * 为“:x”占位符解析绑定命名语句参数，每个“x”名称与 SQL 语句中的“:x”占位符匹配。
		 * @param name 参数名称
		 * @param value 要绑定的参数值
		 * @return 语句规范（用于链接）
		 * @see org.springframework.jdbc.core.namedparam.MapSqlParameterSource#addValue(String, Object)
		 */
		StatementSpec param(String name, @Nullable Object value);

		/**
		 * 为“:x”占位符解析绑定命名语句参数，每个“x”名称与 SQL 语句中的“:x”占位符匹配。
		 * @param name 参数名称
		 * @param value 要绑定的参数值
		 * @param sqlType 关联的 SQL 类型（请参阅 {@link java.sql.Types}）
		 * @return 语句规范（用于链接）
		 * @see org.springframework.jdbc.core.namedparam.MapSqlParameterSource#addValue(String, Object, int)
		 */
		StatementSpec param(String name, @Nullable Object value, int sqlType);

		/**
		 * 为“?”绑定位置参数的 var-args 列表占位符分辨率。 <p>给定列表将添加到现有位置参数（如果有）中。完整列表中的每个元素将作为 JDBC 位置参数与相应的 JDBC 
		 * 索引（即列表索引 + 1）绑定。
		 * @param values 要绑定的参数值
		 * @return 语句规范（用于链接）
		 * @see #param(Object)
		 * @see #params(List)
		 */
		StatementSpec params(Object... values);

		/**
		 * 为“?”绑定位置参数列表占位符分辨率。 <p>给定列表将添加到现有位置参数（如果有）中。完整列表中的每个元素将作为 JDBC 位置参数与相应的 JDBC 索引（即列表索引 + 
		 * 1）绑定。
		 * @param values 要绑定的参数值
		 * @return 语句规范（用于链接）
		 * @see #param(Object)
		 */
		StatementSpec params(List<?> values);

		/**
		 * 为“:x”占位符解析绑定命名语句参数。 <p>给定的映射将合并到现有的命名参数（如果有）中。
		 * @param paramMap 要绑定的名称和参数值的映射
		 * @return 语句规范（用于链接）
		 * @see #param(String, Object)
		 */
		StatementSpec params(Map<String, ?> paramMap);

		/**
		 * 为“:x”占位符解析绑定命名语句参数。 <p>给定的参数对象将根据其JavaBean属性、记录组件或原始字段定义所有命名参数。 Map 实例也可以作为完整的参数源提供。
		 * @param namedParamObject 具有用作语句参数的命名属性的自定义参数对象（例如，JavaBean、记录类或字段持有者）
		 * @return 语句规范（用于链接）
		 * @see #paramSource(SqlParameterSource)
		 * @see org.springframework.jdbc.core.namedparam.MapSqlParameterSource
		 * @see org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource
		 */
		StatementSpec paramSource(Object namedParamObject);

		/**
		 * 为“:x”占位符解析绑定命名语句参数。 <p>给定的参数源将定义所有命名参数，可能将特定的 SQL 类型与每个值相关联。
		 * @param namedParamSource 自定义 {@link SqlParameterSource} 实例
		 * @return 语句规范（用于链接）
		 * @see org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource#registerSqlType
		 */
		StatementSpec paramSource(SqlParameterSource namedParamSource);

		/**
		 * 继续执行查询，返回的查询规范中提供多个结果选项。
		 * @return 结果查询规范
		 * @see java.sql.PreparedStatement#executeQuery()
		 */
		ResultQuerySpec query();

		/**
		 * 继续执行映射查询，返回的查询规范中有多个可用选项。
		 * @param mappedClass 应用 RowMapper 的目标类（用于单列映射的简单值类型或多列映射的 JavaBean/记录类/字段持有者）
		 * @return 映射查询规范
		 * @see #query(RowMapper)
		 * @see org.springframework.jdbc.core.SingleColumnRowMapper
		 * @see org.springframework.jdbc.core.SimplePropertyRowMapper
		 */
		<T> MappedQuerySpec<@Nullable T> query(Class<T> mappedClass);

		/**
		 * 继续执行映射查询，返回的查询规范中有多个可用选项。
		 * @param rowMapper 用于映射 ResultSet 中每一行的回调
		 * @return 映射查询规范
		 * @see java.sql.PreparedStatement#executeQuery()
		 */
		<T extends @Nullable Object> MappedQuerySpec<T> query(RowMapper<T> rowMapper);

		/**
		 * 使用提供的 SQL 语句执行查询，使用给定的回调处理每一行。
		 * @param rch 用于处理 ResultSet 中每一行的回调
		 * @see java.sql.PreparedStatement#executeQuery()
		 */
		void query(RowCallbackHandler rch);

		/**
		 * 使用提供的 SQL 语句执行查询，返回整个 ResultSet 的结果对象。
		 * @param rse 用于处理整个 ResultSet 的回调
		 * @return ResultSetExtractor 返回的值
		 * @see java.sql.PreparedStatement#executeQuery()
		 */
		<T extends @Nullable Object> T query(ResultSetExtractor<T> rse);

		/**
		 * 执行提供的 SQL 语句作为更新。
		 * @return 受影响的行数
		 * @see java.sql.PreparedStatement#executeUpdate()
		 */
		int update();

		/**
		 * 执行提供的 SQL 语句作为更新。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
		 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder（通常是 {@link org.springframework.jdbc.support.GeneratedKeyHolder}）
		 * @return 受影响的行数
		 * @see java.sql.PreparedStatement#executeUpdate()
		 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
		 */
		int update(KeyHolder generatedKeyHolder);

		/**
		 * 执行提供的 SQL 语句作为更新。 <p>此方法需要支持 JDBC 驱动程序中生成的键。
		 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder（通常是 {@link org.springframework.jdbc.support.GeneratedKeyHolder}）
		 * @param keyColumnNames 将为其生成键的列的名称
		 * @return 受影响的行数
		 * @see java.sql.PreparedStatement#executeUpdate()
		 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
		 */
		int update(KeyHolder generatedKeyHolder, String... keyColumnNames);
	}


	/**
	 * 简单结果查询的规范。
	 */
	interface ResultQuerySpec {

		/**
		 * 以行集形式检索结果。
		 * @return 原始数据库结果的分离行集表示
		 */
		SqlRowSet rowSet();

		/**
		 * 以行列表形式检索结果，保留原始数据库结果的顺序。
		 * @return （可能为空）行列表，每个结果行表示为不区分大小写的列名称到列值的映射
		 */
		List<Map<String, @Nullable Object>> listOfRows();

		/**
		 * 检索单行结果。
		 * @return 结果行表示为不区分大小写的列名称到列值的映射
		 */
		Map<String, @Nullable Object> singleRow();

		/**
		 * 检索单列结果，保留原始数据库结果的顺序。
		 * @return （可能为空）行列表，每行表示为其单列值
		 */
		List<@Nullable Object> singleColumn();

		/**
		 * 检索单个值结果。 <p>注意：从 6.2 开始，这将按照最初的设计强制执行非空结果值（只是之前不小心没有强制执行）。 （从来没有 {@code null}）
		 * @see #optionalValue()
		 * @see DataAccessUtils#requiredSingleResult(Collection)
		 */
		default Object singleValue() {
			return DataAccessUtils.requiredSingleResult(singleColumn());
		}

		/**
		 * 检索单值结果（如果可用）作为 {@link Optional} 句柄。
		 * @return 带有单行单列值的可选句柄
		 * @since 6.2
		 * @see #singleValue()
		 * @see DataAccessUtils#optionalResult(Collection)
		 */
		default Optional<Object> optionalValue() {
			return DataAccessUtils.optionalResult(singleColumn());
		}
	}


	/**
	 * RowMapper 映射查询的规范。
	 * @param <T> RowMapper 声明的结果类型
	 */
	interface MappedQuerySpec<T extends @Nullable Object> {

		/**
		 * 以延迟解析的映射对象流的形式检索结果，保留原始数据库结果的顺序。
		 * @return 结果流，包含映射对象，完全处理后需要关闭（例如，通过 try-with-resources 子句）
		 */
		Stream<T> stream();

		/**
		 * 将结果检索为映射对象的预解析列表，保留原始数据库结果的顺序。
		 * @return 结果作为独立列表，包含映射对象
		 */
		List<T> list();

		/**
		 * 将结果检索为一组保留顺序的映射对象。
		 * @return 结果作为一个独立的集合，包含映射对象
		 * @see #list()
		 * @see LinkedHashSet
		 */
		default Set<T> set() {
			return new LinkedHashSet<>(list());
		}

		/**
		 * 检索单个结果作为所需的对象实例。 <p>注意：从 6.2 开始，这将按照最初的设计强制执行非空结果值（只是之前不小心没有强制执行）。
		 * @return 单个结果对象（绝不是 {@code null}）
		 * @see #optional()
		 * @see DataAccessUtils#requiredSingleResult(Collection)
		 */
		default @NonNull T single() {
			return DataAccessUtils.requiredSingleResult(list());
		}

		/**
		 * 检索单个结果（如果可用）作为 {@link Optional} 句柄。
		 * @return 带有单个结果对象或无结果对象的可选句柄
		 * @see #single()
		 * @see DataAccessUtils#optionalResult(Collection)
		 */
		default Optional<@NonNull T> optional() {
			return DataAccessUtils.optionalResult(list());
		}
	}

}
