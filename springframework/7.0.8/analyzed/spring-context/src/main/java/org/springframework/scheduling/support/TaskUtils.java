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

package org.springframework.scheduling.support;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ErrorHandler;
import org.springframework.util.ReflectionUtils;

/**
 * 为任务装饰错误处理逻辑的实用方法。
 *
 * <p><b>注意：</b>本类供 Spring 调度器实现内部使用。
 * 仅因其他包中的实现类需要访问而公开，<i>不</i>面向一般用途。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
public abstract class TaskUtils {

	/**
	 * 记录异常但不作进一步处理的 ErrorHandler 策略。
	 * 会抑制错误，以免阻止任务的后续执行。
	 */
	public static final ErrorHandler LOG_AND_SUPPRESS_ERROR_HANDLER = new LoggingErrorHandler();

	/**
	 * 以 error 级别记录并重新抛出异常的 ErrorHandler 策略。
	 * 注意：这通常会阻止调度任务的后续执行。
	 */
	public static final ErrorHandler LOG_AND_PROPAGATE_ERROR_HANDLER = new PropagatingErrorHandler();


	/**
	 * 为任务装饰错误处理。若提供的 {@link ErrorHandler} 非 {@code null} 则使用之；
	 * 否则重复任务默认抑制错误，一次性任务默认传播错误
	 *（因其错误可能通过返回的 {@link Future} 被预期）。两种情况下均会记录错误。
	 */
	public static DelegatingErrorHandlingRunnable decorateTaskWithErrorHandler(
			Runnable task, @Nullable ErrorHandler errorHandler, boolean isRepeatingTask) {

		if (task instanceof DelegatingErrorHandlingRunnable dehRunnable) {
			return dehRunnable;
		}
		ErrorHandler eh = (errorHandler != null ? errorHandler : getDefaultErrorHandler(isRepeatingTask));
		return new DelegatingErrorHandlingRunnable(task, eh);
	}

	/**
	 * 根据任务是否重复，返回默认 {@link ErrorHandler} 实现。
	 * 重复任务抑制错误，一次性任务传播错误；两种情况下均会记录错误。
	 */
	public static ErrorHandler getDefaultErrorHandler(boolean isRepeatingTask) {
		return (isRepeatingTask ? LOG_AND_SUPPRESS_ERROR_HANDLER : LOG_AND_PROPAGATE_ERROR_HANDLER);
	}


	/**
	 * 在 error 级别记录 Throwable 的 {@link ErrorHandler} 实现，
 	 * 不作额外错误处理。适用于有意抑制错误的场景。
	 */
	private static class LoggingErrorHandler implements ErrorHandler {

		private final Log logger = LogFactory.getLog(LoggingErrorHandler.class);

		@Override
		public void handleError(Throwable t) {
			logger.error("Unexpected error occurred in scheduled task", t);
		}
	}


	/**
	 * 在 error 级别记录 Throwable 并传播的 {@link ErrorHandler} 实现。
	 */
	private static class PropagatingErrorHandler extends LoggingErrorHandler {

		@Override
		public void handleError(Throwable t) {
			super.handleError(t);
			ReflectionUtils.rethrowRuntimeException(t);
		}
	}

}
