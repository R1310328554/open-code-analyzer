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
 * 包装异常抛出后 Advice 的拦截器。
 *
 * <p>{@code ThrowsAdvice} 实现中处理方法的签名须为：<br>
 *
 * {@code void afterThrowing([Method, args, target], ThrowableSubclass);}
 *
 * <p>仅最后一个参数为必需。
 *
 * <p>有效方法示例：
 *
 * <pre class="code">public void afterThrowing(Exception ex)</pre>
 * <pre class="code">public void afterThrowing(RemoteException)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>
 *
 * <p>框架内部类，Spring 用户无需直接使用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see MethodBeforeAdviceInterceptor
 * @see AfterReturningAdviceInterceptor
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

	private static final String AFTER_THROWING = "afterThrowing";

	private static final Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);


	private final Object throwsAdvice;

	/** 异常抛出 Advice 上的方法，按异常类索引。 */
	private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();


	/**
	 * 为给定 ThrowsAdvice 创建新的 ThrowsAdviceInterceptor。
	 * @param throwsAdvice 定义异常处理方法的对象
	 *（通常为 {@link org.springframework.aop.ThrowsAdvice} 实现）
	 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		Assert.notNull(throwsAdvice, "Advice must not be null");
		this.throwsAdvice = throwsAdvice;

		Method[] methods = throwsAdvice.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(AFTER_THROWING)) {
				Class<?> throwableParam = null;
				if (method.getParameterCount() == 1) {
					// 仅一个 Throwable 参数
					throwableParam = method.getParameterTypes()[0];
					if (!Throwable.class.isAssignableFrom(throwableParam)) {
						throw new AopConfigException("Invalid afterThrowing signature: " +
								"single argument must be a Throwable subclass");
					}
				}
				else if (method.getParameterCount() == 4) {
					// Method、Object[]、target、throwable
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
				// 待注册的异常处理器...
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
	 * 返回本 Advice 中处理方法的数量。
	 */
	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
	}


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
	 * 确定给定异常对应的异常处理方法。
	 * @param exception 抛出的异常
	 * @return 给定异常类型的处理器，未找到则返回 {@code null}
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
