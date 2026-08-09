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
 * RDBMS 存储过程对象抽象的父类。
 * 本类为抽象类，子类应提供类型化调用方法，委托给 {@link #execute} 方法。
 *
 * <p>继承的 {@link #setSql sql} 属性为 RDBMS 中存储过程的名称。
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public abstract class StoredProcedure extends SqlCall {

	/**
	 * 允许作为 Bean 使用。
	 */
	protected StoredProcedure() {
	}

	/**
	 * 为存储过程创建新的对象包装器。
	 * @param ds 本对象生命周期内用于获取连接的 DataSource
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
	 * StoredProcedure 的参数 Map 默认允许包含未实际用作参数的额外条目。
	 */
	@Override
	protected boolean allowsUnusedParameters() {
		return true;
	}

	/**
	 * 声明参数。
	 * <p>声明为 {@code SqlParameter} 和 {@code SqlInOutParameter} 的参数始终用于提供输入值。
	 * 此外，声明为 {@code SqlOutParameter} 且提供了非空输入值的参数也会作为输入参数。
	 * <b>注意：declareParameter 调用顺序必须与数据库存储过程参数列表一致。</b>
	 * <p>名称仅用于辅助映射。
	 * @param param 参数对象
	 * @throws InvalidDataAccessApiUsageException 参数无名称或操作已编译无法继续配置时
	 */
	@Override
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (param.getName() == null) {
			throw new InvalidDataAccessApiUsageException("Parameters to stored procedures must have names as well as types");
		}
		super.declareParameter(param);
	}

	/**
	 * 以提供的参数值执行存储过程。
	 * 便捷方法，传入参数值的顺序必须与声明顺序一致。
	 * @param inParams 可变数量的输入参数，输出参数不应包含在内；值为 {@code null} 合法，
	 * 将以 NULL 参数调用存储过程。
	 * @return 输出参数映射，键为参数声明中的名称，
	 * 存储过程调用后输出参数的值将出现在此映射中。
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
	 * 执行存储过程。子类应定义强类型 execute 方法（有意义的名称）调用本方法，
	 * 填充输入映射并从输出映射提取类型化值；子类 execute 方法通常接收领域对象并返回值，也可返回 void。
	 * @param inParams 输入参数映射，键为参数声明中的名称；
	 * 输出参数可不（但可）包含在内；值为 {@code null} 合法。
	 * @return 输出参数映射，存储过程调用后输出参数的值将出现在此映射中。
	 */
	public Map<String, @Nullable Object> execute(Map<String, ?> inParams) throws DataAccessException {
		validateParameters(inParams.values().toArray());
		return getJdbcTemplate().call(newCallableStatementCreator(inParams), getDeclaredParameters());
	}

	/**
	 * 执行存储过程。子类应定义强类型 execute 方法调用本方法，
	 * 传入 ParameterMapper 填充输入映射；ParameterMapper 可访问 Connection，便于映射数据库特定特性。
	 * execute 方法还负责从输出映射提取类型化值。
	 * @param inParamMapper 参数映射器，填充输入参数映射
	 * @return 输出参数映射，存储过程调用后输出参数的值将出现在此映射中。
	 */
	public Map<String, @Nullable Object> execute(ParameterMapper inParamMapper) throws DataAccessException {
		checkCompiled();
		return getJdbcTemplate().call(newCallableStatementCreator(inParamMapper), getDeclaredParameters());
	}

}
