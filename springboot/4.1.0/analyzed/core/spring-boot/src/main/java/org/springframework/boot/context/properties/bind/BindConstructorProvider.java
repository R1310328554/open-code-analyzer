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

import java.lang.reflect.Constructor;

import org.jspecify.annotations.Nullable;

/**
 * 用于确定绑定时使用哪个构造器的策略接口。
 *
 * @author Madhura Bhave
 * @since 2.2.1
 */
@FunctionalInterface
public interface BindConstructorProvider {

	/**
	 * 默认 {@link BindConstructorProvider} 实现：仅当存在唯一构造器且
	 * Bindable 尚无现有值时才返回构造器。
	 */
	BindConstructorProvider DEFAULT = new DefaultBindConstructorProvider();

	/**
	 * 返回给定类型应使用的绑定构造器；不支持构造器绑定时返回 {@code null}。
	 *
	 * @param type 要检查的类型
	 * @param isNestedConstructorBinding 是否为构造器绑定内的嵌套绑定
	 * @return 绑定构造器，或 {@code null}
	 * @since 3.0.0
	 */
	default @Nullable Constructor<?> getBindConstructor(Class<?> type, boolean isNestedConstructorBinding) {
		return getBindConstructor(Bindable.of(type), isNestedConstructorBinding);
	}

	/**
	 * 返回给定 Bindable 应使用的绑定构造器；不支持构造器绑定时返回 {@code null}。
	 *
	 * @param bindable 要检查的 Bindable
	 * @param isNestedConstructorBinding 是否为构造器绑定内的嵌套绑定
	 * @return 绑定构造器，或 {@code null}
	 */
	@Nullable Constructor<?> getBindConstructor(Bindable<?> bindable, boolean isNestedConstructorBinding);

}
