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

package org.springframework.beans;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jspecify.annotations.Nullable;

/**
 * 包含一个或多个 {@link PropertyValue} 对象的持有者，
 * 通常表示对某个目标 bean 的一次更新。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValue
 */
public interface PropertyValues extends Iterable<PropertyValue> {

	/**
	 * 返回属性值上的 {@link Iterator}。
	 * @since 5.1
	 */
	@Override
	default Iterator<PropertyValue> iterator() {
		return Arrays.asList(getPropertyValues()).iterator();
	}

	/**
	 * 返回属性值上的 {@link Spliterator}。
	 * @since 5.1
	 */
	@Override
	default Spliterator<PropertyValue> spliterator() {
		return Spliterators.spliterator(getPropertyValues(), 0);
	}

	/**
	 * 返回包含属性值的顺序 {@link Stream}。
	 * @since 5.1
	 */
	default Stream<PropertyValue> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * 返回本对象所持有的 PropertyValue 数组。
	 */
	PropertyValue[] getPropertyValues();

	/**
	 * 返回具有给定名称的属性值（若存在）。
	 * @param propertyName 要查找的名称
	 * @return 属性值；若不存在则为 {@code null}
	 */
	@Nullable PropertyValue getPropertyValue(String propertyName);

	/**
	 * 返回自先前 PropertyValues 以来的变更。
	 * 子类还应覆盖 {@code equals}。
	 * @param old 旧的属性值
	 * @return 已更新或新增的属性。
	 * 若无变更则返回空的 PropertyValues。
	 * @see Object#equals
	 */
	PropertyValues changesSince(PropertyValues old);

	/**
	 * 是否存在该属性的属性值（或其他处理条目）？
	 * @param propertyName 所关注的属性名
	 * @return 该属性是否有属性值
	 */
	boolean contains(String propertyName);

	/**
	 * 本持有者是否完全不包含任何 PropertyValue 对象？
	 */
	boolean isEmpty();

}
