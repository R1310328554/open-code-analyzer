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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jspecify.annotations.Nullable;

/**
 * 方法覆盖集合，决定 Spring IoC 容器在运行时是否覆盖受管对象上的哪些方法。
 *
 * <p>当前支持的 {@link MethodOverride} 变体为
 * {@link LookupOverride} 与 {@link ReplaceOverride}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see MethodOverride
 */
public class MethodOverrides {

	/** 已注册的方法覆盖项。 */
	private final Set<MethodOverride> overrides = new CopyOnWriteArraySet<>();


	/**
	 * 创建新的 MethodOverrides。
	 */
	public MethodOverrides() {
	}

	/**
	 * 深拷贝构造器。
	 */
	public MethodOverrides(MethodOverrides other) {
		addOverrides(other);
	}


	/**
	 * 将给定方法覆盖全部复制到本对象。
	 */
	public void addOverrides(@Nullable MethodOverrides other) {
		if (other != null) {
			this.overrides.addAll(other.overrides);
		}
	}

	/**
	 * 添加给定的方法覆盖。
	 */
	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	/**
	 * 返回本对象包含的全部方法覆盖。
	 * @return MethodOverride 对象的集合
	 * @see MethodOverride
	 */
	public Set<MethodOverride> getOverrides() {
		return this.overrides;
	}

	/**
	 * 返回方法覆盖集合是否为空。
	 */
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}

	/**
	 * 返回给定方法对应的覆盖（若有）。
	 * @param method 待查找覆盖的方法
	 * @return 方法覆盖，若无则返回 {@code null}
	 */
	public @Nullable MethodOverride getOverride(Method method) {
		MethodOverride match = null;
		for (MethodOverride candidate : this.overrides) {
			if (candidate.matches(method)) {
				match = candidate;
			}
		}
		return match;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof MethodOverrides that &&
				this.overrides.equals(that.overrides)));
	}

	@Override
	public int hashCode() {
		return this.overrides.hashCode();
	}

}
