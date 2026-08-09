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
import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * {@link CallMetaDataProvider} 接口的 Derby 特定实现。此类供 Simple JDBC 类内部使用。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class DerbyCallMetaDataProvider extends GenericCallMetaDataProvider {

	/**
	 * 创建 `DerbyCallMetaDataProvider` 的新实例。
	 */
	public DerbyCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	/**
	 * 方法 `metaDataSchemaNameToUse`：完成本类中与「meta Data Schema Name To Use」相关的职责。
	 */
	@Override
	public @Nullable String metaDataSchemaNameToUse(@Nullable String schemaName) {
		if (schemaName != null) {
			return super.metaDataSchemaNameToUse(schemaName);
		}

		// 如果未指定架构，则使用当前用户架构...
		String userName = getUserName();
		return (userName != null ? userName.toUpperCase(Locale.ROOT) : null);
	}

}
