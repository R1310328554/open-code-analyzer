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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

/**
 * 简单的 {@link ProblemReporter} 实现，遇到错误时采用快速失败行为。
 *
 * <p>遇到的第一个错误将导致抛出 {@link BeanDefinitionParsingException}。
 *
 * <p>警告写入本类的 {@link #setLogger(org.apache.commons.logging.Log) 日志}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public class FailFastProblemReporter implements ProblemReporter {

	/** 用于记录警告的日志。 */
	private Log logger = LogFactory.getLog(getClass());


	/**
	 * 设置用于报告警告的 {@link Log} 日志。
	 * <p>若设为 {@code null}，则使用以实例类名命名的默认 {@link Log} 日志。
	 * @param logger 用于报告警告的 {@link Log} 日志
	 */
	public void setLogger(@Nullable Log logger) {
		this.logger = (logger != null ? logger : LogFactory.getLog(getClass()));
	}


	/**
	 * 抛出 {@link BeanDefinitionParsingException}，详述已发生的错误。
	 * @param problem 错误来源
	 */
	@Override
	public void fatal(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	/**
	 * 抛出 {@link BeanDefinitionParsingException}，详述已发生的错误。
	 * @param problem 错误来源
	 */
	@Override
	public void error(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	/**
	 * 以 {@code WARN} 级别将提供的 {@link Problem} 写入 {@link Log}。
	 * @param problem 警告来源
	 */
	@Override
	public void warning(Problem problem) {
		logger.warn(problem, problem.getRootCause());
	}

}
