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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;

/**
 * 用于跟踪的基本 {@code MethodInterceptor} 实现。
 * <p> 默认情况下，日志消息写入拦截器类的日志，而不是被拦截的类的日志。将 {@code useDynamicLogger} bean 属性设置为 {@code true} 会
 * 导致所有日志消息都写入到被拦截的目标类的 {@code Log} 中。
 * <p>子类必须实现 {@code invokeUnderTrace} 方法，该方法仅在应跟踪特定调用时由此类调用。子类应写入提供的 {@code Log} 实例。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setUseDynamicLogger
 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
 */
@SuppressWarnings("serial")
public abstract class AbstractTraceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * 用于写入跟踪消息的默认 {@code Log} 实例。该实例映射到实现 {@code Class}。
	 */
	protected transient @Nullable Log defaultLogger = LogFactory.getLog(getClass());

	/**
	 * 指示使用动态记录器时是否应隐藏代理类名称。
	 * @see #setUseDynamicLogger
	 */
	private boolean hideProxyClassNames = false;

	/**
	 * 指示是否将异常传递给记录器。
	 * @see #writeToLog(Log, String, Throwable)
	 */
	private boolean logExceptionStackTrace = true;


	/**
	 * 设置是使用动态记录器还是静态记录器。默认是此跟踪拦截器的静态记录器。 <p> 用于确定应使用哪个 {@code Log} 实例来为特定方法调用写入日志消息：动态实例用于调用 {
	 * @code Class}，静态实例用于跟踪拦截器的 {@code Class}。 <p><b>NOTE:</b> 指定此属性或“loggerName”，而不是两者。
	 * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setUseDynamicLogger(boolean useDynamicLogger) {
		// 如果不使用默认记录器，则释放它。
		this.defaultLogger = (useDynamicLogger ? null : LogFactory.getLog(getClass()));
	}

	/**
	 * 设置要使用的记录器的名称。该名称将通过 Commons Logging 传递到底层记录器实现，根据记录器的配置被解释为日志类别。 <p>This 可以指定为不登录到类的类别（无
	 * 论是此拦截器的类还是被调用的类），而是登录到特定的命名类别。 <p><b>NOTE:</b> 指定此属性或“useDynamicLogger”，而不是同时指定两者。
	 * @see org.apache.commons.logging.LogFactory#getLog(String)
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public void setLoggerName(String loggerName) {
		this.defaultLogger = LogFactory.getLog(loggerName);
	}

	/**
	 * 设置为“true”以使 {@link #setUseDynamicLogger dynamic loggers} 尽可能隐藏代理类名称。默认为“假”。
	 */
	public void setHideProxyClassNames(boolean hideProxyClassNames) {
		this.hideProxyClassNames = hideProxyClassNames;
	}

	/**
	 * 设置是否将异常传递给记录器，建议将其堆栈跟踪包含到日志中。默认为“true”；将其设置为“false”，以便将日志输出减少为仅跟踪消息（可能包括异常类名称和异常消息，如果适用）
	 * 。
	 * @since 4.3.10
	 */
	public void setLogExceptionStackTrace(boolean logExceptionStackTrace) {
		this.logExceptionStackTrace = logExceptionStackTrace;
	}


	/**
	 * 确定是否为特定 {@code MethodInvocation} 启用日志记录。如果不是，则方法调用正常进行，否则方法调用将传递给 {@code invokeUnderTrac
	 * e} 方法进行处理。
	 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		Log logger = getLoggerForInvocation(invocation);
		if (isInterceptorEnabled(invocation, logger)) {
			return invokeUnderTrace(invocation, logger);
		}
		else {
			return invocation.proceed();
		}
	}

	/**
	 * 返回适当的 {@code Log} 实例以用于给定的 {@code MethodInvocation}。如果设置了 {@code useDynamicLogger} 标志，则
	 * {@code Log} 实例将用于 {@code MethodInvocation} 的目标类，否则 {@code Log} 将是默认静态记录器。
	 * @param invocation 正在跟踪的 {@code MethodInvocation}
	 * @return 要使用的 {@code Log} 实例
	 * @see #setUseDynamicLogger
	 */
	protected Log getLoggerForInvocation(MethodInvocation invocation) {
		if (this.defaultLogger != null) {
			return this.defaultLogger;
		}
		else {
			Object target = invocation.getThis();
			Assert.state(target != null, "Target must not be null");
			return LogFactory.getLog(getClassForLogging(target));
		}
	}

	/**
	 * 确定用于日志记录目的的类。
	 * @param target 内省的目标对象
	 * @return 给定对象的目标类
	 * @see #setHideProxyClassNames
	 */
	protected Class<?> getClassForLogging(Object target) {
		return (this.hideProxyClassNames ? AopUtils.getTargetClass(target) : target.getClass());
	}

	/**
	 * 判断是否应该启动拦截器，即是否应该调用{@code invokeUnderTrace}方法。 <p>Default 行为是检查给定的 {@code Log} 实例是否已启用。子
	 * 类也可以重写它以在其他情况下应用拦截器。
	 * @param invocation 正在跟踪的 {@code MethodInvocation}
	 * @param logger 要检查的 {@code Log} 实例
	 * @see #invokeUnderTrace
	 * @see #isLogEnabled
	 */
	protected boolean isInterceptorEnabled(MethodInvocation invocation, Log logger) {
		return isLogEnabled(logger);
	}

	/**
	 * 确定是否启用给定的 {@link Log} 实例。当启用“跟踪”级别时，<p>Default 为 {@code true}。子类可以覆盖它以更改“跟踪”发生的级别。
	 * @param logger 要检查的 {@code Log} 实例
	 */
	protected boolean isLogEnabled(Log logger) {
		return logger.isTraceEnabled();
	}

	/**
	 * 将提供的跟踪消息写入提供的 {@code Log} 实例。 <p> 由 {@link #invokeUnderTrace} 调用以获取进入/退出消息。 <p>D委托
	 * {@link #writeToLog(Log, String, Throwable)} 作为控制底层记录器调用的最终委托。
	 * @since 4.3.10
	 * @see #writeToLog(Log, String, Throwable)
	 */
	protected void writeToLog(Log logger, String message) {
		writeToLog(logger, message, null);
	}

	/**
	 * 将提供的跟踪消息和 {@link Throwable} 写入提供的 {@code Log} 实例。 <p> 由 {@link #invokeUnderTrace}
	 * 调用以获取进入/退出结果，可能包括异常。请注意，当 {@link #setLogExceptionStackTrace} 为“false”时，不会记录异常的堆栈跟踪。
	 * <p>默认消息是在 {@code TRACE} 级别写入的。子类可以重写此方法来控制消息写入的级别，通常也会相应地重写 {@link #isLogEnabled}。
	 * @since 4.3.10
	 * @see #setLogExceptionStackTrace
	 * @see #isLogEnabled
	 */
	protected void writeToLog(Log logger, String message, @Nullable Throwable ex) {
		if (ex != null && this.logExceptionStackTrace) {
			logger.trace(message, ex);
		}
		else {
			logger.trace(message);
		}
	}


	/**
	 * 子类必须重写此方法才能对提供的 {@code MethodInvocation} 执行任何跟踪。子类负责通过调用 {@code
	 * MethodInvocation.proceed()} 确保 {@code MethodInvocation} 实际执行。 <p> 默认情况下，传入的 {@code Log}
	 * 实例将启用日志级别“trace”。子类不必再次检查这一点，除非它们覆盖 {@code isInterceptorEnabled} 方法来修改默认行为，并且可以委托 {@code
	 * writeToLog} 来写入实际消息。
	 * @param logger 用于写入跟踪消息的 {@code Log}
	 * @return 调用 {@code MethodInvocation.proceed()} 的结果
	 * @throws Throwable 如果对 {@code MethodInvocation.proceed()} 的调用遇到任何错误
	 * @see #isLogEnabled
	 * @see #writeToLog(Log, String)
	 * @see #writeToLog(Log, String, Throwable)
	 */
	protected abstract @Nullable Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable;

}
