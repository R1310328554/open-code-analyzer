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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 使用外部资源中定义的 SQL 脚本填充、初始化或清理数据库。
 *
 * <ul>
 * <li>调用 {@link #addScript} 添加单个 SQL 脚本位置。
 * <li>调用 {@link #addScripts} 添加多个 SQL 脚本位置。
 * <li>参阅本类 setter 方法获取更多配置选项。
 * <li>调用 {@link #populate} 或 {@link #execute} 使用已配置脚本初始化或清理数据库。
 * </ul>
 *
 * @author Keith Donald
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Oliver Gierke
 * @author Sam Brannen
 * @author Chris Baldwin
 * @author Phillip Webb
 * @since 3.0
 * @see DatabasePopulatorUtils
 * @see ScriptUtils
 */
public class ResourceDatabasePopulator implements DatabasePopulator {

	List<Resource> scripts = new ArrayList<>();

	private @Nullable String sqlScriptEncoding;

	private String separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR;

	private String[] commentPrefixes = ScriptUtils.DEFAULT_COMMENT_PREFIXES;

	private String blockCommentStartDelimiter = ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER;

	private String blockCommentEndDelimiter = ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER;

	private boolean continueOnError = false;

	private boolean ignoreFailedDrops = false;


	/**
	 * 使用默认设置构造新的 {@code ResourceDatabasePopulator}。
	 * @since 4.0.3
	 */
	public ResourceDatabasePopulator() {
	}

	/**
	 * 使用默认设置构造新的 {@code ResourceDatabasePopulator}，并指定脚本。
	 * @param scripts 用于初始化或清理数据库的脚本（永不为 {@code null}）
	 * @since 4.0.3
	 */
	public ResourceDatabasePopulator(Resource... scripts) {
		setScripts(scripts);
	}

	/**
	 * 使用给定参数构造新的 {@code ResourceDatabasePopulator}。
	 * @param continueOnError 是否记录 SQL 失败但不导致整体失败
	 * @param ignoreFailedDrops 是否忽略失败的 SQL {@code DROP} 语句
	 * @param sqlScriptEncoding 所供 SQL 脚本的编码
	 * （可为 {@code null} 或<em>空</em> 表示平台编码）
	 * @param scripts 用于初始化或清理数据库的脚本（永不为 {@code null}）
	 * @since 4.0.3
	 */
	public ResourceDatabasePopulator(boolean continueOnError, boolean ignoreFailedDrops,
			@Nullable String sqlScriptEncoding, Resource... scripts) {

		this.continueOnError = continueOnError;
		this.ignoreFailedDrops = ignoreFailedDrops;
		setSqlScriptEncoding(sqlScriptEncoding);
		setScripts(scripts);
	}


	/**
	 * 添加用于初始化或清理数据库的脚本。
	 * @param script SQL 脚本路径（永不为 {@code null}）
	 */
	public void addScript(Resource script) {
		Assert.notNull(script, "'script' must not be null");
		this.scripts.add(script);
	}

	/**
	 * 添加多个用于初始化或清理数据库的脚本。
	 * @param scripts 要执行的脚本（永不为 {@code null}）
	 */
	public void addScripts(Resource... scripts) {
		assertContentsOfScriptArray(scripts);
		this.scripts.addAll(Arrays.asList(scripts));
	}

	/**
	 * 设置用于初始化或清理数据库的脚本，替换此前添加的所有脚本。
	 * @param scripts 要执行的脚本（永不为 {@code null}）
	 */
	public void setScripts(Resource... scripts) {
		assertContentsOfScriptArray(scripts);
		// Ensure that the list is modifiable
		this.scripts = new ArrayList<>(Arrays.asList(scripts));
	}

	private void assertContentsOfScriptArray(Resource... scripts) {
		Assert.notNull(scripts, "'scripts' must not be null");
		Assert.noNullElements(scripts, "'scripts' must not contain null elements");
	}

	/**
	 * 指定已配置 SQL 脚本的编码（若与平台编码不同）。
	 * @param sqlScriptEncoding 脚本使用的编码
	 * （可为 {@code null} 或空表示平台编码）
	 * @see #addScript(Resource)
	 */
	public void setSqlScriptEncoding(@Nullable String sqlScriptEncoding) {
		this.sqlScriptEncoding = (StringUtils.hasText(sqlScriptEncoding) ? sqlScriptEncoding : null);
	}

	/**
	 * 指定语句分隔符（若使用自定义分隔符）。
	 * <p>未指定时默认为 {@code ";"}，最后回退为 {@code "\n"}；
	 * 可设为 {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}
	 * 表示每个脚本为无分隔符的单条语句。
	 * @param separator 脚本语句分隔符
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * 设置 SQL 脚本中标识单行注释的前缀。
	 * <p>默认为 {@code "--"}。
	 * @param commentPrefix 单行注释前缀
	 * @see #setCommentPrefixes(String...)
	 */
	public void setCommentPrefix(String commentPrefix) {
		Assert.hasText(commentPrefix, "'commentPrefix' must not be null or empty");
		this.commentPrefixes = new String[] { commentPrefix };
	}

	/**
	 * 设置 SQL 脚本中标识单行注释的前缀。
	 * <p>默认为 {@code ["--"]}。
	 * @param commentPrefixes 单行注释前缀
	 * @since 5.2
	 */
	public void setCommentPrefixes(String... commentPrefixes) {
		Assert.notEmpty(commentPrefixes, "'commentPrefixes' must not be null or empty");
		Assert.noNullElements(commentPrefixes, "'commentPrefixes' must not contain null elements");
		this.commentPrefixes = commentPrefixes;
	}

	/**
	 * 设置 SQL 脚本中标识块注释的起始分隔符。
	 * <p>默认为 {@code "/*"}。
	 * @param blockCommentStartDelimiter 块注释起始分隔符
	 * （永不为 {@code null} 或空）
	 * @since 4.0.3
	 * @see #setBlockCommentEndDelimiter
	 */
	public void setBlockCommentStartDelimiter(String blockCommentStartDelimiter) {
		Assert.hasText(blockCommentStartDelimiter, "'blockCommentStartDelimiter' must not be null or empty");
		this.blockCommentStartDelimiter = blockCommentStartDelimiter;
	}

	/**
	 * 设置 SQL 脚本中标识块注释的结束分隔符。
	 * <p>默认为 <code>"*&#47;"</code>。
	 * @param blockCommentEndDelimiter 块注释结束分隔符
	 * （永不为 {@code null} 或空）
	 * @since 4.0.3
	 * @see #setBlockCommentStartDelimiter
	 */
	public void setBlockCommentEndDelimiter(String blockCommentEndDelimiter) {
		Assert.hasText(blockCommentEndDelimiter, "'blockCommentEndDelimiter' must not be null or empty");
		this.blockCommentEndDelimiter = blockCommentEndDelimiter;
	}

	/**
	 * 是否记录 SQL 失败但不导致整体失败。
	 * <p>默认为 {@code false}。
	 * @param continueOnError 出错时是否继续执行脚本
	 */
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	/**
	 * 是否忽略失败的 SQL {@code DROP} 语句。
	 * <p>适用于 SQL 方言在 {@code DROP} 语句中不支持 {@code IF EXISTS} 的非嵌入式数据库。
	 * <p>默认为 {@code false}，若 populator 意外运行且脚本以 {@code DROP} 开头将快速失败。
	 * @param ignoreFailedDrops 是否忽略失败的 DROP 语句
	 */
	public void setIgnoreFailedDrops(boolean ignoreFailedDrops) {
		this.ignoreFailedDrops = ignoreFailedDrops;
	}


	/**
	 * {@inheritDoc}
	 * @see #execute(DataSource)
	 */
	@Override
	public void populate(Connection connection) throws ScriptException {
		Assert.notNull(connection, "'connection' must not be null");
		for (Resource script : this.scripts) {
			EncodedResource encodedScript = new EncodedResource(script, this.sqlScriptEncoding);
			ScriptUtils.executeSqlScript(connection, encodedScript, this.continueOnError, this.ignoreFailedDrops,
					this.commentPrefixes, this.separator, this.blockCommentStartDelimiter, this.blockCommentEndDelimiter);
		}
	}

	/**
	 * 针对给定 {@link DataSource} 执行本 {@code ResourceDatabasePopulator}。
	 * <p>委托 {@link DatabasePopulatorUtils#execute}。
	 * @param dataSource 要执行的目标 {@code DataSource}（永不为 {@code null}）
	 * @throws ScriptException 发生错误时
	 * @since 4.1
	 * @see #populate(Connection)
	 */
	public void execute(DataSource dataSource) throws ScriptException {
		DatabasePopulatorUtils.execute(this, dataSource);
	}

}
