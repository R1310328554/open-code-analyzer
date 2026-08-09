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

package org.springframework.boot.context.config;

import org.apache.commons.logging.Log;

import org.springframework.core.log.LogMessage;

/**
 * 未捕获 {@link ConfigDataNotFoundException} 时采取的操作。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public enum ConfigDataNotFoundAction {

	/**
	 * 抛出异常以使启动失败。
	 */
	FAIL {

		@Override
		void handle(Log logger, ConfigDataNotFoundException ex) {
			throw ex;
		}

	},

	/**
	 * 忽略异常并继续处理剩余位置。
	 */
	IGNORE {

		@Override
		void handle(Log logger, ConfigDataNotFoundException ex) {
			logger.trace(LogMessage.format("Ignoring missing config data %s", ex.getReferenceDescription()));
		}

	};

	/**
	 * 处理给定异常。
	 *
	 * @param logger 用于输出的日志记录器
	 * @param ex 待处理的异常
	 */
	abstract void handle(Log logger, ConfigDataNotFoundException ex);

}
