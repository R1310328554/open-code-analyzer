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
 * 用于跟踪的 {@code MethodInterceptor} 基类实现。
 *
 * <p>默认情况下，日志消息写入拦截器类的日志，
 * 而非被拦截类的日志。将 {@code useDynamicLogger} Bean 属性
 * 设为 {@code true} 时，所有日志消息写入被拦截目标类的 {@code Log}。
 *
 * <p>子类必须实现 {@code invokeUnderTrace} 方法，
 * 本类仅在特定调用应被跟踪时才调用它。
 * 子类应写入提供的 {@code Log} 实例。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setUseDynamicLogger
 * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)
 */
@SuppressWarnings("serial")
public abstract class AbstractTraceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * 用于写入跟踪消息的默认 {@code Log} 实例。
	 * 该实例映射到实现 {@code Class}。
	 */
	protected transient @Nullable Log defaultLogger = LogFactory.getLog(getClass());

	/**
	 * 使用动态 Logger 时是否隐藏代理类名。
	 * @see #setUseDynamicLogger
	 */
	private boolean hideProxyClassNames = false;

	/**
	 * 是否将异常传递给 Logger。
	 * @see #writeToLog(Log, String, Throwable)
	 */
	private boolean logExceptionStackTrace = true;


	/**
	 * 设置使用动态 Logger 还是静态 Logger。
	 * 默认为本跟踪拦截器的静态 Logger。
	 * <p>用于确定特定方法调用应使用哪个 {@code Log} 实例写入日志：
	 * 被调用 {@code Class} 的动态 Logger，或跟踪拦截器 {@code Class} 的静态 Logger。
	 * <p><b>注意：</b>请指定此属性或 "loggerName" 之一，不可同时指定。
	 * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)
	 */
	public void setUseDynamicLogger(boolean useDynamicLogger) {
		// Release default logger if it is not being used.
		this.defaultLogger = (useDynamicLogger ? null : LogFactory.getLog(getClass()));
	}

	/**
	 * 设置要使用的 Logger 名称。名称通过 Commons Logging 传递给底层 Logger 实现，
	 * 根据 Logger 配置解释为日志类别。
	 * <p>可指定不写入类类别（无论是本拦截器类还是被调用类），
	 * 而是写入特定命名类别。
	 * <p><b>注意：</b>请指定此属性或 "useDynamicLogger" 之一，不可同时指定。
	 * @see org.apache.commons.logging.LogFactory#getLog(String)
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public void setLoggerName(String loggerName) {
		this.defaultLogger = LogFactory.getLog(loggerName);
	}

	/**
	 * 设为 "true" 时，{@link #setUseDynamicLogger 动态 Logger} 尽可能隐藏代理类名。
	 * 默认为 "false"。
	 */
	public void setHideProxyClassNames(boolean hideProxyClassNames) {
		this.hideProxyClassNames = hideProxyClassNames;
	}

	/**
	 * 设置是否将异常传递给 Logger，建议将其堆栈跟踪写入日志。
	 * 默认为 "true"；设为 "false" 可将日志输出缩减为仅跟踪消息
	 * （可能包含异常类名和异常消息）。
	 * @since 4.3.10
	 */
	public void setLogExceptionStackTrace(boolean logExceptionStackTrace) {
		this.logExceptionStackTrace = logExceptionStackTrace;
	}


	/**
	 * 判断特定 {@code MethodInvocation} 是否启用日志。
	 * 若否，方法调用正常进行；否则将方法调用传递给 {@code invokeUnderTrace} 处理。
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
	 * 返回给定 {@code MethodInvocation} 应使用的合适 {@code Log} 实例。
	 * 若设置了 {@code useDynamicLogger} 标志，{@code Log} 实例
	 * 对应 {@code MethodInvocation} 的目标类；否则为默认静态 Logger。
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
	 * 确定用于日志目的的类。
	 * @param target 要内省的目标对象
	 * @return 给定对象的目标类
	 * @see #setHideProxyClassNames
	 */
	protected Class<?> getClassForLogging(Object target) {
		return (this.hideProxyClassNames ? AopUtils.getTargetClass(target) : target.getClass());
	}

	/**
	 * 判断拦截器是否应生效，即是否应调用 {@code invokeUnderTrace} 方法。
	 * <p>默认行为是检查给定 {@code Log} 实例是否启用。
	 * 子类可覆盖以在其他情况下也应用拦截器。
	 * @param invocation 正在跟踪的 {@code MethodInvocation}
	 * @param logger 要检查的 {@code Log} 实例
	 * @see #invokeUnderTrace
	 * @see #isLogEnabled
	 */
	protected boolean isInterceptorEnabled(MethodInvocation invocation, Log logger) {
		return isLogEnabled(logger);
	}

	/**
	 * 判断给定 {@link Log} 实例是否启用。
	 * <p>默认在 "trace" 级别启用时为 {@code true}。
	 * 子类可覆盖以更改发生「跟踪」的级别。
	 * @param logger 要检查的 {@code Log} 实例
	 */
	protected boolean isLogEnabled(Log logger) {
		return logger.isTraceEnabled();
	}

	/**
	 * 将提供的跟踪消息写入提供的 {@code Log} 实例。
	 * <p>由 {@link #invokeUnderTrace} 调用以处理进入/退出消息。
	 * <p>委托给 {@link #writeToLog(Log, String, Throwable)} 作为
	 * 控制底层 Logger 调用的最终委托。
	 * @since 4.3.10
	 * @see #writeToLog(Log, String, Throwable)
	 */
	protected void writeToLog(Log logger, String message) {
		writeToLog(logger, message, null);
	}

	/**
	 * 将提供的跟踪消息和 {@link Throwable} 写入提供的 {@code Log} 实例。
	 * <p>由 {@link #invokeUnderTrace} 调用以处理进入/退出结果，
	 * 可能包含异常。注意当 {@link #setLogExceptionStackTrace} 为 "false" 时
	 * 不会记录异常堆栈跟踪。
	 * <p>默认以 {@code TRACE} 级别写入消息。子类可覆盖以控制写入级别，
	 * 通常也相应覆盖 {@link #isLogEnabled}。
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
	 * 子类必须覆盖本方法以对提供的 {@code MethodInvocation} 执行跟踪。
	 * 子类负责通过调用 {@code MethodInvocation.proceed()} 确保
	 * {@code MethodInvocation} 实际执行。
	 * <p>默认情况下，传入的 {@code Log} 实例已启用 "trace" 级别。
	 * 子类无需再次检查，除非覆盖 {@code isInterceptorEnabled} 修改默认行为，
	 * 并可委托 {@code writeToLog} 写入实际消息。
	 * @param logger 写入跟踪消息的 {@code Log}
	 * @return 对 {@code MethodInvocation.proceed()} 调用的结果
	 * @throws Throwable 若 {@code MethodInvocation.proceed()} 调用遇到错误
	 * @see #isLogEnabled
	 * @see #writeToLog(Log, String)
	 * @see #writeToLog(Log, String, Throwable)
	 */
	protected abstract @Nullable Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable;

}
