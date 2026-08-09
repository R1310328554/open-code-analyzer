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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * {@link MethodOverride} 的扩展，表示 IoC 容器对某方法的任意覆盖。
 *
 * <p>任何非 final 方法均可被覆盖，与其参数及返回类型无关。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class ReplaceOverride extends MethodOverride {

	/** 实现 {@link MethodReplacer} 的 Bean 名称。 */
	private final String methodReplacerBeanName;

	/** 用于消歧重载方法的参数类型标识片段列表。 */
	private final List<String> typeIdentifiers = new ArrayList<>();


	/**
	 * 构造新的 ReplaceOverride。
	 * @param methodName 要覆盖的方法名
	 * @param methodReplacerBeanName {@link MethodReplacer} 的 Bean 名称
	 */
	public ReplaceOverride(String methodName, String methodReplacerBeanName) {
		super(methodName);
		Assert.notNull(methodReplacerBeanName, "Method replacer bean name must not be null");
		this.methodReplacerBeanName = methodReplacerBeanName;
	}

	/**
	 * 构造新的 ReplaceOverride。
	 * @param methodName 要覆盖的方法名
	 * @param methodReplacerBeanName {@link MethodReplacer} 的 Bean 名称
	 * @param typeIdentifiers 参数类型的类型标识列表
	 * @since 6.2.9
	 */
	public ReplaceOverride(String methodName, String methodReplacerBeanName, List<String> typeIdentifiers) {
		super(methodName);
		Assert.notNull(methodReplacerBeanName, "Method replacer bean name must not be null");
		this.methodReplacerBeanName = methodReplacerBeanName;
		this.typeIdentifiers.addAll(typeIdentifiers);
	}


	/**
	 * 返回实现 MethodReplacer 的 Bean 名称。
	 */
	public String getMethodReplacerBeanName() {
		return this.methodReplacerBeanName;
	}

	/**
	 * 添加类名字符串片段（如 "Exception" 或 "java.lang.Exc"），
	 * 用于标识某一参数类型。
	 * @param identifier 全限定类名的子串
	 */
	public void addTypeIdentifier(String identifier) {
		this.typeIdentifiers.add(identifier);
	}

	/**
	 * 返回已注册的类型标识列表（类名字符串片段）。
	 * @since 6.2.9
	 * @see #addTypeIdentifier
	 */
	public List<String> getTypeIdentifiers() {
		return Collections.unmodifiableList(this.typeIdentifiers);
	}


	@Override
	public boolean matches(Method method) {
		if (!method.getName().equals(getMethodName())) {
			return false;
		}
		if (!isOverloaded()) {
			// 非重载：无需匹配参数类型
			return true;
		}
		// 重载方法：必须精确匹配参数类型标识
		if (this.typeIdentifiers.size() != method.getParameterCount()) {
			return false;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = this.typeIdentifiers.get(i);
			if (!parameterTypes[i].getName().contains(identifier)) {
				return false;
			}
		}
		return true;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (other instanceof ReplaceOverride that && super.equals(that) &&
				this.methodReplacerBeanName.equals(that.methodReplacerBeanName) &&
				this.typeIdentifiers.equals(that.typeIdentifiers));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.methodReplacerBeanName, this.typeIdentifiers);
	}

	@Override
	public String toString() {
		return "ReplaceOverride for method '" + getMethodName() + "'";
	}

}
