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

package org.springframework.jdbc.datasource.init;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 用于填充、初始化或清理数据库的策略。
 * @author Keith Donald
 * @author Sam Brannen
 * @since 3.0
 * @see ResourceDatabasePopulator
 * @see DatabasePopulatorUtils
 * @see DataSourceInitializer
 */
@FunctionalInterface
public interface DatabasePopulator {

	/**
	 * 使用提供的 JDBC 连接填充、初始化或清理数据库。 <p><strong>Warning</strong>：具体实现不应关闭提供的 {@link Connection}。
	 * <p>具体实现 <em>可能会在遇到错误时 </em> 抛出 {@link SQLException}，但 <em> 强烈鼓励 </em> 抛出特定的 {@link
	 * ScriptException}。例如Spring的{@link ResourceDatabasePopulator}和{@link
	 * DatabasePopulatorUtils}将所有{@code SQLExceptions}包装在{@code ScriptExceptions}中。
	 * @param connection 要使用的 JDBC 连接；已经配置并可以使用；从来没有 {@code null}
	 * @throws SQLException 如果与数据库交互时发生不可恢复的数据访问异常
	 * @throws ScriptException 在所有其他错误情况下
	 * @see DatabasePopulatorUtils#execute
	 */
	void populate(Connection connection) throws SQLException, ScriptException;

}
