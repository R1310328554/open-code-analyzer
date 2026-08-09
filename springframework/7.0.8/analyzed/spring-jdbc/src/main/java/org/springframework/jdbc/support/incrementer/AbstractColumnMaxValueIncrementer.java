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

import org.springframework.util.Assert;

/**
 * 使用自定义序列表中列的 {@link DataFieldMaxValueIncrementer} 实现的抽象基类。
 * 子类须在 {@link #getNextKey()} 中提供对该表的具体处理。
 *
 * @author Juergen Hoeller
 * @since 2.5.3
 */
public abstract class AbstractColumnMaxValueIncrementer extends AbstractDataFieldMaxValueIncrementer {

	/** 本序列使用的列名。 */
	private String columnName;

	/** 缓存中缓冲的键数量。 */
	private int cacheSize = 1;


	/**
	 * Bean 属性风格使用的默认构造器。
	 * @see #setDataSource
	 * @see #setIncrementerName
	 * @see #setColumnName
	 */
	@SuppressWarnings("NullAway.Init")
	public AbstractColumnMaxValueIncrementer() {
	}

	/**
	 * 便捷构造器。
	 * @param dataSource 要使用的 DataSource
	 * @param incrementerName 要使用的序列/表名
	 * @param columnName 序列表中要使用的列名
	 */
	public AbstractColumnMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName);
		Assert.notNull(columnName, "Column name must not be null");
		this.columnName = columnName;
	}


	/**
	 * 设置序列表中的列名。
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * 返回序列表中的列名。
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * 设置缓冲键数量。
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	/**
	 * 返回缓冲键数量。
	 */
	public int getCacheSize() {
		return this.cacheSize;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.columnName == null) {
			throw new IllegalArgumentException("Property 'columnName' is required");
		}
	}

}
