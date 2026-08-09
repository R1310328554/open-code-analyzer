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

import org.jspecify.annotations.Nullable;

/**
 * 用于调用处理的特定参数元数据持有者。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 * @see GenericCallMetaDataProvider
 */
public class CallParameterMetaData {

	private final boolean function;

	private final @Nullable String parameterName;

	private final int parameterType;

	private final int sqlType;

	private final @Nullable String typeName;

	private final boolean nullable;


	/**
	 * 接收全部属性（含函数标记）的构造函数。
	 * @since 5.2.9
	 */
	public CallParameterMetaData(boolean function, @Nullable String columnName, int columnType,
			int sqlType, @Nullable String typeName, boolean nullable) {

		this.function = function;
		this.parameterName = columnName;
		this.parameterType = columnType;
		this.sqlType = sqlType;
		this.typeName = typeName;
		this.nullable = nullable;
	}


	/**
	 * 返回本参数是否声明于函数中。
	 * @since 5.2.9
	 */
	public boolean isFunction() {
		return this.function;
	}

	/**
	 * 返回参数名。
	 */
	public @Nullable String getParameterName() {
		return this.parameterName;
	}

	/**
	 * 返回参数类型。
	 */
	public int getParameterType() {
		return this.parameterType;
	}

	/**
	 * 判断声明的参数是否视为「返回」参数：
	 * 类型为 {@link DatabaseMetaData#procedureColumnReturn} 或
	 * {@link DatabaseMetaData#procedureColumnResult}；函数则为
	 * {@link DatabaseMetaData#functionReturn}。
	 * @since 4.3.15
	 */
	public boolean isReturnParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionReturn :
				(this.parameterType == DatabaseMetaData.procedureColumnReturn ||
						this.parameterType == DatabaseMetaData.procedureColumnResult));
	}

	/**
	 * 判断声明的参数是否视为 OUT 参数：
	 * 类型为 {@link DatabaseMetaData#procedureColumnOut}；
	 * 函数则为 {@link DatabaseMetaData#functionColumnOut}。
	 * @since 5.3.31
	 */
	public boolean isOutParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionColumnOut :
				this.parameterType == DatabaseMetaData.procedureColumnOut);
	}

	/**
	 * 判断声明的参数是否视为 IN/OUT 参数：
	 * 类型为 {@link DatabaseMetaData#procedureColumnInOut}；
	 * 函数则为 {@link DatabaseMetaData#functionColumnInOut}。
	 * @since 5.3.31
	 */
	public boolean isInOutParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionColumnInOut :
				this.parameterType == DatabaseMetaData.procedureColumnInOut);
	}

	/**
	 * 返回参数的 SQL 类型。
	 */
	public int getSqlType() {
		return this.sqlType;
	}

	/**
	 * 返回参数类型名。
	 */
	public @Nullable String getTypeName() {
		return this.typeName;
	}

	/**
	 * 返回参数是否可为 null。
	 */
	public boolean isNullable() {
		return this.nullable;
	}

}
