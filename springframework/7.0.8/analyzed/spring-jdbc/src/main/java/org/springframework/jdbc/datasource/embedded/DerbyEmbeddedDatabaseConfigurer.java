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

package org.springframework.jdbc.datasource.embedded;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.jspecify.annotations.Nullable;

/**
 * Apache Derby 数据库的 {@link EmbeddedDatabaseConfigurer}。
 *
 * <p>调用 {@link #getInstance()} 获取本类的单例实例。
 *
 * @author Oliver Gierke
 * @author Juergen Hoeller
 * @since 3.0
 */
final class DerbyEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

	private static final String URL_TEMPLATE = "jdbc:derby:memory:%s;%s";

	private static @Nullable DerbyEmbeddedDatabaseConfigurer instance;


	/**
	 * 获取 {@link DerbyEmbeddedDatabaseConfigurer} 单例实例。
	 * @return 配置器实例
	 */
	public static synchronized DerbyEmbeddedDatabaseConfigurer getInstance() {
		if (instance == null) {
			// 禁用日志文件
			System.setProperty("derby.stream.error.method",
					OutputStreamFactory.class.getName() + ".getNoopOutputStream");
			instance = new DerbyEmbeddedDatabaseConfigurer();
		}
		return instance;
	}


	private DerbyEmbeddedDatabaseConfigurer() {
	}

	@Override
	public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
		properties.setDriverClass(EmbeddedDriver.class);
		properties.setUrl(String.format(URL_TEMPLATE, databaseName, "create=true"));
		properties.setUsername("sa");
		properties.setPassword("");
	}

	@Override
	public void shutdown(DataSource dataSource, String databaseName) {
		try {
			new EmbeddedDriver().connect(
					String.format(URL_TEMPLATE, databaseName, "drop=true"), new Properties());
		}
		catch (SQLException ex) {
			// 表示成功关闭的错误码
			if (!"08006".equals(ex.getSQLState())) {
				LogFactory.getLog(getClass()).warn("Could not shut down embedded Derby database", ex);
			}
		}
	}

}
