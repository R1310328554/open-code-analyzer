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
 * 用于呼叫处理的特定参数的元数据的持有者。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 * @see GenericCallMetaDataProvider
 */
public class CallParameterMetaData {

	/** `function`：该类的成员状态。 */
	private final boolean function;

	/** 参数相关状态（`parameterName`）。 */
	private final @Nullable String parameterName;

	/** 参数相关状态（`parameterType`）。 */
	private final int parameterType;

	/** 类型相关状态（`sqlType`）。 */
	private final int sqlType;

	/** 类型相关状态（`typeName`）。 */
	private final @Nullable String typeName;

	/** `nullable`：该类的成员状态。 */
	private final boolean nullable;


	/**
	 * 构造函数采用所有属性，包括函数标记。
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
	 * 返回此参数是否在函数中声明。
	 * @since 5.2.9
	 */
	public boolean isFunction() {
		return this.function;
	}

	/**
	 * 返回参数名称。
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
	 * 确定声明的参数是否符合我们的目的的“返回”参数：类型 {@link DatabaseMetaData#procedureColumnReturn} 或 {@link Datab
	 * aseMetaData#procedureColumnResult}，或者如果是函数，则为 {@link DatabaseMetaData#functionReturn}。
	 * @since 4.3.15
	 */
	public boolean isReturnParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionReturn :
				(this.parameterType == DatabaseMetaData.procedureColumnReturn ||
						this.parameterType == DatabaseMetaData.procedureColumnResult));
	}

	/**
	 * 确定声明的参数是否符合我们的目的的“输出”参数：类型 {@link DatabaseMetaData#procedureColumnOut}，或者如果是函数，则为 {@link
	 *  DatabaseMetaData#functionColumnOut}。
	 * @since 5.3.31
	 */
	public boolean isOutParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionColumnOut :
				this.parameterType == DatabaseMetaData.procedureColumnOut);
	}

	/**
	 * 确定声明的参数是否符合我们的目的的“输入输出”参数：类型 {@link DatabaseMetaData#procedureColumnInOut}，或者如果是函数，则为 {@
	 * link DatabaseMetaData#functionColumnInOut}。
	 * @since 5.3.31
	 */
	public boolean isInOutParameter() {
		return (this.function ? this.parameterType == DatabaseMetaData.functionColumnInOut :
				this.parameterType == DatabaseMetaData.procedureColumnInOut);
	}

	/**
	 * 返回参数 SQL 类型。
	 */
	public int getSqlType() {
		return this.sqlType;
	}

	/**
	 * 返回参数类型名称。
	 */
	public @Nullable String getTypeName() {
		return this.typeName;
	}

	/**
	 * 返回参数是否可为空。
	 */
	public boolean isNullable() {
		return this.nullable;
	}

}
