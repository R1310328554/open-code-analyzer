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

package org.springframework.boot.logging;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 可用于打印 {@link Throwable} 堆栈跟踪的接口。
 *
 * @author Phillip Webb
 * @since 3.5.0
 * @see StandardStackTracePrinter
 */
@FunctionalInterface
public interface StackTracePrinter {

	/**
	 * 返回给定 {@link Throwable} 的堆栈跟踪字符串。
	 *
	 * @param throwable 需要打印堆栈跟踪的 throwable
	 * @return 堆栈跟踪字符串
	 */
	default String printStackTraceToString(Throwable throwable) {
		try {
			StringBuilder out = new StringBuilder(4096);
			printStackTrace(throwable, out);
			return out.toString();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * 打印给定 {@link Throwable} 的堆栈跟踪。
	 *
	 * @param throwable 需要打印堆栈跟踪的 throwable
	 * @param out 输出目标
	 * @throws IOException IO 错误时抛出
	 */
	void printStackTrace(Throwable throwable, Appendable out) throws IOException;

}
