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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ProxyMethodInvocation;

/**
 * Spring AOP 围绕通知 (MethodInterceptor) 封装了 AspectJ 建议方法。公开 ProceedingJoinPoint。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor, Serializable {

	/**
	 * 创建 `AspectJAroundAdvice` 的新实例。
	 */
	public AspectJAroundAdvice(
			Method aspectJAroundAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJAroundAdviceMethod, pointcut, aif);
	}


	/**
	 * 判断是否 Before Advice。
	 */
	@Override
	public boolean isBeforeAdvice() {
		return false;
	}

	/**
	 * 判断是否 After Advice。
	 */
	@Override
	public boolean isAfterAdvice() {
		return false;
	}

	/**
	 * 方法 `supportsProceedingJoinPoint`：完成本类中与「supports Proceeding Join Point」相关的职责。
	 */
	@Override
	protected boolean supportsProceedingJoinPoint() {
		return true;
	}

	/**
	 * 调用（方法 `invoke`）。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		if (!(mi instanceof ProxyMethodInvocation pmi)) {
			throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		ProceedingJoinPoint pjp = lazyGetProceedingJoinPoint(pmi);
		JoinPointMatch jpm = getJoinPointMatch(pmi);
		return invokeAdviceMethod(pjp, jpm, null, null);
	}

	/**
	 * 返回当前调用的 ProceedingJoinPoint，如果尚未绑定到线程则延迟实例化它。
	 * @param rmi 当前的 Spring AOP ReflectiveMethodInitation，我们将使用它来进行属性绑定
	 * @return ProceedingJoinPoint 提供建议方法
	 */
	protected ProceedingJoinPoint lazyGetProceedingJoinPoint(ProxyMethodInvocation rmi) {
		return new MethodInvocationProceedingJoinPoint(rmi);
	}

}
