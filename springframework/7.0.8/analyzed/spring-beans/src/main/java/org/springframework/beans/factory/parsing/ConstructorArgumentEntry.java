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

import org.springframework.util.Assert;

/**
 * 表示（可能带索引的）构造器参数的 {@link ParseState} 条目。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ConstructorArgumentEntry implements ParseState.Entry {

	/** 构造器参数索引，未知时为 -1。 */
	private final int index;


	/**
	 * 创建表示索引尚未确定的构造器参数的 {@link ConstructorArgumentEntry} 实例。
	 */
	public ConstructorArgumentEntry() {
		this.index = -1;
	}

	/**
	 * 创建表示指定 {@code index} 处构造器参数的 {@link ConstructorArgumentEntry} 实例。
	 * @param index 构造器参数的索引
	 * @throws IllegalArgumentException 若 {@code index} 小于零
	 */
	public ConstructorArgumentEntry(int index) {
		Assert.isTrue(index >= 0, "Constructor argument index must be greater than or equal to zero");
		this.index = index;
	}


	@Override
	public String toString() {
		return "Constructor-arg" + (this.index >= 0 ? " #" + this.index : "");
	}

}
