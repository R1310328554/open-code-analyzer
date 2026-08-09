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

package org.springframework.jdbc.core;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 表示 SQL 参数定义的对象。
 *
 * <p>参数可以是匿名的，此时 "name" 为 {@code null}。
 * 但所有参数必须根据 {@link java.sql.Types} 定义 SQL 类型。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see java.sql.Types
 */
public class SqlParameter {

	// 参数名称（若有）
	private @Nullable String name;

	// 来自 {@code java.sql.Types} 的 SQL 类型常量
	private final int sqlType;

	// 用于用户命名类型，如 STRUCT、DISTINCT、JAVA_OBJECT、命名数组类型
	private @Nullable String typeName;

	// NUMERIC 或 DECIMAL 类型的小数位数（若有）
	private @Nullable Integer scale;


	/**
	 * 创建新的匿名 SqlParameter，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 */
	public SqlParameter(int sqlType) {
		this.sqlType = sqlType;
	}

	/**
	 * 创建新的匿名 SqlParameter，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param typeName 参数的类型名（可选）
	 */
	public SqlParameter(int sqlType, @Nullable String typeName) {
		this.sqlType = sqlType;
		this.typeName = typeName;
	}

	/**
	 * 创建新的匿名 SqlParameter，指定 SQL 类型。
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）
	 */
	public SqlParameter(int sqlType, int scale) {
		this.sqlType = sqlType;
		this.scale = scale;
	}

	/**
	 * 创建新的 SqlParameter，指定名称和 SQL 类型。
	 * @param name 参数名称，用于输入和输出 Map
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 */
	public SqlParameter(String name, int sqlType) {
		this.name = name;
		this.sqlType = sqlType;
	}

	/**
	 * 创建新的 SqlParameter，指定名称和 SQL 类型。
	 * @param name 参数名称，用于输入和输出 Map
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param typeName 参数的类型名（可选）
	 */
	public SqlParameter(String name, int sqlType, @Nullable String typeName) {
		this.name = name;
		this.sqlType = sqlType;
		this.typeName = typeName;
	}

	/**
	 * 创建新的 SqlParameter，指定名称和 SQL 类型。
	 * @param name 参数名称，用于输入和输出 Map
	 * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}
	 * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）
	 */
	public SqlParameter(String name, int sqlType, int scale) {
		this.name = name;
		this.sqlType = sqlType;
		this.scale = scale;
	}

	/**
	 * 拷贝构造器。
	 * @param otherParam 要复制的 SqlParameter 对象
	 */
	public SqlParameter(SqlParameter otherParam) {
		Assert.notNull(otherParam, "SqlParameter object must not be null");
		this.name = otherParam.name;
		this.sqlType = otherParam.sqlType;
		this.typeName = otherParam.typeName;
		this.scale = otherParam.scale;
	}


	/**
	 * 返回参数名称，匿名时返回 {@code null}。
	 */
	public @Nullable String getName() {
		return this.name;
	}

	/**
	 * 返回参数的 SQL 类型。
	 */
	public int getSqlType() {
		return this.sqlType;
	}

	/**
	 * 返回参数的类型名（若有）。
	 */
	public @Nullable String getTypeName() {
		return this.typeName;
	}

	/**
	 * 返回参数的小数位数（若有）。
	 */
	public @Nullable Integer getScale() {
		return this.scale;
	}


	/**
	 * 返回此参数是否持有应在执行前设置的输入值，
	 * 即使值为 {@code null}。
	 * <p>本实现始终返回 {@code true}。
	 */
	public boolean isInputValueProvided() {
		return true;
	}

	/**
	 * 返回此参数是否为 {@code CallableStatement.getMoreResults/getUpdateCount}
	 * 结果处理期间使用的隐式返回参数。
	 * <p>本实现始终返回 {@code false}。
	 */
	public boolean isResultsParameter() {
		return false;
	}


	/**
	 * 将 {@code java.sql.Types} 中定义的 JDBC 类型列表
	 * 转换为本包使用的 SqlParameter 对象 List。
	 */
	public static List<SqlParameter> sqlTypesToAnonymousParameterList(int @Nullable ... types) {
		if (types == null) {
			return new ArrayList<>();
		}
		List<SqlParameter> result = new ArrayList<>(types.length);
		for (int type : types) {
			result.add(new SqlParameter(type));
		}
		return result;
	}

}
