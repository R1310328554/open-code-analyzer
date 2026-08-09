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
 * 提供失败前已成功批次额外信息的 {@link BatchUpdateException}。
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public class AggregatedBatchUpdateException extends BatchUpdateException {

	private final int[][] successfulUpdateCounts;

	private final BatchUpdateException originalException;

	/**
	 * 创建聚合异常，包含给定 {@code cause} 之前已完成的批次。
	 * @param successfulUpdateCounts 成功批次的更新计数
	 * @param original 本实例聚合的原始异常
	 */
	public AggregatedBatchUpdateException(int[][] successfulUpdateCounts, BatchUpdateException original) {
		super(original.getMessage(), original.getSQLState(), original.getErrorCode(),
				original.getUpdateCounts(), original.getCause());
		this.successfulUpdateCounts = successfulUpdateCounts;
		this.originalException = original;
		// Copy state of the original exception
		setNextException(original.getNextException());
		for (Throwable suppressed : original.getSuppressed()) {
			addSuppressed(suppressed);
		}
	}

	/**
	 * 返回本异常之前已成功完成的批次。
	 * <p>失败批次信息可通过 {@link #getUpdateCounts()} 获取。
	 * @return 数组：每个批次对应一个子数组，表示该批次各更新受影响行数
	 * @see #getUpdateCounts()
	 */
	public int[][] getSuccessfulUpdateCounts() {
		return this.successfulUpdateCounts;
	}

	/**
	 * 返回本异常聚合的原始 {@link BatchUpdateException}。
	 * @return 原始异常
	 */
	public BatchUpdateException getOriginalException() {
		return this.originalException;
	}

}
