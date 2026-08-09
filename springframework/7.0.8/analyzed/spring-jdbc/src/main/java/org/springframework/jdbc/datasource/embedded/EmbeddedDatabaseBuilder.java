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
 * 一个构建器，提供方便的 API 来构建嵌入式数据库。
 * <h3>使用示例</h3> <pre class="code"> EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
 * .generateUniqueName(true) .setType(H2) .setScriptEncoding("UTF-8")
 * .ignoreFailedDrops(true) .addScript("schema.sql") .addScripts("user_data.sql",
 * "country_data.sql") .build();
 * // 对数据库执行操作（EmbeddedDatabase 扩展了 javax.sql.DataSource）
 * db.shutdown(); OCAJAVA0文档
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

	/** 工厂相关状态（`databaseFactory`）。 */
	private final EmbeddedDatabaseFactory databaseFactory;

	/** `databasePopulator`：该类的成员状态。 */
	private final ResourceDatabasePopulator databasePopulator;

	/** 来源相关状态（`resourceLoader`）。 */
	private final ResourceLoader resourceLoader;


	/**
	 * 使用 {@link DefaultResourceLoader} 创建新的嵌入式数据库生成器。
	 */
	public EmbeddedDatabaseBuilder() {
		this(new DefaultResourceLoader());
	}

	/**
	 * 使用给定的 {@link ResourceLoader} 创建新的嵌入式数据库生成器。
	 * @param resourceLoader {@code ResourceLoader} 委托给
	 */
	public EmbeddedDatabaseBuilder(ResourceLoader resourceLoader) {
		this.databaseFactory = new EmbeddedDatabaseFactory();
		this.databasePopulator = new ResourceDatabasePopulator();
		this.databaseFactory.setDatabasePopulator(this.databasePopulator);
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 指定是否应生成唯一 ID 并将其用作数据库名称。 <p>如果此构建器的配置在单个 JVM 内的多个应用程序上下文中重用，则此标志应为 <em>enabled</em>（即设置为
	 *  {@code true}），以确保每个应用程序上下文都有自己的嵌入式数据库。 <p>启用此标志将覆盖通过 {@link #setName} 设置的任何显式名称。
	 * @param flag {@code true} 是否应生成唯一的数据库名称
	 * @return this}，以促进方法链接
	 * @since 4.2
	 * @see #setName
	 */
	public EmbeddedDatabaseBuilder generateUniqueName(boolean flag) {
		this.databaseFactory.setGenerateUniqueDatabaseName(flag);
		return this;
	}

	/**
	 * 设置嵌入数据库的名称。 <p> 如果未调用，则默认为 {@link EmbeddedDatabaseFactory#DEFAULT_DATABASE_NAME}。如果
	 * {@code generateUniqueName} 标志已设置为 {@code true}，则 <p> 将被覆盖。
	 * @param databaseName 要构建的嵌入式数据库的名称
	 * @return this}，以促进方法链接
	 * @see #generateUniqueName
	 */
	public EmbeddedDatabaseBuilder setName(String databaseName) {
		this.databaseFactory.setDatabaseName(databaseName);
		return this;
	}

	/**
	 * 设置嵌入式数据库的类型。如果需要自定义连接属性，请考虑使用 {@link #setDatabaseConfigurer}。 <p>如果不调用则默认为HSQL。
	 * @param databaseType 要构建的嵌入式数据库的类型
	 * @return this}，以促进方法链接
	 */
	public EmbeddedDatabaseBuilder setType(EmbeddedDatabaseType databaseType) {
		this.databaseFactory.setDatabaseType(databaseType);
		return this;
	}

	/**
	 * 设置 {@linkplain EmbeddedDatabaseConfigurer configurer} 以用于配置嵌入式数据库，作为 {@link #setType}
	 * 的替代方案。
	 * @param configurer 嵌入式数据库的配置器
	 * @return this}，以促进方法链接
	 * @since 6.2
	 * @see EmbeddedDatabaseConfigurers
	 */
	public EmbeddedDatabaseBuilder setDatabaseConfigurer(EmbeddedDatabaseConfigurer configurer) {
		this.databaseFactory.setDatabaseConfigurer(configurer);
		return this;
	}

	/**
	 * 设置用于创建连接到嵌入式数据库的 {@link DataSource} 实例的工厂。 <p>D默认为 {@link
	 * SimpleDriverDataSourceFactory}，但可以覆盖，例如引入连接池。
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		Assert.notNull(dataSourceFactory, "DataSourceFactory is required");
		this.databaseFactory.setDataSourceFactory(dataSourceFactory);
		return this;
	}

	/**
	 * 添加要执行的默认 SQL 脚本以填充数据库。 <p> 默认脚本是用于创建数据库架构的 {@code "schema.sql"} 和用于使用数据填充数据库的 {@code "da
	 * ta.sql"}。
	 * @return this}，以促进方法链接
	 */
	public EmbeddedDatabaseBuilder addDefaultScripts() {
		return addScripts("schema.sql", "data.sql");
	}

	/**
	 * 添加要执行的 SQL 脚本以初始化或填充数据库。
	 * @param script 要执行的脚本
	 * @return this}，以促进方法链接
	 */
	public EmbeddedDatabaseBuilder addScript(String script) {
		this.databasePopulator.addScript(this.resourceLoader.getResource(script));
		return this;
	}

	/**
	 * 添加多个要执行的 SQL 脚本来初始化或填充数据库。
	 * @param scripts 要执行的脚本
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder addScripts(String... scripts) {
		for (String script : scripts) {
			addScript(script);
		}
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中使用的字符编码（如果与平台编码不同）。
	 * @param scriptEncoding 脚本中使用的编码
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setScriptEncoding(String scriptEncoding) {
		this.databasePopulator.setSqlScriptEncoding(scriptEncoding);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中使用的语句分隔符（如果是自定义的）。 <p> 如果未指定，则默认为 {@code ";"}，并回退到 {@code "\n"} 作为最后的手段；可以设置
	 * 为 {@link ScriptUtils#EOF_STATEMENT_SEPARATOR} 以表明每个脚本包含一个不带分隔符的语句。
	 * @param separator 语句分隔符
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder setSeparator(String separator) {
		this.databasePopulator.setSeparator(separator);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中使用的单行注释前缀。 <p>默认为 {@code "--"}。
	 * @param commentPrefix 单行注释的前缀
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 * @see #setCommentPrefixes(String...)
	 */
	public EmbeddedDatabaseBuilder setCommentPrefix(String commentPrefix) {
		this.databasePopulator.setCommentPrefix(commentPrefix);
		return this;
	}

	/**
	 * 指定在所有 SQL 脚本中标识单行注释的前缀。 <p>默认为 {@code ["--"]}。
	 * @param commentPrefixes 单行注释的前缀
	 * @return this}，以促进方法链接
	 * @since 5.2
	 */
	public EmbeddedDatabaseBuilder setCommentPrefixes(String... commentPrefixes) {
		this.databasePopulator.setCommentPrefixes(commentPrefixes);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中块注释的起始分隔符。 <p>默认为 {@code "/*"}。
	 * @param blockCommentStartDelimiter 块注释的起始分隔符
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 * @see #setBlockCommentEndDelimiter
	 */
	public EmbeddedDatabaseBuilder setBlockCommentStartDelimiter(String blockCommentStartDelimiter) {
		this.databasePopulator.setBlockCommentStartDelimiter(blockCommentStartDelimiter);
		return this;
	}

	/**
	 * 指定所有 SQL 脚本中块注释的结束分隔符。 <p>默认为 <code>"*&#47;"</code>。
	 * @param blockCommentEndDelimiter 块注释的结束分隔符
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 * @see #setBlockCommentStartDelimiter
	 */
	public EmbeddedDatabaseBuilder setBlockCommentEndDelimiter(String blockCommentEndDelimiter) {
		this.databasePopulator.setBlockCommentEndDelimiter(blockCommentEndDelimiter);
		return this;
	}

	/**
	 * 指定应记录执行 SQL 脚本时发生的所有失败，但不应导致失败。 <p>默认为 {@code false}。
	 * @param flag {@code true} 如果脚本执行出现错误应继续
	 * @return this}，以促进方法链接
	 * @since 4.0.3
	 */
	public EmbeddedDatabaseBuilder continueOnError(boolean flag) {
		this.databasePopulator.setContinueOnError(flag);
		return this;
	}

	/**
	 * 指定可以忽略已执行脚本中失败的 SQL {@code DROP} 语句。 <p> 这对于 SQL 方言不支持 {@code DROP} 语句中的 {@code IF
	 * EXISTS} 子句的数据库很有用。 <p>默认为 {@code false}，因此如果脚本以 {@code DROP} 语句开头，{@link #build
	 * building} 将快速失败。
	 * @param flag {@code true} 如果失败的删除语句应被忽略
	 * @return this}，以促进方法链接
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
