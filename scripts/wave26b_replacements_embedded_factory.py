"""Chinese JavaDoc replacements for springframework wave26b embedded builder/factory classes."""

EMBEDDED_FACTORY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "EmbeddedDatabaseBuilder.java": [
        (
            "/**\n * A builder that provides a convenient API for constructing an embedded database.\n *\n * <h3>Usage Example</h3>\n * <pre class=\"code\">\n * EmbeddedDatabase db = new EmbeddedDatabaseBuilder()\n *     .generateUniqueName(true)\n *     .setType(H2)\n *     .setScriptEncoding(\"UTF-8\")\n *     .ignoreFailedDrops(true)\n *     .addScript(\"schema.sql\")\n *     .addScripts(\"user_data.sql\", \"country_data.sql\")\n *     .build();\n *\n * // perform actions against the db (EmbeddedDatabase extends javax.sql.DataSource)\n *\n * db.shutdown();\n * </pre>\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @author Dave Syer\n * @author Sam Brannen\n * @since 3.0\n * @see org.springframework.jdbc.datasource.init.ScriptUtils\n * @see org.springframework.jdbc.datasource.init.ResourceDatabasePopulator\n * @see org.springframework.jdbc.datasource.init.DatabasePopulatorUtils\n */",
            "/**\n * 提供便捷 API 以构建嵌入式数据库的 Builder。\n *\n * <h3>使用示例</h3>\n * <pre class=\"code\">\n * EmbeddedDatabase db = new EmbeddedDatabaseBuilder()\n *     .generateUniqueName(true)\n *     .setType(H2)\n *     .setScriptEncoding(\"UTF-8\")\n *     .ignoreFailedDrops(true)\n *     .addScript(\"schema.sql\")\n *     .addScripts(\"user_data.sql\", \"country_data.sql\")\n *     .build();\n *\n * // 对数据库执行操作（EmbeddedDatabase 继承 javax.sql.DataSource）\n *\n * db.shutdown();\n * </pre>\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @author Dave Syer\n * @author Sam Brannen\n * @since 3.0\n * @see org.springframework.jdbc.datasource.init.ScriptUtils\n * @see org.springframework.jdbc.datasource.init.ResourceDatabasePopulator\n * @see org.springframework.jdbc.datasource.init.DatabasePopulatorUtils\n */",
        ),
        (
            "\t/**\n\t * Create a new embedded database builder with a {@link DefaultResourceLoader}.\n\t */",
            "\t/**\n\t * 使用 {@link DefaultResourceLoader} 创建嵌入式数据库 Builder。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new embedded database builder with the given {@link ResourceLoader}.\n\t * @param resourceLoader the {@code ResourceLoader} to delegate to\n\t */",
            "\t/**\n\t * 使用给定 {@link ResourceLoader} 创建嵌入式数据库 Builder。\n\t * @param resourceLoader 要委托的 {@code ResourceLoader}\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether a unique ID should be generated and used as the database name.\n\t * <p>If the configuration for this builder is reused across multiple\n\t * application contexts within a single JVM, this flag should be <em>enabled</em>\n\t * (i.e., set to {@code true}) in order to ensure that each application context\n\t * gets its own embedded database.\n\t * <p>Enabling this flag overrides any explicit name set via {@link #setName}.\n\t * @param flag {@code true} if a unique database name should be generated\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.2\n\t * @see #setName\n\t */",
            "\t/**\n\t * 指定是否生成唯一 ID 作为数据库名称。\n\t * <p>若同一 JVM 内多个应用上下文复用此 Builder 配置，\n\t * 应<em>启用</em>此标志（设为 {@code true}），\n\t * 以确保每个应用上下文拥有独立嵌入式数据库。\n\t * <p>启用此标志将覆盖 {@link #setName} 设置的显式名称。\n\t * @param flag 是否生成唯一数据库名称\n\t * @return {@code this}，便于链式调用\n\t * @since 4.2\n\t * @see #setName\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the embedded database.\n\t * <p>Defaults to {@link EmbeddedDatabaseFactory#DEFAULT_DATABASE_NAME} if\n\t * not called.\n\t * <p>Will be overridden if the {@code generateUniqueName} flag has been\n\t * set to {@code true}.\n\t * @param databaseName the name of the embedded database to build\n\t * @return {@code this}, to facilitate method chaining\n\t * @see #generateUniqueName\n\t */",
            "\t/**\n\t * 设置嵌入式数据库名称。\n\t * <p>未调用时默认为 {@link EmbeddedDatabaseFactory#DEFAULT_DATABASE_NAME}。\n\t * <p>若 {@code generateUniqueName} 标志为 {@code true} 则被覆盖。\n\t * @param databaseName 要构建的嵌入式数据库名称\n\t * @return {@code this}，便于链式调用\n\t * @see #generateUniqueName\n\t */",
        ),
        (
            "\t/**\n\t * Set the type of embedded database. Consider using {@link #setDatabaseConfigurer}\n\t * if customization of the connections properties is necessary.\n\t * <p>Defaults to HSQL if not called.\n\t * @param databaseType the type of embedded database to build\n\t * @return {@code this}, to facilitate method chaining\n\t */",
            "\t/**\n\t * 设置嵌入式数据库类型。若需定制连接属性，可考虑 {@link #setDatabaseConfigurer}。\n\t * <p>未调用时默认为 HSQL。\n\t * @param databaseType 要构建的嵌入式数据库类型\n\t * @return {@code this}，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@linkplain EmbeddedDatabaseConfigurer configurer} to use to\n\t * configure the embedded database, as an alternative to {@link #setType}.\n\t * @param configurer the configurer of the embedded database\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 6.2\n\t * @see EmbeddedDatabaseConfigurers\n\t */",
            "\t/**\n\t * 设置用于配置嵌入式数据库的 {@linkplain EmbeddedDatabaseConfigurer 配置器}，\n\t * 作为 {@link #setType} 的替代。\n\t * @param configurer 嵌入式数据库配置器\n\t * @return {@code this}，便于链式调用\n\t * @since 6.2\n\t * @see EmbeddedDatabaseConfigurers\n\t */",
        ),
        (
            "\t/**\n\t * Set the factory to use to create the {@link DataSource} instance that\n\t * connects to the embedded database.\n\t * <p>Defaults to {@link SimpleDriverDataSourceFactory} but can be overridden,\n\t * for example to introduce connection pooling.\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 设置用于创建连接嵌入式数据库的 {@link DataSource} 实例的工厂。\n\t * <p>默认为 {@link SimpleDriverDataSourceFactory}，可覆盖，例如引入连接池。\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Add default SQL scripts to execute to populate the database.\n\t * <p>The default scripts are {@code \"schema.sql\"} to create the database\n\t * schema and {@code \"data.sql\"} to populate the database with data.\n\t * @return {@code this}, to facilitate method chaining\n\t */",
            "\t/**\n\t * 添加用于填充数据库的默认 SQL 脚本。\n\t * <p>默认脚本为创建数据库结构的 {@code \"schema.sql\"}\n\t * 和填充数据的 {@code \"data.sql\"}。\n\t * @return {@code this}，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Add an SQL script to execute to initialize or populate the database.\n\t * @param script the script to execute\n\t * @return {@code this}, to facilitate method chaining\n\t */",
            "\t/**\n\t * 添加用于初始化或填充数据库的 SQL 脚本。\n\t * @param script 要执行的脚本\n\t * @return {@code this}，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Add multiple SQL scripts to execute to initialize or populate the database.\n\t * @param scripts the scripts to execute\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 添加多个用于初始化或填充数据库的 SQL 脚本。\n\t * @param scripts 要执行的脚本\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Specify the character encoding used in all SQL scripts, if different from\n\t * the platform encoding.\n\t * @param scriptEncoding the encoding used in scripts\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本的字符编码（若与平台编码不同）。\n\t * @param scriptEncoding 脚本使用的编码\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Specify the statement separator used in all SQL scripts, if a custom one.\n\t * <p>Defaults to {@code \";\"} if not specified and falls back to {@code \"\\n\"}\n\t * as a last resort; may be set to {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}\n\t * to signal that each script contains a single statement without a separator.\n\t * @param separator the statement separator\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本的语句分隔符（若使用自定义分隔符）。\n\t * <p>未指定时默认为 {@code \";\"}，最后回退为 {@code \"\\n\"}；\n\t * 可设为 {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}\n\t * 表示每个脚本为无分隔符的单条语句。\n\t * @param separator 语句分隔符\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Specify the single-line comment prefix used in all SQL scripts.\n\t * <p>Defaults to {@code \"--\"}.\n\t * @param commentPrefix the prefix for single-line comments\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t * @see #setCommentPrefixes(String...)\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本的单行注释前缀。\n\t * <p>默认为 {@code \"--\"}。\n\t * @param commentPrefix 单行注释前缀\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t * @see #setCommentPrefixes(String...)\n\t */",
        ),
        (
            "\t/**\n\t * Specify the prefixes that identify single-line comments within all SQL scripts.\n\t * <p>Defaults to {@code [\"--\"]}.\n\t * @param commentPrefixes the prefixes for single-line comments\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本中标识单行注释的前缀。\n\t * <p>默认为 {@code [\"--\"]}。\n\t * @param commentPrefixes 单行注释前缀\n\t * @return {@code this}，便于链式调用\n\t * @since 5.2\n\t */",
        ),
        (
            "\t/**\n\t * Specify the start delimiter for block comments in all SQL scripts.\n\t * <p>Defaults to {@code \"/*\"}.\n\t * @param blockCommentStartDelimiter the start delimiter for block comments\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t * @see #setBlockCommentEndDelimiter\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本中块注释的起始分隔符。\n\t * <p>默认为 {@code \"/*\"}。\n\t * @param blockCommentStartDelimiter 块注释起始分隔符\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t * @see #setBlockCommentEndDelimiter\n\t */",
        ),
        (
            "\t/**\n\t * Specify the end delimiter for block comments in all SQL scripts.\n\t * <p>Defaults to <code>\"*&#47;\"</code>.\n\t * @param blockCommentEndDelimiter the end delimiter for block comments\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t * @see #setBlockCommentStartDelimiter\n\t */",
            "\t/**\n\t * 指定所有 SQL 脚本中块注释的结束分隔符。\n\t * <p>默认为 <code>\"*&#47;\"</code>。\n\t * @param blockCommentEndDelimiter 块注释结束分隔符\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t * @see #setBlockCommentStartDelimiter\n\t */",
        ),
        (
            "\t/**\n\t * Specify that all failures which occur while executing SQL scripts should\n\t * be logged but should not cause a failure.\n\t * <p>Defaults to {@code false}.\n\t * @param flag {@code true} if script execution should continue on error\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 指定执行 SQL 脚本时的所有失败应记录日志但不导致失败。\n\t * <p>默认为 {@code false}。\n\t * @param flag 出错时是否继续执行脚本\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Specify that a failed SQL {@code DROP} statement within an executed\n\t * script can be ignored.\n\t * <p>This is useful for a database whose SQL dialect does not support an\n\t * {@code IF EXISTS} clause in a {@code DROP} statement.\n\t * <p>The default is {@code false} so that {@link #build building} will fail\n\t * fast if a script starts with a {@code DROP} statement.\n\t * @param flag {@code true} if failed drop statements should be ignored\n\t * @return {@code this}, to facilitate method chaining\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 指定可忽略已执行脚本中失败的 SQL {@code DROP} 语句。\n\t * <p>适用于 SQL 方言在 {@code DROP} 语句中不支持 {@code IF EXISTS} 的数据库。\n\t * <p>默认为 {@code false}，脚本以 {@code DROP} 开头时 {@link #build 构建} 将快速失败。\n\t * @param flag 是否忽略失败的 DROP 语句\n\t * @return {@code this}，便于链式调用\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Build the embedded database.\n\t * @return the embedded database\n\t */",
            "\t/**\n\t * 构建嵌入式数据库。\n\t * @return 嵌入式数据库\n\t */",
        ),
    ],
    "EmbeddedDatabaseFactory.java": [
        (
            "/**\n * Factory for creating an {@link EmbeddedDatabase} instance.\n *\n * <p>Callers are guaranteed that the returned database has been fully\n * initialized and populated.\n *\n * <p>The factory can be configured as follows:\n * <ul>\n * <li>Call {@link #generateUniqueDatabaseName} to set a unique, random name\n * for the database.\n * <li>Call {@link #setDatabaseName} to set an explicit name for the database.\n * <li>Call {@link #setDatabaseType} to set the database type if you wish to\n * use one of the pre-supported types with its default settings.\n * <li>Call {@link #setDatabaseConfigurer} to configure support for a custom\n * embedded database type, or\n * {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer customize} the\n * defaults for one of the pre-supported types.\n * <li>Call {@link #setDatabasePopulator} to change the algorithm used to\n * populate the database.\n * <li>Call {@link #setDataSourceFactory} to change the type of\n * {@link DataSource} used to connect to the database.\n * </ul>\n *\n * <p>After configuring the factory, call {@link #getDatabase()} to obtain\n * a reference to the {@link EmbeddedDatabase} instance.\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 3.0\n */",
            "/**\n * 创建 {@link EmbeddedDatabase} 实例的工厂。\n *\n * <p>调用方保证返回的数据库已完全初始化并填充。\n *\n * <p>可按如下方式配置工厂：\n * <ul>\n * <li>调用 {@link #generateUniqueDatabaseName} 设置唯一随机数据库名称。\n * <li>调用 {@link #setDatabaseName} 设置显式数据库名称。\n * <li>调用 {@link #setDatabaseType} 设置数据库类型，以使用预支持类型的默认设置。\n * <li>调用 {@link #setDatabaseConfigurer} 配置自定义嵌入式数据库类型，\n * 或 {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer 定制} 预支持类型的默认值。\n * <li>调用 {@link #setDatabasePopulator} 更改填充数据库的算法。\n * <li>调用 {@link #setDataSourceFactory} 更改连接数据库的 {@link DataSource} 类型。\n * </ul>\n *\n * <p>配置完成后，调用 {@link #getDatabase()} 获取 {@link EmbeddedDatabase} 实例引用。\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Default name for an embedded database: {@value}.\n\t */",
            "\t/**\n\t * 嵌入式数据库的默认名称：{@value}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@code generateUniqueDatabaseName} flag to enable or disable\n\t * generation of a pseudo-random unique ID to be used as the database name.\n\t * <p>Setting this flag to {@code true} overrides any explicit name set\n\t * via {@link #setDatabaseName}.\n\t * @since 4.2\n\t * @see #setDatabaseName\n\t */",
            "\t/**\n\t * 设置 {@code generateUniqueDatabaseName} 标志，启用或禁用\n\t * 生成伪随机唯一 ID 作为数据库名称。\n\t * <p>设为 {@code true} 将覆盖 {@link #setDatabaseName} 设置的显式名称。\n\t * @since 4.2\n\t * @see #setDatabaseName\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the database.\n\t * <p>Defaults to {@value #DEFAULT_DATABASE_NAME}.\n\t * <p>Will be overridden if the {@code generateUniqueDatabaseName} flag\n\t * has been set to {@code true}.\n\t * @param databaseName name of the embedded database\n\t * @see #setGenerateUniqueDatabaseName\n\t */",
            "\t/**\n\t * 设置数据库名称。\n\t * <p>默认为 {@value #DEFAULT_DATABASE_NAME}。\n\t * <p>若 {@code generateUniqueDatabaseName} 标志为 {@code true} 则被覆盖。\n\t * @param databaseName 嵌入式数据库名称\n\t * @see #setGenerateUniqueDatabaseName\n\t */",
        ),
        (
            "\t/**\n\t * Set the factory to use to create the {@link DataSource} instance that\n\t * connects to the embedded database.\n\t * <p>Defaults to {@link SimpleDriverDataSourceFactory}.\n\t */",
            "\t/**\n\t * 设置用于创建连接嵌入式数据库的 {@link DataSource} 实例的工厂。\n\t * <p>默认为 {@link SimpleDriverDataSourceFactory}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the type of embedded database to use.\n\t * <p>Call this when you wish to configure one of the pre-supported types\n\t * with its default settings.\n\t * <p>Defaults to HSQL.\n\t * @param type the database type\n\t */",
            "\t/**\n\t * 设置要使用的嵌入式数据库类型。\n\t * <p>若要以默认设置配置预支持类型之一，调用此方法。\n\t * <p>默认为 HSQL。\n\t * @param type 数据库类型\n\t */",
        ),
        (
            "\t/**\n\t * Set the strategy that will be used to configure the embedded database instance.\n\t * <p>Call this with\n\t * {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer customizeConfigurer}\n\t * when you wish to customize the settings of one of the pre-supported types.\n\t * Alternatively, use this when you wish to use an embedded database type not\n\t * already supported.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 设置用于配置嵌入式数据库实例的策略。\n\t * <p>若需定制预支持类型的设置，\n\t * 配合 {@linkplain EmbeddedDatabaseConfigurers#customizeConfigurer customizeConfigurer} 调用。\n\t * 也可用于尚未支持的嵌入式数据库类型。\n\t * @since 6.2\n\t */",
        ),
        (
            "\t/**\n\t * Set the strategy that will be used to initialize or populate the embedded\n\t * database.\n\t * <p>Defaults to {@code null}.\n\t */",
            "\t/**\n\t * 设置用于初始化或填充嵌入式数据库的策略。\n\t * <p>默认为 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Factory method that returns the {@linkplain EmbeddedDatabase embedded database}\n\t * instance, which is also a {@link DataSource}.\n\t */",
            "\t/**\n\t * 工厂方法，返回 {@linkplain EmbeddedDatabase 嵌入式数据库} 实例，\n\t * 同时也是 {@link DataSource}。\n\t */",
        ),
        (
            "\t/**\n\t * Hook to initialize the embedded database.\n\t * <p>If the {@code generateUniqueDatabaseName} flag has been set to {@code true},\n\t * the current value of the {@linkplain #setDatabaseName database name} will\n\t * be overridden with an auto-generated name.\n\t * <p>Subclasses may call this method to force initialization; however,\n\t * this method should only be invoked once.\n\t * <p>After calling this method, {@link #getDataSource()} returns the\n\t * {@link DataSource} providing connectivity to the database.\n\t */",
            "\t/**\n\t * 初始化嵌入式数据库的钩子。\n\t * <p>若 {@code generateUniqueDatabaseName} 标志为 {@code true}，\n\t * 当前 {@linkplain #setDatabaseName 数据库名称} 将被自动生成的名称覆盖。\n\t * <p>子类可调用此方法强制初始化，但仅应调用一次。\n\t * <p>调用后 {@link #getDataSource()} 返回提供数据库连接的 {@link DataSource}。\n\t */",
        ),
        (
            "\t/**\n\t * Hook to shut down the embedded database. Subclasses may call this method\n\t * to force shutdown.\n\t * <p>After calling, {@link #getDataSource()} returns {@code null}.\n\t * <p>Does nothing if no embedded database has been initialized.\n\t */",
            "\t/**\n\t * 关闭嵌入式数据库的钩子。子类可调用此方法强制关闭。\n\t * <p>调用后 {@link #getDataSource()} 返回 {@code null}。\n\t * <p>若未初始化嵌入式数据库则不执行任何操作。\n\t */",
        ),
        (
            "\t/**\n\t * Hook that gets the {@link DataSource} that provides the connectivity to the\n\t * embedded database.\n\t * <p>Returns {@code null} if the {@code DataSource} has not been initialized\n\t * or if the database has been shut down. Subclasses may call this method to\n\t * access the {@code DataSource} instance directly.\n\t */",
            "\t/**\n\t * 获取提供嵌入式数据库连接的 {@link DataSource} 的钩子。\n\t * <p>若 {@code DataSource} 未初始化或数据库已关闭则返回 {@code null}。\n\t * 子类可调用此方法直接访问 {@code DataSource} 实例。\n\t */",
        ),
        (
            "\t\t// Create the embedded database first",
            "\t\t// 先创建嵌入式数据库",
        ),
        (
            "\t\t// Now populate the database",
            "\t\t// 再填充数据库",
        ),
        (
            "\t\t\t\t// failed to populate, so leave it as not initialized",
            "\t\t\t\t// 填充失败，保持未初始化状态",
        ),
        (
            "\t\t// getParentLogger() is required for JDBC 4.1 compatibility",
            "\t\t// getParentLogger() 为 JDBC 4.1 兼容性所需",
        ),
    ],
}
