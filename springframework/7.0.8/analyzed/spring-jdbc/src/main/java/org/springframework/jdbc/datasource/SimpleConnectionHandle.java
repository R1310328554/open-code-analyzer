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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

import org.springframework.util.Assert;

/**
 * {@link ConnectionHandle} 接口的简单实现，包含给定的 JDBC 连接。
 * @author Juergen Hoeller
 * @since 1.1
 */
public class SimpleConnectionHandle implements ConnectionHandle {

	/** 连接相关状态（`connection`）。 */
	private final Connection connection;


	/**
	 * 为给定的 Connection 创建一个新的 SimpleConnectionHandle。
	 * @param connection JDBC 连接
	 */
	public SimpleConnectionHandle(Connection connection) {
		Assert.notNull(connection, "Connection must not be null");
		this.connection = connection;
	}

	/**
	 * 按原样返回指定的连接。
	 */
	@Override
	public Connection getConnection() {
		return this.connection;
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "SimpleConnectionHandle: " + this.connection;
	}

}
