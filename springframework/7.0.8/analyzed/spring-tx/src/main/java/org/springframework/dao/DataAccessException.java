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

package org.springframework.dao;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NestedRuntimeException;

/**
 * 数据访问异常层次结构的根类，详见
 * <a href="https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>。
 * 该书第 9 章对本包的设计动机有详细论述。
 *
 * <p>本异常层次结构使用户代码无需了解具体数据访问 API（如 JDBC）的细节，
 * 即可识别并处理所遇错误类型。例如，可在不知底层使用 JDBC 的情况下
 * 响应乐观锁失败。
 *
 * <p>本类为运行时异常；若将任何错误视为致命（通常情况），
 * 用户代码无需捕获本类或其子类。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public abstract class DataAccessException extends NestedRuntimeException {

	/**
	 * DataAccessException 构造函数。
	 * @param msg 详细消息
	 */
	public DataAccessException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * DataAccessException 构造函数。
	 * @param msg 详细消息
	 * @param cause 根因（通常来自底层数据访问 API，如 JDBC）
	 */
	public DataAccessException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}