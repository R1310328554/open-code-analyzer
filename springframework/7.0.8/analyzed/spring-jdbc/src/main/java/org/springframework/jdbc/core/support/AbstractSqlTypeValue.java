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
 * SqlTypeValue 接口的抽象实现，用于方便创建应传递到 {@code PreparedStatement.setObject} 方法中的类型值。 {@code
 * createTypeValue} 回调方法可以访问底层 Connection（如果需要创建任何特定于数据库的对象）。
 * StoredProcedure 中的 <p>A 使用示例（将其与超类 javadoc 中的纯 SqlTypeValue 版本进行比较）：
 * <pre class="code">proc.declareParameter(new SqlParameter("myarray", Types.ARRAY,
 * "NUMBERS")); ...
 * 映射<字符串，对象> in = new HashMap<String, Object>(); in.put("myarray", new
 * AbstractSqlTypeValue() { public Object createTypeValue(Connection con, int sqlType,
 * String typeName) throws SQLException { oracle.sql.ArrayDescriptor desc = new
 * oracle.sql.ArrayDescriptor(typeName, con); return new oracle.sql.ARRAY(desc, con,席位); }
 * });映射出=执行（输入）； OCAJAVA0文档
 * @author Juergen Hoeller
 * @since 1.1
 * @see java.sql.PreparedStatement#setObject(int, Object, int)
 * @see org.springframework.jdbc.object.StoredProcedure
 */
public abstract class AbstractSqlTypeValue implements SqlTypeValue {

	/**
	 * 设置 Type Value（`TypeValue`）。
	 */
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
	 * 创建要传递到 {@code PreparedStatement.setObject} 的类型值。
	 * @param con JDBC 连接（如果需要创建任何特定于数据库的对象）
	 * @param sqlType 我们正在设置的参数的 SQL 类型
	 * @param typeName 参数的类型名称
	 * @return 类型值
	 * @throws SQLException 如果设置参数值时遇到 SQLException（即无需捕获 SQLException）
	 * @see java.sql.PreparedStatement#setObject(int, Object, int)
	 */
	protected abstract Object createTypeValue(Connection con, int sqlType, @Nullable String typeName)
			throws SQLException;

}
