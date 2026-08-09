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

package org.springframework.boot.logging.logback;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.logging.StackTracePrinter;
import org.springframework.util.Assert;

/**
 * 从 {@link ILoggingEvent} 提取消息与堆栈跟踪等条目的工具类。
 * 优先使用 {@link StackTracePrinter}，否则回退到 {@link ThrowableProxyConverter}。
 *
 * @author Phillip Webb
 */
class Extractor {

	private final @Nullable StackTracePrinter stackTracePrinter;

	private final ThrowableProxyConverter throwableProxyConverter;

	Extractor(@Nullable StackTracePrinter stackTracePrinter, ThrowableProxyConverter throwableProxyConverter) {
		this.stackTracePrinter = stackTracePrinter;
		this.throwableProxyConverter = throwableProxyConverter;
	}

	String messageAndStackTrace(ILoggingEvent event) {
		return event.getFormattedMessage() + "\n\n" + stackTrace(event);
	}

	String stackTrace(ILoggingEvent event) {
		if (this.stackTracePrinter != null) {
			IThrowableProxy throwableProxy = event.getThrowableProxy();
			Assert.state(throwableProxy instanceof ThrowableProxy,
					"Instance must be a ThrowableProxy in order to print exception");
			Throwable throwable = ((ThrowableProxy) throwableProxy).getThrowable();
			return this.stackTracePrinter.printStackTraceToString(throwable);
		}
		return this.throwableProxyConverter.convert(event);
	}

}
