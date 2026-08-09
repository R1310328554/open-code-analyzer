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
 * 在 DB2 LUW（Linux、Unix 和 Windows）上获取给定序列下一值的 {@link DataFieldMaxValueIncrementer}。
 *
 * <p>感谢 Mark MacMahon 的建议！
 *
 * @author Juergen Hoeller
 * @since 4.3.15
 * @see Db2MainframeMaxValueIncrementer
 */
public class Db2LuwMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 */
	public Db2LuwMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 */
	public Db2LuwMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		super(dataSource, incrementerName);
	}


	@Override
	protected String getSequenceQuery() {
		return "values nextval for " + getIncrementerName();
	}

}
