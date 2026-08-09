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

import java.sql.BatchUpdateException;

/**
 * 一份 {@link BatchUpdateException}，提供有关在失败之前成功的批次的附加信息。
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public class AggregatedBatchUpdateException extends BatchUpdateException {

	/** `successfulUpdateCounts`：该类的成员状态。 */
	private final int[][] successfulUpdateCounts;

	/** 异常相关状态（`originalException`）。 */
	private final BatchUpdateException originalException;

	/**
	 * 使用在给定 {@code cause} 之前完成的批次创建聚合异常。
	 * @param successfulUpdateCounts 成功运行的批次的计数
	 * @param original 该实例聚合的异常
	 */
	public AggregatedBatchUpdateException(int[][] successfulUpdateCounts, BatchUpdateException original) {
		super(original.getMessage(), original.getSQLState(), original.getErrorCode(),
				original.getUpdateCounts(), original.getCause());
		this.successfulUpdateCounts = successfulUpdateCounts;
		this.originalException = original;
		// 复制原始异常的状态
		setNextException(original.getNextException());
		for (Throwable suppressed : original.getSuppressed()) {
			addSuppressed(suppressed);
		}
	}

	/**
	 * 返回在此异常之前已成功完成的批次。 <p> 有关失败批次的信息可通过 {@link #getUpdateCounts()} 获得。
	 * @return 对于每个批次，包含另一个数组，该数组包含受批次中每个更新影响的行数
	 * @see #getUpdateCounts()
	 */
	public int[][] getSuccessfulUpdateCounts() {
		return this.successfulUpdateCounts;
	}

	/**
	 * 返回此异常聚合的原始 {@link BatchUpdateException}。
	 * @return 原始异常
	 */
	public BatchUpdateException getOriginalException() {
		return this.originalException;
	}

}
