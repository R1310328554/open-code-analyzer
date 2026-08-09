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
 * {@link PreparedStatementSetter} 的简单适配器，应用给定的参数数组和 JDBC 参数类型。
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	/** `args`：该类的成员状态。 */
	private final @Nullable Object @Nullable [] args;

	/** 类型相关状态（`argTypes`）。 */
	private final int @Nullable [] argTypes;


	/**
	 * 为给定的参数和类型创建一个新的 {@code ArgumentTypePreparedStatementSetter}。
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


	/**
	 * 设置 Values（`Values`）。
	 */
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
	 * 使用提供的值和类型设置准备语句的指定参数位置的值。 <p> 如果需要，该方法可以被子类覆盖。
	 * @param ps 准备好的声明
	 * @param parameterPosition 参数位置索引
	 * @param argType 参数类型
	 * @param argValue 参数值
	 * @throws SQLException 如果由PreparedStatement方法抛出
	 */
	protected void doSetValue(PreparedStatement ps, int parameterPosition, int argType, @Nullable Object argValue)
			throws SQLException {

		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, argValue);
	}

	/**
	 * 方法 `cleanupParameters`：完成本类中与「cleanup Parameters」相关的职责。
	 */
	@Override
	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
