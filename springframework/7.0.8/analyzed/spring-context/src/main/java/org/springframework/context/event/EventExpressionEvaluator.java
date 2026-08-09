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

package org.springframework.context.event;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 处理应用事件 SpEL 表达式解析的工具类。
 * <p>设计为可复用、线程安全的组件。
 *
 * @author Stephane Nicoll
 * @since 4.2
 * @see CachedExpressionEvaluator
 */
class EventExpressionEvaluator extends CachedExpressionEvaluator {

	private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

	private final StandardEvaluationContext originalEvaluationContext;

	EventExpressionEvaluator(StandardEvaluationContext originalEvaluationContext) {
		this.originalEvaluationContext = originalEvaluationContext;
	}

	/**
	 * 判断指定表达式定义的条件是否求值为 {@code true}。
	 */
	public boolean condition(String conditionExpression, ApplicationEvent event, Method targetMethod,
			AnnotatedElementKey methodKey, @Nullable Object[] args) {

		// 1. 构造 SpEL 根对象，封装事件与监听器方法参数
		EventExpressionRootObject rootObject = new EventExpressionRootObject(event, args);
		// 2. 基于根对象、目标方法与参数创建求值上下文
		EvaluationContext evaluationContext = createEvaluationContext(rootObject, targetMethod, args);
		// 3. 从缓存获取或解析条件表达式，并在上下文中求值为布尔值
		return (Boolean.TRUE.equals(getExpression(this.conditionCache, methodKey, conditionExpression).getValue(
				evaluationContext, Boolean.class)));
	}

	private EvaluationContext createEvaluationContext(EventExpressionRootObject rootObject,
			Method method, @Nullable Object[] args) {

		// 1. 创建基于方法的求值上下文，绑定根对象、方法与参数
		MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject,
				method, args, getParameterNameDiscoverer());
		// 2. 将原始上下文的委托（如 Bean 解析器）应用到新上下文
		this.originalEvaluationContext.applyDelegatesTo(evaluationContext);
		return evaluationContext;
	}

}
