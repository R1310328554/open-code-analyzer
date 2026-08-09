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
 * {@link DataFieldMaxValueIncrementer} 增加给定 SQL Server 表的最大值，相当于自动增量列。注意：如果您使用此类，您的表键列应将
 * <i>NOT</i> 定义为 IDENTITY 列，因为序列表会完成此工作。
 * <p> 该类旨在与 Microsoft SQL Server 一起使用。
 * <p>序列保存在一个表中。每个表应该有一个需要自动生成键的序列表。
 * <p>示例：
 * <pre class="code">创建表tab（id int不为空主键，文本varchar（100））创建表tab_sequence（id
 * bigint标识）插入tab_sequence默认值</pre>
 * 如果设置了“cacheSize”，则无需查询数据库即可提供中间值。如果服务器或您的应用程序停止或崩溃或事务回滚，则永远不会提供未使用的值。因此，编号中的最大空洞大小就是cach
 * eSize 的值。
 * <b>HINT:</b> 由于 Microsoft SQL Server 支持 JDBC {@code getGeneratedKeys} 方法，因此建议直接在表中使用
 * IDENTITY 列，然后在调用 {@link org.springframework.jdbc.core.JdbcTemplate} 的 {@code
 * update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时使用 {@link
 * org.springframework.jdbc.core.simple.SimpleJdbcInsert} 或 {@link
 * org.springframework.jdbc.support.KeyHolder}。
 * <p>感谢 Preben Nilsson 的建议！
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public class SqlServerMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public SqlServerMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 * @param columnName 序列表中要使用的列的名称
	 */
	public SqlServerMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	/**
	 * 获取 Increment Statement（`IncrementStatement`）。
	 */
	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " default values";
	}

	/**
	 * 获取 Identity Statement（`IdentityStatement`）。
	 */
	@Override
	protected String getIdentityStatement() {
		return "select @@identity";
	}

}
