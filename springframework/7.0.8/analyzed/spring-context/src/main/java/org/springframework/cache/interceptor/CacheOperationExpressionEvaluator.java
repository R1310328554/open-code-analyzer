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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * 处理 SpEL 表达式解析的工具类。
 * 设计为可复用、线程安全的组件。
 *
 * <p>为提升性能，使用 {@link AnnotatedElementKey} 进行内部缓存。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 3.1
 */
class CacheOperationExpressionEvaluator extends CachedExpressionEvaluator {

	/**
	 * 表示当前尚无返回值。
	 */
	public static final Object NO_RESULT = new Object();

	/**
	 * 表示 {@code result} 变量完全不可用。
	 */
	public static final Object RESULT_UNAVAILABLE = new Object();

	/**
	 * 存放返回值的变量名。
	 */
	public static final String RESULT_VARIABLE = "result";


	/** key 表达式的解析缓存。 */
	private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<>(64);

	/** condition 表达式的解析缓存。 */
	private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

	/** unless 表达式的解析缓存。 */
	private final Map<ExpressionKey, Expression> unlessCache = new ConcurrentHashMap<>(64);

	/** 求值上下文工厂。 */
	private final CacheEvaluationContextFactory evaluationContextFactory;

	/** 构造表达式求值器并关联上下文工厂。 */
	public CacheOperationExpressionEvaluator(CacheEvaluationContextFactory evaluationContextFactory) {
		super();
		this.evaluationContextFactory = evaluationContextFactory;
		this.evaluationContextFactory.setParameterNameDiscoverer(this::getParameterNameDiscoverer);
	}

	/**
	 * 创建 {@link EvaluationContext}。
	 * @param caches 当前涉及的缓存
	 * @param method 方法
	 * @param args 方法参数
	 * @param target 目标对象
	 * @param targetClass 目标 Class
	 * @param result 返回值（可为 {@code null}），或当前尚无返回值时使用 {@link #NO_RESULT}
	 * @return 求值上下文
	 */
	public EvaluationContext createEvaluationContext(Collection<? extends Cache> caches,
			Method method, @Nullable Object[] args, Object target, Class<?> targetClass, Method targetMethod,
			@Nullable Object result) {

		CacheExpressionRootObject rootObject = new CacheExpressionRootObject(
				caches, method, args, target, targetClass);
		CacheEvaluationContext evaluationContext = this.evaluationContextFactory
				.forOperation(rootObject, targetMethod, args);
		if (result == RESULT_UNAVAILABLE) {
			// 标记 result 变量不可用（如调用前求值 condition）
			evaluationContext.addUnavailableVariable(RESULT_VARIABLE);
		}
		else if (result != NO_RESULT) {
			evaluationContext.setVariable(RESULT_VARIABLE, result);
		}
		return evaluationContext;
	}

	/** 求值 key 表达式并返回结果。 */
	public @Nullable Object key(String keyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return getExpression(this.keyCache, methodKey, keyExpression).getValue(evalContext);
	}

	/** 求值 condition 表达式，返回是否为 {@code true}。 */
	public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return (Boolean.TRUE.equals(getExpression(this.conditionCache, methodKey, conditionExpression).getValue(
				evalContext, Boolean.class)));
	}

	/** 求值 unless 表达式，返回是否为 {@code true}。 */
	public boolean unless(String unlessExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return (Boolean.TRUE.equals(getExpression(this.unlessCache, methodKey, unlessExpression).getValue(
				evalContext, Boolean.class)));
	}

	/**
	 * 清空所有表达式缓存。
	 */
	void clear() {
		this.keyCache.clear();
		this.conditionCache.clear();
		this.unlessCache.clear();
	}

}
