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

package org.springframework.boot.web.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;

/**
 * 对 {@link MessageSourceResolvable} 错误进行包装、且可安全进行 JSON 序列化的类。
 *
 * @author Yongjun Hong
 * @author Phillip Webb
 * @since 3.5.0
 */
public final class Error implements MessageSourceResolvable {

	private final MessageSourceResolvable cause;

	/**
	 * 使用指定原因创建新的 {@code Error} 实例。
	 *
	 * @param cause the error cause (must not be {@code null}) 错误原因（不得为 {@code null}）
	 */
	private Error(MessageSourceResolvable cause) {
		Assert.notNull(cause, "'cause' must not be null");
		this.cause = cause;
	}

	@Override
	public String @Nullable [] getCodes() {
		return this.cause.getCodes();
	}

	@Override
	public Object @Nullable [] getArguments() {
		return this.cause.getArguments();
	}

	@Override
	public @Nullable String getDefaultMessage() {
		return this.cause.getDefaultMessage();
	}

	/**
	 * 返回错误的原始原因。
	 *
	 * @return the error cause 错误原因
	 */
	@JsonIgnore
	public MessageSourceResolvable getCause() {
		return this.cause;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return Objects.equals(this.cause, ((Error) obj).cause);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.cause);
	}

	@Override
	public String toString() {
		return this.cause.toString();
	}

	/**
	 * 如有必要，包装给定错误使其适合 JSON 序列化。
	 * 已知适合序列化的 {@link MessageSourceResolvable} 实现不会被包装。
	 *
	 * @param errors the errors to wrap 待包装的错误
	 * @return a new Error list 新的 Error 列表
	 * @since 3.5.4
	 */
	public static List<MessageSourceResolvable> wrapIfNecessary(List<? extends MessageSourceResolvable> errors) {
		if (CollectionUtils.isEmpty(errors)) {
			return Collections.emptyList();
		}
		List<MessageSourceResolvable> result = new ArrayList<>(errors.size());
		for (MessageSourceResolvable error : errors) {
			result.add(requiresWrapping(error) ? new Error(error) : error);
		}
		return List.copyOf(result);
	}

	private static boolean requiresWrapping(MessageSourceResolvable error) {
		return !(error instanceof ObjectError);
	}

}
