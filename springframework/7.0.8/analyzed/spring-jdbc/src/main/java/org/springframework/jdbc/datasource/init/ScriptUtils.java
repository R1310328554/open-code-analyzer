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

package org.springframework.jdbc.datasource.init;

import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用于将 SQL 脚本与 JDBC 结合使用的通用实用程序方法。
 * <p>主要供框架内部使用。
 * @author Thomas Risberg
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Dave Syer
 * @author Chris Beams
 * @author Oliver Gierke
 * @author Chris Baldwin
 * @author Nicolas Debeissat
 * @author Phillip Webb
 * @since 4.0.3
 * @see org.springframework.r2dbc.connection.init.ScriptUtils
 */
public abstract class ScriptUtils {

	/**
	 * SQL 脚本中的默认语句分隔符：{@code ";"}。
	 */
	public static final String DEFAULT_STATEMENT_SEPARATOR = ";";

	/**
	 * SQL 脚本中的后备语句分隔符：{@code "\n"}。如果给定脚本中既不存在自定义分隔符也不存在 {@link
	 * #DEFAULT_STATEMENT_SEPARATOR}，则使用 <p>。
	 */
	public static final String FALLBACK_STATEMENT_SEPARATOR = "\n";

	/**
	 * 文件结束 (EOF) SQL 语句分隔符：{@code "^^^ END OF SCRIPT ^^^"}。 <p> 该值可以作为 {@code separator} 提供给
	 * {@link #executeSqlScript(Connection, EncodedResource, boolean, boolean, String, String,
	 * String, String)}，以表示 SQL 脚本包含单个语句（可能跨越多行），没有显式语句分隔符。请注意，这样的脚本实际上不应包含此值；它只是一个
	 * <em>virtual</em> 语句分隔符。
	 */
	public static final String EOF_STATEMENT_SEPARATOR = "^^^ END OF SCRIPT ^^^";

	/**
	 * SQL 脚本中单行注释的默认前缀：{@code "--"}。
	 */
	public static final String DEFAULT_COMMENT_PREFIX = "--";

	/**
	 * SQL 脚本中单行注释的默认前缀：{@code ["--"]}。
	 * @since 5.2
	 */
	public static final String[] DEFAULT_COMMENT_PREFIXES = {DEFAULT_COMMENT_PREFIX};

	/**
	 * SQL 脚本中块注释的默认起始分隔符：{@code "/*"}。
	 */
	public static final String DEFAULT_BLOCK_COMMENT_START_DELIMITER = "/*";

	/**
	 * SQL 脚本中块注释的默认结束分隔符：<code>"*&#47;"</code>。
	 */
	public static final String DEFAULT_BLOCK_COMMENT_END_DELIMITER = "*/";


	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(ScriptUtils.class);


	/**
	 * 使用语句分隔符、注释分隔符和异常处理标志的默认设置执行给定的 SQL 脚本。 <p>语句分隔符和注释将在执行所提供脚本中的各个语句之前被删除。 <p><strong>Warni
	 * ng</strong>：此方法执行 <em>not</em> 释放提供的 {@link Connection}。
	 * @param connection 用于执行脚本的 JDBC 连接；已经配置并可以使用
	 * @param resource 从中加载 SQL 脚本的资源；使用当前平台的默认编码进行编码
	 * @throws ScriptException 如果执行 SQL 脚本时发生错误
	 * @see #executeSqlScript(Connection, EncodedResource, boolean, boolean, String, String, String, String)
	 * @see #DEFAULT_STATEMENT_SEPARATOR
	 * @see #DEFAULT_COMMENT_PREFIX
	 * @see #DEFAULT_BLOCK_COMMENT_START_DELIMITER
	 * @see #DEFAULT_BLOCK_COMMENT_END_DELIMITER
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public static void executeSqlScript(Connection connection, Resource resource) throws ScriptException {
		executeSqlScript(connection, new EncodedResource(resource));
	}

	/**
	 * 使用语句分隔符、注释分隔符和异常处理标志的默认设置执行给定的 SQL 脚本。 <p>语句分隔符和注释将在执行所提供脚本中的各个语句之前被删除。 <p><strong>Warni
	 * ng</strong>：此方法执行 <em>not</em> 释放提供的 {@link Connection}。
	 * @param connection 用于执行脚本的 JDBC 连接；已经配置并可以使用
	 * @param resource 用于加载 SQL 脚本的资源（可能与特定编码相关）
	 * @throws ScriptException 如果执行 SQL 脚本时发生错误
	 * @see #executeSqlScript(Connection, EncodedResource, boolean, boolean, String, String, String, String)
	 * @see #DEFAULT_STATEMENT_SEPARATOR
	 * @see #DEFAULT_COMMENT_PREFIX
	 * @see #DEFAULT_BLOCK_COMMENT_START_DELIMITER
	 * @see #DEFAULT_BLOCK_COMMENT_END_DELIMITER
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public static void executeSqlScript(Connection connection, EncodedResource resource) throws ScriptException {
		executeSqlScript(connection, resource, false, false, DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
				DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
	}

	/**
	 * 执行给定的 SQL 脚本。 <p>语句分隔符和注释将在执行所提供脚本中的各个语句之前被删除。 <p><strong>Warning</strong>：此方法执行 <em>not
	 * </em> 释放提供的 {@link Connection}。
	 * @param connection 用于执行脚本的 JDBC 连接；已经配置并可以使用
	 * @param resource 用于加载 SQL 脚本的资源（可能与特定编码相关）
	 * @param continueOnError 发生错误时是否继续而不抛出异常
	 * @param ignoreFailedDrops 当 {@code DROP} 语句出现特定错误时是否继续
	 * @param commentPrefix 标识 SQL 脚本中单行注释的前缀（通常为“--”）
	 * @param separator 脚本语句分隔符；如果未指定，则默认为 {@value #DEFAULT_STATEMENT_SEPARATOR}，并回退到 {@value #FALLBACK_STATEMENT_SEPARATOR} 作为最后的手段；可以设置为 {@value #EOF_STATEMENT_SEPARATOR} 以表明脚本包含单个不带分隔符的语句
	 * @param blockCommentStartDelimiter <em>start</em> 块注释分隔符
	 * @param blockCommentEndDelimiter <em>end</em> 块注释分隔符
	 * @throws ScriptException 如果执行 SQL 脚本时发生错误
	 * @see #DEFAULT_STATEMENT_SEPARATOR
	 * @see #FALLBACK_STATEMENT_SEPARATOR
	 * @see #EOF_STATEMENT_SEPARATOR
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public static void executeSqlScript(Connection connection, EncodedResource resource, boolean continueOnError,
			boolean ignoreFailedDrops, String commentPrefix, @Nullable String separator,
			String blockCommentStartDelimiter, String blockCommentEndDelimiter) throws ScriptException {

		executeSqlScript(connection, resource, continueOnError, ignoreFailedDrops,
				new String[] { commentPrefix }, separator, blockCommentStartDelimiter,
				blockCommentEndDelimiter);
	}

	/**
	 * 执行给定的 SQL 脚本。 <p>语句分隔符和注释将在执行所提供脚本中的各个语句之前被删除。 <p><strong>Warning</strong>：此方法执行 <em>not
	 * </em> 释放提供的 {@link Connection}。
	 * @param connection 用于执行脚本的 JDBC 连接；已经配置并可以使用
	 * @param resource 用于加载 SQL 脚本的资源（可能与特定编码相关）
	 * @param continueOnError 发生错误时是否继续而不抛出异常
	 * @param ignoreFailedDrops 当 {@code DROP} 语句出现特定错误时是否继续
	 * @param commentPrefixes 标识 SQL 脚本中单行注释的前缀（通常为“--”）
	 * @param separator 脚本语句分隔符；如果未指定，则默认为 {@value #DEFAULT_STATEMENT_SEPARATOR}，并回退到 {@value #FALLBACK_STATEMENT_SEPARATOR} 作为最后的手段；可以设置为 {@value #EOF_STATEMENT_SEPARATOR} 以表明脚本包含单个不带分隔符的语句
	 * @param blockCommentStartDelimiter <em>start</em> 块注释分隔符
	 * @param blockCommentEndDelimiter <em>end</em> 块注释分隔符
	 * @throws ScriptException 如果执行 SQL 脚本时发生错误
	 * @since 5.2
	 * @see #DEFAULT_STATEMENT_SEPARATOR
	 * @see #FALLBACK_STATEMENT_SEPARATOR
	 * @see #EOF_STATEMENT_SEPARATOR
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public static void executeSqlScript(Connection connection, EncodedResource resource, boolean continueOnError,
			boolean ignoreFailedDrops, String[] commentPrefixes, @Nullable String separator,
			String blockCommentStartDelimiter, String blockCommentEndDelimiter) throws ScriptException {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing SQL script from " + resource);
			}
			long startTime = System.currentTimeMillis();

			String script;
			try {
				script = readScript(resource, separator, commentPrefixes, blockCommentEndDelimiter);
			}
			catch (IOException ex) {
				throw new CannotReadScriptException(resource, ex);
			}

			if (separator == null) {
				separator = DEFAULT_STATEMENT_SEPARATOR;
			}
			if (!EOF_STATEMENT_SEPARATOR.equals(separator) &&
					!containsStatementSeparator(resource, script, separator, commentPrefixes,
						blockCommentStartDelimiter, blockCommentEndDelimiter)) {
				separator = FALLBACK_STATEMENT_SEPARATOR;
			}

			List<String> statements = new ArrayList<>();
			splitSqlScript(resource, script, separator, commentPrefixes, blockCommentStartDelimiter,
					blockCommentEndDelimiter, statements);

			int stmtNumber = 0;
			Statement stmt = connection.createStatement();
			try {
				for (String statement : statements) {
					stmtNumber++;
					try {
						boolean hasResultSet = stmt.execute(statement);
						int updateCount = -1;
						if (logger.isDebugEnabled()) {
							logSqlWarnings(stmt);
						}
						do {
							if (hasResultSet) {
								// 我们调用 getResultSet() 以确保 JDBC 驱动程序进程
								// 它，但我们故意忽略返回的结果集，因为
								// 我们不能在这里用它做任何有意义的事情。
								stmt.getResultSet();
								if (logger.isDebugEnabled()) {
									logger.debug("ResultSet returned for SQL: " + statement);
								}
							}
							else {
								updateCount = stmt.getUpdateCount();
								if (updateCount >= 0 && logger.isDebugEnabled()) {
									logger.debug(updateCount + " returned as update count for SQL: " + statement);
								}
							}
							hasResultSet = stmt.getMoreResults();
						} while (hasResultSet || updateCount != -1);
					}
					catch (SQLException ex) {
						boolean dropStatement = StringUtils.startsWithIgnoreCase(statement.trim(), "drop");
						if (continueOnError || (dropStatement && ignoreFailedDrops)) {
							if (logger.isDebugEnabled()) {
								logger.debug(ScriptStatementFailedException.buildErrorMessage(statement, stmtNumber, resource), ex);
							}
						}
						else {
							throw new ScriptStatementFailedException(statement, stmtNumber, resource, ex);
						}
					}
				}
			}
			finally {
				try {
					stmt.close();
				}
				catch (Throwable ex) {
					logger.trace("Could not close JDBC Statement", ex);
				}
			}

			long elapsedTime = System.currentTimeMillis() - startTime;
			if (logger.isDebugEnabled()) {
				logger.debug("Executed SQL script from " + resource + " in " + elapsedTime + " ms.");
			}
		}
		catch (Exception ex) {
			if (ex instanceof ScriptException scriptException) {
				throw scriptException;
			}
			throw new UncategorizedScriptException(
				"Failed to execute database script from resource [" + resource + "]", ex);
		}
	}

	/**
	 * 以 debug 级别记录 Statement 上的 SQLWarning 链。
	 */
	private static void logSqlWarnings(Statement stmt) throws SQLException {
		SQLWarning warningToLog = stmt.getWarnings();
		while (warningToLog != null) {
			logger.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() +
					"', error code '" + warningToLog.getErrorCode() +
					"', message [" + warningToLog.getMessage() + "]");
			warningToLog = warningToLog.getNextWarning();
		}
	}

	/**
	 * 使用提供的注释前缀和语句分隔符从提供的资源中读取脚本，并构建包含这些行的 {@code String}。 <p> 具有注释前缀之一的 <em>beginning</em> 行将
	 * 从结果中排除；然而，在其他地方都可以添加注释——例如，在一个语句中——将包含在结果中。
	 * @param resource 包含要处理的脚本的 {@code EncodedResource}
	 * @param separator SQL 脚本中的语句分隔符（通常为“;”）
	 * @param commentPrefixes 标识 SQL 脚本中注释的前缀（通常为“--”）
	 * @param blockCommentEndDelimiter <em>end</em> 块注释分隔符
	 * @return {@code String} 包含脚本行
	 * @throws IOException 发生 I/O 错误时
	 */
	static String readScript(EncodedResource resource, @Nullable String separator,
			String[] commentPrefixes, String blockCommentEndDelimiter) throws IOException {

		try (LineNumberReader lnr = new LineNumberReader(resource.getReader())) {
			return readScript(lnr, commentPrefixes, separator, blockCommentEndDelimiter);
		}
	}

	/**
	 * 从 {@link LineNumberReader} 逐行读取脚本并拼接为字符串。
	 */
	private static String readScript(LineNumberReader lineNumberReader, String @Nullable [] commentPrefixes,
			@Nullable String separator, @Nullable String blockCommentEndDelimiter) throws IOException {

		String currentStatement = lineNumberReader.readLine();
		StringBuilder scriptBuilder = new StringBuilder();
		while (currentStatement != null) {
			if ((blockCommentEndDelimiter != null && currentStatement.contains(blockCommentEndDelimiter)) ||
				(commentPrefixes != null && !startsWithAny(currentStatement, commentPrefixes, 0))) {
				if (scriptBuilder.length() > 0) {
					scriptBuilder.append('\n');
				}
				scriptBuilder.append(currentStatement);
			}
			currentStatement = lineNumberReader.readLine();
		}
		appendSeparatorToScriptIfNecessary(scriptBuilder, separator);
		return scriptBuilder.toString();
	}

	/**
	 * 若语句分隔符末尾含空白，则在脚本末尾追加必要部分。
	 */
	private static void appendSeparatorToScriptIfNecessary(StringBuilder scriptBuilder, @Nullable String separator) {
		if (separator == null) {
			return;
		}
		String trimmed = separator.trim();
		if (trimmed.length() == separator.length()) {
			return;
		}
		// 分隔符以空格结尾，因此我们可能想查看脚本是否正在尝试
		// 以同样的方式结束
		if (scriptBuilder.lastIndexOf(trimmed) == scriptBuilder.length() - trimmed.length()) {
			scriptBuilder.append(separator.substring(trimmed.length()));
		}
	}

	/**
	 * 确定提供的 SQL 脚本是否包含指定的语句分隔符。 <p>此方法旨在用于查找分隔每个 SQL 语句的字符串 –例如，“;”特点。 OCAJAVA1DO如果脚本中出现的任何分隔符
	 * 位于用单引号 ({@code '}) 或双引号 ({@code "}) 括起来的 <em>literal</em> 文本块内、使用反斜杠 ({@code \}) 转义或者位于单
	 * 行注释或块注释内，则该分隔符将被忽略。
	 * @param resource 从中读取脚本的资源，或 {@code null}（如果未知）
	 * @param script 要在其中搜索的 SQL 脚本
	 * @param separator 要搜索的语句分隔符
	 * @param commentPrefixes 标识单行注释的前缀（通常是 {@code "--"}）
	 * @param blockCommentStartDelimiter <em>start</em> 块注释分隔符（通常为 {@code "/*"}）
	 * @param blockCommentEndDelimiter <em>end</em> 块注释分隔符（通常为 <code>"*&#47;"</code>）
	 * @since 5.2.16
	 */
	static boolean containsStatementSeparator(@Nullable EncodedResource resource, String script,
			String separator, String[] commentPrefixes, String blockCommentStartDelimiter,
			String blockCommentEndDelimiter) throws ScriptException {

		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inEscape = false;

		for (int i = 0; i < script.length(); i++) {
			char c = script.charAt(i);
			if (inEscape) {
				inEscape = false;
				continue;
			}
			// MySQL 风格的转义
			if (c == '\\') {
				inEscape = true;
				continue;
			}
			if (!inDoubleQuote && (c == '\'')) {
				inSingleQuote = !inSingleQuote;
			}
			else if (!inSingleQuote && (c == '"')) {
				inDoubleQuote = !inDoubleQuote;
			}
			if (!inSingleQuote && !inDoubleQuote) {
				if (script.startsWith(separator, i)) {
					return true;
				}
				else if (startsWithAny(script, commentPrefixes, i)) {
					// 跳过从评论开头到 EOL 的任何内容
					int indexOfNextNewline = script.indexOf('\n', i);
					if (indexOfNextNewline > i) {
						i = indexOfNextNewline;
						continue;
					}
					else {
						// 如果没有 EOL，我们必须处于脚本的末尾，因此在此停止。
						break;
					}
				}
				else if (script.startsWith(blockCommentStartDelimiter, i)) {
					// 跳过任何块注释
					int indexOfCommentEnd = script.indexOf(blockCommentEndDelimiter, i);
					if (indexOfCommentEnd > i) {
						i = indexOfCommentEnd + blockCommentEndDelimiter.length() - 1;
						continue;
					}
					else {
						throw new ScriptParseException(
								"Missing block comment end delimiter: " + blockCommentEndDelimiter, resource);
					}
				}
			}
		}

		return false;
	}

	/**
	 * 将 SQL 脚本拆分为由提供的分隔符字符串分隔的单独语句。每个单独的声明都将添加到提供的 {@code List} 中。 <p> 在脚本中，将遵循提供的 {@code comm
	 * entPrefixes}：任何以注释前缀之一开头并延伸到行尾的文本都将从输出中省略。同样，将遵循提供的 {@code blockCommentStartDelimiter} 和
	 *  {@code blockCommentEndDelimiter} 分隔符：块注释中包含的任何文本都将从输出中省略。此外，多个相邻的空白字符将被折叠成一个空格。
	 * @param resource 从中读取脚本的资源
	 * @param script SQL脚本
	 * @param separator 分隔每个语句的文本（通常是“;”或换行符）
	 * @param commentPrefixes 标识 SQL 行注释的前缀（通常为“--”）
	 * @param blockCommentStartDelimiter <em>start</em> 块注释分隔符；绝不是 {@code null} 或空
	 * @param blockCommentEndDelimiter <em>end</em> 块注释分隔符；绝不是 {@code null} 或空
	 * @param statements 将包含各个语句的列表
	 * @throws ScriptException 如果拆分 SQL 脚本时发生错误
	 * @since 5.2
	 */
	static void splitSqlScript(@Nullable EncodedResource resource, String script,
			String separator, String[] commentPrefixes, String blockCommentStartDelimiter,
			String blockCommentEndDelimiter, List<String> statements) throws ScriptException {

		Assert.hasText(script, "'script' must not be null or empty");
		Assert.notNull(separator, "'separator' must not be null");
		Assert.notEmpty(commentPrefixes, "'commentPrefixes' must not be null or empty");
		for (String commentPrefix : commentPrefixes) {
			Assert.hasText(commentPrefix, "'commentPrefixes' must not contain null or empty elements");
		}
		Assert.hasText(blockCommentStartDelimiter, "'blockCommentStartDelimiter' must not be null or empty");
		Assert.hasText(blockCommentEndDelimiter, "'blockCommentEndDelimiter' must not be null or empty");

		StringBuilder sb = new StringBuilder();
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inEscape = false;

		for (int i = 0; i < script.length(); i++) {
			char c = script.charAt(i);
			if (inEscape) {
				inEscape = false;
				sb.append(c);
				continue;
			}
			// MySQL 风格的转义
			if (c == '\\') {
				inEscape = true;
				sb.append(c);
				continue;
			}
			if (!inDoubleQuote && (c == '\'')) {
				inSingleQuote = !inSingleQuote;
			}
			else if (!inSingleQuote && (c == '"')) {
				inDoubleQuote = !inDoubleQuote;
			}
			if (!inSingleQuote && !inDoubleQuote) {
				if (script.startsWith(separator, i)) {
					// 我们已到达当前声明的结尾
					if (sb.length() > 0) {
						statements.add(sb.toString());
						sb = new StringBuilder();
					}
					i += separator.length() - 1;
					continue;
				}
				else if (startsWithAny(script, commentPrefixes, i)) {
					// 跳过从评论开头到 EOL 的任何内容
					int indexOfNextNewline = script.indexOf('\n', i);
					if (indexOfNextNewline > i) {
						i = indexOfNextNewline;
						continue;
					}
					else {
						// 如果没有 EOL，我们必须处于脚本的末尾，因此在此停止。
						break;
					}
				}
				else if (script.startsWith(blockCommentStartDelimiter, i)) {
					// 跳过任何块注释
					int indexOfCommentEnd = script.indexOf(blockCommentEndDelimiter, i);
					if (indexOfCommentEnd > i) {
						i = indexOfCommentEnd + blockCommentEndDelimiter.length() - 1;
						continue;
					}
					else {
						throw new ScriptParseException(
								"Missing block comment end delimiter: " + blockCommentEndDelimiter, resource);
					}
				}
				else if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
					// 避免多个相邻的空白字符
					if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
						c = ' ';
					}
					else {
						continue;
					}
				}
			}
			sb.append(c);
		}

		if (StringUtils.hasText(sb)) {
			statements.add(sb.toString());
		}
	}

	/**
	 * 检查脚本在指定偏移处是否以任一前缀开头。
	 */
	private static boolean startsWithAny(String script, String[] prefixes, int offset) {
		for (String prefix : prefixes) {
			if (script.startsWith(prefix, offset)) {
				return true;
			}
		}
		return false;
	}

}
