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

package org.springframework.jdbc.support;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

/**
 * 用于保存特定数据库的自定义 JDBC 错误代码转换的 JavaBean。 “exceptionClass”属性定义对于 errorCodes 属性中指定的错误代码列表将引发哪个
 * 异常。
 * @author Thomas Risberg
 * @since 1.1
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class CustomSQLErrorCodesTranslation {

	private String[] errorCodes = new String[0];

	/** 异常相关状态（`exceptionClass`）。 */
	private @Nullable Class<?> exceptionClass;


	/**
	 * 设置匹配的 SQL 错误代码。
	 */
	public void setErrorCodes(String... errorCodes) {
		this.errorCodes = StringUtils.sortStringArray(errorCodes);
	}

	/**
	 * 返回要匹配的 SQL 错误代码。
	 */
	public String[] getErrorCodes() {
		return this.errorCodes;
	}

	/**
	 * 设置指定错误代码的异常类。
	 */
	public void setExceptionClass(@Nullable Class<?> exceptionClass) {
		if (exceptionClass != null && !DataAccessException.class.isAssignableFrom(exceptionClass)) {
			throw new IllegalArgumentException("Invalid exception class [" + exceptionClass +
					"]: needs to be a subclass of [org.springframework.dao.DataAccessException]");
		}
		this.exceptionClass = exceptionClass;
	}

	/**
	 * 返回指定错误代码的异常类。
	 */
	public @Nullable Class<?> getExceptionClass() {
		return this.exceptionClass;
	}

}
