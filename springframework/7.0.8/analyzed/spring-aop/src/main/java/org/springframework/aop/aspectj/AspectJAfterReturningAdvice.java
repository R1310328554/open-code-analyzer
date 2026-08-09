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
import java.lang.reflect.Type;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.util.ClassUtils;
import org.springframework.util.TypeUtils;

/**
 * Spring AOP 建议包装 AspectJ 返回建议方法。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice
		implements AfterReturningAdvice, AfterAdvice, Serializable {

	/**
	 * 创建 `AspectJAfterReturningAdvice` 的新实例。
	 */
	public AspectJAfterReturningAdvice(
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
	 * 设置 Returning Name（`ReturningName`）。
	 */
	@Override
	public void setReturningName(String name) {
		setReturningNameNoCheck(name);
	}

	/**
	 * 在…之后回调：Returning（方法 `afterReturning`）。
	 */
	@Override
	public void afterReturning(@Nullable Object returnValue, Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable {
		if (shouldInvokeOnReturnValueOf(method, returnValue)) {
			invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
		}
	}


	/**
	 * 遵循 AspectJ 语义，如果指定了返回子句，则仅当返回值是给定返回类型的实例且泛型类型参数（如果有）匹配赋值规则时才会调用通知。如果返回类型是 Object，则“始终”会调
	 * 用该建议。
	 * @param returnValue 目标方法的返回值
	 * @return 为给定的返回值调用通知方法
	 */
	private boolean shouldInvokeOnReturnValueOf(Method method, @Nullable Object returnValue) {
		Class<?> type = getDiscoveredReturningType();
		Type genericType = getDiscoveredReturningGenericType();
		// 如果我们不处理原始类型，请检查泛型参数是否可分配。
		return (matchesReturnValue(type, method, returnValue) &&
				(genericType == null || genericType == type ||
						TypeUtils.isAssignable(genericType, method.getGenericReturnType())));
	}

	/**
	 * 遵循 AspectJ 语义，如果返回值为 null（或返回类型为 void），则应使用目标方法的返回类型来确定是否调用通知。另外，即使返回类型为 void，如果在通知方法中声明
	 * 的参数类型为 Object，则仍然必须调用该通知。
	 * @param type 在通知方法中声明的参数类型
	 * @param method 建议法
	 * @param returnValue 目标方法的返回值
	 * @return 为给定的返回值和类型调用通知方法
	 */
	private boolean matchesReturnValue(Class<?> type, Method method, @Nullable Object returnValue) {
		if (returnValue != null) {
			return ClassUtils.isAssignableValue(type, returnValue);
		}
		else if (Object.class == type && void.class == method.getReturnType()) {
			return true;
		}
		else {
			return ClassUtils.isAssignable(type, method.getReturnType());
		}
	}

}
