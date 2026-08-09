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

package org.springframework.beans.factory.parsing;

/**
 * SPI 接口，允许工具及其他外部进程处理 Bean 定义解析过程中报告的错误与警告。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Problem
 */
public interface ProblemReporter {

	/**
	 * 解析过程中遇到致命错误时调用。
	 * <p>实现必须将给定问题视为致命，即最终必须抛出异常。
	 * @param problem 错误来源（永不为 {@code null}）
	 */
	void fatal(Problem problem);

	/**
	 * 解析过程中遇到错误时调用。
	 * <p>实现可选择将错误视为致命。
	 * @param problem 错误来源（永不为 {@code null}）
	 */
	void error(Problem problem);

	/**
	 * 解析过程中引发警告时调用。
	 * <p>警告<strong>永不</strong>被视为致命。
	 * @param problem 警告来源（永不为 {@code null}）
	 */
	void warning(Problem problem);

}
