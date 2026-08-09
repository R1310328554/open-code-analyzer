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
 * 递增给定 Derby 表最大值的 {@link DataFieldMaxValueIncrementer}，等效于自增列。
 * 注意：使用本类时，Derby 键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键。
 *
 * <p>序列保存在表中，每个需要自动生成键的表应有一个序列表。
 *
 * <p>Derby 插入时需要额外列，因为无法向 identity 列插入 null 并生成值。
 * 通过在序列表中创建 dummy 列并在插入时使用其名称解决。
 *
 * <p>示例：
 *
 * <pre class="code">create table tab (id int not null primary key, text varchar(100));
 * create table tab_sequence (value int generated always as identity, dummy char(1));
 * insert into tab_sequence (dummy) values(null);</pre>
 *
 * 若设置 "cacheSize"，中间值无需查询数据库即可提供。
 * 若服务器或应用停止、崩溃或事务回滚，未使用的值将永远不会被使用，
 * 因此编号最大空洞即为 cacheSize 的值。
 *
 * <b>提示：</b>Derby 支持 JDBC {@code getGeneratedKeys} 方法，
 * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate}
 * 的 {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 时使用
 * {@link org.springframework.jdbc.support.KeyHolder}。
 *
 * <p>感谢 Endre Stolsvik 的建议！
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class DerbyMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/** dummy 列名的默认值。 */
	private static final String DEFAULT_DUMMY_NAME = "dummy";

	/** 插入时使用的 dummy 列名。 */
	private String dummyName = DEFAULT_DUMMY_NAME;


	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public DerbyMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 * @param columnName 序列表中要使用的列名
	 */
	public DerbyMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
		this.dummyName = DEFAULT_DUMMY_NAME;
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 * @param columnName 序列表中要使用的列名
	 * @param dummyName 插入时使用的 dummy 列名
	 */
	public DerbyMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName, String dummyName) {
		super(dataSource, incrementerName, columnName);
		this.dummyName = dummyName;
	}


	/**
	 * 设置 dummy 列名。
	 */
	public void setDummyName(String dummyName) {
		this.dummyName = dummyName;
	}

	/**
	 * 返回 dummy 列名。
	 */
	public String getDummyName() {
		return this.dummyName;
	}


	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " (" + getDummyName() + ") values(null)";
	}

	@Override
	protected String getIdentityStatement() {
		return "select IDENTITY_VAL_LOCAL() from " + getIncrementerName();
	}

}
