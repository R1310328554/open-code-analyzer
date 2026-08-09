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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于将复杂类型设置为语句参数的简单接口。
 * <p>I实现执行设置实际值的实际工作。它们必须实现回调方法 {@code setValue}，该方法可以抛出 SQLException，这些异常将由调用代码捕获和转换。如果需要
 * 创建任何特定于数据库的对象，则此回调方法可以通过给定的PreparedStatement对象访问底层Connection。
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see org.springframework.jdbc.core.SqlTypeValue
 * @see org.springframework.jdbc.core.DisposableSqlTypeValue
 */
public interface SqlValue {

	/**
	 * 设置给定的PreparedStatement 的值。
	 * @param ps 要处理的PreparedStatement
	 * @param paramIndex 我们需要为其设置值的参数的索引
	 * @throws SQLException 如果在设置参数值时遇到 SQLException
	 */
	void setValue(PreparedStatement ps, int paramIndex) throws SQLException;

	/**
	 * 清理该值对象持有的资源。
	 */
	void cleanup();

}
