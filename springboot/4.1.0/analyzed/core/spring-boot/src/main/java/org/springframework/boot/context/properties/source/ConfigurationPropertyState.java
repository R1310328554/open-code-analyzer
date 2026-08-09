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

package org.springframework.boot.context.properties.source;

import java.util.function.Predicate;

import org.springframework.util.Assert;

/**
 * {@link ConfigurationPropertySource} 中内容的状态。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public enum ConfigurationPropertyState {

	/**
	 * {@link ConfigurationPropertySource} 至少有一个匹配的 {@link ConfigurationProperty}。
	 */
	PRESENT,

	/**
	 * {@link ConfigurationPropertySource} 没有匹配的 {@link ConfigurationProperty 配置属性}。
	 */
	ABSENT,

	/**
	 * 无法确定 {@link ConfigurationPropertySource} 是否存在匹配的
	 * {@link ConfigurationProperty 配置属性}。
	 */
	UNKNOWN;

	/**
	 * 使用谓词搜索给定可迭代对象，判断内容是 {@link #PRESENT} 还是 {@link #ABSENT}。
	 *
	 * @param <T> 数据类型
	 * @param source 要搜索的可迭代源
	 * @param predicate 用于检测是否存在的谓词
	 * @return {@link #PRESENT} if the iterable contains a matching item, otherwise {@link #ABSENT} 若存在匹配项则为 {@link #PRESENT}，否则为 {@link #ABSENT}
	 */
	static <T> ConfigurationPropertyState search(Iterable<T> source, Predicate<T> predicate) {
		Assert.notNull(source, "'source' must not be null");
		Assert.notNull(predicate, "'predicate' must not be null");
		for (T item : source) {
			if (predicate.test(item)) {
				return PRESENT;
			}
		}
		return ABSENT;
	}

	/**
	 * 使用谓词搜索给定数组区间，判断内容是 {@link #PRESENT} 还是 {@link #ABSENT}。
	 *
	 * @param <T> 数据类型
	 * @param source 要搜索的数组源
	 * @param startInclusive 起始索引（含）
	 * @param endExclusive 结束索引（不含）
	 * @param predicate 用于检测是否存在的谓词
	 * @return {@link #PRESENT} if the iterable contains a matching item, otherwise {@link #ABSENT} 若存在匹配项则为 {@link #PRESENT}，否则为 {@link #ABSENT}
	 */
	static <T> ConfigurationPropertyState search(T[] source, int startInclusive, int endExclusive,
			Predicate<T> predicate) {
		Assert.notNull(source, "'source' must not be null");
		Assert.notNull(predicate, "'predicate' must not be null");
		for (int i = startInclusive; i < endExclusive; i++) {
			if (predicate.test(source[i])) {
				return PRESENT;
			}
		}
		return ABSENT;
	}

}
