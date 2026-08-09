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

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * 大多数 {@code FailureAnalyzer} 实现的抽象基类。
 *
 * @param <T> 待分析异常的类型
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 1.4.0
 */
public abstract class AbstractFailureAnalyzer<T extends Throwable> implements FailureAnalyzer {

	@Override
	public @Nullable FailureAnalysis analyze(Throwable failure) {
		T cause = findCause(failure, getCauseType());
		return (cause != null) ? analyze(failure, cause) : null;
	}

	/**
	 * 返回对给定 {@code rootFailure} 的分析，若无法分析则返回 {@code null}。
	 *
	 * @param rootFailure 传入分析器的根失败
	 * @param cause 实际找到的 cause
	 * @return 分析结果或 {@code null}
	 */
	protected abstract @Nullable FailureAnalysis analyze(Throwable rootFailure, T cause);

	/**
	 * 返回分析器处理的 cause 类型。默认使用类的泛型参数。
	 *
	 * @return cause 类型
	 */
	@SuppressWarnings("unchecked")
	protected Class<? extends T> getCauseType() {
		Class<? extends T> type = (Class<? extends T>) ResolvableType
			.forClass(AbstractFailureAnalyzer.class, getClass())
			.resolveGeneric();
		Assert.state(type != null, "Unable to resolve generic");
		return type;
	}

	@SuppressWarnings("unchecked")
	protected final <E extends Throwable> @Nullable E findCause(@Nullable Throwable failure, Class<E> type) {
		while (failure != null) {
			if (type.isInstance(failure)) {
				return (E) failure;
			}
			failure = failure.getCause();
		}
		return null;
	}

}
