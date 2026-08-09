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
 * 保存给定对象的 {@link org.springframework.aop.TargetSource} 接口的实现。这是 Spring AOP 框架使用的
 * TargetSource 接口的默认实现。通常不需要在应用程序代码中创建此类的对象。
 * <p>该类是可序列化的。然而，SingletonTargetSource 的实际可序列化性将取决于目标是否可序列化。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)
 */
public class SingletonTargetSource implements TargetSource, Serializable {

	/**
	 */
	private static final long serialVersionUID = 9031246629662423738L;


	/**
	 */
	@SuppressWarnings("serial")
	private final Object target;


	/**
	 * 为给定目标创建一个新的 SingletonTargetSource。
	 * @param target 目标对象
	 */
	public SingletonTargetSource(Object target) {
		Assert.notNull(target, "Target object must not be null");
		this.target = target;
	}


	/**
	 * 获取 Target Class（`TargetClass`）。
	 */
	@Override
	public Class<?> getTargetClass() {
		return this.target.getClass();
	}

	/**
	 * 获取 Target（`Target`）。
	 */
	@Override
	public Object getTarget() {
		return this.target;
	}

	/**
	 * 判断是否 Static。
	 */
	@Override
	public boolean isStatic() {
		return true;
	}


	/**
	 * 如果两个调用者拦截器具有相同的目标或者目标或多个目标相等，则它们相等。
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

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "SingletonTargetSource for target object [" + ObjectUtils.identityToString(this.target) + "]";
	}

}
