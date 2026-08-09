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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;

/**
 * {@link InterruptibleBatchPreparedStatementSetter} 接口的抽象实现，
 * 将可用值检查和值设置合并为单一回调方法 {@link #setValuesIfAvailable}。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setValuesIfAvailable
 */
public abstract class AbstractInterruptibleBatchPreparedStatementSetter
		implements InterruptibleBatchPreparedStatementSetter {

	private boolean exhausted;


	/**
	 * 本实现调用 {@link #setValuesIfAvailable}
	 * 并据此设置本实例的耗尽标志。
	 */
	@Override
	public final void setValues(PreparedStatement ps, int i) throws SQLException {
		this.exhausted = !setValuesIfAvailable(ps, i);
	}

	/**
	 * 本实现返回本实例当前的耗尽标志。
	 */
	@Override
	public final boolean isBatchExhausted(int i) {
		return this.exhausted;
	}

	/**
	 * 本实现返回 {@code Integer.MAX_VALUE}。
	 * 子类可覆盖以降低最大批大小。
	 */
	@Override
	public int getBatchSize() {
		return Integer.MAX_VALUE;
	}


	/**
	 * 检查可用值并将其设置到给定 PreparedStatement 上。
	 * 若无更多可用值，返回 {@code false}。
	 * @param ps 将调用 setter 方法的 PreparedStatement
	 * @param i 批中当前语句的索引，从 0 开始
	 * @return 是否有值可应用（即应用的参数是否应加入批中，
	 * 以及是否应再次调用本方法进行下一轮迭代）
	 * @throws SQLException 遇到 SQLException 时抛出（无需捕获）
	 */
	protected abstract boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException;

}
