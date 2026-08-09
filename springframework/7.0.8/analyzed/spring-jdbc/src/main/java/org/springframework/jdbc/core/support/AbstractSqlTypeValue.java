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

package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.SqlTypeValue;

/**
 * {@link SqlTypeValue} 接口的抽象实现，便于创建应传入
 * {@code PreparedStatement.setObject} 方法的类型值。
 * {@code createTypeValue} 回调可访问底层 Connection，以便创建数据库特定对象。
 *
 * <p>StoredProcedure 中的使用示例（可与父类 javadoc 中的普通 SqlTypeValue 写法对比）：
 *
 * <pre class="code">proc.declareParameter(new SqlParameter("myarray", Types.ARRAY, "NUMBERS"));
 * ...
 *
 * Map&lt;String, Object&gt; in = new HashMap&lt;String, Object&gt;();
 * in.put("myarray", new AbstractSqlTypeValue() {
 *   public Object createTypeValue(Connection con, int sqlType, String typeName) throws SQLException {
 *	   oracle.sql.ArrayDescriptor desc = new oracle.sql.ArrayDescriptor(typeName, con);
 *	   return new oracle.sql.ARRAY(desc, con, seats);
 *   }
 * });
 * Map out = execute(in);
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see java.sql.PreparedStatement#setObject(int, Object, int)
 * @see org.springframework.jdbc.object.StoredProcedure
 */
public abstract class AbstractSqlTypeValue implements SqlTypeValue {

	@Override
	public final void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException {

		Object value = createTypeValue(ps.getConnection(), sqlType, typeName);
		if (sqlType == TYPE_UNKNOWN) {
			ps.setObject(paramIndex, value);
		}
		else {
			ps.setObject(paramIndex, value, sqlType);
		}
	}

	/**
	 * 创建要传入 {@code PreparedStatement.setObject} 的类型值。
	 * @param con JDBC Connection，创建数据库特定对象时可能需要
	 * @param sqlType 要设置的参数的 SQL 类型
	 * @param typeName 参数的类型名称
	 * @return 类型值
	 * @throws SQLException 设置参数值时遇到 SQLException（无需捕获）
	 * @see java.sql.PreparedStatement#setObject(int, Object, int)
	 */
	protected abstract Object createTypeValue(Connection con, int sqlType, @Nullable String typeName)
			throws SQLException;

}
