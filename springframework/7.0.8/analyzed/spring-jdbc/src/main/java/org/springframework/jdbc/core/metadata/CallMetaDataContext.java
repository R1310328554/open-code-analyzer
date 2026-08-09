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

package org.springframework.jdbc.core.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用于管理用于存储过程调用的配置和执行的上下文元数据的类。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Kiril Nugmanov
 * @since 2.5
 */
public class CallMetaDataContext {

	// 记录器可用于子类
	/**
	 * 获取 Log（`Log`）。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	// 要调用的过程名称
	/** 名称相关状态（`procedureName`）。 */
	private @Nullable String procedureName;

	// 调用目录名称
	/** 名称相关状态（`catalogName`）。 */
	private @Nullable String catalogName;

	// 调用的模式名称
	/** 名称相关状态（`schemaName`）。 */
	private @Nullable String schemaName;

	// 调用执行中使用的 SqlParameter 对象列表
	private List<SqlParameter> callParameters = new ArrayList<>();

	// 用于输出映射中返回值的实际名称
	/** 名称相关状态（`actualFunctionReturnName`）。 */
	private @Nullable String actualFunctionReturnName;

	// 一组参数名称以排除用于任何未列出的内容
	private Set<String> limitedInParameterNames = new HashSet<>();

	// 输出参数的 SqlParameter 名称列表
	private List<String> outParameterNames = new ArrayList<>();

	// 指示这是一个过程还是一个函数
	/** `false`：该类的成员状态。 */
	private boolean function = false;

	// 指示是否应包含此过程的返回值
	/** `false`：该类的成员状态。 */
	private boolean returnValueRequired = false;

	// 我们是否应该访问调用参数元数据信息
	/** `true`：该类的成员状态。 */
	private boolean accessCallParameterMetaData = true;

	// 我们应该按名称绑定参数吗
	/** 名称相关状态（`namedBinding`）。 */
	private boolean namedBinding;

	// 通话元数据提供者
	/** `metaDataProvider`：该类的成员状态。 */
	private @Nullable CallMetaDataProvider metaDataProvider;


	/**
	 * 指定用于函数返回值的名称。
	 */
	public void setFunctionReturnName(String functionReturnName) {
		this.actualFunctionReturnName = functionReturnName;
	}

	/**
	 * 获取用于函数返回值的名称。
	 */
	public String getFunctionReturnName() {
		return (this.actualFunctionReturnName != null ? this.actualFunctionReturnName : "return");
	}

	/**
	 * 指定要使用的一组有限的 {@code in} 参数。
	 */
	public void setLimitedInParameterNames(Set<String> limitedInParameterNames) {
		this.limitedInParameterNames = limitedInParameterNames;
	}

	/**
	 * 获取要使用的 {@code in} 参数的有限集。
	 */
	public Set<String> getLimitedInParameterNames() {
		return this.limitedInParameterNames;
	}

	/**
	 * 指定 {@code out} 参数的名称。
	 */
	public void setOutParameterNames(List<String> outParameterNames) {
		this.outParameterNames = outParameterNames;
	}

	/**
	 * 获取 {@code out} 参数名称列表。
	 */
	public List<String> getOutParameterNames() {
		return this.outParameterNames;
	}

	/**
	 * 指定过程的名称。
	 */
	public void setProcedureName(@Nullable String procedureName) {
		this.procedureName = procedureName;
	}

	/**
	 * 获取过程的名称。
	 */
	public @Nullable String getProcedureName() {
		return this.procedureName;
	}

	/**
	 * 指定目录的名称。
	 */
	public void setCatalogName(@Nullable String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * 获取目录的名称。
	 */
	public @Nullable String getCatalogName() {
		return this.catalogName;
	}

	/**
	 * 指定架构的名称。
	 */
	public void setSchemaName(@Nullable String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * 获取架构的名称。
	 */
	public @Nullable String getSchemaName() {
		return this.schemaName;
	}

	/**
	 * 指定此调用是否是函数调用。
	 */
	public void setFunction(boolean function) {
		this.function = function;
	}

	/**
	 * 检查这个调用是否是一个函数调用。
	 */
	public boolean isFunction() {
		return this.function;
	}

	/**
	 * 指定是否需要返回值。
	 */
	public void setReturnValueRequired(boolean returnValueRequired) {
		this.returnValueRequired = returnValueRequired;
	}

	/**
	 * 检查是否需要返回值。
	 */
	public boolean isReturnValueRequired() {
		return this.returnValueRequired;
	}

	/**
	 * 指定是否应访问调用参数元数据。
	 */
	public void setAccessCallParameterMetaData(boolean accessCallParameterMetaData) {
		this.accessCallParameterMetaData = accessCallParameterMetaData;
	}

	/**
	 * 检查是否应访问调用参数元数据。
	 */
	public boolean isAccessCallParameterMetaData() {
		return this.accessCallParameterMetaData;
	}

	/**
	 * 指定参数是否应按名称绑定。
	 * @since 4.2
	 */
	public void setNamedBinding(boolean namedBinding) {
		this.namedBinding = namedBinding;
	}

	/**
	 * 检查参数是否应按名称绑定。
	 * @since 4.2
	 */
	public boolean isNamedBinding() {
		return this.namedBinding;
	}


	/**
	 * 使用数据库中的元数据初始化此类。
	 * @param dataSource 用于检索元数据的 DataSource
	 */
	public void initializeMetaData(DataSource dataSource) {
		this.metaDataProvider = CallMetaDataProviderFactory.createMetaDataProvider(dataSource, this);
	}

	/**
	 * 方法 `obtainMetaDataProvider`：完成本类中与「obtain Meta Data Provider」相关的职责。
	 */
	private CallMetaDataProvider obtainMetaDataProvider() {
		Assert.state(this.metaDataProvider != null, "No CallMetaDataProvider - call initializeMetaData first");
		return this.metaDataProvider;
	}

	/**
	 * 根据所用数据库所使用的 JDBC 驱动程序提供的支持创建 ReturnResultSetParameter/SqlOutParameter。
	 * @param parameterName 参数的名称（也用作输出中返回的列表的名称）
	 * @param rowMapper RowMapper 实现，用于映射结果集中返回的数据
	 * @return 适当的Sql参数
	 */
	public SqlParameter createReturnResultSetParameter(String parameterName, RowMapper<?> rowMapper) {
		CallMetaDataProvider provider = obtainMetaDataProvider();
		if (provider.isReturnResultSetSupported()) {
			return new SqlReturnResultSet(parameterName, rowMapper);
		}
		else {
			if (provider.isRefCursorSupported()) {
				return new SqlOutParameter(parameterName, provider.getRefCursorSqlType(), rowMapper);
			}
			else {
				throw new InvalidDataAccessApiUsageException(
						"Return of a ResultSet from a stored procedure is not supported");
			}
		}
	}

	/**
	 * 获取此调用的单个输出参数的名称。如果有多个参数，则返回第一个参数的名称。
	 */
	public @Nullable String getScalarOutParameterName() {
		if (isFunction()) {
			return getFunctionReturnName();
		}
		else {
			if (this.outParameterNames.size() > 1) {
				logger.info("Accessing single output value when procedure has more than one output parameter");
			}
			return (!this.outParameterNames.isEmpty() ? this.outParameterNames.get(0) : null);
		}
	}

	/**
	 * 获取要在调用执行中使用的 SqlParameter 对象的列表。
	 */
	public List<SqlParameter> getCallParameters() {
		return this.callParameters;
	}

	/**
	 * 处理提供的参数列表，如果使用过程列元数据，则参数将与元数据信息进行匹配，并且将自动包含任何缺失的参数。
	 * @param parameters 用作基础的参数列表
	 */
	public void processParameters(List<SqlParameter> parameters) {
		this.callParameters = reconcileParameters(parameters);
	}

	/**
	 * 将提供的参数与可用的元数据进行协调，并在适当的情况下添加新参数。
	 */
	protected List<SqlParameter> reconcileParameters(List<SqlParameter> parameters) {
		CallMetaDataProvider provider = obtainMetaDataProvider();

		final List<SqlParameter> declaredReturnParams = new ArrayList<>();
		final Map<String, SqlParameter> declaredParams = new LinkedHashMap<>();
		boolean returnDeclared = false;
		List<String> outParamNames = new ArrayList<>();
		List<String> metaDataParamNames = new ArrayList<>();

		// 获取元数据参数的名称
		for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
			if (!meta.isReturnParameter()) {
				metaDataParamNames.add(lowerCase(meta.getParameterName()));
			}
		}

		// 将隐式返回参数与显式参数分开...
		for (SqlParameter param : parameters) {
			if (param.isResultsParameter()) {
				declaredReturnParams.add(param);
			}
			else {
				String paramName = param.getName();
				if (paramName == null) {
					throw new IllegalArgumentException("Anonymous parameters not supported for calls - " +
							"please specify a name for the parameter of SQL type " + param.getSqlType());
				}
				String paramNameToMatch = lowerCase(provider.parameterNameToUse(paramName));
				declaredParams.put(paramNameToMatch, param);
				if (param instanceof SqlOutParameter) {
					outParamNames.add(paramName);
					if (isFunction() && !metaDataParamNames.contains(paramNameToMatch) && !returnDeclared) {
						if (logger.isDebugEnabled()) {
							logger.debug("Using declared out parameter '" + paramName +
									"' for function return value");
						}
						this.actualFunctionReturnName = paramName;
						returnDeclared = true;
					}
				}
			}
		}
		setOutParameterNames(outParamNames);

		List<SqlParameter> workParams = new ArrayList<>(declaredReturnParams);
		if (!provider.isProcedureColumnMetaDataUsed()) {
			workParams.addAll(declaredParams.values());
			return workParams;
		}

		Map<String, String> limitedInParamNamesMap = CollectionUtils.newHashMap(this.limitedInParameterNames.size());
		for (String limitedParamName : this.limitedInParameterNames) {
			limitedInParamNamesMap.put(lowerCase(provider.parameterNameToUse(limitedParamName)), limitedParamName);
		}

		for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
			String paramName = meta.getParameterName();
			String paramNameToCheck = null;
			if (paramName != null) {
				paramNameToCheck = lowerCase(provider.parameterNameToUse(paramName));
			}
			String paramNameToUse = provider.parameterNameToUse(paramName);
			if (declaredParams.containsKey(paramNameToCheck) || (meta.isReturnParameter() && returnDeclared)) {
				SqlParameter param;
				if (meta.isReturnParameter()) {
					param = declaredParams.get(getFunctionReturnName());
					if (param == null && !getOutParameterNames().isEmpty()) {
						param = declaredParams.get(getOutParameterNames().get(0).toLowerCase(Locale.ROOT));
					}
					if (param == null) {
						throw new InvalidDataAccessApiUsageException(
								"Unable to locate declared parameter for function return value - " +
								" add an SqlOutParameter with name '" + getFunctionReturnName() + "'");
					}
					else {
						this.actualFunctionReturnName = param.getName();
					}
				}
				else {
					param = declaredParams.get(paramNameToCheck);
				}
				if (param != null) {
					workParams.add(param);
					if (logger.isDebugEnabled()) {
						logger.debug("Using declared parameter for '" +
								(paramNameToUse != null ? paramNameToUse : getFunctionReturnName()) + "'");
					}
				}
			}
			else {
				if (meta.isReturnParameter()) {
					// DatabaseMetaData.procedureColumnReturn 或可能 procedureColumnResult
					if (!isFunction() && !isReturnValueRequired() && paramName != null &&
							provider.byPassReturnParameter(paramName)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Bypassing meta-data return parameter for '" + paramName + "'");
						}
					}
					else {
						String returnNameToUse =
								(StringUtils.hasLength(paramNameToUse) ? paramNameToUse : getFunctionReturnName());
						workParams.add(provider.createDefaultOutParameter(returnNameToUse, meta));
						if (isFunction()) {
							this.actualFunctionReturnName = returnNameToUse;
							outParamNames.add(returnNameToUse);
						}
						if (logger.isDebugEnabled()) {
							logger.debug("Added meta-data return parameter for '" + returnNameToUse + "'");
						}
					}
				}
				else {
					if (paramNameToUse == null) {
						paramNameToUse = "";
					}
					if (meta.isOutParameter()) {
						workParams.add(provider.createDefaultOutParameter(paramNameToUse, meta));
						outParamNames.add(paramNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added meta-data out parameter for '" + paramNameToUse + "'");
						}
					}
					else if (meta.isInOutParameter()) {
						workParams.add(provider.createDefaultInOutParameter(paramNameToUse, meta));
						outParamNames.add(paramNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added meta-data in-out parameter for '" + paramNameToUse + "'");
						}
					}
					else {
						// DatabaseMetaData.procedureColumnIn 或可能 procedureColumnUnknown
						if (this.limitedInParameterNames.isEmpty() ||
								limitedInParamNamesMap.containsKey(lowerCase(paramNameToUse))) {
							workParams.add(provider.createDefaultInParameter(paramNameToUse, meta));
							if (logger.isDebugEnabled()) {
								logger.debug("Added meta-data in parameter for '" + paramNameToUse + "'");
							}
						}
						else {
							if (logger.isDebugEnabled()) {
								logger.debug("Limited set of parameters " + limitedInParamNamesMap.keySet() +
										" skipped parameter for '" + paramNameToUse + "'");
							}
						}
					}
				}
			}
		}

		return workParams;
	}

	/**
	 * 将输入参数值与声明要在调用中使用的参数进行匹配。
	 * @param parameterSource 输入值
	 * @return 包含匹配参数名称和从输入中获取的值的映射
	 */
	public Map<String, Object> matchInParameterValuesWithCallParameters(SqlParameterSource parameterSource) {
		// 对于参数源查找，我们需要提供不区分大小写的查找支持
		// 因为数据库元数据不一定提供区分大小写的参数名称。
		Map<String, String> caseInsensitiveParameterNames =
				SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource);

		Map<String, String> callParameterNames = CollectionUtils.newHashMap(this.callParameters.size());
		Map<String, Object> matchedParameters = CollectionUtils.newHashMap(this.callParameters.size());
		for (SqlParameter parameter : this.callParameters) {
			if (parameter.isInputValueProvided()) {
				String parameterName = parameter.getName();
				String parameterNameToMatch = obtainMetaDataProvider().parameterNameToUse(parameterName);
				if (parameterNameToMatch != null) {
					callParameterNames.put(parameterNameToMatch.toLowerCase(Locale.ROOT), parameterName);
				}
				if (parameterName != null) {
					if (parameterSource.hasValue(parameterName)) {
						matchedParameters.put(parameterName,
								SqlParameterSourceUtils.getTypedValue(parameterSource, parameterName));
					}
					else {
						String lowerCaseName = parameterName.toLowerCase(Locale.ROOT);
						if (parameterSource.hasValue(lowerCaseName)) {
							matchedParameters.put(parameterName,
									SqlParameterSourceUtils.getTypedValue(parameterSource, lowerCaseName));
						}
						else {
							String englishLowerCaseName = parameterName.toLowerCase(Locale.ENGLISH);
							if (parameterSource.hasValue(englishLowerCaseName)) {
								matchedParameters.put(parameterName,
										SqlParameterSourceUtils.getTypedValue(parameterSource, englishLowerCaseName));
							}
							else {
								String propertyName = JdbcUtils.convertUnderscoreNameToPropertyName(parameterName);
								if (parameterSource.hasValue(propertyName)) {
									matchedParameters.put(parameterName,
											SqlParameterSourceUtils.getTypedValue(parameterSource, propertyName));
								}
								else {
									if (caseInsensitiveParameterNames.containsKey(lowerCaseName)) {
										String sourceName = caseInsensitiveParameterNames.get(lowerCaseName);
										matchedParameters.put(parameterName,
												SqlParameterSourceUtils.getTypedValue(parameterSource, sourceName));
									}
									else if (logger.isInfoEnabled()) {
										logger.info("Unable to locate the corresponding parameter value for '" +
												parameterName + "' within the parameter values provided: " +
												caseInsensitiveParameterNames.values());
									}
								}
							}
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Matching " + caseInsensitiveParameterNames.values() + " with " + callParameterNames.values());
			logger.debug("Found match for " + matchedParameters.keySet());
		}
		return matchedParameters;
	}

	/**
	 * 将输入参数值与声明要在调用中使用的参数进行匹配。
	 * @param inParameters 输入值
	 * @return 包含匹配参数名称和从输入中获取的值的映射
	 */
	public Map<String, ?> matchInParameterValuesWithCallParameters(Map<String, ?> inParameters) {
		CallMetaDataProvider provider = obtainMetaDataProvider();
		if (!provider.isProcedureColumnMetaDataUsed()) {
			return inParameters;
		}

		Map<String, String> callParameterNames = CollectionUtils.newHashMap(this.callParameters.size());
		for (SqlParameter parameter : this.callParameters) {
			if (parameter.isInputValueProvided()) {
				String parameterName = parameter.getName();
				String parameterNameToMatch = provider.parameterNameToUse(parameterName);
				if (parameterNameToMatch != null) {
					callParameterNames.put(parameterNameToMatch.toLowerCase(Locale.ROOT), parameterName);
				}
			}
		}

		Map<String, Object> matchedParameters = CollectionUtils.newHashMap(inParameters.size());
		inParameters.forEach((parameterName, parameterValue) -> {
			String parameterNameToMatch = provider.parameterNameToUse(parameterName);
			String callParameterName = callParameterNames.get(lowerCase(parameterNameToMatch));
			if (callParameterName == null) {
				if (logger.isDebugEnabled()) {
					Object value = parameterValue;
					if (value instanceof SqlParameterValue sqlParameterValue) {
						value = sqlParameterValue.getValue();
					}
					if (value != null) {
						logger.debug("Unable to locate the corresponding IN or IN-OUT parameter for \"" +
								parameterName + "\" in the parameters used: " + callParameterNames.keySet());
					}
				}
			}
			else {
				matchedParameters.put(callParameterName, parameterValue);
			}
		});

		if (matchedParameters.size() < callParameterNames.size()) {
			for (String parameterName : callParameterNames.keySet()) {
				String parameterNameToMatch = provider.parameterNameToUse(parameterName);
				String callParameterName = callParameterNames.get(lowerCase(parameterNameToMatch));
				if (!matchedParameters.containsKey(callParameterName) && logger.isInfoEnabled()) {
					logger.info("Unable to locate the corresponding parameter value for '" + parameterName +
							"' within the parameter values provided: " + inParameters.keySet());
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Matching " + inParameters.keySet() + " with " + callParameterNames.values());
			logger.debug("Found match for " + matchedParameters.keySet());
		}
		return matchedParameters;
	}

	/**
	 * 匹配：In Parameter Values With Call Parameters（方法 `matchInParameterValuesWithCallParameters`）。
	 */
	public Map<String, ?> matchInParameterValuesWithCallParameters(Object[] parameterValues) {
		Map<String, Object> matchedParameters = CollectionUtils.newHashMap(parameterValues.length);
		int i = 0;
		for (SqlParameter parameter : this.callParameters) {
			if (parameter.isInputValueProvided()) {
				String parameterName = parameter.getName();
				matchedParameters.put(parameterName, parameterValues[i++]);
			}
		}
		return matchedParameters;
	}

	/**
	 * 根据配置和元数据信息构建调用字符串。
	 * @return 要使用的调用字符串
	 */
	public String createCallString() {
		Assert.state(this.metaDataProvider != null, "No CallMetaDataProvider available");

		StringBuilder callString;
		int parameterCount = 0;
		String catalogNameToUse;
		String schemaNameToUse;

		// 对于不支持目录的 Oracle，我们需要反转模式名称
		// 和目录名称，因为目录用于包名称
		if (this.metaDataProvider.isSupportsSchemasInProcedureCalls() &&
				!this.metaDataProvider.isSupportsCatalogsInProcedureCalls()) {
			schemaNameToUse = this.metaDataProvider.catalogNameToUse(getCatalogName());
			catalogNameToUse = this.metaDataProvider.schemaNameToUse(getSchemaName());
		}
		else {
			catalogNameToUse = this.metaDataProvider.catalogNameToUse(getCatalogName());
			schemaNameToUse = this.metaDataProvider.schemaNameToUse(getSchemaName());
		}

		if (isFunction() || isReturnValueRequired()) {
			callString = new StringBuilder("{? = call ");
			parameterCount = -1;
		}
		else {
			callString = new StringBuilder("{call ");
		}

		if (StringUtils.hasLength(catalogNameToUse)) {
			callString.append(catalogNameToUse).append('.');
		}
		if (StringUtils.hasLength(schemaNameToUse)) {
			callString.append(schemaNameToUse).append('.');
		}
		callString.append(this.metaDataProvider.procedureNameToUse(getProcedureName()));
		callString.append('(');

		for (SqlParameter parameter : this.callParameters) {
			if (!parameter.isResultsParameter()) {
				if (parameterCount > 0) {
					callString.append(", ");
				}
				if (parameterCount >= 0) {
					callString.append(createParameterBinding(parameter));
				}
				parameterCount++;
			}
		}
		callString.append(")}");

		return callString.toString();
	}

	/**
	 * 构建参数绑定片段。
	 * @param parameter 调用参数
	 * @return 结合片段
	 * @since 4.2
	 */
	protected String createParameterBinding(SqlParameter parameter) {
		Assert.state(this.metaDataProvider != null, "No CallMetaDataProvider available");
		return (isNamedBinding() ? this.metaDataProvider.namedParameterBindingToUse(parameter.getName()) : "?");
	}

	/**
	 * 方法 `lowerCase`：完成本类中与「lower Case」相关的职责。
	 */
	private static String lowerCase(@Nullable String paramName) {
		return (paramName != null ? paramName.toLowerCase(Locale.ROOT) : "");
	}

}
