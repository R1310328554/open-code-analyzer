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

import org.springframework.dao.DataAccessException;

/**
 * 定义增加任何数据存储字段的最大值的契约的接口。工作原理很像序列号生成器。
 * <p>典型的实现可以使用标准 SQL、本机 RDBMS 序列或存储过程来完成这项工作。
 * @author Dmitriy Kopylenko
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public interface DataFieldMaxValueIncrementer {

	/**
	 * 将数据存储字段的最大值递增为 int。
	 * @return 下一个数据存储值，例如 <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException 如果出现错误
	 */
	int nextIntValue() throws DataAccessException;

	/**
	 * 增加数据存储字段的最大值。
	 * @return 下一个数据存储值，例如 <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException 如果出现错误
	 */
	long nextLongValue() throws DataAccessException;

	/**
	 * 以字符串形式增加数据存储字段的最大值。
	 * @return 数据存储值，例如 <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException 如果出现错误
	 */
	String nextStringValue() throws DataAccessException;

}
