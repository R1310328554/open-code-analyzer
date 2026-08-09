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
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.util.ObjectUtils;

/**
 * 无目标（或仅已知目标类）时的标准 {@code TargetSource}，
 * 行为完全由接口与 Advisor 提供。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public final class EmptyTargetSource implements TargetSource, Serializable {

	/** 使用 Spring 1.2 的 serialVersionUID 以保证互操作性。 */
	private static final long serialVersionUID = 3680494563553489691L;


	//---------------------------------------------------------------------
	// 静态工厂方法
	//---------------------------------------------------------------------

	/**
	 * 本 {@link EmptyTargetSource} 的标准（单例）实例。
	 */
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);


	/**
	 * 为给定目标 Class 返回 EmptyTargetSource。
	 * @param targetClass 目标 Class（可为 {@code null}）
	 * @see #getTargetClass()
	 */
	public static EmptyTargetSource forClass(@Nullable Class<?> targetClass) {
		return forClass(targetClass, true);
	}

	/**
	 * 为给定目标 Class 返回 EmptyTargetSource。
	 * @param targetClass 目标 Class（可为 {@code null}）
	 * @param isStatic TargetSource 是否标记为 static
	 * @see #getTargetClass()
	 */
	public static EmptyTargetSource forClass(@Nullable Class<?> targetClass, boolean isStatic) {
		return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
	}


	//---------------------------------------------------------------------
	// 实例实现
	//---------------------------------------------------------------------

	private final @Nullable Class<?> targetClass;

	private final boolean isStatic;


	/**
	 * 创建 {@link EmptyTargetSource} 的新实例。
	 * <p>本构造器为 {@code private}，以强制单例/工厂方法模式。
	 * @param targetClass 要暴露的目标类（可为 {@code null}）
	 * @param isStatic TargetSource 是否标记为 static
	 */
	private EmptyTargetSource(@Nullable Class<?> targetClass, boolean isStatic) {
		this.targetClass = targetClass;
		this.isStatic = isStatic;
	}


	/**
	 * 始终返回指定的目标 Class；若无则 {@code null}。
	 */
	@Override
	public @Nullable Class<?> getTargetClass() {
		return this.targetClass;
	}

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	public boolean isStatic() {
		return this.isStatic;
	}

	/**
	 * 始终返回 {@code null}。
	 */
	@Override
	public @Nullable Object getTarget() {
		return null;
	}


	/**
	 * 在无目标类时反序列化返回标准实例，从而保护单例模式。
	 */
	private Object readResolve() {
		return (this.targetClass == null && this.isStatic ? INSTANCE : this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof EmptyTargetSource that &&
				ObjectUtils.nullSafeEquals(this.targetClass, that.targetClass) &&
				this.isStatic == that.isStatic));
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), this.targetClass);
	}

	@Override
	public String toString() {
		return "EmptyTargetSource: " +
				(this.targetClass != null ? "target class [" + this.targetClass.getName() + "]" : "no target class") +
				", " + (this.isStatic ? "static" : "dynamic");
	}

}
