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

package org.springframework.context.event;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.ErrorHandler;

/**
 * {@link ApplicationEventMulticaster} 接口的简单实现。
 *
 * <p>将所有事件多播给所有已注册监听器，由监听器自行忽略不感兴趣的事件。
 * 监听器通常会对传入的事件对象执行相应的 {@code instanceof} 检查。
 *
 * <p>默认情况下，所有监听器在调用线程中执行。
 * 这存在恶意监听器阻塞整个应用的风险，但开销最小。
 * 可指定替代的任务执行器，让监听器在不同线程（例如线程池）中执行。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @see #setTaskExecutor
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	/** 异步执行监听器的任务执行器（可选）。 */
	private @Nullable Executor taskExecutor;

	/** 监听器抛出异常时的错误处理器（可选）。 */
	private @Nullable ErrorHandler errorHandler;

	/** 延迟初始化的日志记录器，用于抑制 ClassCastException。 */
	private volatile @Nullable Log lazyLogger;


	/**
	 * 创建新的 SimpleApplicationEventMulticaster。
	 */
	public SimpleApplicationEventMulticaster() {
	}

	/**
	 * 为给定 BeanFactory 创建新的 SimpleApplicationEventMulticaster。
	 */
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}


	/**
	 * 设置用于调用各监听器的自定义执行器（通常为
	 * {@link org.springframework.core.task.TaskExecutor}）。
	 * <p>默认等价于 {@link org.springframework.core.task.SyncTaskExecutor}，
	 * 在调用线程中同步执行所有监听器。
	 * <p>可在此指定异步任务执行器，避免调用方阻塞至所有监听器执行完毕。
	 * 但请注意，异步执行不会参与调用方线程上下文（类加载器、事务上下文），
	 * 除非 TaskExecutor 显式支持。
	 * <p>声明不支持异步执行的 {@link ApplicationListener} 实例
	 * （{@link ApplicationListener#supportsAsyncExecution()}）
	 * 始终在发布事件的原始线程中运行，例如事务同步的
	 * {@link org.springframework.transaction.event.TransactionalApplicationListener}。
	 * @since 2.0
	 * @see org.springframework.core.task.SyncTaskExecutor
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 */
	public void setTaskExecutor(@Nullable Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * 返回本多播器当前使用的任务执行器。
	 * @since 2.0
	 */
	protected @Nullable Executor getTaskExecutor() {
		return this.taskExecutor;
	}

	/**
	 * 设置监听器抛出异常时调用的 {@link ErrorHandler}。
	 * <p>默认无错误处理器：监听器异常会中断当前多播并传播给事件发布方。
	 * 若指定了 {@linkplain #setTaskExecutor 任务执行器}，各监听器异常会传播给执行器，
	 * 但不一定阻止其他监听器继续执行。
	 * <p>可设置捕获并记录异常的 {@link ErrorHandler} 实现（类似
	 * {@link org.springframework.scheduling.support.TaskUtils#LOG_AND_SUPPRESS_ERROR_HANDLER}），
	 * 或记录异常同时仍传播的实现（例如
	 * {@link org.springframework.scheduling.support.TaskUtils#LOG_AND_PROPAGATE_ERROR_HANDLER}）。
	 * @since 4.1
	 */
	public void setErrorHandler(@Nullable ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * 返回本多播器当前使用的错误处理器。
	 * @since 4.1
	 */
	protected @Nullable ErrorHandler getErrorHandler() {
		return this.errorHandler;
	}

	@Override
	public void multicastEvent(ApplicationEvent event) {
		multicastEvent(event, null);
	}

	@Override
	public void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
		Executor executor = getTaskExecutor();
		for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			if (executor != null && listener.supportsAsyncExecution()) {
				try {
					executor.execute(() -> invokeListener(listener, event));
				}
				catch (RejectedExecutionException ex) {
					// 可能正在关闭——改为在本地线程调用监听器
					invokeListener(listener, event);
				}
			}
			else {
				invokeListener(listener, event);
			}
		}
	}

	/**
	 * 使用给定事件调用监听器。
	 * @param listener 要调用的 ApplicationListener
	 * @param event 当前要传播的事件
	 * @since 4.1
	 */
	protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		ErrorHandler errorHandler = getErrorHandler();
		if (errorHandler != null) {
			try {
				doInvokeListener(listener, event);
			}
			catch (Throwable err) {
				errorHandler.handleError(err);
			}
		}
		else {
			doInvokeListener(listener, event);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
			listener.onApplicationEvent(event);
		}
		catch (ClassCastException ex) {
			String msg = ex.getMessage();
			if (msg == null || matchesClassCastMessage(msg, event.getClass()) ||
					(event instanceof PayloadApplicationEvent payloadEvent &&
							matchesClassCastMessage(msg, payloadEvent.getPayload().getClass()))) {
				// 可能是无法解析泛型事件类型的 lambda 监听器——抑制该异常。
				Log loggerToUse = this.lazyLogger;
				if (loggerToUse == null) {
					loggerToUse = LogFactory.getLog(getClass());
					this.lazyLogger = loggerToUse;
				}
				if (loggerToUse.isTraceEnabled()) {
					loggerToUse.trace("Non-matching event type for listener: " + listener, ex);
				}
			}
			else {
				throw ex;
			}
		}
	}

	private boolean matchesClassCastMessage(String classCastMessage, Class<?> eventClass) {
		// Java 8 上消息以类名开头："java.lang.String cannot be cast..."
		if (classCastMessage.startsWith(eventClass.getName())) {
			return true;
		}
		// Java 11 上消息以 "class ..." 开头，即 Class.toString()
		if (classCastMessage.startsWith(eventClass.toString())) {
			return true;
		}
		// Java 9 上消息曾包含模块名："java.base/java.lang.String cannot be cast..."
		int moduleSeparatorIndex = classCastMessage.indexOf('/');
		if (moduleSeparatorIndex != -1 && classCastMessage.startsWith(eventClass.getName(), moduleSeparatorIndex + 1)) {
			return true;
		}
		// 假定是与事件无关的类型转换失败...
		return false;
	}

}
