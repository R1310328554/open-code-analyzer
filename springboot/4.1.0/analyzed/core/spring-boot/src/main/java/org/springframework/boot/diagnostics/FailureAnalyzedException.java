/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.diagnostics;

import org.jspecify.annotations.Nullable;

/**
 * 包含 {@link FailureAnalysis} 的 {@link RuntimeException}。
 *
 * @author Phillip Webb
 * @since 4.1.0
 */
public class FailureAnalyzedException extends RuntimeException {

	private final @Nullable String action;

	/**
	 * 创建新的 {@link FailureAnalyzedException} 实例。
	 *
	 * @param description {@link FailureAnalysis} 描述
	 * @param action {@link FailureAnalysis} 建议操作
	 */
	public FailureAnalyzedException(String description, @Nullable String action) {
		super(description);
		this.action = action;
	}

	/**
	 * 创建新的 {@link FailureAnalyzedException} 实例。
	 *
	 * @param description {@link FailureAnalysis} 描述
	 * @param action {@link FailureAnalysis} 建议操作
	 * @param cause 异常原因
	 */
	public FailureAnalyzedException(String description, @Nullable String action, Throwable cause) {
		super(description, cause);
		this.action = action;
	}

	/**
	 * 返回此异常对应的 {@link FailureAnalysis}。
	 *
	 * @return 失败分析
	 */
	public FailureAnalysis analysis() {
		return new FailureAnalysis(getMessage(), this.action, this);
	}

	static @Nullable FailureAnalysis analyze(Throwable failure) {
		while (failure != null) {
			if (failure instanceof FailureAnalyzedException failureAnalyzedException) {
				return failureAnalyzedException.analysis();
			}
			failure = failure.getCause();
		}
		return null;
	}

}
