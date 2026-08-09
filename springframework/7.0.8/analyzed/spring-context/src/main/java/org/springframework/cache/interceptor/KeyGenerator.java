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

import org.jspecify.annotations.Nullable;

/**
 * 缓存键生成器。根据给定方法（作为上下文）及其参数创建缓存键。
 *
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @since 3.1
 */
@FunctionalInterface
public interface KeyGenerator {

	/**
	 * 为给定方法及其参数生成缓存键。
	 * @param target 目标实例
	 * @param method 被调用的方法
	 * @param params 方法参数（可变参数已展开）
	 * @return 生成的缓存键
	 */
	Object generate(Object target, Method method, @Nullable Object... params);

}
