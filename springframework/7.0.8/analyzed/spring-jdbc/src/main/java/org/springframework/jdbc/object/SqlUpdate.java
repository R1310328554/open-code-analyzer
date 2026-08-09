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

package org.springframework.jdbc.object;

import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 可复用的 SQL 更新操作对象。
 *
 * <p>本类提供多个 {@code update} 方法，类似查询对象的 {@code execute} 方法。
 *
 * <p>本类为具体类，可子类化（例如添加自定义 update 方法），
 * 也可通过设置 SQL 和声明参数轻松配置。
 *
 * <p>与 Spring Framework 中所有 {@code RdbmsOperation} 类一样，
 * {@code SqlUpdate} 实例在初始化完成后线程安全——
 * 即构造并通过 setter 配置后，可安全地在多线程中使用。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see SqlQuery
 */
public class SqlUpdate extends SqlOperation {

	/**
	 * 更新可影响的最大行数，超出则抛异常；为 0 时忽略。
	 */
	private int maxRowsAffected = 0;

	/**
	 * 必须影响的确切行数；为 0 时忽略。
	 */
	private int requiredRowsAffected = 0;


	/**
	 * 允许作为 JavaBean 使用的构造器，编译和使用前必须提供 DataSource 和 SQL。
	 * @see #setDataSource
	 * @see #setSql
	 */
	public SqlUpdate() {
	}

	/**
	 * 使用给定 DataSource 和 SQL 构造更新对象。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 */
	public SqlUpdate(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}

	/**
	 * 使用给定 DataSource、SQL 和匿名参数构造更新对象。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}
	 * @see java.sql.Types
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
	}

	/**
	 * 使用给定 DataSource、SQL、匿名参数及最大影响行数构造更新对象。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL 语句
	 * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}
	 * @param maxRowsAffected 更新可影响的最大行数
	 * @see java.sql.Types
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types, int maxRowsAffected) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.maxRowsAffected = maxRowsAffected;
	}


	/**
	 * 设置本更新可影响的最大行数，默认 0 表示不限制。
	 * @param maxRowsAffected 超出此值时 update 方法视为错误
	 */
	public void setMaxRowsAffected(int maxRowsAffected) {
		this.maxRowsAffected = maxRowsAffected;
	}

	/**
	 * 设置本更新必须影响的<i>确切</i>行数，默认 0 表示任意行数均可。
	 * <p>这是设置最大影响行数的替代方案。
	 * @param requiredRowsAffected 行数不符时 update 方法视为错误
	 */
	public void setRequiredRowsAffected(int requiredRowsAffected) {
		this.requiredRowsAffected = requiredRowsAffected;
	}

	/**
	 * 检查实际影响行数是否符合最大或必需行数限制。
	 * @param rowsAffected 实际影响行数
	 * @throws JdbcUpdateAffectedIncorrectNumberOfRowsException 行数超出限制时
	 * @see #setMaxRowsAffected
	 * @see #setRequiredRowsAffected
	 */
	protected void checkRowsAffected(int rowsAffected) throws JdbcUpdateAffectedIncorrectNumberOfRowsException {
		if (this.maxRowsAffected > 0 && rowsAffected > this.maxRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(resolveSql(), this.maxRowsAffected, rowsAffected);
		}
		if (this.requiredRowsAffected > 0 && rowsAffected != this.requiredRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(resolveSql(), this.requiredRowsAffected, rowsAffected);
		}
	}


	/**
	 * 以给定参数执行更新的通用方法，其他 update 方法均调用本方法。
	 * @param params 参数对象数组
	 * @return 更新影响的行数
	 */
	public int update(Object... params) throws DataAccessException {
		validateParameters(params);
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(params));
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

	/**
	 * 以给定参数执行更新，并通过 KeyHolder 获取生成的主键。
	 * @param params 参数对象数组
	 * @param generatedKeyHolder 存放生成主键的 KeyHolder
	 * @return 更新影响的行数
	 */
	public int update(Object[] params, KeyHolder generatedKeyHolder) throws DataAccessException {
		if (!isReturnGeneratedKeys() && getGeneratedKeysColumnNames() == null) {
			throw new InvalidDataAccessApiUsageException(
					"The update method taking a KeyHolder should only be used when generated keys have " +
					"been configured by calling either 'setReturnGeneratedKeys' or " +
					"'setGeneratedKeysColumnNames'.");
		}
		validateParameters(params);
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(params), generatedKeyHolder);
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

	/**
	 * 无参数执行更新的便捷方法。
	 */
	public int update() throws DataAccessException {
		return update(new Object[0]);
	}

	/**
	 * 以单个 int 参数执行更新的便捷方法。
	 */
	public int update(int p1) throws DataAccessException {
		return update(new Object[] {p1});
	}

	/**
	 * 以两个 int 参数执行更新的便捷方法。
	 */
	public int update(int p1, int p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 以单个 long 参数执行更新的便捷方法。
	 */
	public int update(long p1) throws DataAccessException {
		return update(new Object[] {p1});
	}

	/**
	 * 以两个 long 参数执行更新的便捷方法。
	 */
	public int update(long p1, long p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 以单个 String 参数执行更新的便捷方法。
	 */
	public int update(String p) throws DataAccessException {
		return update(new Object[] {p});
	}

	/**
	 * 以两个 String 参数执行更新的便捷方法。
	 */
	public int update(String p1, String p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 以命名参数执行更新的通用方法。
	 * @param paramMap 参数名到参数对象的映射，与 SQL 中的命名参数对应
	 * @return 更新影响的行数
	 */
	public int updateByNamedParam(Map<String, ?> paramMap) throws DataAccessException {
		validateNamedParameters(paramMap);
		ParsedSql parsedSql = getParsedSql();
		MapSqlParameterSource paramSource = new MapSqlParameterSource(paramMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		@Nullable Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, getDeclaredParameters());
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(sqlToUse, params));
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

	/**
	 * 以命名参数执行更新，并通过 KeyHolder 获取生成的主键。
	 * @param paramMap 参数名到参数对象的映射，与 SQL 中的命名参数对应
	 * @param generatedKeyHolder 存放生成主键的 KeyHolder
	 * @return 更新影响的行数
	 */
	public int updateByNamedParam(Map<String, ?> paramMap, KeyHolder generatedKeyHolder) throws DataAccessException {
		validateNamedParameters(paramMap);
		ParsedSql parsedSql = getParsedSql();
		MapSqlParameterSource paramSource = new MapSqlParameterSource(paramMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		@Nullable Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, getDeclaredParameters());
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(sqlToUse, params), generatedKeyHolder);
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

}
