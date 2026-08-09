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

package org.springframework.boot.context.properties.bind;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.Binder.Context;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * {@link Binder} 用于绑定数据对象的内部策略。数据对象本身由递归绑定的属性组成。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @see JavaBeanBinder
 * @see ValueObjectBinder
 */
interface DataObjectBinder {

	/**
	 * 返回绑定后的实例；若 {@link DataObjectBinder} 不支持指定 {@link Bindable} 则返回 {@code null}。
	 *
	 * @param <T> 源类型
	 * @param name 正在绑定的名称
	 * @param target 待绑定的 bindable
	 * @param context 绑定上下文
	 * @param propertyBinder 属性绑定器
	 * @param fallbackToDefaultValue 未绑定任何值时是否尝试返回新的默认值
	 * @return 绑定后的实例或 {@code null}
	 */
	<T> @Nullable T bind(ConfigurationPropertyName name, Bindable<T> target, Context context,
			DataObjectPropertyBinder propertyBinder, boolean fallbackToDefaultValue);

	/**
	 * 返回新创建的实例；若 {@link DataObjectBinder} 不支持指定 {@link Bindable} 则返回 {@code null}。
	 *
	 * @param <T> 源类型
	 * @param target 待创建的 bindable
	 * @param context 绑定上下文
	 * @return 创建的实例
	 */
	<T> @Nullable T create(Bindable<T> target, Context context);

	/**
	 * 无法创建实例时，可用于添加额外被抑制异常的回调。
	 *
	 * @param <T> 源类型
	 * @param target 正在创建的 bindable
	 * @param context 绑定上下文
	 * @param exception 即将抛出的异常
	 */
	default <T> void onUnableToCreateInstance(Bindable<T> target, Context context, RuntimeException exception) {
	}

}
