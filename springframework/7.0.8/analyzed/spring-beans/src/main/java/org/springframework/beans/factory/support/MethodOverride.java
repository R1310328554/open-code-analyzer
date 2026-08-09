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

import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 表示 IoC 容器对受管对象上某方法的覆盖。
 *
 * <p>注意：覆盖机制<i>并非</i>用于在任意位置插入横切代码；此类需求应使用 AOP。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public abstract class MethodOverride implements BeanMetadataElement {

	/** 待覆盖的方法名。 */
	private final String methodName;

	/** 被覆盖的方法是否为重载方法（需按参数类型消歧）。 */
	private boolean overloaded = true;

	/** 该元数据元素对应的配置来源对象。 */
	private @Nullable Object source;


	/**
	 * 为给定方法构造新的覆盖描述。
	 * @param methodName 要覆盖的方法名
	 */
	protected MethodOverride(String methodName) {
		Assert.notNull(methodName, "Method name must not be null");
		this.methodName = methodName;
	}


	/**
	 * 返回要覆盖的方法名。
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * 设置被覆盖的方法是否为<i>重载</i>方法
	 * （即是否需要按参数类型匹配来消歧同名方法）。
	 * <p>默认为 {@code true}；可设为 {@code false} 以优化运行时性能。
	 */
	protected void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}

	/**
	 * 返回被覆盖的方法是否为<i>重载</i>方法
	 * （即是否需要按参数类型匹配来消歧同名方法）。
	 */
	protected boolean isOverloaded() {
		return this.overloaded;
	}

	/**
	 * 设置该元数据元素的配置来源 {@code Object}。
	 * <p>对象的具体类型取决于所使用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}

	/**
	 * 子类必须重写此方法，以指示是否<i>匹配</i>给定方法。
	 * 既可检查参数列表，也可检查方法名。
	 * @param method 待检查的方法
	 * @return 本覆盖是否匹配给定方法
	 */
	public abstract boolean matches(Method method);


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof MethodOverride that &&
				this.methodName.equals(that.methodName) &&
				ObjectUtils.nullSafeEquals(this.source, that.source)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.methodName, this.source);
	}

}
