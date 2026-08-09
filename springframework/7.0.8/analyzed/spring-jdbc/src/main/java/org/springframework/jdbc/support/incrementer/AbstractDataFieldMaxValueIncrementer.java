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
 * {@link DataFieldMaxValueIncrementer} 的基础实现，委托给返回 {@code long} 的
 * {@link #getNextKey} 模板方法。String 值基于 long，必要时前补零。
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public abstract class AbstractDataFieldMaxValueIncrementer implements DataFieldMaxValueIncrementer, InitializingBean {

	@SuppressWarnings("NullAway.Init")
	private DataSource dataSource;

	/** 包含序列的序列/表名。 */
	@SuppressWarnings("NullAway.Init")
	private String incrementerName;

	/** String 结果前补零的目标长度。 */
	protected int paddingLength = 0;


	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 */
	public AbstractDataFieldMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 */
	public AbstractDataFieldMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		Assert.notNull(dataSource, "DataSource must not be null");
		Assert.notNull(incrementerName, "Incrementer name must not be null");
		this.dataSource = dataSource;
		this.incrementerName = incrementerName;
	}


	/**
	 * 设置用于获取值的数据源。
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 返回用于获取值的数据源。
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 设置序列/表名。
	 */
	public void setIncrementerName(String incrementerName) {
		this.incrementerName = incrementerName;
	}

	/**
	 * 返回序列/表名。
	 */
	public String getIncrementerName() {
		return this.incrementerName;
	}

	/**
	 * 设置填充长度，即 String 结果前补零的目标长度。
	 */
	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	/**
	 * 返回 String 值的填充长度。
	 */
	public int getPaddingLength() {
		return this.paddingLength;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (this.incrementerName == null) {
			throw new IllegalArgumentException("Property 'incrementerName' is required");
		}
	}


	@Override
	public int nextIntValue() throws DataAccessException {
		return (int) getNextKey();
	}

	@Override
	public long nextLongValue() throws DataAccessException {
		return getNextKey();
	}

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
	 * 确定下一个要使用的键（long 形式）。
	 * @return 以 long 表示的键，后续由本类 public 具体方法转换为其他格式。
	 */
	protected abstract long getNextKey();

}
