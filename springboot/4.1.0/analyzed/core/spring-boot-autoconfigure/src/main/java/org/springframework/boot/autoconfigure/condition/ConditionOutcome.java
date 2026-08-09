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

package org.springframework.boot.autoconfigure.condition;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 条件匹配的结果，包含日志消息。
 *
 * @author Phillip Webb
 * @since 1.0.0
 * @see ConditionMessage
 */
public class ConditionOutcome {

	private final boolean match;

	private final ConditionMessage message;

	/**
	 * 创建新的 {@link ConditionOutcome} 实例。为获得更一致的消息，
	 * 建议使用 {@link #ConditionOutcome(boolean, ConditionMessage)}。
	 * @param match 条件是否匹配
	 * @param message 条件消息
	 */
	public ConditionOutcome(boolean match, String message) {
		this(match, ConditionMessage.of(message));
	}

	/**
	 * 创建新的 {@link ConditionOutcome} 实例。
	 * @param match 条件是否匹配
	 * @param message 条件消息
	 */
	public ConditionOutcome(boolean match, ConditionMessage message) {
		Assert.notNull(message, "'message' must not be null");
		this.match = match;
		this.message = message;
	}

	/**
	 * 创建表示“匹配”的 {@link ConditionOutcome} 实例。
	 * @return {@link ConditionOutcome}
	 */
	public static ConditionOutcome match() {
		return match(ConditionMessage.empty());
	}

	/**
	 * 创建表示“匹配”的 {@link ConditionOutcome} 实例。为获得更一致的消息，
	 * 建议使用 {@link #match(ConditionMessage)}。
	 * @param message 消息
	 * @return {@link ConditionOutcome}
	 */
	public static ConditionOutcome match(String message) {
		return new ConditionOutcome(true, message);
	}

	/**
	 * 创建表示“匹配”的 {@link ConditionOutcome} 实例。
	 * @param message 消息
	 * @return {@link ConditionOutcome}
	 */
	public static ConditionOutcome match(ConditionMessage message) {
		return new ConditionOutcome(true, message);
	}

	/**
	 * 创建表示“不匹配”的 {@link ConditionOutcome} 实例。为获得更一致的消息，
	 * 建议使用 {@link #noMatch(ConditionMessage)}。
	 * @param message 消息
	 * @return {@link ConditionOutcome}
	 */
	public static ConditionOutcome noMatch(String message) {
		return new ConditionOutcome(false, message);
	}

	/**
	 * 创建表示“不匹配”的 {@link ConditionOutcome} 实例。
	 * @param message 消息
	 * @return {@link ConditionOutcome}
	 */
	public static ConditionOutcome noMatch(ConditionMessage message) {
		return new ConditionOutcome(false, message);
	}

	/**
	 * 若结果为匹配则返回 {@code true}。
	 * @return 若匹配则为 {@code true}
	 */
	public boolean isMatch() {
		return this.match;
	}

	/**
	 * 返回结果消息，或 {@code null}。
	 * @return 消息或 {@code null}
	 */
	public @Nullable String getMessage() {
		return this.message.isEmpty() ? null : this.message.toString();
	}

	/**
	 * 返回结果消息。
	 * @return 消息
	 */
	public ConditionMessage getConditionMessage() {
		return this.message;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() == obj.getClass()) {
			ConditionOutcome other = (ConditionOutcome) obj;
			return (this.match == other.match && ObjectUtils.nullSafeEquals(this.message, other.message));
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(this.match) * 31 + ObjectUtils.nullSafeHashCode(this.message);
	}

	@Override
	public String toString() {
		return (this.message != null) ? this.message.toString() : "";
	}

}
