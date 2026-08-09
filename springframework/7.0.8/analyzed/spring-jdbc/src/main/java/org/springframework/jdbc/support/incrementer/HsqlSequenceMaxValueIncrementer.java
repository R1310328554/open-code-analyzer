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
 * 检索给定 HSQL 序列下一个值的 {@link DataFieldMaxValueIncrementer}。
 *
 * <p>感谢 Guillaume Bilodeau 的建议！
 *
 * <p><b>NOTE:</b> 这是使用常规表生成唯一键的替代方案，
 * 在旧版 HSQL 中曾需要这种方式。
 *
 * @author Thomas Risberg
 * @since 2.5
 * @see HsqlMaxValueIncrementer
 */
public class HsqlSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * 允许作为 JavaBean 使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 */
	public HsqlSequenceMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 */
	public HsqlSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		super(dataSource, incrementerName);
	}


	@Override
	protected String getSequenceQuery() {
		return "call next value for " + getIncrementerName();
	}

}
