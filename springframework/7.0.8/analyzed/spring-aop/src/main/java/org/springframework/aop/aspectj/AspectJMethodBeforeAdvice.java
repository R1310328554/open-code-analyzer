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

import org.jspecify.annotations.Nullable;

import org.springframework.aop.MethodBeforeAdvice;

/**
 * Spring AOP 建议包装 AspectJ before 方法。
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice, Serializable {

	/**
	 * 创建 `AspectJMethodBeforeAdvice` 的新实例。
	 */
	public AspectJMethodBeforeAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}


	/**
	 * 在…之前回调（方法 `before`）。
	 */
	@Override
	public void before(Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable {
		invokeAdviceMethod(getJoinPointMatch(), null, null);
	}

	/**
	 * 判断是否 Before Advice。
	 */
	@Override
	public boolean isBeforeAdvice() {
		return true;
	}

	/**
	 * 判断是否 After Advice。
	 */
	@Override
	public boolean isAfterAdvice() {
		return false;
	}

}
