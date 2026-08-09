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
 * {@link DataFieldMaxValueIncrementer} 检索给定 HSQL 序列的下一个值。
 * <p>感谢 Guillaume Bilodeau 的建议！
 * <p><b>NOTE:</b> 这是使用常规表的替代方法来支持生成以前版本的 HSQL 中必需的唯一键。
 * @author Thomas Risberg
 * @since 2.5
 * @see HsqlMaxValueIncrementer
 */
public class HsqlSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 */
	public HsqlSequenceMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 */
	public HsqlSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		super(dataSource, incrementerName);
	}


	/**
	 * 获取 Sequence Query（`SequenceQuery`）。
	 */
	@Override
	protected String getSequenceQuery() {
		return "call next value for " + getIncrementerName();
	}

}
