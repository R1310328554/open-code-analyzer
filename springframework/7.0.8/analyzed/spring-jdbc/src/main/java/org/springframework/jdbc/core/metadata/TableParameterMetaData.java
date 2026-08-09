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

/**
 * 用于表处理的特定参数的元数据的持有者。
 * @author Thomas Risberg
 * @since 2.5
 * @see GenericTableMetaDataProvider
 */
public class TableParameterMetaData {

	/** 参数相关状态（`parameterName`）。 */
	private final String parameterName;

	/** 类型相关状态（`sqlType`）。 */
	private final int sqlType;

	/** `nullable`：该类的成员状态。 */
	private final boolean nullable;


	/**
	 * 构造函数获取所有属性。
	 */
	public TableParameterMetaData(String columnName, int sqlType, boolean nullable) {
		this.parameterName = columnName;
		this.sqlType = sqlType;
		this.nullable = nullable;
	}


	/**
	 * 获取参数名称。
	 */
	public String getParameterName() {
		return this.parameterName;
	}

	/**
	 * 获取参数SQL类型。
	 */
	public int getSqlType() {
		return this.sqlType;
	}

	/**
	 * 获取参数/列是否可为空。
	 */
	public boolean isNullable() {
		return this.nullable;
	}

}
