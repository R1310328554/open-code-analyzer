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

import java.io.PrintStream;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.Environment;

/**
 * 以编程方式输出 Banner 的接口。
 *
 * @author Phillip Webb
 * @author Michael Stummvoll
 * @author Jeremy Rickard
 * @since 1.2.0
 */
@FunctionalInterface
public interface Banner {

	/**
	 * 将 Banner 输出到指定的打印流。
	 *
	 * @param environment Spring 环境
	 * @param sourceClass 应用的源类，或 {@code null}
	 * @param out 输出打印流
	 */
	void printBanner(Environment environment, @Nullable Class<?> sourceClass, PrintStream out);

	/**
	 * 配置 Banner 显示方式的枚举值。
	 */
	enum Mode {

		/**
		 * 禁用 Banner 输出。
		 */
		OFF,

		/**
		 * 将 Banner 输出到 System.out。
		 */
		CONSOLE,

		/**
		 * 将 Banner 输出到日志文件。
		 */
		LOG

	}

}
