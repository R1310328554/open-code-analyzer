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
 * 递增给定 HSQL 表最大值的 {@link DataFieldMaxValueIncrementer}，等效于自增列。
 * 注意：使用本类时，HSQL 键列<i>不应</i>设为 auto-increment，序列表负责生成键。
 *
 * <p>序列保存在表中，每个需要自动生成键的表应有一个序列表。
 *
 * <p>示例：
 *
 * <pre class="code">create table tab (id int not null primary key, text varchar(100));
 * create table tab_sequence (value identity);
 * insert into tab_sequence values(0);</pre>
 *
 * 若设置 "cacheSize"，中间值无需查询数据库即可提供。
 * 若服务器或应用停止、崩溃或事务回滚，未使用的值将永远不会被使用，
 * 因此编号最大空洞即为 cacheSize 的值。
 *
 * <p><b>注意：</b>HSQL 现已支持序列，建议改用 {@link HsqlSequenceMaxValueIncrementer}。
 *
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see HsqlSequenceMaxValueIncrementer
 */
public class HsqlMaxValueIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	public HsqlMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 * @param columnName 序列表中要使用的列名
	 */
	public HsqlMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}


	@Override
	protected String getIncrementStatement() {
		return "insert into " + getIncrementerName() + " values(null)";
	}

	@Override
	protected String getIdentityStatement() {
		return "select max(identity()) from " + getIncrementerName();
	}

}
