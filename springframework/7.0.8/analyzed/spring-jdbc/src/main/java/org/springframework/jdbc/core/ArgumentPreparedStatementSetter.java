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
 * {@link PreparedStatementSetter} 的简单适配器，应用给定的参数数组。
 * @author Juergen Hoeller
 * @since 3.2.3
 */
public class ArgumentPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	/** `args`：该类的成员状态。 */
	private final @Nullable Object @Nullable [] args;


	/**
	 * 为给定参数创建一个新的 {@code ArgumentPreparedStatementSetter}。
	 * @param args 要设置的参数
	 */
	public ArgumentPreparedStatementSetter(@Nullable Object @Nullable [] args) {
		this.args = args;
	}


	/**
	 * 设置 Values（`Values`）。
	 */
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
	 * 使用提供的值设置准备语句的指定参数位置的值。 <p> 如果需要，该方法可以被子类覆盖。
	 * @param ps 准备好的声明
	 * @param parameterPosition 参数位置索引
	 * @param argValue 要设置的值
	 * @throws SQLException 如果由PreparedStatement方法抛出
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

	/**
	 * 方法 `cleanupParameters`：完成本类中与「cleanup Parameters」相关的职责。
	 */
	@Override
	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
