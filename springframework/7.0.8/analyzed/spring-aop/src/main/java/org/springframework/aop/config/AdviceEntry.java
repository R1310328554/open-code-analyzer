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

package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

/**
 * 代表建议元素的 {@link ParseState} 条目。
 * @author Mark Fisher
 * @since 2.0
 */
public class AdviceEntry implements ParseState.Entry {

	/** `kind`：该类的成员状态。 */
	private final String kind;


	/**
	 * 创建一个新的 {@code AdviceEntry} 实例。
	 * @param kind 该条目所代表的建议类型（之前、之后、周围）
	 */
	public AdviceEntry(String kind) {
		this.kind = kind;
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "Advice (" + this.kind + ")";
	}

}
