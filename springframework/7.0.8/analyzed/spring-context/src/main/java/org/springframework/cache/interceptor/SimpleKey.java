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

package org.springframework.cache.interceptor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 由 {@link SimpleKeyGenerator} 返回的简单缓存键。
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @since 4.0
 * @see SimpleKeyGenerator
 */
@SuppressWarnings("serial")
public class SimpleKey implements Serializable {

	/**
	 * 空键。
	 */
	public static final SimpleKey EMPTY = new SimpleKey();


	/** 键元素数组。 */
	private final @Nullable Object[] params;

	// 逻辑上为 final，反序列化时重新计算
	private transient int hashCode;


	/**
	 * 创建新的 {@link SimpleKey} 实例。
	 * @param elements 键的元素
	 */
	public SimpleKey(@Nullable Object... elements) {
		Assert.notNull(elements, "Elements must not be null");
		this.params = elements.clone();
		// 预计算 hashCode 字段
		this.hashCode = calculateHash(this.params);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof SimpleKey that && Arrays.deepEquals(this.params, that.params)));
	}

	@Override
	public final int hashCode() {
		// 暴露预计算的 hashCode 字段
		return this.hashCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + Arrays.deepToString(this.params);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// 反序列化时重新计算 hashCode 字段
		this.hashCode = calculateHash(this.params);
	}

	/**
	 * 使用键元素计算哈希值，并以 MurmurHash3 的最终混合函数混合结果。
	 */
	private static int calculateHash(@Nullable Object[] params) {
		int hash = Arrays.deepHashCode(params);
		hash = (hash ^ (hash >>> 16)) * 0x85ebca6b;
		hash = (hash ^ (hash >>> 13)) * 0xc2b2ae35;
		return hash ^ (hash >>> 16);
	}

}
