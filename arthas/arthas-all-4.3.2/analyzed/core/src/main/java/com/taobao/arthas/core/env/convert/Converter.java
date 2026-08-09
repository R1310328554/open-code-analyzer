/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.env.convert;

/**
 * 类型转换器接口：将源类型 {@code S} 的对象转换为目标类型 {@code T}。
 * <p>
 * 实现类须线程安全，可在 {@link DefaultConversionService} 中共享注册。
 * 可选实现 {@link ConditionalConverter} 以声明条件转换能力。
 *
 * @author Keith Donald
 * @since 3.0
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface Converter<S, T> {

	/**
	 * 将类型 {@code S} 的源对象转换为目标类型 {@code T}。
	 * @param source 待转换的源对象，必须是 {@code S} 的实例（永不为 {@code null}）
	 * @param targetType 目标类型
	 * @return 转换后的对象，必须是 {@code T} 的实例（可能为 {@code null}）
	 * @throws IllegalArgumentException 源对象无法转换为目标类型时抛出
	 */
	T convert(S source, Class<T> targetType);

}
