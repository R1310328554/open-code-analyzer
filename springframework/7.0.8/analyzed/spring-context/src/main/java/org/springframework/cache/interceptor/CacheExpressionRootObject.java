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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

/**
 * 描述表达式求值期间使用的根对象。
 *
 * @author Costin Leau
 * @author Sam Brannen
 * @since 3.1
 */
class CacheExpressionRootObject {

	/** 当前操作涉及的缓存集合。 */
	private final Collection<? extends Cache> caches;

	/** 被拦截的方法。 */
	private final Method method;

	/** 方法调用参数。 */
	private final @Nullable Object[] args;

	/** 目标对象实例。 */
	private final Object target;

	/** 目标对象的 Class。 */
	private final Class<?> targetClass;


	/** 构造表达式求值根对象。 */
	public CacheExpressionRootObject(
			Collection<? extends Cache> caches, Method method, @Nullable Object[] args, Object target, Class<?> targetClass) {

		this.method = method;
		this.target = target;
		this.targetClass = targetClass;
		this.args = args;
		this.caches = caches;
	}


	/** 返回当前操作涉及的缓存集合。 */
	public Collection<? extends Cache> getCaches() {
		return this.caches;
	}

	/** 返回被拦截的方法。 */
	public Method getMethod() {
		return this.method;
	}

	/** 返回方法名。 */
	public String getMethodName() {
		return this.method.getName();
	}

	/** 返回方法调用参数。 */
	public @Nullable Object[] getArgs() {
		return this.args;
	}

	/** 返回目标对象实例。 */
	public Object getTarget() {
		return this.target;
	}

	/** 返回目标对象的 Class。 */
	public Class<?> getTargetClass() {
		return this.targetClass;
	}

}
