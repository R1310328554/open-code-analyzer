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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * 将给定参数数组及 JDBC 参数类型应用到
 * {@link PreparedStatementSetter} 的简单适配器。
 *
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final @Nullable Object @Nullable [] args;

	private final int @Nullable [] argTypes;


	/**
	 * 为给定参数及类型创建 {@code ArgumentTypePreparedStatementSetter}。
	 * @param args 要设置的参数
	 * @param argTypes 参数对应的 SQL 类型
	 */
	public ArgumentTypePreparedStatementSetter(@Nullable Object @Nullable [] args, int @Nullable [] argTypes) {
		if ((args == null && argTypes != null) || (args != null && (argTypes == null || args.length != argTypes.length))) {
			throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
		}
		this.args = args;
		this.argTypes = argTypes;
	}


	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		int parameterPosition = 1;
		if (this.args != null && this.argTypes != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				if (arg instanceof Collection<?> entries && this.argTypes[i] != Types.ARRAY) {
					for (Object entry : entries) {
						if (entry instanceof Object[] valueArray) {
							for (Object argValue : valueArray) {
								doSetValue(ps, parameterPosition, this.argTypes[i], argValue);
								parameterPosition++;
							}
						}
						else {
							doSetValue(ps, parameterPosition, this.argTypes[i], entry);
							parameterPosition++;
						}
					}
				}
				else {
					doSetValue(ps, parameterPosition, this.argTypes[i], arg);
					parameterPosition++;
				}
			}
		}
	}

	/**
	 * 使用给定值和类型为 PreparedStatement 指定参数位置设值。
	 * <p>子类可按需覆盖。
	 * @param ps PreparedStatement
	 * @param parameterPosition 参数位置索引
	 * @param argType 参数类型
	 * @param argValue 参数值
	 * @throws SQLException 若 PreparedStatement 方法抛出
	 */
	protected void doSetValue(PreparedStatement ps, int parameterPosition, int argType, @Nullable Object argValue)
			throws SQLException {

		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, argValue);
	}

	@Override
	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
