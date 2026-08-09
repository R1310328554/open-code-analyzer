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
 * 代表 SQL 更新的可重用操作对象。
 * <p>该类提供了许多{@code update}方法，类似于查询对象的{@code execute}方法。
 * <p>这个类是具体的。虽然它可以被子类化（例如添加自定义更新方法），但它可以通过设置 SQL 和声明参数轻松地参数化。
 * <p> 与 Spring 框架附带的所有 {@code RdbmsOperation} 类一样，{@code SqlQuery} 实例在初始化完成后是线程安全的。也就是说，在通
 * 过 setter 方法构造和配置它们之后，可以从多个线程安全地使用它们。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see SqlQuery
 */
public class SqlUpdate extends SqlOperation {

	/**
	 * 更新可能影响的最大行数。如果更多的人受到影响，则会抛出异常。如果为 0，则忽略。
	 */
	private int maxRowsAffected = 0;

	/**
	 * 必须受影响的确切行数。如果为 0，则忽略。
	 */
	private int requiredRowsAffected = 0;


	/**
	 * 允许用作 JavaBean 的构造函数。编译和使用前必须提供DataSource和SQL。
	 * @see #setDataSource
	 * @see #setSql
	 */
	public SqlUpdate() {
	}

	/**
	 * 使用给定的 DataSource 和 SQL 构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 */
	public SqlUpdate(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}

	/**
	 * 使用给定的 DataSource、SQL 和匿名参数构造更新对象。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @see java.sql.Types
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
	}

	/**
	 * 使用给定的 DataSource、SQL、匿名参数构造一个更新对象，并指定可能受影响的最大行数。
	 * @param ds 用于获取连接的 DataSource
	 * @param sql 要执行的SQL语句
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @param maxRowsAffected 可能受更新影响的最大行数
	 * @see java.sql.Types
	 */
	public SqlUpdate(DataSource ds, String sql, int[] types, int maxRowsAffected) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.maxRowsAffected = maxRowsAffected;
	}


	/**
	 * 设置可能受此更新影响的最大行数。默认值为 0，不限制受影响的行数。
	 * @param maxRowsAffected 如果此类的更新方法不将其视为错误，则可以受此更新影响的最大行数
	 */
	public void setMaxRowsAffected(int maxRowsAffected) {
		this.maxRowsAffected = maxRowsAffected;
	}

	/**
	 * 设置 <i>exact</i> 必须受此更新影响的行数。默认值为 0，允许影响任意数量的行。 <p>这是设置 <i>maximum</i> 可能受影响的行数的替代方法。
	 * @param requiredRowsAffected 在没有此类更新方法将其视为错误的情况下，必须受此更新影响的确切行数
	 */
	public void setRequiredRowsAffected(int requiredRowsAffected) {
		this.requiredRowsAffected = requiredRowsAffected;
	}

	/**
	 * 根据指定的最大数量或所需数量检查给定的受影响行数。
	 * @param rowsAffected 受影响的行数
	 * @throws JdbcUpdateAffectedIncorrectNumberOfRowsException 如果实际受影响的行超出范围
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
	 * 执行更新给定参数的通用方法。所有其他更新方法都会调用此方法。
	 * @param params 参数对象数组
	 * @return 受更新影响的行数
	 */
	public int update(Object... params) throws DataAccessException {
		validateParameters(params);
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(params));
		checkRowsAffected(rowsAffected);
		return rowsAffected;
	}

	/**
	 * 执行更新给定参数并使用 KeyHolder 检索生成的密钥的方法。
	 * @param params 参数对象数组
	 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder
	 * @return 受更新影响的行数
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
	 * 不带参数执行更新的便捷方法。
	 */
	public int update() throws DataAccessException {
		return update(new Object[0]);
	}

	/**
	 * 给定一个 int arg 执行更新的便捷方法。
	 */
	public int update(int p1) throws DataAccessException {
		return update(new Object[] {p1});
	}

	/**
	 * 给定两个 int 参数执行更新的便捷方法。
	 */
	public int update(int p1, int p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 给定一个长参数来执行更新的便捷方法。
	 */
	public int update(long p1) throws DataAccessException {
		return update(new Object[] {p1});
	}

	/**
	 * 给定两个长参数来执行更新的便捷方法。
	 */
	public int update(long p1, long p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 给定一个字符串参数执行更新的便捷方法。
	 */
	public int update(String p) throws DataAccessException {
		return update(new Object[] {p});
	}

	/**
	 * 给定两个字符串参数执行更新的便捷方法。
	 */
	public int update(String p1, String p2) throws DataAccessException {
		return update(new Object[] {p1, p2});
	}

	/**
	 * 执行给定命名参数更新的通用方法。所有其他更新方法都会调用此方法。
	 * @param paramMap 参数名称到参数对象的映射，匹配 SQL 语句中指定的命名参数
	 * @return 受更新影响的行数
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
	 * 执行更新给定参数并使用 KeyHolder 检索生成的密钥的方法。
	 * @param paramMap 参数名称到参数对象的映射，匹配 SQL 语句中指定的命名参数
	 * @param generatedKeyHolder 将保存生成的密钥的 KeyHolder
	 * @return 受更新影响的行数
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
