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
 * 缓存本地目标对象、但允许在应用运行期间交换目标的
 * {@link org.springframework.aop.TargetSource} 实现。
 *
 * <p>在 Spring IoC 容器中配置本类对象时，请使用构造器注入。
 *
 * <p>若目标在序列化时可序列化，则本 TargetSource 可序列化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class HotSwappableTargetSource implements TargetSource, Serializable {

	/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */
	private static final long serialVersionUID = 7497929212653839187L;


	/** 当前目标对象。 */
	@SuppressWarnings("serial")
	private Object target;


	/**
	 * 以给定初始目标对象创建 HotSwappableTargetSource。
	 * @param initialTarget 初始目标对象
	 */
	public HotSwappableTargetSource(Object initialTarget) {
		Assert.notNull(initialTarget, "Target object must not be null");
		this.target = initialTarget;
	}


	/**
	 * 返回当前目标对象的类型。
	 * <p>返回值通常对所有目标对象保持一致。
	 */
	@Override
	public synchronized Class<?> getTargetClass() {
		return this.target.getClass();
	}

	@Override
	public synchronized Object getTarget() {
		return this.target;
	}


	/**
	 * 交换目标，返回旧目标对象。
	 * @param newTarget 新目标对象
	 * @return 旧目标对象
	 * @throws IllegalArgumentException 若新目标无效
	 */
	public synchronized Object swap(Object newTarget) throws IllegalArgumentException {
		Assert.notNull(newTarget, "Target object must not be null");
		Object old = this.target;
		this.target = newTarget;
		return old;
	}


	/**
	 * 若当前目标对象相等，则两个 HotSwappableTargetSource 相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof HotSwappableTargetSource that &&
				this.target.equals(that.target)));
	}

	@Override
	public int hashCode() {
		return HotSwappableTargetSource.class.hashCode();
	}

	@Override
	public String toString() {
		return "HotSwappableTargetSource for target: " + this.target;
	}

}
