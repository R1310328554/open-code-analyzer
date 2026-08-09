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
import org.springframework.util.ObjectUtils;

/**
 * 持有给定对象的 {@link org.springframework.aop.TargetSource} 接口实现。
 * 这是 Spring AOP 框架使用的 TargetSource 接口默认实现。
 * 应用代码通常无需创建本类对象。
 *
 * <p>本类可序列化，但 SingletonTargetSource 的实际可序列性取决于目标是否可序列化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)
 */
public class SingletonTargetSource implements TargetSource, Serializable {

	/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */
	private static final long serialVersionUID = 9031246629662423738L;


	/** 缓存并通过反射调用的目标对象。 */
	@SuppressWarnings("serial")
	private final Object target;


	/**
	 * 为给定目标创建 SingletonTargetSource。
	 * @param target 目标对象
	 */
	public SingletonTargetSource(Object target) {
		Assert.notNull(target, "Target object must not be null");
		this.target = target;
	}


	@Override
	public Class<?> getTargetClass() {
		return this.target.getClass();
	}

	@Override
	public Object getTarget() {
		return this.target;
	}

	@Override
	public boolean isStatic() {
		return true;
	}


	/**
	 * 若目标相同或目标对象相等，则两个调用拦截器相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof SingletonTargetSource that &&
				this.target.equals(that.target)));
	}

	/**
	 * SingletonTargetSource 使用目标对象的哈希码。
	 */
	@Override
	public int hashCode() {
		return this.target.hashCode();
	}

	@Override
	public String toString() {
		return "SingletonTargetSource for target object [" + ObjectUtils.identityToString(this.target) + "]";
	}

}
