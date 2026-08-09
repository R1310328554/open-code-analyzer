/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * {@link UncaughtExceptionHandler}，用于抑制已记录异常的重复处理，并在需要时触发系统退出。
 * <p>
 * 通过线程本地附加到当前线程，跟踪 {@link SpringApplication} 已记录的异常与退出码。
 *
 * @author Phillip Webb
 */
class SpringBootExceptionHandler implements UncaughtExceptionHandler {

	private static final Set<String> LOG_CONFIGURATION_MESSAGES;

	static {
		Set<String> messages = new HashSet<>();
		messages.add("Logback configuration error detected");
		LOG_CONFIGURATION_MESSAGES = Collections.unmodifiableSet(messages);
	}

	private static final LoggedExceptionHandlerThreadLocal handler = new LoggedExceptionHandlerThreadLocal();

	private final @Nullable UncaughtExceptionHandler parent;

	private final List<Throwable> loggedExceptions = new ArrayList<>();

	private int exitCode;

	SpringBootExceptionHandler(@Nullable UncaughtExceptionHandler parent) {
		this.parent = parent;
	}

	void registerLoggedException(Throwable exception) {
		this.loggedExceptions.add(exception);
	}

	void registerExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			if (isPassedToParent(ex) && this.parent != null) {
				this.parent.uncaughtException(thread, ex);
			}
		}
		finally {
			this.loggedExceptions.clear();
			if (this.exitCode != 0) {
				System.exit(this.exitCode);
			}
		}
	}

	private boolean isPassedToParent(Throwable ex) {
		return isLogConfigurationMessage(ex) || !isRegistered(ex);
	}

	/**
	 * 判断异常是否为日志配置错误消息（日志调用可能未实际输出任何内容）。
	 *
	 * @param ex 源异常
	 * @return 若异常包含日志配置消息则为 {@code true}
	 */
	private boolean isLogConfigurationMessage(@Nullable Throwable ex) {
		if (ex == null) {
			return false;
		}
		if (ex instanceof InvocationTargetException) {
			return isLogConfigurationMessage(ex.getCause());
		}
		String message = ex.getMessage();
		if (message != null) {
			for (String candidate : LOG_CONFIGURATION_MESSAGES) {
				if (message.contains(candidate)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isRegistered(@Nullable Throwable ex) {
		if (ex == null) {
			return false;
		}
		if (this.loggedExceptions.contains(ex)) {
			return true;
		}
		if (ex instanceof InvocationTargetException) {
			return isRegistered(ex.getCause());
		}
		return false;
	}

	static SpringBootExceptionHandler forCurrentThread() {
		return handler.get();
	}

	/**
	 * 用于附加并跟踪处理器的线程本地变量。
	 */
	private static final class LoggedExceptionHandlerThreadLocal extends ThreadLocal<SpringBootExceptionHandler> {

		@Override
		protected SpringBootExceptionHandler initialValue() {
			SpringBootExceptionHandler handler = new SpringBootExceptionHandler(
					Thread.currentThread().getUncaughtExceptionHandler());
			Thread.currentThread().setUncaughtExceptionHandler(handler);
			return handler;
		}

	}

}
