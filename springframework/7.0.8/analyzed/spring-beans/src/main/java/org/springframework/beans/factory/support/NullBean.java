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

package org.springframework.beans.factory.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;

/**
 * 空 Bean 实例的内部表示，例如 {@link FactoryBean#getObject()} 或工厂方法返回的 {@code null} 值。
 *
 * <p>每个此类空 Bean 由专用的 {@code NullBean} 实例表示；
 * 各实例彼此不相等，从而在 {@link org.springframework.beans.factory.BeanFactory#getBean}
 * 各变体的返回值中唯一区分每个 Bean。然而，每个实例对 {@code #equals(null)} 返回 {@code true}，
 * 且 {@code #toString()} 返回 "null"，因此可在外部进行检测（本类本身非 public）。
 *
 * @author Juergen Hoeller
 * @since 5.0
 */
final class NullBean {

	NullBean() {
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || other == null);
	}

	@Override
	public int hashCode() {
		return NullBean.class.hashCode();
	}

	@Override
	public String toString() {
		return "null";
	}

}
