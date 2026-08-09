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

package org.springframework.jdbc.core.namedparam;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存有关已解析 SQL 语句的信息。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ParsedSql {

	/** `originalSql`：该类的成员状态。 */
	private final String originalSql;

	private final List<String> parameterNames = new ArrayList<>();

	private final List<int[]> parameterIndexes = new ArrayList<>();

	/** 参数相关状态（`namedParameterCount`）。 */
	private int namedParameterCount;

	/** 参数相关状态（`unnamedParameterCount`）。 */
	private int unnamedParameterCount;

	/** 参数相关状态（`totalParameterCount`）。 */
	private int totalParameterCount;


	/**
	 * 创建 {@link ParsedSql} 类的新实例。
	 * @param originalSql 正在（或将要）解析的 SQL 语句
	 */
	ParsedSql(String originalSql) {
		this.originalSql = originalSql;
	}

	/**
	 * 返回正在解析的SQL语句。
	 */
	String getOriginalSql() {
		return this.originalSql;
	}


	/**
	 * 添加从此 SQL 语句解析的命名参数。
	 * @param parameterName 参数名称
	 * @param startIndex 原始 SQL 字符串中的起始索引
	 * @param endIndex 原始 SQL 字符串中的结束索引
	 */
	void addNamedParameter(String parameterName, int startIndex, int endIndex) {
		this.parameterNames.add(parameterName);
		this.parameterIndexes.add(new int[] {startIndex, endIndex});
	}

	/**
	 * 返回解析后的SQL语句中的所有参数（绑定变量）。此处包含重复出现的相同参数名称。
	 */
	List<String> getParameterNames() {
		return this.parameterNames;
	}

	/**
	 * 返回指定参数的参数索引。
	 * @param parameterPosition 参数的位置（作为参数名称列表中的索引）
	 * @return 起始索引和结束索引，组合成长度为2的int数组
	 */
	int[] getParameterIndexes(int parameterPosition) {
		return this.parameterIndexes.get(parameterPosition);
	}

	/**
	 * 设置 SQL 语句中命名参数的数量。每个参数名称计算一次；重复出现的次数不计入此处。
	 */
	void setNamedParameterCount(int namedParameterCount) {
		this.namedParameterCount = namedParameterCount;
	}

	/**
	 * 返回 SQL 语句中命名参数的计数。每个参数名称计算一次；重复出现的次数不计入此处。
	 */
	int getNamedParameterCount() {
		return this.namedParameterCount;
	}

	/**
	 * 设置SQL语句中所有未命名参数的数量。
	 */
	void setUnnamedParameterCount(int unnamedParameterCount) {
		this.unnamedParameterCount = unnamedParameterCount;
	}

	/**
	 * 返回 SQL 语句中所有未命名参数的计数。
	 */
	int getUnnamedParameterCount() {
		return this.unnamedParameterCount;
	}

	/**
	 * 设置SQL语句中所有参数的总数。此处重复出现相同的参数名称确实有效。
	 */
	void setTotalParameterCount(int totalParameterCount) {
		this.totalParameterCount = totalParameterCount;
	}

	/**
	 * 返回SQL语句中所有参数的总数。此处重复出现相同的参数名称确实有效。
	 */
	int getTotalParameterCount() {
		return this.totalParameterCount;
	}


	/**
	 * 公开原始 SQL 字符串。
	 */
	@Override
	public String toString() {
		return this.originalSql;
	}

}
