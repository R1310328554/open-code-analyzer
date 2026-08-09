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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * {@link DataFieldMaxValueIncrementer} 的基本实现，委托给返回 {@code long} 的单个 {@link #getNextKey}
 * 模板方法。使用长整型作为字符串值，如果需要则用零填充。
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public abstract class AbstractDataFieldMaxValueIncrementer implements DataFieldMaxValueIncrementer, InitializingBean {

	/** 来源相关状态（`dataSource`）。 */
	@SuppressWarnings("NullAway.Init")
	private DataSource dataSource;

	/**
	 */
	@SuppressWarnings("NullAway.Init")
	private String incrementerName;

	/**
	 */
	protected int paddingLength = 0;


	/**
	 * bean 属性样式使用的默认构造函数。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 */
	public AbstractDataFieldMaxValueIncrementer() {
	}

	/**
	 * 方便构造函数。
	 * @param dataSource 要使用的数据源
	 * @param incrementerName 要使用的序列/表的名称
	 */
	public AbstractDataFieldMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		Assert.notNull(dataSource, "DataSource must not be null");
		Assert.notNull(incrementerName, "Incrementer name must not be null");
		this.dataSource = dataSource;
		this.incrementerName = incrementerName;
	}


	/**
	 * 设置要从中检索值的数据源。
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 返回要从中检索值的数据源。
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 设置序列/表的名称。
	 */
	public void setIncrementerName(String incrementerName) {
		this.incrementerName = incrementerName;
	}

	/**
	 * 返回序列/表的名称。
	 */
	public String getIncrementerName() {
		return this.incrementerName;
	}

	/**
	 * 设置填充长度，即字符串结果应在前面添加零的长度。
	 */
	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	/**
	 * 返回字符串值的填充长度。
	 */
	public int getPaddingLength() {
		return this.paddingLength;
	}

	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (this.incrementerName == null) {
			throw new IllegalArgumentException("Property 'incrementerName' is required");
		}
	}


	/**
	 * 方法 `nextIntValue`：完成本类中与「next Int Value」相关的职责。
	 */
	@Override
	public int nextIntValue() throws DataAccessException {
		return (int) getNextKey();
	}

	/**
	 * 方法 `nextLongValue`：完成本类中与「next Long Value」相关的职责。
	 */
	@Override
	public long nextLongValue() throws DataAccessException {
		return getNextKey();
	}

	/**
	 * 方法 `nextStringValue`：完成本类中与「next String Value」相关的职责。
	 */
	@Override
	public String nextStringValue() throws DataAccessException {
		String s = Long.toString(getNextKey());
		int len = s.length();
		if (len < this.paddingLength) {
			s = "0".repeat(this.paddingLength - len) + s;
		}
		return s;
	}


	/**
	 * 确定下一个要使用的键，如长键。
	 * @return 键作为长键使用。它最终将由此类的公共具体方法稍后转换为另一种格式。
	 */
	protected abstract long getNextKey();

}
