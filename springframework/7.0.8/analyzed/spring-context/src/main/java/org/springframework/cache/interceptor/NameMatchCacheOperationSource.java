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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;

/**
 * 简单的 {@link CacheOperationSource} 实现，允许通过注册的方法名匹配缓存属性。
 *
 * @author Costin Leau
 * @since 3.1
 */
@SuppressWarnings("serial")
public class NameMatchCacheOperationSource implements CacheOperationSource, Serializable {

	/**
	 * 子类可用的日志记录器。
	 * <p>声明为 static 以优化序列化。
	 */
	protected static final Log logger = LogFactory.getLog(NameMatchCacheOperationSource.class);


	/** 键为方法名；值为 CacheOperation 集合。 */
	private final Map<String, Collection<CacheOperation>> nameMap = new LinkedHashMap<>();


	/**
	 * 设置方法名/属性映射，由方法名（例如 "myMethod"）与
	 * CacheOperation 实例（或将被转换为 CacheOperation 的字符串）组成。
	 * @see CacheOperation
	 */
	public void setNameMap(Map<String, Collection<CacheOperation>> nameMap) {
		nameMap.forEach(this::addCacheMethod);
	}

	/**
	 * 为可缓存方法添加属性。
	 * <p>方法名可以是精确匹配，或 "xxx*"、"*xxx"、"*xxx*" 模式以匹配多个方法。
	 * @param methodName 方法名
	 * @param ops 与方法关联的缓存操作
	 */
	public void addCacheMethod(String methodName, Collection<CacheOperation> ops) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding method [" + methodName + "] with cache operations [" + ops + "]");
		}
		this.nameMap.put(methodName, ops);
	}

	@Override
	public @Nullable Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
		// 先尝试方法名精确匹配
		String methodName = method.getName();
		Collection<CacheOperation> ops = this.nameMap.get(methodName);

		if (ops == null) {
			// 查找最具体的名称匹配
			String bestNameMatch = null;
			for (String mappedName : this.nameMap.keySet()) {
				if (isMatch(methodName, mappedName) &&
						(bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
					ops = this.nameMap.get(mappedName);
					bestNameMatch = mappedName;
				}
			}
		}

		return ops;
	}

	/**
	 * 判断给定方法名是否与映射名匹配。
	 * <p>默认实现检查 "xxx*"、"*xxx"、"*xxx*" 模式以及直接相等。
	 * 子类可覆盖。
	 * @param methodName 类的方法名
	 * @param mappedName 描述符中的映射名
	 * @return 名称是否匹配
	 * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String methodName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, methodName);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof NameMatchCacheOperationSource otherCos &&
				ObjectUtils.nullSafeEquals(this.nameMap, otherCos.nameMap)));
	}

	@Override
	public int hashCode() {
		return NameMatchCacheOperationSource.class.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.nameMap;
	}

}
