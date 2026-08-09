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
 * 失败分析的结果。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public class FailureAnalysis {

	private final String description;

	private final @Nullable String action;

	private final Throwable cause;

	/**
	 * 使用给定 {@code description} 与 {@code action}（若有）创建新的 {@code FailureAnalysis}，
	 * 供用户采取以解决问题。失败具有给定的底层 {@code cause}。
	 *
	 * @param description 描述
	 * @param action 建议操作
	 * @param cause 原因
	 */
	public FailureAnalysis(@Nullable String description, @Nullable String action, Throwable cause) {
		this.description = (description != null) ? description : "";
		this.action = action;
		this.cause = cause;
	}

	/**
	 * 返回失败描述。
	 *
	 * @return 描述
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * 返回为解决失败建议采取的操作（若有）。
	 *
	 * @return 操作或 {@code null}
	 */
	public @Nullable String getAction() {
		return this.action;
	}

	/**
	 * 返回失败原因。
	 *
	 * @return 原因
	 */
	public Throwable getCause() {
		return this.cause;
	}

}
