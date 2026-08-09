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

package org.springframework.beans.factory.aot;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 待自动装配的已解析参数。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanInstanceSupplier
 * @see AutowiredMethodArgumentsResolver
 */
@FunctionalInterface
public interface AutowiredArguments {

	/**
	 * 返回指定索引处已解析的参数，并校验类型。
	 * @param <T> 参数类型
	 * @param index 参数索引
	 * @param requiredType 要求的参数类型
	 * @return 参数值
	 */
	@SuppressWarnings("unchecked")
	default <T> @Nullable T get(int index, Class<T> requiredType) {
		Object value = getObject(index);
		if (!ClassUtils.isAssignableValue(requiredType, value)) {
			throw new IllegalArgumentException("Argument type mismatch: expected '" +
					ClassUtils.getQualifiedName(requiredType) + "' for value [" + value + "]");
		}
		return (T) value;
	}

	/**
	 * 返回指定索引处已解析的参数。
	 * @param <T> 参数类型
	 * @param index 参数索引
	 * @return 参数值
	 */
	@SuppressWarnings("unchecked")
	default <T> @Nullable T get(int index) {
		return (T) getObject(index);
	}

	/**
	 * 返回指定索引处已解析的参数。
	 * @param index 参数索引
	 * @return 参数值
	 */
	default @Nullable Object getObject(int index) {
		return toArray()[index];
	}

	/**
	 * 将参数作为对象数组返回。
	 * @return 参数对象数组
	 */
	@Nullable Object[] toArray();

	/**
	 * 工厂方法：根据给定对象数组创建新的 {@link AutowiredArguments} 实例。
	 * @param arguments 参数数组
	 * @return 新的 {@link AutowiredArguments} 实例
	 */
	static AutowiredArguments of(@Nullable Object[] arguments) {
		Assert.notNull(arguments, "'arguments' must not be null");
		return () -> arguments;
	}

}
