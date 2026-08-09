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
 * JdbcUtils 类使用的回调接口。本接口的实现执行提取数据库元数据的实际工作，
 * 无需担心异常处理——SQLException 将由 JdbcUtils 正确捕获和处理。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @param <T> 结果类型
 * @see JdbcUtils#extractDatabaseMetaData(javax.sql.DataSource, DatabaseMetaDataCallback)
 */
@FunctionalInterface
public interface DatabaseMetaDataCallback<T extends @Nullable Object> {

	/**
	 * 实现必须实现本方法处理传入的元数据，具体处理逻辑由实现决定。
	 * @param dbmd 要处理的 DatabaseMetaData
	 * @return 从元数据提取的结果对象（可为实现所需的任意对象）
	 * @throws SQLException 获取列值时遇到 SQLException（无需自行捕获）
	 * @throws MetaDataAccessException 提取元数据时其他失败（如反射失败）
	 */
	T processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException;

}
