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

/**
 * {@link BatchPreparedStatementSetter} 接口的扩展，增加了批次耗尽检查。
 *
 * <p>本接口允许标记批次结束，而无需事先确定确切的批次大小。
 * 批次大小仍然生效，但现仅作为批次的最大上限。
 *
 * <p>每次调用 {@link #setValues} 后都会调用 {@link #isBatchExhausted}，
 * 以判断本次是否添加了新值，或批次是否已确定完成
 * （即最后一次 {@code setValues} 调用未提供额外值）。
 *
 * <p>建议扩展
 * {@link org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter}
 * 基类而非直接实现本接口：只需实现单个 {@code setValuesIfAvailable} 回调，
 * 检查可用值并设置，同时返回是否实际提供了值。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)
 * @see org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter
 */
public interface InterruptibleBatchPreparedStatementSetter extends BatchPreparedStatementSetter {

	/**
	 * 返回批次是否已完成，即最后一次 {@code setValues} 调用是否未添加额外值。
	 * <p><b>注意：</b>若此方法返回 {@code true}，最后一次 {@code setValues} 调用
	 * 中可能已设置的任何参数都将被忽略！若在 {@code setValues} 实现
	 * <i>开头</i>检测到耗尽，请设置相应的内部标志，
	 * 让此方法基于该标志返回 {@code true}。
	 * @param i 批次中当前语句的索引，从 0 开始
	 * @return 批次是否已耗尽
	 * @see #setValues
	 * @see org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter#setValuesIfAvailable
	 */
	boolean isBatchExhausted(int i);

}
