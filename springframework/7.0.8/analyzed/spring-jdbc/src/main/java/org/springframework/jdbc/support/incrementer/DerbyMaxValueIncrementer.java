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
 * {@link DataFieldMaxValueIncrementer} 增加给定 Derby 表的最大值，相当于自动增量列。注意：如果您使用此类，您的 Derby 键列应将
 * <i>NOT</i> 定义为 IDENTITY 列，因为序列表会完成此工作。
 * <p>序列保存在一个表中。每个表应该有一个需要自动生成键的序列表。
 * <p>Derby 需要一个附加列用于插入，因为不可能将 null 插入到标识列中并生成值。这是通过提供也必须在序列表中创建的虚拟列的名称来解决的。
 * <p>示例：
 * <pre class="code">创建表选项卡（id int不为空主键，text varchar（100））；创建表 tab_sequence （始终作为标识生成的值
 * int，虚拟 char(1)）；插入 tab_sequence（虚拟）值（空）；</pre>
 * 如果设置了“cacheSize”，则无需查询数据库即可提供中间值。如果服务器或您的应用程序停止或崩溃或事务回滚，则永远不会提供未使用的值。因此，编号中的最大空洞大小就是cach
 * eSize 的值。
 * <b>HINT:</b> 由于 Derby 支持 JDBC {@code getGeneratedKeys} 方法，因此建议直接在表中使用 IDENTITY 列，然后在调用
 * {@link org.springframework.jdbc.core.JdbcTemplate} 的 {@code
 * update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时使用 {@link
 * org.springframework.jdbc.support.KeyHolder}。
 * <p>感谢 Endre Stolsvik 的建议！
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class DerbyMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/**
	 */
	private static final String DEFAULT_DUMMY_NAME = "dummy";

	/**
	 */
	private String dummyName = DEFAULT_DUMMY_NAME;


	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public DerbyMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 * @param columnName 序列表中要使用的列的名称
	 */
	public DerbyMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
		this.dummyName = DEFAULT_DUMMY_NAME;
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 * @param columnName 序列表中要使用的列的名称
	 * @param dummyName 用于插入的虚拟列的名称
	 */
	public DerbyMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName, String dummyName) {
		super(dataSource, incrementerName, columnName);
		this.dummyName = dummyName;
	}


	/**
	 * 设置虚拟列的名称。
	 */
	public void setDummyName(String dummyName) {
		this.dummyName = dummyName;
	}

	/**
	 * 返回虚拟列的名称。
	 */
	public String getDummyName() {
		return this.dummyName;
	}


	/**
	 * 获取 Increment Statement（`IncrementStatement`）。
	 */
	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " (" + getDummyName() + ") values(null)";
	}

	/**
	 * 获取 Identity Statement（`IdentityStatement`）。
	 */
	@Override
	protected String getIdentityStatement() {
		return "select IDENTITY_VAL_LOCAL() from " + getIncrementerName();
	}

}
