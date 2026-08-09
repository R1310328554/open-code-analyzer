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

package org.springframework.boot.origin;

import org.jspecify.annotations.Nullable;

/**
 * 可根据给定键查找 {@link Origin} 信息的接口。
 * 可用于为现有类添加来源追踪支持。
 *
 * @param <K> the lookup key type 查找键类型
 * @author Phillip Webb
 * @since 2.0.0
 */
@FunctionalInterface
public interface OriginLookup<K> {

	/**
	 * 返回给定键的来源；若无法确定则返回 {@code null}。
	 *
	 * @param key the key to lookup 待查找的键
	 * @return the origin of the key or {@code null} 键的来源或 {@code null}
	 */
	@Nullable Origin getOrigin(K key);

	/**
	 * 尝试从给定源对象查找来源。若源对象不是 {@link OriginLookup}，
	 * 或查找过程中抛出异常，则返回 {@code null}。
	 *
	 * @param source the source object 源对象
	 * @param key the key to lookup 待查找的键
	 * @param <K> the key type 键类型
	 * @return an {@link Origin} or {@code null} {@link Origin} 或 {@code null}
	 */
	@SuppressWarnings("unchecked")
	static <K> @Nullable Origin getOrigin(@Nullable Object source, K key) {
		if (!(source instanceof OriginLookup)) {
			return null;
		}
		try {
			return ((OriginLookup<K>) source).getOrigin(key);
		}
		catch (Throwable ex) {
			return null;
		}
	}

}
