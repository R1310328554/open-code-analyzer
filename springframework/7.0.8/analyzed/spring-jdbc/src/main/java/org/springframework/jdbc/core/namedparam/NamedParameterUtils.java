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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

/**
 * 用于命名参数解析的辅助方法。
 * <p>仅用于 Spring 的 JDBC 框架内部使用。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Yanming Zhou
 * @since 2.0
 */
public abstract class NamedParameterUtils {

	/**
	 * 符合注释或引用起始字符资格的字符集。
	 */
	private static final String[] START_SKIP = {"'", "\"", "--", "/*", "`"};

	/**
	 * 作为相应注释或引用结束字符的字符集。
	 */
	private static final String[] STOP_SKIP = {"'", "\"", "\n", "*/", "`"};

	/**
	 * 符合参数分隔符的字符集，指示 SQL 字符串中的参数名称已结束。
	 */
	private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";

	/**
	 * 每个字符代码带有分隔符标志的索引。从技术上讲，此时只需要 34 到 124 之间。
	 */
	private static final boolean[] separatorIndex = new boolean[128];

	static {
		for (char c : PARAMETER_SEPARATORS.toCharArray()) {
			separatorIndex[c] = true;
		}
	}


	//-------------------------------------------------------------------------
	// NamedParameterJdbcTemplate 和 SqlQuery/SqlUpdate 使用的核心方法
	//-------------------------------------------------------------------------

	/**
	 * 解析 SQL 语句并找到所有占位符或命名参数。命名参数替代 JDBC 占位符。
	 * @param sql SQL语句
	 * @return 解析后的语句，表示为 {@link ParsedSql} 实例
	 */
	public static ParsedSql parseSqlStatement(String sql) {
		Assert.notNull(sql, "SQL must not be null");

		Set<String> namedParameters = new HashSet<>();
		StringBuilder sqlToUse = new StringBuilder(sql);
		List<ParameterHolder> parameterList = new ArrayList<>();

		char[] statement = sql.toCharArray();
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int escapes = 0;
		int i = 0;
		while (i < statement.length) {
			int skipToPosition = i;
			while (i < statement.length) {
				skipToPosition = skipCommentsAndQuotes(statement, i);
				if (i == skipToPosition) {
					break;
				}
				else {
					i = skipToPosition;
				}
			}
			if (i >= statement.length) {
				break;
			}
			char c = statement[i];
			if (c == ':' || c == '&') {
				int j = i + 1;
				if (c == ':' && j < statement.length && statement[j] == ':') {
					// 应跳过 Postgres 风格的“::”转换运算符
					i = i + 2;
					continue;
				}
				String parameter = null;
				if (c == ':' && j < statement.length && statement[j] == '{') {
					// :{x} 样式参数
					while (statement[j] != '}') {
						j++;
						if (j >= statement.length) {
							throw new InvalidDataAccessApiUsageException(
									"Non-terminated named parameter declaration at position " + i +
									" in statement: " + sql);
						}
						if (statement[j] == ':' || statement[j] == '{') {
							throw new InvalidDataAccessApiUsageException(
									"Parameter name contains invalid character '" + statement[j] +
									"' at position " + i + " in statement: " + sql);
						}
					}
					if (j - i > 2) {
						parameter = sql.substring(i + 2, j);
						namedParameterCount = addNewNamedParameter(
								namedParameters, namedParameterCount, parameter);
						totalParameterCount = addNamedParameter(
								parameterList, totalParameterCount, escapes, i, j + 1, parameter);
					}
					j++;
				}
				else {
					boolean paramWithSquareBrackets = false;
					while (j < statement.length) {
						c = statement[j];
						if (isParameterSeparator(c)) {
							break;
						}
						if (c == '[') {
							paramWithSquareBrackets = true;
						}
						else if (c == ']') {
							if (!paramWithSquareBrackets) {
								break;
							}
							paramWithSquareBrackets = false;
						}
						j++;
					}
					if (j - i > 1) {
						parameter = sql.substring(i + 1, j);
						namedParameterCount = addNewNamedParameter(
								namedParameters, namedParameterCount, parameter);
						totalParameterCount = addNamedParameter(
								parameterList, totalParameterCount, escapes, i, j, parameter);
					}
				}
				i = j - 1;
			}
			else {
				if (c == '\\') {
					int j = i + 1;
					if (j < statement.length && statement[j] == ':') {
						// 应跳过转义的“：”
						sqlToUse.deleteCharAt(i - escapes);
						escapes++;
						i = i + 2;
						continue;
					}
				}
				if (c == '?') {
					int j = i + 1;
					if (j < statement.length && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
						// 应跳过 Postgres 风格的“??”、“?|”、“?&”运算符
						i = i + 2;
						continue;
					}
					unnamedParameterCount++;
					totalParameterCount++;
				}
			}
			i++;
		}
		ParsedSql parsedSql = new ParsedSql(sqlToUse.toString());
		for (ParameterHolder ph : parameterList) {
			parsedSql.addNamedParameter(ph.getParameterName(), ph.getStartIndex(), ph.getEndIndex());
		}
		parsedSql.setNamedParameterCount(namedParameterCount);
		parsedSql.setUnnamedParameterCount(unnamedParameterCount);
		parsedSql.setTotalParameterCount(totalParameterCount);
		return parsedSql;
	}

	/** 将已解析的命名参数登记到参数列表并递增总计数。 */
	private static int addNamedParameter(List<ParameterHolder> parameterList,
			int totalParameterCount, int escapes, int i, int j, String parameter) {

		parameterList.add(new ParameterHolder(parameter, i - escapes, j - escapes));
		totalParameterCount++;
		return totalParameterCount;
	}

	/** 首次出现的命名参数加入集合并递增命名参数计数。 */
	private static int addNewNamedParameter(Set<String> namedParameters, int namedParameterCount, String parameter) {
		if (!namedParameters.contains(parameter)) {
			namedParameters.add(parameter);
			namedParameterCount++;
		}
		return namedParameterCount;
	}

	/**
	 * 跳过 SQL 语句中出现的注释和引用名称。
	 * @param statement 包含SQL语句的字符数组
	 * @param position 语句的当前位置
	 * @return 跳过任何评论或引用后要处理的位置
	 */
	private static int skipCommentsAndQuotes(char[] statement, int position) {
		for (int i = 0; i < START_SKIP.length; i++) {
			if (statement[position] == START_SKIP[i].charAt(0)) {
				boolean match = true;
				for (int j = 1; j < START_SKIP[i].length(); j++) {
					if (statement[position + j] != START_SKIP[i].charAt(j)) {
						match = false;
						break;
					}
				}
				if (match) {
					int offset = START_SKIP[i].length();
					for (int m = position + offset; m < statement.length; m++) {
						if (statement[m] == STOP_SKIP[i].charAt(0)) {
							boolean endMatch = true;
							int endPos = m;
							for (int n = 1; n < STOP_SKIP[i].length(); n++) {
								if (m + n >= statement.length) {
									// 最后评论未正确关闭
									return statement.length;
								}
								if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
									endMatch = false;
									break;
								}
								endPos = m + n;
							}
							if (endMatch) {
								// 找到结束评论或引用的字符序列
								return endPos + 1;
							}
						}
					}
					// 未找到结束注释或引用的字符序列
					return statement.length;
				}
			}
		}
		return position;
	}

	/**
	 * 解析 SQL 语句并找到所有占位符或命名参数。命名参数会替换 JDBC 占位符，并且任何选择列表都会扩展到所需数量的占位符。选择列表可能包含对象数组，在这种情况下，占位符将被分
	 * 组并用括号括起来。这允许在 SQL 语句中使用“表达式列表”，例如： <br /><br /> {@code select id, name, state from table
	 *  where (name, age) in (('John', 35), ('Ann', 50))} <p> 传入的参数值用于确定要用于选择列表的占位符数量。选择列表不应为空，
	 * 并且应限制为 100 个或更少的元素。空列表或大量元素不保证数据库支持，并且严格取决于供应商。
	 * @param parsedSql SQL 语句的解析表示
	 * @param paramSource 命名参数的来源
	 * @return 带有替换参数的 SQL 语句
	 * @see #parseSqlStatement
	 */
	public static String substituteNamedParameters(ParsedSql parsedSql, @Nullable SqlParameterSource paramSource) {
		String originalSql = parsedSql.getOriginalSql();
		List<String> paramNames = parsedSql.getParameterNames();
		if (paramNames.isEmpty()) {
			return originalSql;
		}

		StringBuilder actualSql = new StringBuilder(originalSql.length());
		int lastIndex = 0;
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			int[] indexes = parsedSql.getParameterIndexes(i);
			int startIndex = indexes[0];
			int endIndex = indexes[1];
			actualSql.append(originalSql, lastIndex, startIndex);
			if (paramSource != null && paramSource.hasValue(paramName)) {
				Object value = paramSource.getValue(paramName);
				if (value instanceof SqlParameterValue sqlParameterValue) {
					value = sqlParameterValue.getValue();
				}
				if (value instanceof Iterable<?> iterable) {
					int k = 0;
					for (Object entryItem : iterable) {
						if (k > 0) {
							actualSql.append(", ");
						}
						k++;
						if (entryItem instanceof Object[] expressionList) {
							actualSql.append('(');
							for (int m = 0; m < expressionList.length; m++) {
								if (m > 0) {
									actualSql.append(", ");
								}
								actualSql.append('?');
							}
							actualSql.append(')');
						}
						else {
							actualSql.append('?');
						}
					}
				}
				else {
					actualSql.append('?');
				}
			}
			else {
				actualSql.append('?');
			}
			lastIndex = endIndex;
		}
		actualSql.append(originalSql, lastIndex, originalSql.length());
		return actualSql.toString();
	}

	/**
	 * 将命名参数值的 Map 转换为相应的数组。
	 * @param parsedSql 解析后的SQL语句
	 * @param paramSource 命名参数的来源
	 * @param declaredParams 声明的 SqlParameter 对象的列表（可能是 {@code null}）。如果指定，参数元数据将以 SqlParameterValue 对象的形式构建到值数组中。
	 * @return 值数组
	 */
	public static @Nullable Object[] buildValueArray(
			ParsedSql parsedSql, SqlParameterSource paramSource, @Nullable List<SqlParameter> declaredParams) {

		@Nullable Object[] paramArray = new Object[parsedSql.getTotalParameterCount()];
		if (parsedSql.getNamedParameterCount() > 0 && parsedSql.getUnnamedParameterCount() > 0) {
			throw new InvalidDataAccessApiUsageException(
					"Not allowed to mix named and traditional ? placeholders. You have " +
					parsedSql.getNamedParameterCount() + " named parameter(s) and " +
					parsedSql.getUnnamedParameterCount() + " traditional placeholder(s) in statement: " +
					parsedSql.getOriginalSql());
		}
		List<String> paramNames = parsedSql.getParameterNames();
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			try {
				Object paramValue = paramSource.getValue(paramName);
				if (paramValue instanceof SqlParameterValue) {
					paramArray[i] = paramValue;
				}
				else {
					SqlParameter param = findParameter(declaredParams, paramName, i);
					paramArray[i] = (param != null ? new SqlParameterValue(param, paramValue) :
							SqlParameterSourceUtils.getTypedValue(paramSource, paramName));
				}
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(
						"No value supplied for the SQL parameter '" + paramName + "': " + ex.getMessage());
			}
		}
		return paramArray;
	}

	/**
	 * 在给定的声明参数列表中查找匹配的参数。
	 * @param declaredParams 声明的 SqlParameter 对象
	 * @param paramName 所需参数的名称
	 * @param paramIndex 所需参数的索引
	 * @return 声明 SqlParameter，如果未找到则为 {@code null}
	 */
	private static @Nullable SqlParameter findParameter(
			@Nullable List<SqlParameter> declaredParams, String paramName, int paramIndex) {

		if (declaredParams != null) {
			// 第一遍：查找命名参数匹配。
			for (SqlParameter declaredParam : declaredParams) {
				if (paramName.equals(declaredParam.getName())) {
					return declaredParam;
				}
			}
			// 第二遍：寻找参数索引匹配。
			if (paramIndex < declaredParams.size()) {
				SqlParameter declaredParam = declaredParams.get(paramIndex);
				// 仅接受索引匹配的未命名参数。
				if (declaredParam.getName() == null) {
					return declaredParam;
				}
			}
		}
		return null;
	}

	/**
	 * 确定参数名称是否以当前位置结束，即给定字符是否符合分隔符的条件。
	 */
	private static boolean isParameterSeparator(char c) {
		return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
	}

	/**
	 * 将参数类型从 SqlParameterSource 转换为相应的 int 数组。为了重用 JdbcTemplate 上的现有方法，这是必要的。根据解析的 SQL 语句信息，任何
	 * 命名参数类型都会放置在对象数组中的正确位置。
	 * @param parsedSql 解析后的SQL语句
	 * @param paramSource 命名参数的来源
	 */
	public static int[] buildSqlTypeArray(ParsedSql parsedSql, SqlParameterSource paramSource) {
		int[] sqlTypes = new int[parsedSql.getTotalParameterCount()];
		List<String> paramNames = parsedSql.getParameterNames();
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			sqlTypes[i] = paramSource.getSqlType(paramName);
		}
		return sqlTypes;
	}

	/**
	 * 将参数声明从 SqlParameterSource 转换为相应的 SqlParameters 列表。为了重用 JdbcTemplate 上的现有方法，这是必要的。根据解析的
	 * SQL 语句信息，命名参数的 SqlParameter 被放置在结果列表中的正确位置。
	 * @param parsedSql 解析后的SQL语句
	 * @param paramSource 命名参数的来源
	 */
	public static List<SqlParameter> buildSqlParameterList(ParsedSql parsedSql, SqlParameterSource paramSource) {
		List<String> paramNames = parsedSql.getParameterNames();
		List<SqlParameter> params = new ArrayList<>(paramNames.size());
		for (String paramName : paramNames) {
			params.add(new SqlParameter(
					paramName, paramSource.getSqlType(paramName), paramSource.getTypeName(paramName)));
		}
		return params;
	}


	//-------------------------------------------------------------------------
	// 对纯 SQL 字符串进行操作的便捷方法
	//-------------------------------------------------------------------------

	/**
	 * 解析 SQL 语句并找到所有占位符或命名参数。 <p>Named 参数替换 JDBC 占位符。 <p>这是{@link
	 * #parseSqlStatement(String)}与{@link #substituteNamedParameters(ParsedSql,
	 * SqlParameterSource)}结合的快捷版本。
	 * @param sql SQL语句
	 * @return 实际的（已解析的）SQL 语句
	 */
	public static String parseSqlStatementIntoString(String sql) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return substituteNamedParameters(parsedSql, null);
	}

	/**
	 * 解析 SQL 语句并找到所有占位符或命名参数。 <p>Named 参数会替换 JDBC 占位符，并且任何选择列表都会扩展到所需数量的占位符。 <p>这是{@link
	 * #parseSqlStatement(String)}与{@link #substituteNamedParameters(ParsedSql,
	 * SqlParameterSource)}结合的快捷版本。
	 * @param sql SQL语句
	 * @param paramSource 命名参数的来源
	 * @return 带有替换参数的 SQL 语句
	 */
	public static String substituteNamedParameters(String sql, SqlParameterSource paramSource) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return substituteNamedParameters(parsedSql, paramSource);
	}

	/**
	 * 将命名参数值的 Map 转换为相应的数组。 <p>这是{@link #buildValueArray(ParsedSql, SqlParameterSource,
	 * java.util.List)}的快捷版本。
	 * @param sql SQL语句
	 * @param paramMap 参数图
	 * @return 值数组
	 */
	public static @Nullable Object[] buildValueArray(String sql, Map<String, ?> paramMap) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		return buildValueArray(parsedSql, new MapSqlParameterSource(paramMap), null);
	}


	private static class ParameterHolder {

		private final String parameterName;

		private final int startIndex;

		private final int endIndex;

		public ParameterHolder(String parameterName, int startIndex, int endIndex) {
			this.parameterName = parameterName;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		public String getParameterName() {
			return this.parameterName;
		}

		public int getStartIndex() {
			return this.startIndex;
		}

		public int getEndIndex() {
			return this.endIndex;
		}
	}

}
