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

import org.jspecify.annotations.Nullable;

/**
 * 将给定参数数组应用到 {@link PreparedStatementSetter} 的简单适配器。
 *
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final @Nullable Object @Nullable [] args;


	/**
	 * 为给定参数创建 {@code ArgumentPreparedStatementSetter}。
	 * @param args 要设置的参数
	 */
	public ArgumentPreparedStatementSetter(@Nullable Object @Nullable [] args) {
		this.args = args;
	}


	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				doSetValue(ps, i + 1, arg);
			}
		}
	}

	/**
	 * 使用给定值为 PreparedStatement 指定参数位置设值。
	 * <p>子类可按需覆盖。
	 * @param ps PreparedStatement
	 * @param parameterPosition 参数位置索引
	 * @param argValue 要设置的值
	 * @throws SQLException 若 PreparedStatement 方法抛出
	 */
	protected void doSetValue(PreparedStatement ps, int parameterPosition, @Nullable Object argValue)
			throws SQLException {

		if (argValue instanceof SqlParameterValue paramValue) {
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue, paramValue.getValue());
		}
		else {
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN, argValue);
		}
	}

	@Override
	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
