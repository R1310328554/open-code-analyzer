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

package org.springframework.jdbc.datasource.embedded;

import javax.sql.DataSource;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.util.Assert;

/**
 * 提供便捷 API 以构建嵌入式数据库的 Builder。
 *
 * <h3>使用示例</h3>
 * <pre class="code">
 * EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
 *     .generateUniqueName(true)
 *     .setType(H2)
 *     .setScriptEncoding("UTF-8")
 *     .ignoreFailedDrops(true)
 *     .addScript("schema.sql")
 *     .addScripts("user_data.sql", "country_data.sql")
 *     .build();
 *
 * // 对数据库执行操作（EmbeddedDatabase 继承 javax.sql.DataSource）
 *
 * db.shutdown();
 * </pre>
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Sam Brannen
 * @since 3.0
 * @see org.springframework.jdbc.datasource.init.ScriptUtils
 * @see org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
 * @see org.springframework.jdbc.datasource.init.DatabasePopulatorUtils
 */
public class EmbeddedDatabaseBuilder {

	private final EmbeddedDatabaseFactory databaseFactory;

	private final ResourceDatabasePopulator databasePopulator;

	private final ResourceLoader resourceLoader;


	/**
	 * 使用 {@link DefaultResourceLoader} 创建嵌入式数据库 Builder。
	 */
	public EmbeddedDatabaseBuilder() {
		this(new DefaultResourceLoader());
	}

	/**
	 * 使用给定 {@link ResourceLoader} 创建嵌入式数据库 Builder。
	 * @param resourceLoader 要委托的 {@code ResourceLoader}
	 */
	public EmbeddedDatabaseBuilder(ResourceLoader resourceLoader) {
		this.databaseFactory = new EmbeddedDatabaseFactory();
		this.databasePopulator = new ResourceDatabasePopulator();
		this.databaseFactory.setDatabasePopulator(this.databasePopulator);
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 指定是否生成唯一 ID 作为数据库名称。
	 * <p>若同一 JVM 内多个应用上下文复用此 Builder 配置，
	 * 应<em>启用</em>此标志（设为 {@code true}），
	 * 以确保每个应用上下文拥有独立嵌入式数据库。
	 * <p>启用此标志将覆盖 {@link #setName} 设置的显式名称。
	 * @param flag 是否生成唯一数据库名称
	 * @return {@code this}，便于链式调用
	 * @since 4.2
	 * @see #setName
	 */
	public EmbeddedDatabaseBuilder generateUniqueName(boolean flag) {
		this.databaseFactory.setGenerateUniqueDatabaseName(flag);
		return this;
	}

	/**
	 * 设置嵌入式数据库名称。
	 * <p>未调用时默认为 {@link EmbeddedDatabaseFactory#DEFAULT_DATABASE_NAME}。
	 * <p>若 {@code generateUniqueName} 标志为 {@code true} 则被覆盖。
	 * @param databaseName 要构建的嵌入式数据库名称
	 * @return {@code this}，便于链式调用
	 * @see #generateUniqueName
	 */
	public EmbeddedDatabaseBuilder setName(String databaseName) {
		this.databaseFactory.setDatabaseName(databaseName);
		return this;
	}

	/**
	 * 设置嵌入式数据库类型。若需定制连接属性，可考虑 {@link #setDatabaseConfigurer}。
	 * <p>未调用时默认为 HSQL。
	 * @param databaseType 要构建的嵌入式数据库类型
	 * @return {@code this}，便于链式调用
	 */
	public EmbeddedDatabaseBuilder setType(EmbeddedDatabaseType databaseType) {
		this.databaseFactory.setDatabaseType(databaseType);
		return this;
	}

	/**
	 * 设置用于配置嵌入式数据库的 {@linkplain EmbeddedDatabaseConfigurer 配置器}，
	 * 作为 {@link #setType} 的替代。
	 * @param configurer 嵌入式数据库配置器
	 * @return {@code this}，便于链式调用
	 * @since 6.2
	 * @see EmbeddedDatabaseConfigurers
	 */
	public EmbeddedDatabaseBuilder setDatabaseConfigurer(EmbeddedDatabaseConfigurer configurer) {
		this.databaseFactory.setDatabaseConfigurer(configurer);
		return this;
	}

	/**
	 * 设置用于创建连接嵌入式数据库的 {@link DataSource} 实例的工厂。
	 * <p>默认为 {@link SimpleDriverDataSourceFactory}，可覆盖，例如引入连接池。
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		Assert.notNull(dataSourceFactory, "DataSourceFactory is required");
		this.databaseFactory.setDataSourceFactory(dataSourceFactory);
		return this;
	}

	/**
	 * 添加用于填充数据库的默认 SQL 脚本。
	 * <p>默认脚本为创建数据库结构的 {@code "schema.sql"}
	 * 和填充数据的 {@code "data.sql"}。
	 * @return {@code this}，便于链式调用
	 */
	public EmbeddedDatabaseBuilder addDefaultScripts() {
		return addScripts("schema.sql", "data.sql");
	}

	/**
	 * 添加用于初始化或填充数据库的 SQL 脚本。
	 * @param script 要执行的脚本
	 * @return {@code this}，便于链式调用
	 */
	public EmbeddedDatabaseBuilder addScript(String script) {
		this.databasePopulator.addScript(this.resourceLoader.getResource(script));
		return this;
	}

	/**
	 * 添加多个用于初始化或填充数据库的 SQL 脚本。
	 * @param scripts 要执行的脚本
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder addScripts(String... scripts) {
		for (String script : scripts) {
			addScript(script);
		}
		return this;
	}

	/**
	 * 指定所有 SQL 脚本的字符编码（若与平台编码不同）。
	 * @param scriptEncoding 脚本使用的编码
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setScriptEncoding(String scriptEncoding) {
		this.databasePopulator.setSqlScriptEncoding(scriptEncoding);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本的语句分隔符（若使用自定义分隔符）。
	 * <p>未指定时默认为 {@code ";"}，最后回退为 {@code "\n"}；
	 * 可设为 {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}
	 * 表示每个脚本为无分隔符的单条语句。
	 * @param separator 语句分隔符
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setSeparator(String separator) {
		this.databasePopulator.setSeparator(separator);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本的单行注释前缀。
	 * <p>默认为 {@code "--"}。
	 * @param commentPrefix 单行注释前缀
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 * @see #setCommentPrefixes(String...)
	 */
	public EmbeddedDatabaseBuilder setCommentPrefix(String commentPrefix) {
		this.databasePopulator.setCommentPrefix(commentPrefix);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中标识单行注释的前缀。
	 * <p>默认为 {@code ["--"]}。
	 * @param commentPrefixes 单行注释前缀
	 * @return {@code this}，便于链式调用
	 * @since 5.2
	 */
	public EmbeddedDatabaseBuilder setCommentPrefixes(String... commentPrefixes) {
		this.databasePopulator.setCommentPrefixes(commentPrefixes);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中块注释的起始分隔符。
	 * <p>默认为 {@code "/*"}。
	 * @param blockCommentStartDelimiter 块注释起始分隔符
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 * @see #setBlockCommentEndDelimiter
	 */
	public EmbeddedDatabaseBuilder setBlockCommentStartDelimiter(String blockCommentStartDelimiter) {
		this.databasePopulator.setBlockCommentStartDelimiter(blockCommentStartDelimiter);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中块注释的结束分隔符。
	 * <p>默认为 <code>"*&#47;"</code>。
	 * @param blockCommentEndDelimiter 块注释结束分隔符
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 * @see #setBlockCommentStartDelimiter
	 */
	public EmbeddedDatabaseBuilder setBlockCommentEndDelimiter(String blockCommentEndDelimiter) {
		this.databasePopulator.setBlockCommentEndDelimiter(blockCommentEndDelimiter);
		return this;
	}

	/**
	 * 指定执行 SQL 脚本时的所有失败应记录日志但不导致失败。
	 * <p>默认为 {@code false}。
	 * @param flag 出错时是否继续执行脚本
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder continueOnError(boolean flag) {
		this.databasePopulator.setContinueOnError(flag);
		return this;
	}

	/**
	 * 指定可忽略已执行脚本中失败的 SQL {@code DROP} 语句。
	 * <p>适用于 SQL 方言在 {@code DROP} 语句中不支持 {@code IF EXISTS} 的数据库。
	 * <p>默认为 {@code false}，脚本以 {@code DROP} 开头时 {@link #build 构建} 将快速失败。
	 * @param flag 是否忽略失败的 DROP 语句
	 * @return {@code this}，便于链式调用
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder ignoreFailedDrops(boolean flag) {
		this.databasePopulator.setIgnoreFailedDrops(flag);
		return this;
	}

	/**
	 * 构建嵌入式数据库。
	 * @return 嵌入式数据库
	 */
	public EmbeddedDatabase build() {
		return this.databaseFactory.getDatabase();
	}

}
