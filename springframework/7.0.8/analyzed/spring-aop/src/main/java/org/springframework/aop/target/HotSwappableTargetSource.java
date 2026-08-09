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

package org.springframework.aop.target;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.aop.TargetSource} 实现缓存本地目标对象，但允许在应用程序运行时交换目标。
 * <p>如果在 Spring IoC 容器中配置此类的对象，请使用构造函数注入。
 * <p> 如果目标在序列化时，则此 TargetSource 是可序列化的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class HotSwappableTargetSource implements TargetSource, Serializable {

	/**
	 */
	private static final long serialVersionUID = 7497929212653839187L;


	/**
	 */
	@SuppressWarnings("serial")
	private Object target;


	/**
	 * 使用给定的初始目标对象创建一个新的 HotSwappableTargetSource。
	 * @param initialTarget 初始目标对象
	 */
	public HotSwappableTargetSource(Object initialTarget) {
		Assert.notNull(initialTarget, "Target object must not be null");
		this.target = initialTarget;
	}


	/**
	 * 返回当前目标对象的类型。 <p>返回的类型通常在所有目标对象中应该是不变的。
	 */
	@Override
	public synchronized Class<?> getTargetClass() {
		return this.target.getClass();
	}

	/**
	 * 获取 Target（`Target`）。
	 */
	@Override
	public synchronized Object getTarget() {
		return this.target;
	}


	/**
	 * 交换目标，返回旧的目标对象。
	 * @param newTarget 新的目标对象
	 * @return 旧目标对象
	 * @throws IllegalArgumentException 如果新目标无效
	 */
	public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
		Assert.notNull(newTarget, "Target object must not be null");
		Object old = this.target;
		this.target = newTarget;
		return old;
	}


	/**
	 * 如果当前目标对象相等，则两个 HotSwappableTargetSource 相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof HotSwappableTargetSource that &&
				this.target.equals(that.target)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return HotSwappableTargetSource.class.hashCode();
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "HotSwappableTargetSource for target: " + this.target;
	}

}
