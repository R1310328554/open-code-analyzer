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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.util.Assert;

/**
 * 复合 {@link DatabasePopulator}，委托给给定 {@code DatabasePopulator} 实现的列表，执行所有脚本。
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Kazuki Shimizu
 * @since 3.1
 */
public class CompositeDatabasePopulator implements DatabasePopulator {

	private final List<DatabasePopulator> populators = new ArrayList<>(4);


	/**
	 * 创建一个空的 {@code CompositeDatabasePopulator}。
	 * @see #setPopulators
	 * @see #addPopulators
	 */
	public CompositeDatabasePopulator() {
	}

	/**
	 * 使用给定的填充程序创建 {@code CompositeDatabasePopulator}。
	 * @param populators 委托给一个或多个填充者
	 * @since 4.3
	 */
	public CompositeDatabasePopulator(Collection<DatabasePopulator> populators) {
		Assert.notNull(populators, "DatabasePopulators must not be null");
		this.populators.addAll(populators);
	}

	/**
	 * 使用给定的填充程序创建 {@code CompositeDatabasePopulator}。
	 * @param populators 委托给一个或多个填充者
	 * @since 4.3
	 */
	public CompositeDatabasePopulator(DatabasePopulator... populators) {
		Assert.notNull(populators, "DatabasePopulators must not be null");
		this.populators.addAll(Arrays.asList(populators));
	}


	/**
	 * 指定要委托给的一个或多个填充器。
	 */
	public void setPopulators(DatabasePopulator... populators) {
		Assert.notNull(populators, "DatabasePopulators must not be null");
		this.populators.clear();
		this.populators.addAll(Arrays.asList(populators));
	}

	/**
	 * 将一个或多个填充者添加到代表列表中。
	 */
	public void addPopulators(DatabasePopulator... populators) {
		Assert.notNull(populators, "DatabasePopulators must not be null");
		this.populators.addAll(Arrays.asList(populators));
	}

	/**
	 * 填充（方法 `populate`）。
	 */
	@Override
	public void populate(Connection connection) throws SQLException, ScriptException {
		Assert.notNull(connection, "Connection must not be null");
		for (DatabasePopulator populator : this.populators) {
			populator.populate(connection);
		}
	}

}
