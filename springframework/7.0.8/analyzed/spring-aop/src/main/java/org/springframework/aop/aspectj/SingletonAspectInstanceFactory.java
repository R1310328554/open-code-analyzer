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

package org.springframework.aop.aspectj;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * 由指定单例对象支持的 {@link AspectInstanceFactory} 实现，为每个 {@link #getAspectInstance()} 调用返回相同的实例。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleAspectInstanceFactory
 */
@SuppressWarnings("serial")
public class SingletonAspectInstanceFactory implements AspectInstanceFactory, Serializable {

	/** `aspectInstance`：该类的成员状态。 */
	private final Object aspectInstance;


	/**
	 * 为给定的方面实例创建一个新的 SingletonAspectInstanceFactory。
	 * @param aspectInstance 单例方面实例
	 */
	public SingletonAspectInstanceFactory(Object aspectInstance) {
		Assert.notNull(aspectInstance, "Aspect instance must not be null");
		this.aspectInstance = aspectInstance;
	}


	/**
	 * 获取 Aspect Instance（`AspectInstance`）。
	 */
	@Override
	public final Object getAspectInstance() {
		return this.aspectInstance;
	}

	/**
	 * 获取 Aspect Class Loader（`AspectClassLoader`）。
	 */
	@Override
	public @Nullable ClassLoader getAspectClassLoader() {
		return this.aspectInstance.getClass().getClassLoader();
	}

	/**
	 * 确定该工厂方面实例的顺序，可以是通过实现 {@link org.springframework.core.Ordered} 接口表达的特定于实例的顺序，也可以是回退顺序。
	 * @see org.springframework.core.Ordered
	 * @see #getOrderForAspectClass
	 */
	@Override
	public int getOrder() {
		if (this.aspectInstance instanceof Ordered ordered) {
			return ordered.getOrder();
		}
		return getOrderForAspectClass(this.aspectInstance.getClass());
	}

	/**
	 * 通过实现 {@link org.springframework.core.Ordered} 接口，确定方面实例不表达特定于实例的顺序的情况的回退顺序。
	 * <p>默认实现只是返回{@code Ordered.LOWEST_PRECEDENCE}。
	 * @param aspectClass 方面类
	 */
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
