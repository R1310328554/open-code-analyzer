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

package org.springframework.jdbc.support.xml;

import java.sql.Connection;
import java.sql.ResultSet;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * 当底层实现不支持 API 所请求的特性时抛出的异常。
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @deprecated 自 6.2 起弃用，推荐直接使用 {@link ResultSet#getSQLXML} 和
 * {@link Connection#createSQLXML()}，必要时结合自定义
 * {@link org.springframework.jdbc.support.SqlValue} 实现
 */
@Deprecated(since = "6.2")
@SuppressWarnings("serial")
public class SqlXmlFeatureNotImplementedException extends InvalidDataAccessApiUsageException {

	/**
	 * 构造 SqlXmlFeatureNotImplementedException。
	 * @param msg 详细消息
	 */
	public SqlXmlFeatureNotImplementedException(String msg) {
		super(msg);
	}

	/**
	 * 构造 SqlXmlFeatureNotImplementedException。
	 * @param msg 详细消息
	 * @param cause 所用数据访问 API 的根因
	 */
	public SqlXmlFeatureNotImplementedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
