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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.weaver.tools.ShadowMatch;
import org.jspecify.annotations.Nullable;

/**
 * 内部 {@link ShadowMatch} 工具类。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 6.2
 */
public abstract class ShadowMatchUtils {

	private static final Map<Object, ShadowMatch> shadowMatchCache = new ConcurrentHashMap<>(256);


	/**
	 * 查找指定键对应的 {@link ShadowMatch}。
	 * @param key 使用的键
	 * @return 指定键对应的 {@code ShadowMatch}，
	 * 未找到时返回 {@code null}
	 */
	static @Nullable ShadowMatch getShadowMatch(Object key) {
		return shadowMatchCache.get(key);
	}

	/**
	 * 将 {@link ShadowMatch} 与指定键关联。
	 * 若条目已存在，则忽略给定的 {@code shadowMatch}。
	 * @param key 使用的键
	 * @param shadowMatch 该键尚无条目时使用的 shadow match
	 * @return 指定键应使用的 shadow match
	 */
	static ShadowMatch setShadowMatch(Object key, ShadowMatch shadowMatch) {
		ShadowMatch existing = shadowMatchCache.putIfAbsent(key, shadowMatch);
		return (existing != null ? existing : shadowMatch);
	}

	/**
	 * 清空已计算 {@link ShadowMatch} 实例的缓存。
	 */
	public static void clearCache() {
		shadowMatchCache.clear();
	}

}
