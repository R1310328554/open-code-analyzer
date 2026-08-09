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

package org.springframework.beans.factory.parsing;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 表示 Bean 定义配置问题的对象。
 * 主要作为传入 {@link ProblemReporter} 的通用参数。
 *
 * <p>可表示潜在致命问题（错误）或仅为警告。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see ProblemReporter
 */
public class Problem {

	/** 问题描述消息。 */
	private final String message;

	/** 触发错误的位置。 */
	private final Location location;

	/** 错误发生时的解析状态。 */
	private final @Nullable ParseState parseState;

	/** 根本原因异常。 */
	private final @Nullable Throwable rootCause;


	/**
	 * 创建新的 {@link Problem} 实例。
	 * @param message 详述问题的消息
	 * @param location 触发错误的 Bean 配置来源中的位置
	 */
	public Problem(String message, Location location) {
		this(message, location, null, null);
	}

	/**
	 * 创建新的 {@link Problem} 实例。
	 * @param message 详述问题的消息
	 * @param parseState 错误发生时的 {@link ParseState}
	 * @param location 触发错误的 Bean 配置来源中的位置
	 */
	public Problem(String message, Location location, ParseState parseState) {
		this(message, location, parseState, null);
	}

	/**
	 * 创建新的 {@link Problem} 实例。
	 * @param message 详述问题的消息
	 * @param rootCause 导致错误的底层异常（可为 {@code null}）
	 * @param parseState 错误发生时的 {@link ParseState}
	 * @param location 触发错误的 Bean 配置来源中的位置
	 */
	public Problem(String message, Location location, @Nullable ParseState parseState, @Nullable Throwable rootCause) {
		Assert.notNull(message, "Message must not be null");
		Assert.notNull(location, "Location must not be null");
		this.message = message;
		this.location = location;
		this.parseState = parseState;
		this.rootCause = rootCause;
	}


	/**
	 * 获取详述问题的消息。
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * 获取触发错误的 Bean 配置来源中的位置。
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * 获取触发错误的 Bean 配置来源描述，取自本 Problem 的 Location 对象。
	 * @see #getLocation()
	 */
	public String getResourceDescription() {
		return getLocation().getResource().getDescription();
	}

	/**
	 * 获取错误发生时的 {@link ParseState}（可为 {@code null}）。
	 */
	public @Nullable ParseState getParseState() {
		return this.parseState;
	}

	/**
	 * 获取导致错误的底层异常（可为 {@code null}）。
	 */
	public @Nullable Throwable getRootCause() {
		return this.rootCause;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Configuration problem: ");
		sb.append(getMessage());
		sb.append("\nOffending resource: ").append(getResourceDescription());
		if (getParseState() != null) {
			sb.append('\n').append(getParseState());
		}
		return sb.toString();
	}

}
