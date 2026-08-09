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
import org.jspecify.annotations.Nullable;

import org.springframework.aop.AfterAdvice;

/**
 * Spring AOP 建议包装 AspectJ 后抛出建议方法。
 * @author Rod Johnson
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice
		implements MethodInterceptor, AfterAdvice, Serializable {

	/**
	 * 创建 `AspectJAfterThrowingAdvice` 的新实例。
	 */
	public AspectJAfterThrowingAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJBeforeAdviceMethod, pointcut, aif);
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
		return true;
	}

	/**
	 * 设置 Throwing Name（`ThrowingName`）。
	 */
	@Override
	public void setThrowingName(String name) {
		setThrowingNameNoCheck(name);
	}

	/**
	 * 调用（方法 `invoke`）。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			if (shouldInvokeOnThrowing(ex)) {
				invokeAdviceMethod(getJoinPointMatch(), null, ex);
			}
			throw ex;
		}
	}

	/**
	 * 在 AspectJ 语义中，仅当抛出的异常是给定抛出类型的子类型时，才会调用指定抛出子句的抛出建议。
	 */
	private boolean shouldInvokeOnThrowing(Throwable ex) {
		return getDiscoveredThrowingType().isAssignableFrom(ex.getClass());
	}

}
