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

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spring 的 {@link javax.sql.DataSource} 实现的抽象基类，负责填充。
 * 此类上下文中的 <p>“Padding”表示 {@code DataSource} 接口中某些方法的默认实现，例如 {@link
 * #getLoginTimeout()}、{@link #setLoginTimeout(int)} 等。
 * @author Juergen Hoeller
 * @since 07.05.2003
 * @see DriverManagerDataSource
 */
public abstract class AbstractDataSource implements DataSource {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());


	/**
	 * 返回0，表示使用默认的系统超时。
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	/**
	 * 不支持设置登录超时。
	 */
	@Override
	public void setLoginTimeout(int timeout) throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	/**
	 * 不支持 LogWriter 方法。
	 */
	@Override
	public PrintWriter getLogWriter() {
		throw new UnsupportedOperationException("getLogWriter");
	}

	/**
	 * 不支持 LogWriter 方法。
	 */
	@Override
	public void setLogWriter(PrintWriter pw) throws SQLException {
		throw new UnsupportedOperationException("setLogWriter");
	}

	/**
	 * 获取 Parent Logger（`ParentLogger`）。
	 */
	@Override
	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	/**
	 * 方法 `unwrap`：完成本类中与「unwrap」相关的职责。
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		throw new SQLException("DataSource of type [" + getClass().getName() +
				"] cannot be unwrapped as [" + iface.getName() + "]");
	}

	/**
	 * 判断是否 Wrapper For。
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

}
