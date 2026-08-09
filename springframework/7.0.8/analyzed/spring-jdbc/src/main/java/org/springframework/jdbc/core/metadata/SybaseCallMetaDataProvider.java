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
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * Sybase 针对 {@link CallMetaDataProvider} 接口的特定实现。此类供 Simple JDBC 类内部使用。
 * @author Thomas Risberg
 * @author Giuseppe Milicia
 * @since 2.5
 */
public class SybaseCallMetaDataProvider extends GenericCallMetaDataProvider {

	private static final String REMOVABLE_COLUMN_PREFIX = "@";

	private static final String RETURN_VALUE_NAME = "RETURN_VALUE";


	/**
	 * 创建 `SybaseCallMetaDataProvider` 的新实例。
	 */
	public SybaseCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	/**
	 * 方法 `parameterNameToUse`：完成本类中与「parameter Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String parameterNameToUse(@Nullable String parameterName) {
		if (parameterName == null) {
			return null;
		}
		else if (parameterName.length() > 1 && parameterName.startsWith(REMOVABLE_COLUMN_PREFIX)) {
			return super.parameterNameToUse(parameterName.substring(1));
		}
		else {
			return super.parameterNameToUse(parameterName);
		}
	}

	/**
	 * 方法 `namedParameterBindingToUse`：完成本类中与「named Parameter Binding To Use」相关的职责。
	 */
	@Override
	public String namedParameterBindingToUse(@Nullable String parameterName) {
		return parameterName + " = ?";
	}

	/**
	 * 方法 `byPassReturnParameter`：完成本类中与「by Pass Return Parameter」相关的职责。
	 */
	@Override
	public boolean byPassReturnParameter(String parameterName) {
		return (RETURN_VALUE_NAME.equals(parameterName) ||
				RETURN_VALUE_NAME.equals(parameterNameToUse(parameterName)));
	}

}
