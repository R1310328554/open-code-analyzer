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

package org.springframework.jdbc;

import java.io.IOException;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * 无法检索 LOB 时抛出的异常。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @deprecated 自 6.2 起随 {@link org.springframework.jdbc.support.lob.LobHandler} 一并弃用
 */
@Deprecated(since = "6.2")
@SuppressWarnings("serial")
public class LobRetrievalFailureException extends DataRetrievalFailureException {

	/**
	 * LobRetrievalFailureException 构造器。
	 * @param msg 详细消息
	 */
	public LobRetrievalFailureException(String msg) {
		super(msg);
	}

	/**
	 * LobRetrievalFailureException 构造器。
	 * @param msg 详细消息
	 * @param ex 根因 IOException
	 */
	public LobRetrievalFailureException(String msg, IOException ex) {
		super(msg, ex);
	}

}
