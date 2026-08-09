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
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.function.SingletonSupplier;

/**
 * {@link CacheEvaluationContext} 的工厂，确保内部委托对象可被复用。
 *
 * @author Stephane Nicoll
 * @since 6.1.1
 */
class CacheEvaluationContextFactory {

	/** 原始 SpEL 求值上下文，用于向新建上下文传播委托。 */
	private final StandardEvaluationContext originalContext;

	/** 参数名发现器的延迟供应器。 */
	private @Nullable Supplier<ParameterNameDiscoverer> parameterNameDiscoverer;


	/** 使用给定原始上下文构造工厂。 */
	CacheEvaluationContextFactory(StandardEvaluationContext originalContext) {
		this.originalContext = originalContext;
	}


	/** 设置用于解析方法参数名的发现器供应器。 */
	public void setParameterNameDiscoverer(Supplier<ParameterNameDiscoverer> parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/** 获取参数名发现器，默认使用共享的 {@link DefaultParameterNameDiscoverer} 实例。 */
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		if (this.parameterNameDiscoverer == null) {
			this.parameterNameDiscoverer = SingletonSupplier.of(DefaultParameterNameDiscoverer.getSharedInstance());
		}
		return this.parameterNameDiscoverer.get();
	}


	/**
	 * 为指定缓存操作创建 {@link CacheEvaluationContext}。
	 * @param rootObject 用作求值根对象的 {@code root} 对象
	 * @param targetMethod 目标缓存 {@link Method}
	 * @param args 方法调用的参数
	 * @return 适用于该缓存操作的求值上下文
	 */
	public CacheEvaluationContext forOperation(CacheExpressionRootObject rootObject,
			Method targetMethod, @Nullable Object[] args) {

		CacheEvaluationContext evaluationContext = new CacheEvaluationContext(
				rootObject, targetMethod, args, getParameterNameDiscoverer());
		// 将原始上下文中的委托（如 BeanFactoryResolver）复制到新上下文
		this.originalContext.applyDelegatesTo(evaluationContext);
		return evaluationContext;
	}

}
