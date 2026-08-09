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
 * 扩展{@link BatchPreparedStatementSetter}接口，添加批量耗尽检查。
 * <p>此接口允许您发出批处理结束的信号，而不必预先确定确切的批处理大小。批次大小仍然受到尊重，但现在它是批次的最大大小。
 * <p> 每次调用 {@link #setValues} 后都会调用 {@link #isBatchExhausted} 方法，以确定是否添加了一些值，或者是否确定批次已完成并且
 * 在上次调用 {@code setValues} 期间没有提供其他值。
 * <p>考虑扩展 {@link
 * org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter}
 * 基类，而不是直接实现此接口，使用单个 {@code setValuesIfAvailable} 回调方法检查可用值并设置它们，返回值是否已实际提供。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)
 * @see org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter
 */
public interface InterruptibleBatchPreparedStatementSetter extends BatchPreparedStatementSetter {

	/**
	 * 返回批处理是否完成，即上次 {@code setValues} 调用期间是否没有添加其他值。 <p><b>NOTE:</b> 如果此方法返回 {@code true}，则在上次
	 *  {@code setValues} 调用期间可能设置的任何参数都将被忽略！如果您在 {@code setValues} 实现的开头 </i> 检测到耗尽 <i>，请确保设置相
	 * 应的内部标志，让此方法根据该标志返回 {@code true}。
	 * @param i 我们在批次中发出的语句的索引，从 0 开始
	 * @return 该批次已经用完
	 * @see #setValues
	 * @see org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter#setValuesIfAvailable
	 */
	boolean isBatchExhausted(int i);

}
