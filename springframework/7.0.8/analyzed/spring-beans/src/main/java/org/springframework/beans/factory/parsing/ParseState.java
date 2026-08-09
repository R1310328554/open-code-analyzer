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

import java.util.ArrayDeque;

import org.jspecify.annotations.Nullable;

/**
 * 基于 {@link ArrayDeque} 的简单结构，用于在解析过程中追踪逻辑位置。
 * 在解析阶段的各节点，以读取器特定的方式向 ArrayDeque 添加 {@link Entry 条目}。
 *
 * <p>调用 {@link #toString()} 将以树形样式呈现解析阶段当前的逻辑位置。
 * 此表示旨在用于错误消息。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public final class ParseState {

	/**
	 * 内部 {@link ArrayDeque} 存储。
	 */
	private final ArrayDeque<Entry> state;


	/**
	 * 创建具有空 {@link ArrayDeque} 的新 {@code ParseState}。
	 */
	public ParseState() {
		this.state = new ArrayDeque<>();
	}

	/**
	 * 创建新 {@code ParseState}，其 {@link ArrayDeque} 为传入
	 * {@code ParseState} 状态的克隆。
	 */
	private ParseState(ParseState other) {
		this.state = other.state.clone();
	}


	/**
	 * 向 {@link ArrayDeque} 添加新的 {@link Entry}。
	 */
	public void push(Entry entry) {
		this.state.push(entry);
	}

	/**
	 * 从 {@link ArrayDeque} 移除 {@link Entry}。
	 */
	public void pop() {
		this.state.pop();
	}

	/**
	 * 返回当前位于 {@link ArrayDeque} 顶部的 {@link Entry}，
	 * 若 {@link ArrayDeque} 为空则返回 {@code null}。
	 */
	public @Nullable Entry peek() {
		return this.state.peek();
	}

	/**
	 * 创建本实例的独立快照新 {@link ParseState} 实例。
	 */
	public ParseState snapshot() {
		return new ParseState(this);
	}


	/**
	 * 返回当前 {@code ParseState} 的树形表示。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		int i = 0;
		for (ParseState.Entry entry : this.state) {
			if (i > 0) {
				sb.append('\n');
				sb.append("\t".repeat(i));
				sb.append("-> ");
			}
			sb.append(entry);
			i++;
		}
		return sb.toString();
	}


	/**
	 * {@link ParseState} 条目的标记接口。
	 */
	public interface Entry {
	}

}
