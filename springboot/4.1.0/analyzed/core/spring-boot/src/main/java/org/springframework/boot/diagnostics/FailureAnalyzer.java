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
 * {@code FailureAnalyzer} 用于分析失败并提供可展示给用户的诊断信息。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
@FunctionalInterface
public interface FailureAnalyzer {

	/**
	 * 返回对给定 {@code failure} 的分析，若无法分析则返回 {@code null}。
	 *
	 * @param failure 失败
	 * @return 分析结果或 {@code null}
	 */
	@Nullable FailureAnalysis analyze(Throwable failure);

}
