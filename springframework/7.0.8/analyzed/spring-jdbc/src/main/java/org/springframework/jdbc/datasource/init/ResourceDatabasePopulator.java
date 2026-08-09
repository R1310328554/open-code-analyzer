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
 * <ul> <li>调用 {@link #addScript} 以添加单个 SQL 脚本位置。 <li>调用{@link #addScripts}添加多个SQL脚本位置。
 * <li>请参阅此类中的 setter 方法以获取更多配置选项。 <li>调用 {@link #populate} 或 {@link #execute}
 * 使用配置的脚本初始化或清理数据库。 OCAJAVA9文档
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

	/** `sqlScriptEncoding`：该类的成员状态。 */
	private @Nullable String sqlScriptEncoding;

	private String separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR;

	private String[] commentPrefixes = ScriptUtils.DEFAULT_COMMENT_PREFIXES;

	private String blockCommentStartDelimiter = ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER;

	private String blockCommentEndDelimiter = ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER;

	/** `false`：该类的成员状态。 */
	private boolean continueOnError = false;

	/** `false`：该类的成员状态。 */
	private boolean ignoreFailedDrops = false;


	/**
	 * 使用默认设置构造一个新的 {@code ResourceDatabasePopulator}。
	 * @since 4.0.3
	 */
	public ResourceDatabasePopulator() {
	}

	/**
	 * 使用所提供脚本的默认设置构造一个新的 {@code ResourceDatabasePopulator}。
	 * @param scripts 执行初始化或清理数据库的脚本（绝不是 {@code null}）
	 * @since 4.0.3
	 */
	public ResourceDatabasePopulator(Resource... scripts) {
		setScripts(scripts);
	}

	/**
	 * 使用提供的值构造一个新的 {@code ResourceDatabasePopulator}。
	 * @param continueOnError 标志指示应记录 SQL 中的所有故障但不会导致故障
	 * @param ignoreFailedDrops 标志指示可以忽略失败的 SQL {@code DROP} 语句
	 * @param sqlScriptEncoding 提供的 SQL 脚本的编码（可能是 {@code null} 或 <em>empty</em> 来指示平台编码）
	 * @param scripts 执行初始化或清理数据库的脚本（绝不是 {@code null}）
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
	 * 添加要执行的脚本以初始化或清理数据库。
	 * @param script SQL 脚本的路径（绝不是 {@code null}）
	 */
	public void addScript(Resource script) {
		Assert.notNull(script, "'script' must not be null");
		this.scripts.add(script);
	}

	/**
	 * 添加多个要执行的脚本来初始化或清理数据库。
	 * @param scripts 要执行的脚本（绝不是 {@code null}）
	 */
	public void addScripts(Resource... scripts) {
		assertContentsOfScriptArray(scripts);
		this.scripts.addAll(Arrays.asList(scripts));
	}

	/**
	 * 设置要执行的脚本以初始化或清理数据库，替换任何以前添加的脚本。
	 * @param scripts 要执行的脚本（绝不是 {@code null}）
	 */
	public void setScripts(Resource... scripts) {
		assertContentsOfScriptArray(scripts);
		// 确保列表可修改
		this.scripts = new ArrayList<>(Arrays.asList(scripts));
	}

	/**
	 * 方法 `assertContentsOfScriptArray`：完成本类中与「assert Contents Of Script Array」相关的职责。
	 */
	private void assertContentsOfScriptArray(Resource... scripts) {
		Assert.notNull(scripts, "'scripts' must not be null");
		Assert.noNullElements(scripts, "'scripts' must not contain null elements");
	}

	/**
	 * 如果与平台编码不同，请指定已配置 SQL 脚本的编码。
	 * @param sqlScriptEncoding 脚本中使用的编码（可以是 {@code null} 或空以指示平台编码）
	 * @see #addScript(Resource)
	 */
	public void setSqlScriptEncoding(@Nullable String sqlScriptEncoding) {
		this.sqlScriptEncoding = (StringUtils.hasText(sqlScriptEncoding) ? sqlScriptEncoding : null);
	}

	/**
	 * 指定语句分隔符（如果是自定义分隔符）。 <p> 如果未指定，则默认为 {@code ";"}，并回退到 {@code "\n"} 作为最后的手段；可以设置为 {@link Sc
	 * riptUtils#EOF_STATEMENT_SEPARATOR} 以表明每个脚本包含一个不带分隔符的语句。
	 * @param separator 脚本语句分隔符
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * 设置标识 SQL 脚本中的单行注释的前缀。 <p>默认为 {@code "--"}。
	 * @param commentPrefix 单行注释的前缀
	 * @see #setCommentPrefixes(String...)
	 */
	public void setCommentPrefix(String commentPrefix) {
		Assert.hasText(commentPrefix, "'commentPrefix' must not be null or empty");
		this.commentPrefixes = new String[] { commentPrefix };
	}

	/**
	 * 设置用于标识 SQL 脚本中的单行注释的前缀。 <p>默认为 {@code ["--"]}。
	 * @param commentPrefixes 单行注释的前缀
	 * @since 5.2
	 */
	public void setCommentPrefixes(String... commentPrefixes) {
		Assert.notEmpty(commentPrefixes, "'commentPrefixes' must not be null or empty");
		Assert.noNullElements(commentPrefixes, "'commentPrefixes' must not contain null elements");
		this.commentPrefixes = commentPrefixes;
	}

	/**
	 * 设置标识 SQL 脚本中的块注释的起始分隔符。 <p>默认为 {@code "/*"}。
	 * @param blockCommentStartDelimiter 块注释的起始分隔符（绝不是 {@code null} 或空）
	 * @since 4.0.3
	 * @see #setBlockCommentEndDelimiter
	 */
	public void setBlockCommentStartDelimiter(String blockCommentStartDelimiter) {
		Assert.hasText(blockCommentStartDelimiter, "'blockCommentStartDelimiter' must not be null or empty");
		this.blockCommentStartDelimiter = blockCommentStartDelimiter;
	}

	/**
	 * 设置标识 SQL 脚本内的块注释的结束分隔符。 <p>默认为 <code>"*&#47;"</code>。
	 * @param blockCommentEndDelimiter 块注释的结束分隔符（绝不是 {@code null} 或空）
	 * @since 4.0.3
	 * @see #setBlockCommentStartDelimiter
	 */
	public void setBlockCommentEndDelimiter(String blockCommentEndDelimiter) {
		Assert.hasText(blockCommentEndDelimiter, "'blockCommentEndDelimiter' must not be null or empty");
		this.blockCommentEndDelimiter = blockCommentEndDelimiter;
	}

	/**
	 * 指示应记录 SQL 中的所有故障但不会导致故障的标志。 <p>默认为 {@code false}。
	 * @param continueOnError {@code true} 如果脚本执行出现错误应继续
	 */
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	/**
	 * 指示可以忽略失败的 SQL {@code DROP} 语句的标志。 <p>这对于 SQL 方言不支持 {@code DROP} 语句中的 {@code IF EXISTS} 子
	 * 句的非嵌入式数据库很有用。 <p>默认为 {@code false}，因此如果填充器意外运行，如果脚本以 {@code DROP} 语句开头，它将快速失败。
	 * @param ignoreFailedDrops {@code true} 如果失败的删除语句应被忽略
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
	 * 针对给定的 {@link DataSource} 执行此 {@code ResourceDatabasePopulator}。 <p>委托给 {@link
	 * DatabasePopulatorUtils#execute}。
	 * @param dataSource 要执行的 {@code DataSource}（绝不是 {@code null}）
	 * @throws ScriptException 如果发生错误
	 * @since 4.1
	 * @see #populate(Connection)
	 */
	public void execute(DataSource dataSource) throws ScriptException {
		DatabasePopulatorUtils.execute(this, dataSource);
	}

}
