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
 * {@link DataFieldMaxValueIncrementer} 增加给定 MySQL 表的自动增量列的最大计数器值。
 * <p>序列保存在一个表中。 MySQL 8.0及以上版本中，序列表使用的存储引擎必须是InnoDB，因为数据库服务器重启后需要保留当前的最大自增计数器。
 * <p>示例：
 * <pre class="code"> 创建表 tab_sequence (`id` bigint 无符号主键 auto_increment);</pre>
 * <p> 如果设置了 {@code cacheSize}，则无需查询数据库即可提供中间值。如果服务器或您的应用程序停止或崩溃或事务回滚，则永远不会提供未使用的值。因此，编号中的最
 * 大孔尺寸是 {@code cacheSize} 的值。
 * @author Henning Pöttker
 * @since 6.1.2
 */
public class MySQLIdentityColumnMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public MySQLIdentityColumnMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列表的名称
	 * @param columnName 序列表中要使用的列的名称
	 */
	public MySQLIdentityColumnMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}

	/**
	 * 获取 Increment Statement（`IncrementStatement`）。
	 */
	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " () values ()";
	}

	/**
	 * 获取 Identity Statement（`IdentityStatement`）。
	 */
	@Override
	protected String getIdentityStatement() {
		return "select last_insert_id()";
	}

}
