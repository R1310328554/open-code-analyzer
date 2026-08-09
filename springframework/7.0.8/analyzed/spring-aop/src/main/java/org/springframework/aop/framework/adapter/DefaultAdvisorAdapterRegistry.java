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

package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * {@link AdvisorAdapterRegistry} 接口的默认实现。支持{@link
 * org.aopalliance.intercept.MethodInterceptor}、{@link
 * org.springframework.aop.MethodBeforeAdvice}、{@link
 * org.springframework.aop.AfterReturningAdvice}、{@link
 * org.springframework.aop.ThrowsAdvice}。
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

	private static final MethodInterceptor[] EMPTY_METHOD_INTERCEPTOR_ARRAY = new MethodInterceptor[0];


	private final List<AdvisorAdapter> adapters = new ArrayList<>(3);


	/**
	 * 创建一个新的 DefaultAdvisorAdapterRegistry，注册众所周知的适配器。
	 */
	public DefaultAdvisorAdapterRegistry() {
		registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
		registerAdvisorAdapter(new AfterReturningAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}


	/**
	 * 方法 `wrap`：完成本类中与「wrap」相关的职责。
	 */
	@Override
	public Advisor wrap(Object adviceObject) throws UnknownAdviceTypeException {
		if (adviceObject instanceof Advisor advisor) {
			return advisor;
		}
		if (!(adviceObject instanceof Advice advice)) {
			throw new UnknownAdviceTypeException(adviceObject);
		}
		if (advice instanceof MethodInterceptor) {
			// 如此众所周知，它甚至不需要适配器。
			return new DefaultPointcutAdvisor(advice);
		}
		for (AdvisorAdapter adapter : this.adapters) {
			// 检查是否受支持。
			if (adapter.supportsAdvice(advice)) {
				return new DefaultPointcutAdvisor(advice);
			}
		}
		throw new UnknownAdviceTypeException(advice);
	}

	/**
	 * 获取 Interceptors（`Interceptors`）。
	 */
	@Override
	public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
		List<MethodInterceptor> interceptors = new ArrayList<>(3);
		Advice advice = advisor.getAdvice();
		if (advice instanceof MethodInterceptor methodInterceptor) {
			interceptors.add(methodInterceptor);
		}
		for (AdvisorAdapter adapter : this.adapters) {
			if (adapter.supportsAdvice(advice)) {
				interceptors.add(adapter.getInterceptor(advisor));
			}
		}
		if (interceptors.isEmpty()) {
			throw new UnknownAdviceTypeException(advisor.getAdvice());
		}
		return interceptors.toArray(EMPTY_METHOD_INTERCEPTOR_ARRAY);
	}

	/**
	 * 注册：Advisor Adapter（方法 `registerAdvisorAdapter`）。
	 */
	@Override
	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		this.adapters.add(adapter);
	}

}
