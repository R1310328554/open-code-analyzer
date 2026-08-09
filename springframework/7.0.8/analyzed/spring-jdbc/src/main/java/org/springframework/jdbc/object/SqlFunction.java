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
 * 用于返回单行结果的查询的 SQL“函数”包装器。默认行为是返回 int，但可以通过使用带有额外返回类型参数的构造函数来覆盖。
 * <p>I 旨在用于调用使用“select user()”或“select sysdate from Dual”等查询返回单个结果的 SQL
 * 函数。它不适用于调用更复杂的存储函数或使用 CallableStatement 来调用存储过程或存储函数。使用 StoredProcedure 或 SqlCall
 * 进行此类处理。
 * <p>这是一个具体类，通常不需要子类化。使用此包的代码可以创建此类型的对象，声明 SQL 和参数，然后重复调用适当的 {@code run} 方法来执行该函数。子类只应该为特定
 * 参数和返回类型添加专门的 {@code run} 方法。
 * <p>与所有 RdbmsOperation 对象一样，SqlFunction 对象是线程安全的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @param <T> 结果类型
 * @see StoredProcedure
 */
public class SqlFunction<T> extends MappingSqlQuery<@Nullable T> {

	private final SingleColumnRowMapper<T> rowMapper = new SingleColumnRowMapper<>();


	/**
	 * 允许用作 JavaBean 的构造函数。在调用 {@code compile} 方法和使用此对象之前，必须提供数据源、SQL 和任何参数。
	 * @see #setDataSource
	 * @see #setSql
	 * @see #compile
	 */
	public SqlFunction() {
	}

	/**
	 * 使用 SQL 创建一个新的 SqlFunction 对象，但不带参数。必须添加参数或不设置任何参数。
	 * @param ds 从中获取连接的数据源
	 * @param sql 要执行的 SQL
	 */
	public SqlFunction(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}

	/**
	 * 使用 SQL 和参数创建一个新的 SqlFunction 对象。
	 * @param ds 从中获取连接的数据源
	 * @param sql 要执行的 SQL
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @see java.sql.Types
	 */
	public SqlFunction(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
	}

	/**
	 * 使用 SQL、参数和结果类型创建一个新的 SqlFunction 对象。
	 * @param ds 从中获取连接的数据源
	 * @param sql 要执行的 SQL
	 * @param types 参数的 SQL 类型，如 {@code java.sql.Types} 类中定义
	 * @param resultType 结果对象需要匹配的类型
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
	 * 指定结果对象需要匹配的类型。 <p>如果未指定，则将公开 JDBC 驱动程序返回的结果值。
	 */
	public void setResultType(Class<T> resultType) {
		this.rowMapper.setRequiredType(resultType);
	}


	/**
	 * 此方法的实现从函数返回的单行中提取单个值。如果返回的行数不同，则会将其视为错误。
	 */
	@Override
	protected @Nullable T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return this.rowMapper.mapRow(rs, rowNum);
	}


	/**
	 * 不带参数运行函数的便捷方法。
	 * @return 函数的值
	 */
	public int run() {
		return run(new Object[0]);
	}

	/**
	 * 使用单个 int 参数运行函数的便捷方法。
	 * @param parameter 单个整型参数
	 * @return 函数的值
	 */
	public int run(int parameter) {
		return run(new Object[] {parameter});
	}

	/**
	 * 类似于 SqlQuery.execute([]) 方法。这是执行查询的通用方法，采用多个参数。
	 * @param parameters 参数数组。这些将是基元的对象或对象包装类型。
	 * @return 函数的值
	 */
	public int run(Object... parameters) {
		Object obj = super.findObject(parameters);
		if (!(obj instanceof Number number)) {
			throw new TypeMismatchDataAccessException("Could not convert result object [" + obj + "] to int");
		}
		return number.intValue();
	}

	/**
	 * 不带参数运行函数的便捷方法，将值作为对象返回。
	 * @return 函数的值
	 */
	public @Nullable Object runGeneric() {
		return findObject((Object[]) null, null);
	}

	/**
	 * 使用单个 int 参数运行函数的便捷方法。
	 * @param parameter 单个整型参数
	 * @return 函数作为对象的值
	 */
	public @Nullable Object runGeneric(int parameter) {
		return findObject(parameter);
	}

	/**
	 * 类似于{@code SqlQuery.findObject(Object[])}方法。这是执行查询的通用方法，采用多个参数。
	 * @param parameters 参数数组。这些将是基元的对象或对象包装类型。
	 * @return 函数的值，作为对象
	 * @see #execute(Object[])
	 */
	public @Nullable Object runGeneric(Object[] parameters) {
		return findObject(parameters);
	}

}
