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

package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * 递增给定 MySQL 表自增列最大计数值的 {@link DataFieldMaxValueIncrementer}。
 *
 * <p>序列保存在一张表中。MySQL 8.0 及更高版本中，序列表必须使用 InnoDB 存储引擎，
 * 因为当前最大自增计数器需要在数据库服务器重启后持久保留。
 *
 * <p>示例：
 *
 * <pre class="code">
 * create table tab_sequence (`id` bigint unsigned primary key auto_increment);</pre>
 *
 * <p>若设置了 {@code cacheSize}，中间值将不经查询数据库直接分配。
 * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。
 * 因此编号中可能出现的最大空洞大小等于 {@code cacheSize} 的值。
 *
 * @author Henning Pöttker
 * @since 6.1.2
 */
public class MySQLIdentityColumnMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/**
	 * 允许作为 JavaBean 使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public MySQLIdentityColumnMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列表名
	 * @param columnName 序列表中要使用的列名
	 */
	public MySQLIdentityColumnMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}

	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " () values ()";
	}

	@Override
	protected String getIdentityStatement() {
		return "select last_insert_id()";
	}

}
