"""Chinese JavaDoc replacements for springframework wave27a init classes (part B)."""

INIT_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompositeDatabasePopulator.java": [
        (
            "/**\n * Composite {@link DatabasePopulator} that delegates to a list of given\n * {@code DatabasePopulator} implementations, executing all scripts.\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Kazuki Shimizu\n * @since 3.1\n */",
            "/**\n * 组合式 {@link DatabasePopulator}，委托给一组 {@code DatabasePopulator}\n * 实现并执行全部脚本。\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @author Kazuki Shimizu\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * Create an empty {@code CompositeDatabasePopulator}.\n\t * @see #setPopulators\n\t * @see #addPopulators\n\t */",
            "\t/**\n\t * 创建空的 {@code CompositeDatabasePopulator}。\n\t * @see #setPopulators\n\t * @see #addPopulators\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code CompositeDatabasePopulator} with the given populators.\n\t * @param populators one or more populators to delegate to\n\t * @since 4.3\n\t */",
            "\t/**\n\t * 使用给定 populator 创建 {@code CompositeDatabasePopulator}。\n\t * @param populators 要委托的一个或多个 populator\n\t * @since 4.3\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code CompositeDatabasePopulator} with the given populators.\n\t * @param populators one or more populators to delegate to\n\t * @since 4.3\n\t */",
            "\t/**\n\t * 使用给定 populator 创建 {@code CompositeDatabasePopulator}。\n\t * @param populators 要委托的一个或多个 populator\n\t * @since 4.3\n\t */",
        ),
        (
            "\t/**\n\t * Specify one or more populators to delegate to.\n\t */",
            "\t/**\n\t * 指定要委托的一个或多个 populator。\n\t */",
        ),
        (
            "\t/**\n\t * Add one or more populators to the list of delegates.\n\t */",
            "\t/**\n\t * 向委托列表添加一个或多个 populator。\n\t */",
        ),
    ],
    "DataSourceInitializer.java": [
        (
            "/**\n * Used to {@linkplain #setDatabasePopulator set up} a database during\n * initialization and {@link #setDatabaseCleaner clean up} a database during\n * destruction.\n *\n * @author Dave Syer\n * @author Sam Brannen\n * @since 3.0\n * @see DatabasePopulator\n */",
            "/**\n * 在初始化阶段 {@linkplain #setDatabasePopulator 设置} 数据库，\n * 在销毁阶段 {@link #setDatabaseCleaner 清理} 数据库。\n *\n * @author Dave Syer\n * @author Sam Brannen\n * @since 3.0\n * @see DatabasePopulator\n */",
        ),
        (
            "\t/**\n\t * The {@link DataSource} for the database to populate when this component\n\t * is initialized and to clean up when this component is shut down.\n\t * <p>This property is mandatory with no default provided.\n\t * @param dataSource the DataSource\n\t */",
            "\t/**\n\t * 本组件初始化时要填充、关闭时要清理的数据库 {@link DataSource}。\n\t * <p>此属性为必填，无默认值。\n\t * @param dataSource DataSource\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link DatabasePopulator} to execute during the bean initialization phase,\n\t * if any.\n\t * @param databasePopulator the {@code DatabasePopulator} to use during initialization\n\t * @see #setDatabaseCleaner\n\t */",
            "\t/**\n\t * 设置 Bean 初始化阶段要执行的 {@link DatabasePopulator}（若有）。\n\t * @param databasePopulator 初始化时使用的 {@code DatabasePopulator}\n\t * @see #setDatabaseCleaner\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link DatabasePopulator} to execute during the bean destruction phase,\n\t * if any, cleaning up the database and leaving it in a known state for others.\n\t * @param databaseCleaner the {@code DatabasePopulator} to use during destruction\n\t * @see #setDatabasePopulator\n\t */",
            "\t/**\n\t * 设置 Bean 销毁阶段要执行的 {@link DatabasePopulator}（若有），\n\t * 用于清理数据库并使其处于已知状态。\n\t * @param databaseCleaner 销毁时使用的 {@code DatabasePopulator}\n\t * @see #setDatabasePopulator\n\t */",
        ),
        (
            "\t/**\n\t * Flag to explicitly enable or disable the {@linkplain #setDatabasePopulator\n\t * database populator} and {@linkplain #setDatabaseCleaner database cleaner}.\n\t * @param enabled {@code true} if the database populator and database cleaner\n\t * should be called on startup and shutdown, respectively\n\t */",
            "\t/**\n\t * 显式启用或禁用 {@linkplain #setDatabasePopulator 数据库填充器}\n\t * 与 {@linkplain #setDatabaseCleaner 数据库清理器}。\n\t * @param enabled 若为 {@code true}，启动时调用填充器、关闭时调用清理器\n\t */",
        ),
        (
            "\t/**\n\t * Use the {@linkplain #setDatabasePopulator database populator} to set up\n\t * the database.\n\t */",
            "\t/**\n\t * 使用 {@linkplain #setDatabasePopulator 数据库填充器} 设置数据库。\n\t */",
        ),
        (
            "\t/**\n\t * Use the {@linkplain #setDatabaseCleaner database cleaner} to clean up the\n\t * database.\n\t */",
            "\t/**\n\t * 使用 {@linkplain #setDatabaseCleaner 数据库清理器} 清理数据库。\n\t */",
        ),
    ],
    "ResourceDatabasePopulator.java": [
        (
            "/**\n * Populates, initializes, or cleans up a database using SQL scripts defined in\n * external resources.\n *\n * <ul>\n * <li>Call {@link #addScript} to add a single SQL script location.\n * <li>Call {@link #addScripts} to add multiple SQL script locations.\n * <li>Consult the setter methods in this class for further configuration options.\n * <li>Call {@link #populate} or {@link #execute} to initialize or clean up the\n * database using the configured scripts.\n * </ul>\n *\n * @author Keith Donald\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Oliver Gierke\n * @author Sam Brannen\n * @author Chris Baldwin\n * @author Phillip Webb\n * @since 3.0\n * @see DatabasePopulatorUtils\n * @see ScriptUtils\n */",
            "/**\n * 使用外部资源中定义的 SQL 脚本填充、初始化或清理数据库。\n *\n * <ul>\n * <li>调用 {@link #addScript} 添加单个 SQL 脚本位置。\n * <li>调用 {@link #addScripts} 添加多个 SQL 脚本位置。\n * <li>参阅本类 setter 方法获取更多配置选项。\n * <li>调用 {@link #populate} 或 {@link #execute} 使用已配置脚本初始化或清理数据库。\n * </ul>\n *\n * @author Keith Donald\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Oliver Gierke\n * @author Sam Brannen\n * @author Chris Baldwin\n * @author Phillip Webb\n * @since 3.0\n * @see DatabasePopulatorUtils\n * @see ScriptUtils\n */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ResourceDatabasePopulator} with default settings.\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 使用默认设置构造新的 {@code ResourceDatabasePopulator}。\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ResourceDatabasePopulator} with default settings\n\t * for the supplied scripts.\n\t * @param scripts the scripts to execute to initialize or clean up the database\n\t * (never {@code null})\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 使用默认设置构造新的 {@code ResourceDatabasePopulator}，并指定脚本。\n\t * @param scripts 用于初始化或清理数据库的脚本（永不为 {@code null}）\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ResourceDatabasePopulator} with the supplied values.\n\t * @param continueOnError flag to indicate that all failures in SQL should be\n\t * logged but not cause a failure\n\t * @param ignoreFailedDrops flag to indicate that a failed SQL {@code DROP}\n\t * statement can be ignored\n\t * @param sqlScriptEncoding the encoding for the supplied SQL scripts\n\t * (may be {@code null} or <em>empty</em> to indicate platform encoding)\n\t * @param scripts the scripts to execute to initialize or clean up the database\n\t * (never {@code null})\n\t * @since 4.0.3\n\t */",
            "\t/**\n\t * 使用给定参数构造新的 {@code ResourceDatabasePopulator}。\n\t * @param continueOnError 是否记录 SQL 失败但不导致整体失败\n\t * @param ignoreFailedDrops 是否忽略失败的 SQL {@code DROP} 语句\n\t * @param sqlScriptEncoding 所供 SQL 脚本的编码\n\t * （可为 {@code null} 或<em>空</em> 表示平台编码）\n\t * @param scripts 用于初始化或清理数据库的脚本（永不为 {@code null}）\n\t * @since 4.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Add a script to execute to initialize or clean up the database.\n\t * @param script the path to an SQL script (never {@code null})\n\t */",
            "\t/**\n\t * 添加用于初始化或清理数据库的脚本。\n\t * @param script SQL 脚本路径（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Add multiple scripts to execute to initialize or clean up the database.\n\t * @param scripts the scripts to execute (never {@code null})\n\t */",
            "\t/**\n\t * 添加多个用于初始化或清理数据库的脚本。\n\t * @param scripts 要执行的脚本（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Set the scripts to execute to initialize or clean up the database,\n\t * replacing any previously added scripts.\n\t * @param scripts the scripts to execute (never {@code null})\n\t */",
            "\t/**\n\t * 设置用于初始化或清理数据库的脚本，替换此前添加的所有脚本。\n\t * @param scripts 要执行的脚本（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Specify the encoding for the configured SQL scripts,\n\t * if different from the platform encoding.\n\t * @param sqlScriptEncoding the encoding used in scripts\n\t * (may be {@code null} or empty to indicate platform encoding)\n\t * @see #addScript(Resource)\n\t */",
            "\t/**\n\t * 指定已配置 SQL 脚本的编码（若与平台编码不同）。\n\t * @param sqlScriptEncoding 脚本使用的编码\n\t * （可为 {@code null} 或空表示平台编码）\n\t * @see #addScript(Resource)\n\t */",
        ),
        (
            "\t/**\n\t * Specify the statement separator, if a custom one.\n\t * <p>Defaults to {@code \";\"} if not specified and falls back to {@code \"\\n\"}\n\t * as a last resort; may be set to {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}\n\t * to signal that each script contains a single statement without a separator.\n\t * @param separator the script statement separator\n\t */",
            "\t/**\n\t * 指定语句分隔符（若使用自定义分隔符）。\n\t * <p>未指定时默认为 {@code \";\"}，最后回退为 {@code \"\\n\"}；\n\t * 可设为 {@link ScriptUtils#EOF_STATEMENT_SEPARATOR}\n\t * 表示每个脚本为无分隔符的单条语句。\n\t * @param separator 脚本语句分隔符\n\t */",
        ),
        (
            "\t/**\n\t * Set the prefix that identifies single-line comments within the SQL scripts.\n\t * <p>Defaults to {@code \"--\"}.\n\t * @param commentPrefix the prefix for single-line comments\n\t * @see #setCommentPrefixes(String...)\n\t */",
            "\t/**\n\t * 设置 SQL 脚本中标识单行注释的前缀。\n\t * <p>默认为 {@code \"--\"}。\n\t * @param commentPrefix 单行注释前缀\n\t * @see #setCommentPrefixes(String...)\n\t */",
        ),
        (
            "\t/**\n\t * Set the prefixes that identify single-line comments within the SQL scripts.\n\t * <p>Defaults to {@code [\"--\"]}.\n\t * @param commentPrefixes the prefixes for single-line comments\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 设置 SQL 脚本中标识单行注释的前缀。\n\t * <p>默认为 {@code [\"--\"]}。\n\t * @param commentPrefixes 单行注释前缀\n\t * @since 5.2\n\t */",
        ),
        (
            "\t/**\n\t * Set the start delimiter that identifies block comments within the SQL\n\t * scripts.\n\t * <p>Defaults to {@code \"/*\"}.\n\t * @param blockCommentStartDelimiter the start delimiter for block comments\n\t * (never {@code null} or empty)\n\t * @since 4.0.3\n\t * @see #setBlockCommentEndDelimiter\n\t */",
            "\t/**\n\t * 设置 SQL 脚本中标识块注释的起始分隔符。\n\t * <p>默认为 {@code \"/*\"}。\n\t * @param blockCommentStartDelimiter 块注释起始分隔符\n\t * （永不为 {@code null} 或空）\n\t * @since 4.0.3\n\t * @see #setBlockCommentEndDelimiter\n\t */",
        ),
        (
            "\t/**\n\t * Set the end delimiter that identifies block comments within the SQL\n\t * scripts.\n\t * <p>Defaults to <code>\"*&#47;\"</code>.\n\t * @param blockCommentEndDelimiter the end delimiter for block comments\n\t * (never {@code null} or empty)\n\t * @since 4.0.3\n\t * @see #setBlockCommentStartDelimiter\n\t */",
            "\t/**\n\t * 设置 SQL 脚本中标识块注释的结束分隔符。\n\t * <p>默认为 <code>\"*&#47;\"</code>。\n\t * @param blockCommentEndDelimiter 块注释结束分隔符\n\t * （永不为 {@code null} 或空）\n\t * @since 4.0.3\n\t * @see #setBlockCommentStartDelimiter\n\t */",
        ),
        (
            "\t/**\n\t * Flag to indicate that all failures in SQL should be logged but not cause a failure.\n\t * <p>Defaults to {@code false}.\n\t * @param continueOnError {@code true} if script execution should continue on error\n\t */",
            "\t/**\n\t * 是否记录 SQL 失败但不导致整体失败。\n\t * <p>默认为 {@code false}。\n\t * @param continueOnError 出错时是否继续执行脚本\n\t */",
        ),
        (
            "\t/**\n\t * Flag to indicate that a failed SQL {@code DROP} statement can be ignored.\n\t * <p>This is useful for a non-embedded database whose SQL dialect does not\n\t * support an {@code IF EXISTS} clause in a {@code DROP} statement.\n\t * <p>The default is {@code false} so that if the populator runs accidentally, it will\n\t * fail fast if a script starts with a {@code DROP} statement.\n\t * @param ignoreFailedDrops {@code true} if failed drop statements should be ignored\n\t */",
            "\t/**\n\t * 是否忽略失败的 SQL {@code DROP} 语句。\n\t * <p>适用于 SQL 方言在 {@code DROP} 语句中不支持 {@code IF EXISTS} 的非嵌入式数据库。\n\t * <p>默认为 {@code false}，若 populator 意外运行且脚本以 {@code DROP} 开头将快速失败。\n\t * @param ignoreFailedDrops 是否忽略失败的 DROP 语句\n\t */",
        ),
        (
            "\t/**\n\t * Execute this {@code ResourceDatabasePopulator} against the given\n\t * {@link DataSource}.\n\t * <p>Delegates to {@link DatabasePopulatorUtils#execute}.\n\t * @param dataSource the {@code DataSource} to execute against (never {@code null})\n\t * @throws ScriptException if an error occurs\n\t * @since 4.1\n\t * @see #populate(Connection)\n\t */",
            "\t/**\n\t * 针对给定 {@link DataSource} 执行本 {@code ResourceDatabasePopulator}。\n\t * <p>委托 {@link DatabasePopulatorUtils#execute}。\n\t * @param dataSource 要执行的目标 {@code DataSource}（永不为 {@code null}）\n\t * @throws ScriptException 发生错误时\n\t * @since 4.1\n\t * @see #populate(Connection)\n\t */",
        ),
    ],
}
