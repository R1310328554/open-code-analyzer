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

package org.springframework.aop.support;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.util.Assert;

/**
 * 传递类（以及可选的子类）的简单 ClassFilter 实现。
 * @author Rod Johnson
 * @author Sam Brannen
 */
@SuppressWarnings("serial")
public class RootClassFilter implements ClassFilter, Serializable {

	/** `clazz`：该类的成员状态。 */
	private final Class<?> clazz;


	/**
	 * 创建 `RootClassFilter` 的新实例。
	 */
	public RootClassFilter(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		this.clazz = clazz;
	}


	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Class<?> candidate) {
		return this.clazz.isAssignableFrom(candidate);
	}

	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RootClassFilter that &&
				this.clazz.equals(that.clazz)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return this.clazz.hashCode();
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.clazz.getName();
	}

}
