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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.AfterAdvice;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.util.Assert;

/**
 * 拦截器来包装抛出后的建议。
 * <p>{@code ThrowsAdvice} 实现方法参数上的处理程序方法的签名必须采用以下形式：<br>
 * {@code void afterThrowing([Method, args, target], ThrowableSubclass);}
 * OCAJAVA0DCO仅需要最后一个参数。
 * <p> 有效方法的一些示例是：
 * <pre class="code">public void afterThrowing(Exception ex)</pre> <pre
 * class="code">public void afterThrowing(RemoteException)</pre> <pre class="code">public
 * void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre> <pre
 * class="code">public void afterThrowing(Method method, Object[]
 * args、对象目标、ServletException 前)</pre>
 * <p>这是一个框架类，Spring用户不需要直接使用。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see MethodBeforeAdviceInterceptor
 * @see AfterReturningAdviceInterceptor
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

	private static final String AFTER_THROWING = "afterThrowing";

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);


	/** 通知相关状态（`throwsAdvice`）。 */
	private final Object throwsAdvice;

	/**
	 */
	private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();


	/**
	 * 为给定的 ThrowsAdvice 创建一个新的 ThrowsAdviceInterceptor。
	 * @param throwsAdvice 定义异常处理程序方法的通知对象（通常是 {@link org.springframework.aop.ThrowsAdvice} 实现）
	 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		Assert.notNull(throwsAdvice, "Advice must not be null");
		this.throwsAdvice = throwsAdvice;

		Method[] methods = throwsAdvice.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(AFTER_THROWING)) {
				Class<?> throwableParam = null;
				if (method.getParameterCount() == 1) {
					// 只是一个 Throwable 参数
					throwableParam = method.getParameterTypes()[0];
					if (!Throwable.class.isAssignableFrom(throwableParam)) {
						throw new AopConfigException("Invalid afterThrowing signature: " +
								"single argument must be a Throwable subclass");
					}
				}
				else if (method.getParameterCount() == 4) {
					// 方法、对象[]、目标、可抛出
					Class<?>[] paramTypes = method.getParameterTypes();
					if (!Method.class.equals(paramTypes[0]) || !Object[].class.equals(paramTypes[1]) ||
							Throwable.class.equals(paramTypes[2]) || !Throwable.class.isAssignableFrom(paramTypes[3])) {
						throw new AopConfigException("Invalid afterThrowing signature: " +
								"four arguments must be Method, Object[], target, throwable: " + method);
					}
					throwableParam = paramTypes[3];
				}
				if (throwableParam == null) {
					throw new AopConfigException("Unsupported afterThrowing signature: single throwable argument " +
							"or four arguments Method, Object[], target, throwable expected: " + method);
				}
				// 要注册的异常处理程序...
				Method existingMethod = this.exceptionHandlerMap.put(throwableParam, method);
				if (existingMethod != null) {
					throw new AopConfigException("Only one afterThrowing method per specific Throwable subclass " +
							"allowed: " + method + " / " + existingMethod);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Found exception handler method on throws advice: " + method);
				}
			}
		}

		if (this.exceptionHandlerMap.isEmpty()) {
			throw new AopConfigException(
					"At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
		}
	}


	/**
	 * 返回此建议中处理程序方法的数量。
	 */
	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
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
			Method handlerMethod = getExceptionHandler(ex);
			if (handlerMethod != null) {
				invokeHandlerMethod(mi, ex, handlerMethod);
			}
			throw ex;
		}
	}

	/**
	 * 确定给定异常的异常处理方法。
	 * @param exception 抛出的异常
	 * @return 给定异常类型的处理程序，如果未找到，则为 {@code null}
	 */
	private @Nullable Method getExceptionHandler(Throwable exception) {
		Class<?> exceptionClass = exception.getClass();
		if (logger.isTraceEnabled()) {
			logger.trace("Trying to find handler for exception of type [" + exceptionClass.getName() + "]");
		}
		Method handler = this.exceptionHandlerMap.get(exceptionClass);
		while (handler == null && exceptionClass != Throwable.class) {
			exceptionClass = exceptionClass.getSuperclass();
			handler = this.exceptionHandlerMap.get(exceptionClass);
		}
		if (handler != null && logger.isTraceEnabled()) {
			logger.trace("Found handler for exception of type [" + exceptionClass.getName() + "]: " + handler);
		}
		return handler;
	}

	/**
	 * 调用：Handler Method（方法 `invokeHandlerMethod`）。
	 */
	private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
		Object[] handlerArgs;
		if (method.getParameterCount() == 1) {
			handlerArgs = new Object[] {ex};
		}
		else {
			handlerArgs = new Object[] {mi.getMethod(), mi.getArguments(), mi.getThis(), ex};
		}
		try {
			method.invoke(this.throwsAdvice, handlerArgs);
		}
		catch (InvocationTargetException targetEx) {
			throw targetEx.getTargetException();
		}
	}

}
