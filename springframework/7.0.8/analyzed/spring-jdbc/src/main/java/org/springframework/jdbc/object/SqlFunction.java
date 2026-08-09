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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * SQL"函数"包装器，用于返回单行结果的查询。
 * 默认返回 int，可通过带返回类型参数的构造器覆盖。
 *
 * <p>用于调用返回单个结果的 SQL 函数，如 "select user()" 或 "select sysdate from dual"。
 * 不适用于调用复杂存储函数，也不适用于通过 CallableStatement 调用存储过程或存储函数；
 * 此类场景请使用 StoredProcedure 或 SqlCall。
 *
 * <p>本类为具体类，通常无需子类化。
 * 使用本包的代码可创建本类对象、声明 SQL 和参数，然后反复调用 {@code run} 方法执行函数。
 * 子类仅用于添加针对特定参数和返回类型的 {@code run} 方法。
 *
 * <p>与所有 RdbmsOperation 对象一样，SqlFunction 对象线程安全。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see StoredProcedure
 */
public class SqlFunction<T> extends MappingSqlQuery<@Nullable T> {

	private final SingleColumnRowMapper<T> rowMapper = new SingleColumnRowMapper<>();


	/**
	 * 允许作为 JavaBean 使用的构造器。
	 * 调用 {@code compile} 并使用本对象前，必须提供 DataSource、SQL 及参数。
	 * @see #setDataSource
	 * @see #setSql
	 * @see #compile
	 */
	public SqlFunction() {
	}

	/**
	 * 创建带 SQL 但无参数的 SqlFunction 对象，需添加参数或确认无参数。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 */
	public SqlFunction(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}

	/**
	 * 创建带 SQL 和参数的 SqlFunction 对象。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}
	 * @see java.sql.Types
	 */
	public SqlFunction(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
	}

	/**
	 * 创建带 SQL、参数和结果类型的 SqlFunction 对象。
	 * @param ds 获取连接的 DataSource
	 * @param sql 要执行的 SQL
	 * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}
	 * @param resultType 结果对象必须匹配的类型
	 * @see #setResultType(Class)
	 * @see java.sql.Types
	 */
	public SqlFunction(DataSource ds, String sql, int[] types, Class<T> resultType) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		setResultType(resultType);
	}


	/**
	 * 指定结果对象必须匹配的类型。
	 * <p>未指定时，结果值将按 JDBC 驱动返回的原样暴露。
	 */
	public void setResultType(Class<T> resultType) {
		this.rowMapper.setRequiredType(resultType);
	}


	/**
	 * 本方法从函数返回的单行中提取单个值；若返回行数不符则视为错误。
	 */
	@Override
	protected @Nullable T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return this.rowMapper.mapRow(rs, rowNum);
	}


	/**
	 * 无参数执行函数的便捷方法。
	 * @return 函数返回值
	 */
	public int run() {
		return run(new Object[0]);
	}

	/**
	 * 以单个 int 参数执行函数的便捷方法。
	 * @param parameter 单个 int 参数
	 * @return 函数返回值
	 */
	public int run(int parameter) {
		return run(new Object[] {parameter});
	}

	/**
	 * 类似 SqlQuery.execute([]) 方法，以可变参数执行查询。
	 * @param parameters 参数数组，为基本类型的对象或包装类型
	 * @return 函数返回值
	 */
	public int run(Object... parameters) {
		Object obj = super.findObject(parameters);
		if (!(obj instanceof Number number)) {
			throw new TypeMismatchDataAccessException("Could not convert result object [" + obj + "] to int");
		}
		return number.intValue();
	}

	/**
	 * 无参数执行函数并以 Object 返回值的便捷方法。
	 * @return 函数返回值
	 */
	public @Nullable Object runGeneric() {
		return findObject((Object[]) null, null);
	}

	/**
	 * 以单个 int 参数执行函数并以 Object 返回值的便捷方法。
	 * @param parameter 单个 int 参数
	 * @return 函数返回值（Object 形式）
	 */
	public @Nullable Object runGeneric(int parameter) {
		return findObject(parameter);
	}

	/**
	 * 类似 {@code SqlQuery.findObject(Object[])} 方法，以参数数组执行查询。
	 * @param parameters 参数数组，为基本类型的对象或包装类型
	 * @return 函数返回值（Object 形式）
	 * @see #execute(Object[])
	 */
	public @Nullable Object runGeneric(Object[] parameters) {
		return findObject(parameters);
	}

}
