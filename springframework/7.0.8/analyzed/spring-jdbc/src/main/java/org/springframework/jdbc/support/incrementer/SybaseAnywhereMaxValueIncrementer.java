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
 * 递增给定 Sybase 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。
 * 注意：使用本类时，表主键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键值。
 *
 * <p>本类适用于 Sybase Anywhere。
 *
 * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。
 *
 * <p>示例：
 *
 * <pre class="code">create table tab (id int not null primary key, text varchar(100))
 * create table tab_sequence (id bigint identity)
 * insert into tab_sequence values(DEFAULT)</pre>
 *
 * 若设置了 "cacheSize"，中间值将不经查询数据库直接分配。
 * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。
 * 因此编号中可能出现的最大空洞大小等于 cacheSize 的值。
 *
 * <b>HINT:</b> 由于 Sybase Anywhere 支持 JDBC {@code getGeneratedKeys} 方法，
 * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate} 的
 * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时
 * 使用 {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} 或
 * {@link org.springframework.jdbc.support.KeyHolder}。
 *
 * <p>感谢 Tarald Saxi Stormark 的建议！
 *
 * @author Thomas Risberg
 * @since 3.0.5
 */
public class SybaseAnywhereMaxValueIncrementer extends SybaseMaxValueIncrementer {

	/**
	 * 允许作为 JavaBean 使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public SybaseAnywhereMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 * @param columnName 序列表中要使用的列名
	 */
	public SybaseAnywhereMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " values(DEFAULT)";
	}

}
