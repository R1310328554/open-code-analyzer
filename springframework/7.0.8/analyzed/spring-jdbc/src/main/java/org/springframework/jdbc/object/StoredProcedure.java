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

package org.springframework.jdbc.object;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.jdbc.core.SqlParameter;

/**
 * RDBMS 存储过程的对象抽象的超类。此类是抽象类，旨在让子类提供一个类型化方法来调用，该方法委托给所提供的 {@link #execute} 方法。
 * <p>继承的{@link #setSql sql}属性是RDBMS中存储过程的名称。
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public abstract class StoredProcedure extends SqlCall {

	/**
	 * 允许用作 bean。
	 */
	protected StoredProcedure() {
	}

	/**
	 * 为存储过程创建新的对象包装器。
	 * @param ds 在该对象的整个生命周期中使用的 DataSource 来获取连接
	 * @param name 数据库中存储过程的名称
	 */
	protected StoredProcedure(DataSource ds, String name) {
		setDataSource(ds);
		setSql(name);
	}

	/**
	 * 为存储过程创建新的对象包装器。
	 * @param jdbcTemplate 包装 DataSource 的 JdbcTemplate
	 * @param name 数据库中存储过程的名称
	 */
	protected StoredProcedure(JdbcTemplate jdbcTemplate, String name) {
		setJdbcTemplate(jdbcTemplate);
		setSql(name);
	}


	/**
	 * 默认情况下，StoredProcedure 参数映射允许包含实际不用作参数的附加条目。
	 */
	@Override
	protected boolean allowsUnusedParameters() {
		return true;
	}

	/**
	 * 声明一个参数。 <p> 声明为 {@code SqlParameter} 和 {@code SqlInOutParameter} 的参数将始终用于提供输入值。除此之外，任何声明
	 * 为 {@code SqlOutParameter} 并提供非空输入值的参数也将用作输入参数。 <b>注意：对declareParameter的调用必须按照它们在数据库的存储过程
	 * 参数列表中出现的顺序进行。</b> <p>Names纯粹用于帮助映射。
	 * @param param 参数对象
	 * @throws InvalidDataAccessApiUsageException 如果参数没有名称，或者操作已经编译，因此无法进一步配置
	 */
	@Override
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (param.getName() == null) {
			throw new InvalidDataAccessApiUsageException("Parameters to stored procedures must have names as well as types");
		}
		super.declareParameter(param);
	}

	/**
	 * 使用提供的参数值执行存储过程。这是一种方便的方法，其中传入参数值的顺序必须与声明参数的顺序相匹配。
	 * @param inParams 输入参数的数量可变。输出参数不应包含在此映射中。值为 {@code null} 是合法的，这将使用存储过程的 NULL 参数产生正确的行为。
	 * @return 输出参数，按参数声明中的名称键入。输出参数将出现在此处，以及调用存储过程后的值。
	 */
	public Map<String, @Nullable Object> execute(Object... inParams) {
		Map<String, @Nullable Object> paramsToUse = new HashMap<>();
		validateParameters(inParams);
		int i = 0;
		for (SqlParameter sqlParameter : getDeclaredParameters()) {
			if (sqlParameter.isInputValueProvided() && i < inParams.length) {
				paramsToUse.put(sqlParameter.getName(), inParams[i++]);
			}
		}
		return getJdbcTemplate().call(newCallableStatementCreator(paramsToUse), getDeclaredParameters());
	}

	/**
	 * 执行存储过程。子类应该定义一个强类型执行方法（具有有意义的名称）来调用该方法，填充输入映射并从输出映射中提取类型值。子类执行方法通常将域对象作为参数和返回值。或者，它们可以返回
	 *  void。
	 * @param inParams 输入参数的映射，按参数声明中的名称键入。输出参数不需要（但可以）包含在此映射中。映射条目为 {@code null} 是合法的，这将使用存储过程的 NULL 参数产生正确的行为。
	 * @return 输出参数，按参数声明中的名称键入。输出参数将出现在此处，以及调用存储过程后的值。
	 */
	public Map<String, @Nullable Object> execute(Map<String, ?> inParams) throws DataAccessException {
		validateParameters(inParams.values().toArray());
		return getJdbcTemplate().call(newCallableStatementCreator(inParams), getDeclaredParameters());
	}

	/**
	 * 执行存储过程。子类应该定义一个强类型执行方法（具有有意义的名称）来调用该方法，并传入将填充输入映射的 ParameterMapper。这允许映射数据库特定功能，因为 Param
	 * eterMapper 可以访问 Connection 对象。执行方法还负责从输出映射中提取类型化值。子类执行方法通常将域对象作为参数和返回值。或者，它们可以返回 void。
	 * @param inParamMapper 输入参数的映射，按参数声明中的名称键入。输出参数不需要（但可以）包含在此映射中。映射条目为 {@code null} 是合法的，这将使用存储过程的 NULL 参数产生正确的行为。
	 * @return 输出参数，按参数声明中的名称键入。输出参数将出现在此处，以及调用存储过程后的值。
	 */
	public Map<String, @Nullable Object> execute(ParameterMapper inParamMapper) throws DataAccessException {
		checkCompiled();
		return getJdbcTemplate().call(newCallableStatementCreator(inParamMapper), getDeclaredParameters());
	}

}
