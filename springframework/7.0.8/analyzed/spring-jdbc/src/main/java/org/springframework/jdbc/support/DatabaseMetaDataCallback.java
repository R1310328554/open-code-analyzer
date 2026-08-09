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

package org.springframework.jdbc.support;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * JdbcUtils 类使用的回调接口。该接口的实现执行提取数据库元数据的实际工作，但不需要担心异常处理。 JdbcUtils 类将捕获并正确处理 SQLException。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @param <T> 结果类型
 * @see JdbcUtils#extractDatabaseMetaData(javax.sql.DataSource, DatabaseMetaDataCallback)
 */
@FunctionalInterface
public interface DatabaseMetaDataCallback<T extends @Nullable Object> {

	/**
	 * 实现必须实现此方法来处理传入的元数据。具体实现选择做什么取决于它。
	 * @param dbmd 要处理的数据库元数据
	 * @return 从元数据中提取的结果对象（可以是任意对象，根据实现的需要）
	 * @throws SQLException 如果获取列值时遇到 SQLException（即无需捕获 SQLException）
	 * @throws MetaDataAccessException 提取元数据时发生其他故障（例如反射故障）
	 */
	T processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException;

}
