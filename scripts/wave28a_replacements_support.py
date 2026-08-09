"""Chinese JavaDoc replacements for springframework wave28a support classes."""

SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "KeyHolder.java": [
        (
            "/**\n * Interface for retrieving keys, typically used for auto-generated keys\n * as potentially returned by JDBC insert statements.\n *\n * <p>Implementations of this interface can hold any number of keys.\n * In the general case, the keys are returned as a List containing one Map\n * for each row of keys.\n *\n * <p>Most applications only use one key per row and process only one row at a\n * time in an insert statement. In these cases, just call {@link #getKey() getKey}\n * or {@link #getKeyAs(Class) getKeyAs} to retrieve the key. The value returned\n * by {@code getKey} is a {@link Number}, which is the usual type for auto-generated\n * keys. For any other auto-generated key type, use {@code getKeyAs} instead.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Slawomir Dymitrow\n * @since 1.1\n * @see org.springframework.jdbc.core.JdbcTemplate\n * @see org.springframework.jdbc.object.SqlUpdate\n */",
            "/**\n * 用于检索主键的接口，通常用于 JDBC insert 语句可能返回的自动生成键。\n *\n * <p>本接口的实现可保存任意数量的键。一般情况下，键以 List 形式返回，\n * 其中每个元素是一个 Map，对应一行键值。\n *\n * <p>大多数应用每行仅使用一个键，且 insert 语句一次只处理一行。\n * 此时直接调用 {@link #getKey() getKey} 或 {@link #getKeyAs(Class) getKeyAs} 即可。\n * {@code getKey} 返回 {@link Number}，这是自动生成键的常见类型；\n * 其他类型的自动生成键请使用 {@code getKeyAs}。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Slawomir Dymitrow\n * @since 1.1\n * @see org.springframework.jdbc.core.JdbcTemplate\n * @see org.springframework.jdbc.object.SqlUpdate\n */",
        ),
        (
            "\t/**\n\t * Retrieve the first item from the first map, assuming that there is just\n\t * one item and just one map, and that the item is a number.\n\t * This is the typical case: a single, numeric generated key.\n\t * <p>Keys are held in a List of Maps, where each item in the list represents\n\t * the keys for each row. If there are multiple columns, then the Map will have\n\t * multiple entries as well. If this method encounters multiple entries in\n\t * either the map or the list meaning that multiple keys were returned,\n\t * then an InvalidDataAccessApiUsageException is thrown.\n\t * @return the generated key as a number\n\t * @throws InvalidDataAccessApiUsageException if multiple keys are encountered\n\t * @see #getKeyAs(Class)\n\t */",
            "\t/**\n\t * 从第一个 Map 中取第一个元素，假定仅有一个 Map 且其中仅有一个元素，且该元素为数字。\n\t * 这是典型场景：单个数值型自动生成键。\n\t * <p>键保存在 Map 的 List 中，列表每项代表一行的键。\n\t * 若有多列，Map 也会有多个条目。若 Map 或 List 中出现多个条目（即返回了多个键），\n\t * 则抛出 InvalidDataAccessApiUsageException。\n\t * @return 以数字形式返回的生成键\n\t * @throws InvalidDataAccessApiUsageException 遇到多个键时\n\t * @see #getKeyAs(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the first item from the first map, assuming that there is just\n\t * one item and just one map, and that the item is an instance of specified type.\n\t * This is a common case: a single generated key of the specified type.\n\t * <p>Keys are held in a List of Maps, where each item in the list represents\n\t * the keys for each row. If there are multiple columns, then the Map will have\n\t * multiple entries as well. If this method encounters multiple entries in\n\t * either the map or the list meaning that multiple keys were returned,\n\t * then an InvalidDataAccessApiUsageException is thrown.\n\t * @param keyType the type of the auto-generated key\n\t * @return the generated key as an instance of specified type\n\t * @throws InvalidDataAccessApiUsageException if multiple keys are encountered\n\t * @since 5.3\n\t * @see #getKey()\n\t */",
            "\t/**\n\t * 从第一个 Map 中取第一个元素，假定仅有一个 Map 且其中仅有一个元素，且该元素为指定类型实例。\n\t * 这是常见场景：单个指定类型的自动生成键。\n\t * <p>键保存在 Map 的 List 中，列表每项代表一行的键。\n\t * 若有多列，Map 也会有多个条目。若 Map 或 List 中出现多个条目（即返回了多个键），\n\t * 则抛出 InvalidDataAccessApiUsageException。\n\t * @param keyType 自动生成键的类型\n\t * @return 以指定类型实例形式返回的生成键\n\t * @throws InvalidDataAccessApiUsageException 遇到多个键时\n\t * @since 5.3\n\t * @see #getKey()\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the first map of keys.\n\t * <p>If there are multiple entries in the list (meaning that multiple rows\n\t * had keys returned), then an InvalidDataAccessApiUsageException is thrown.\n\t * @return the Map of generated keys for a single row\n\t * @throws InvalidDataAccessApiUsageException if keys for multiple rows are encountered\n\t */",
            "\t/**\n\t * 获取第一个键 Map。\n\t * <p>若 List 中有多个条目（即多行都返回了键），则抛出 InvalidDataAccessApiUsageException。\n\t * @return 单行生成键的 Map\n\t * @throws InvalidDataAccessApiUsageException 遇到多行键时\n\t */",
        ),
        (
            "\t/**\n\t * Return a reference to the List that contains the keys.\n\t * <p>Can be used for extracting keys for multiple rows (an unusual case),\n\t * and also for adding new maps of keys.\n\t * @return the List for the generated keys, with each entry representing\n\t * an individual row through a Map of column names and key values\n\t */",
            "\t/**\n\t * 返回包含键的 List 引用。\n\t * <p>可用于提取多行键（少见场景），也可用于添加新的键 Map。\n\t * @return 生成键的 List，每项通过列名到键值的 Map 表示一行\n\t */",
        ),
    ],
    "MetaDataAccessException.java": [
        (
            "/**\n * Exception indicating that something went wrong during JDBC meta-data lookup.\n *\n * <p>This is a checked exception since we want it to be caught, logged and\n * handled rather than cause the application to fail. Failure to read JDBC\n * meta-data is usually not a fatal problem.\n *\n * @author Thomas Risberg\n * @since 1.0.1\n */",
            "/**\n * 表示 JDBC 元数据查找过程中出错的异常。\n *\n * <p>这是受检异常，以便被捕获、记录和处理，而非导致应用失败。\n * 读取 JDBC 元数据失败通常不是致命问题。\n *\n * @author Thomas Risberg\n * @since 1.0.1\n */",
        ),
        (
            "\t/**\n\t * Constructor for MetaDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 构造 MetaDataAccessException。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for MetaDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * 构造 MetaDataAccessException。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "SQLErrorCodes.java": [
        (
            "/**\n * JavaBean for holding JDBC error codes for a particular database.\n * Instances of this class are normally loaded through a bean factory.\n *\n * <p>Used by Spring's {@link SQLErrorCodeSQLExceptionTranslator}.\n * The file \"sql-error-codes.xml\" in this package contains default\n * {@code SQLErrorCodes} instances for various databases.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SQLErrorCodesFactory\n * @see SQLErrorCodeSQLExceptionTranslator\n */",
            "/**\n * 保存特定数据库 JDBC 错误码的 JavaBean，实例通常通过 Bean 工厂加载。\n *\n * <p>供 Spring 的 {@link SQLErrorCodeSQLExceptionTranslator} 使用。\n * 本包中的 \"sql-error-codes.xml\" 包含各数据库默认 {@code SQLErrorCodes} 实例。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SQLErrorCodesFactory\n * @see SQLErrorCodeSQLExceptionTranslator\n */",
        ),
        (
            "\t/**\n\t * Set this property if the database name contains spaces,\n\t * in which case we can not use the bean name for lookup.\n\t */",
            "\t/**\n\t * 若数据库名称包含空格（无法使用 Bean 名称查找），设置本属性。\n\t */",
        ),
        (
            "\t/**\n\t * Set this property to specify multiple database names that contains spaces,\n\t * in which case we can not use bean names for lookup.\n\t */",
            "\t/**\n\t * 设置多个包含空格的数据库名称（无法使用 Bean 名称查找）。\n\t */",
        ),
        (
            "\t/**\n\t * Set this property to true for databases that do not provide an error code\n\t * but that do provide SQL State (this includes PostgreSQL).\n\t */",
            "\t/**\n\t * 对不提供错误码但提供 SQL State 的数据库（如 PostgreSQL）设为 true。\n\t */",
        ),
    ],
    "SQLErrorCodesFactory.java": [
        (
            "/**\n * Factory for creating {@link SQLErrorCodes} based on the\n * \"databaseProductName\" taken from the {@link java.sql.DatabaseMetaData}.\n *\n * <p>Returns {@code SQLErrorCodes} populated with vendor codes\n * defined in a configuration file named \"sql-error-codes.xml\".\n * Reads the default file in this package if not overridden by a file in\n * the root of the class path (for example in the \"/WEB-INF/classes\" directory).\n *\n * @author Thomas Risberg\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n */",
            "/**\n * 基于 {@link java.sql.DatabaseMetaData} 中的 \"databaseProductName\" 创建 {@link SQLErrorCodes} 的工厂。\n *\n * <p>返回在 \"sql-error-codes.xml\" 配置文件中定义的厂商错误码填充的 {@code SQLErrorCodes}。\n * 若类路径根目录（如 \"/WEB-INF/classes\"）未覆盖，则读取本包中的默认文件。\n *\n * @author Thomas Risberg\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n */",
        ),
        (
            "\t/**\n\t * The name of custom SQL error codes file, loading from the root\n\t * of the class path (for example, from the \"/WEB-INF/classes\" directory).\n\t */",
            "\t/**\n\t * 自定义 SQL 错误码文件名，从类路径根目录加载（如 \"/WEB-INF/classes\"）。\n\t */",
        ),
        (
            "\t/**\n\t * The name of default SQL error code files, loading from the class path.\n\t */",
            "\t/**\n\t * 默认 SQL 错误码文件名，从类路径加载。\n\t */",
        ),
        (
            "\t/**\n\t * Keep track of a single instance, so we can return it to classes that request it.\n\t * Lazily initialized in order to avoid making {@code SQLErrorCodesFactory} constructor\n\t * reachable on native images when not needed.\n\t */",
            "\t/**\n\t * 跟踪单例实例，以便返回给请求的类。\n\t * 延迟初始化，避免在不需要时使 {@code SQLErrorCodesFactory} 构造器在 native image 中可达。\n\t */",
        ),
        (
            "\t/**\n\t * Return the singleton instance.\n\t */",
            "\t/**\n\t * 返回单例实例。\n\t */",
        ),
        (
            "\t/**\n\t * Map to hold error codes for all databases defined in the config file.\n\t * Key is the database product name, value is the SQLErrorCodes instance.\n\t */",
            "\t/**\n\t * 保存配置文件中所有数据库错误码的映射，键为数据库产品名，值为 SQLErrorCodes 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Map to cache the SQLErrorCodes instance per DataSource.\n\t */",
            "\t/**\n\t * 按 DataSource 缓存 SQLErrorCodes 实例的映射。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link SQLErrorCodesFactory} class.\n\t * <p>Not public to enforce Singleton design pattern. Would be private\n\t * except to allow testing via overriding the\n\t * {@link #loadResource(String)} method.\n\t * <p><b>Do not subclass in application code.</b>\n\t * @see #loadResource(String)\n\t */",
            "\t/**\n\t * 创建 {@link SQLErrorCodesFactory} 新实例。\n\t * <p>非 public，以强制单例模式。若非为允许通过覆盖 {@link #loadResource(String)} 进行测试，本应为 private。\n\t * <p><b>应用代码请勿子类化。</b>\n\t * @see #loadResource(String)\n\t */",
        ),
        (
            "\t/**\n\t * Load the given resource from the class path.\n\t * <p><b>Not to be overridden by application developers, who should obtain\n\t * instances of this class from the static {@link #getInstance()} method.</b>\n\t * <p>Protected for testability.\n\t * @param path resource path; either a custom path or one of either\n\t * {@link #SQL_ERROR_CODE_DEFAULT_PATH} or\n\t * {@link #SQL_ERROR_CODE_OVERRIDE_PATH}.\n\t * @return the resource, or {@code null} if the resource wasn't found\n\t * @see #getInstance\n\t */",
            "\t/**\n\t * 从类路径加载给定资源。\n\t * <p><b>应用开发者不应覆盖，应通过静态 {@link #getInstance()} 获取本类实例。</b>\n\t * <p>protected 以便测试。\n\t * @param path 资源路径，可为自定义路径或 {@link #SQL_ERROR_CODE_DEFAULT_PATH}、{@link #SQL_ERROR_CODE_OVERRIDE_PATH}\n\t * @return 资源，未找到时 {@code null}\n\t * @see #getInstance\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link SQLErrorCodes} instance for the given database.\n\t * <p>No need for a database meta-data lookup.\n\t * @param databaseName the database name (must not be {@code null})\n\t * @return the {@code SQLErrorCodes} instance for the given database\n\t * (never {@code null}; potentially empty)\n\t * @throws IllegalArgumentException if the supplied database name is {@code null}\n\t */",
            "\t/**\n\t * 返回给定数据库的 {@link SQLErrorCodes} 实例，无需数据库元数据查找。\n\t * @param databaseName 数据库名称（不得为 {@code null}）\n\t * @return 给定数据库的 {@code SQLErrorCodes}（永不为 {@code null}，可能为空）\n\t * @throws IllegalArgumentException 数据库名称为 {@code null} 时\n\t */",
        ),
        (
            "\t/**\n\t * Return {@link SQLErrorCodes} for the given {@link DataSource},\n\t * evaluating \"databaseProductName\" from the\n\t * {@link java.sql.DatabaseMetaData}, or an empty error codes\n\t * instance if no {@code SQLErrorCodes} were found.\n\t * @param dataSource the {@code DataSource} identifying the database\n\t * @return the corresponding {@code SQLErrorCodes} object\n\t * (never {@code null}; potentially empty)\n\t * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n\t */",
            "\t/**\n\t * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，\n\t * 从 {@link java.sql.DatabaseMetaData} 读取 \"databaseProductName\"；\n\t * 未找到时返回空错误码实例。\n\t * @param dataSource 标识数据库的 {@code DataSource}\n\t * @return 对应的 {@code SQLErrorCodes}（永不为 {@code null}，可能为空）\n\t * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n\t */",
        ),
        (
            "\t/**\n\t * Return {@link SQLErrorCodes} for the given {@link DataSource},\n\t * evaluating \"databaseProductName\" from the\n\t * {@link java.sql.DatabaseMetaData}, or {@code null} if case\n\t * of a JDBC meta-data access problem.\n\t * @param dataSource the {@code DataSource} identifying the database\n\t * @return the corresponding {@code SQLErrorCodes} object,\n\t * or {@code null} in case of a JDBC meta-data access problem\n\t * @since 5.2.9\n\t * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n\t */",
            "\t/**\n\t * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，\n\t * 从 {@link java.sql.DatabaseMetaData} 读取 \"databaseProductName\"；\n\t * JDBC 元数据访问出问题时返回 {@code null}。\n\t * @param dataSource 标识数据库的 {@code DataSource}\n\t * @return 对应的 {@code SQLErrorCodes}，元数据访问失败时 {@code null}\n\t * @since 5.2.9\n\t * @see java.sql.DatabaseMetaData#getDatabaseProductName()\n\t */",
        ),
        (
            "\t/**\n\t * Associate the specified database name with the given {@link DataSource}.\n\t * @param dataSource the {@code DataSource} identifying the database\n\t * @param databaseName the corresponding database name as stated in the error codes\n\t * definition file (must not be {@code null})\n\t * @return the corresponding {@code SQLErrorCodes} object (never {@code null})\n\t * @see #unregisterDatabase(DataSource)\n\t */",
            "\t/**\n\t * 将指定数据库名称与给定 {@link DataSource} 关联。\n\t * @param dataSource 标识数据库的 {@code DataSource}\n\t * @param databaseName 错误码定义文件中的数据库名称（不得为 {@code null}）\n\t * @return 对应的 {@code SQLErrorCodes}（永不为 {@code null}）\n\t * @see #unregisterDatabase(DataSource)\n\t */",
        ),
        (
            "\t/**\n\t * Clear the cache for the specified {@link DataSource}, if registered.\n\t * @param dataSource the {@code DataSource} identifying the database\n\t * @return the corresponding {@code SQLErrorCodes} object that got removed,\n\t * or {@code null} if not registered\n\t * @since 4.3.5\n\t * @see #registerDatabase(DataSource, String)\n\t */",
            "\t/**\n\t * 清除指定 {@link DataSource} 的缓存（若已注册）。\n\t * @param dataSource 标识数据库的 {@code DataSource}\n\t * @return 被移除的 {@code SQLErrorCodes}，未注册时 {@code null}\n\t * @since 4.3.5\n\t * @see #registerDatabase(DataSource, String)\n\t */",
        ),
        (
            "\t/**\n\t * Build an identification String for the given {@link DataSource},\n\t * primarily for logging purposes.\n\t * @param dataSource the {@code DataSource} to introspect\n\t * @return the identification String\n\t */",
            "\t/**\n\t * 为给定 {@link DataSource} 构建标识字符串，主要用于日志。\n\t * @param dataSource 要内省的 {@code DataSource}\n\t * @return 标识字符串\n\t */",
        ),
        (
            "\t/**\n\t * Check the {@link CustomSQLExceptionTranslatorRegistry} for any entries.\n\t */",
            "\t/**\n\t * 检查 {@link CustomSQLExceptionTranslatorRegistry} 中是否有条目。\n\t */",
        ),
    ],
    "SQLExceptionSubclassTranslator.java": [
        (
            "/**\n * {@link SQLExceptionTranslator} implementation which analyzes the specific\n * {@link java.sql.SQLException} subclass thrown by the JDBC driver.\n *\n * <p>Falls back to a standard {@link SQLStateSQLExceptionTranslator} if the JDBC\n * driver does not actually expose JDBC 4 compliant {@code SQLException} subclasses.\n *\n * <p>This translator serves as the default JDBC exception translator as of 6.0.\n * As of 6.2.12, it specifically introspects {@link java.sql.BatchUpdateException}\n * to look at the underlying exception, analogous to the former default\n * {@link SQLErrorCodeSQLExceptionTranslator}.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n * @see java.sql.SQLTransientException\n * @see java.sql.SQLNonTransientException\n * @see java.sql.SQLRecoverableException\n */",
            "/**\n * 分析 JDBC 驱动抛出的具体 {@link java.sql.SQLException} 子类的 {@link SQLExceptionTranslator} 实现。\n *\n * <p>若 JDBC 驱动未暴露符合 JDBC 4 的 {@code SQLException} 子类，\n * 则回退到标准 {@link SQLStateSQLExceptionTranslator}。\n *\n * <p>自 6.0 起作为默认 JDBC 异常翻译器。\n * 自 6.2.12 起专门内省 {@link java.sql.BatchUpdateException} 查看底层异常，\n * 类似原先默认的 {@link SQLErrorCodeSQLExceptionTranslator}。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n * @see java.sql.SQLTransientException\n * @see java.sql.SQLNonTransientException\n * @see java.sql.SQLRecoverableException\n */",
        ),
    ],
    "SQLExceptionTranslator.java": [
        (
            "/**\n * Strategy interface for translating between {@link SQLException SQLExceptions}\n * and Spring's data access strategy-agnostic {@link DataAccessException}\n * hierarchy.\n *\n * <p>Implementations can be generic (for example, using\n * {@link java.sql.SQLException#getSQLState() SQLState} codes for JDBC) or wholly\n * proprietary (for example, using Oracle error codes) for greater precision.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.dao.DataAccessException\n */",
            "/**\n * 在 {@link SQLException SQLExceptions} 与 Spring 数据访问策略无关的\n * {@link DataAccessException} 层次结构之间翻译的策略接口。\n *\n * <p>实现可以是通用的（如 JDBC 使用 {@link java.sql.SQLException#getSQLState() SQLState} 码），\n * 或完全专有的（如 Oracle 错误码）以获得更高精度。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.dao.DataAccessException\n */",
        ),
        (
            "\t/**\n\t * Translate the given {@link SQLException} into a generic {@link DataAccessException}.\n\t * <p>The returned DataAccessException is supposed to contain the original\n\t * {@code SQLException} as root cause. However, client code may not generally\n\t * rely on this due to DataAccessExceptions possibly being caused by other resource\n\t * APIs as well. That said, a {@code getRootCause() instanceof SQLException}\n\t * check (and subsequent cast) is considered reliable when expecting JDBC-based\n\t * access to have happened.\n\t * @param task readable text describing the task being attempted\n\t * @param sql the SQL query or update that caused the problem (if known)\n\t * @param ex the offending {@code SQLException}\n\t * @return the DataAccessException wrapping the {@code SQLException},\n\t * or {@code null} if no specific translation could be applied\n\t * @see org.springframework.dao.DataAccessException#getRootCause()\n\t */",
            "\t/**\n\t * 将给定 {@link SQLException} 翻译为通用 {@link DataAccessException}。\n\t * <p>返回的 DataAccessException 应包含原始 {@code SQLException} 作为根因；\n\t * 但客户端代码通常不应依赖这一点，因为 DataAccessException 也可能由其他资源 API 引起。\n\t * 在预期发生了基于 JDBC 的访问时，{@code getRootCause() instanceof SQLException} 检查（及后续转型）是可靠的。\n\t * @param task 描述正在尝试任务的可读文本\n\t * @param sql 导致问题的 SQL 查询或更新（若已知）\n\t * @param ex 触发的 {@code SQLException}\n\t * @return 包装 {@code SQLException} 的 DataAccessException；无法翻译时 {@code null}\n\t * @see org.springframework.dao.DataAccessException#getRootCause()\n\t */",
        ),
    ],
    "SQLStateSQLExceptionTranslator.java": [
        (
            "/**\n * {@link SQLExceptionTranslator} implementation that analyzes the SQL state in\n * the {@link SQLException} based on the first two digits (the SQL state \"class\").\n * Detects standard SQL state values and well-known vendor-specific SQL states.\n *\n * <p>Not able to diagnose all problems, but is portable between databases and\n * does not require special initialization (no database vendor detection, etc.).\n * For more precise translation, consider {@link SQLErrorCodeSQLExceptionTranslator}.\n *\n * <p>This translator is commonly used as a {@link #setFallbackTranslator fallback}\n * behind a primary translator such as {@link SQLErrorCodeSQLExceptionTranslator} or\n * {@link SQLExceptionSubclassTranslator}. As of 6.2.12, it specifically introspects\n * {@link java.sql.BatchUpdateException} to look at the underlying exception\n * (for alignment when used behind a {@link SQLExceptionSubclassTranslator}).\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Thomas Risberg\n * @see java.sql.SQLException#getSQLState()\n * @see SQLErrorCodeSQLExceptionTranslator\n * @see SQLExceptionSubclassTranslator\n */",
            "/**\n * 基于 {@link SQLException} 中 SQL state 前两位（SQL state \"class\"）进行分析的 {@link SQLExceptionTranslator} 实现。\n * 识别标准 SQL state 值及常见厂商特定 SQL state。\n *\n * <p>无法诊断所有问题，但在数据库间可移植，无需特殊初始化（如厂商检测）。\n * 需要更精确翻译时可考虑 {@link SQLErrorCodeSQLExceptionTranslator}。\n *\n * <p>通常作为主翻译器（如 {@link SQLErrorCodeSQLExceptionTranslator} 或 {@link SQLExceptionSubclassTranslator}）\n * 背后的 {@link #setFallbackTranslator 回退翻译器}。\n * 自 6.2.12 起专门内省 {@link java.sql.BatchUpdateException} 查看底层异常\n * （与在 {@link SQLExceptionSubclassTranslator} 后使用时对齐）。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Thomas Risberg\n * @see java.sql.SQLException#getSQLState()\n * @see SQLErrorCodeSQLExceptionTranslator\n * @see SQLExceptionSubclassTranslator\n */",
        ),
        (
            "\t\t\t\"07\",  // Dynamic SQL error",
            '\t\t\t"07",  // 动态 SQL 错误',
        ),
        (
            "\t\t\t\"21\",  // Cardinality violation",
            '\t\t\t"21",  // 基数违反',
        ),
        (
            "\t\t\t\"2A\",  // Syntax error direct SQL",
            '\t\t\t"2A",  // 直接 SQL 语法错误',
        ),
        (
            "\t\t\t\"37\",  // Syntax error dynamic SQL",
            '\t\t\t"37",  // 动态 SQL 语法错误',
        ),
        (
            "\t\t\t\"42\",  // General SQL syntax error",
            '\t\t\t"42",  // 通用 SQL 语法错误',
        ),
        (
            "\t\t\t\"65\"   // Oracle: unknown identifier",
            '\t\t\t"65"   // Oracle：未知标识符',
        ),
        (
            "\t\t\t\"01\",  // Data truncation",
            '\t\t\t"01",  // 数据截断',
        ),
        (
            "\t\t\t\"02\",  // No data found",
            '\t\t\t"02",  // 未找到数据',
        ),
        (
            "\t\t\t\"22\",  // Value out of range",
            '\t\t\t"22",  // 值超出范围',
        ),
        (
            "\t\t\t\"23\",  // Integrity constraint violation",
            '\t\t\t"23",  // 完整性约束违反',
        ),
        (
            "\t\t\t\"27\",  // Triggered data change violation",
            '\t\t\t"27",  // 触发器数据变更违反',
        ),
        (
            "\t\t\t\"44\"   // With check violation",
            '\t\t\t"44"   // WITH CHECK 违反',
        ),
        (
            "\t\t\t\"40\",  // Transaction rollback",
            '\t\t\t"40",  // 事务回滚',
        ),
        (
            "\t\t\t\"61\"   // Oracle: deadlock",
            '\t\t\t"61"   // Oracle：死锁',
        ),
        (
            "\t\t\t\"08\",  // Connection exception",
            '\t\t\t"08",  // 连接异常',
        ),
        (
            "\t\t\t\"53\",  // PostgreSQL: insufficient resources (for example, disk full)",
            '\t\t\t"53",  // PostgreSQL：资源不足（如磁盘满）',
        ),
        (
            "\t\t\t\"54\",  // PostgreSQL: program limit exceeded (for example, statement too complex)",
            '\t\t\t"54",  // PostgreSQL：程序限制超出（如语句过于复杂）',
        ),
        (
            "\t\t\t\"57\",  // DB2: out-of-memory exception / database not started",
            '\t\t\t"57",  // DB2：内存不足 / 数据库未启动',
        ),
        (
            "\t\t\t\"58\"   // DB2: unexpected system error",
            '\t\t\t"58"   // DB2：意外系统错误',
        ),
        (
            "\t\t\t\"JW\",  // Sybase: internal I/O error",
            '\t\t\t"JW",  // Sybase：内部 I/O 错误',
        ),
        (
            "\t\t\t\"JZ\",  // Sybase: unexpected I/O error",
            '\t\t\t"JZ",  // Sybase：意外 I/O 错误',
        ),
        (
            "\t\t\t\"S1\"   // DB2: communication failure",
            '\t\t\t"S1"   // DB2：通信失败',
        ),
        (
            "\t\t\t1,     // Oracle",
            "\t\t\t1,     // Oracle",
        ),
        (
            "\t\t\t301,   // SAP HANA",
            "\t\t\t301,   // SAP HANA",
        ),
        (
            "\t\t\t1062,  // MySQL/MariaDB",
            "\t\t\t1062,  // MySQL/MariaDB",
        ),
        (
            "\t\t\t2601,  // MS SQL Server",
            "\t\t\t2601,  // MS SQL Server",
        ),
        (
            "\t\t\t2627,  // MS SQL Server",
            "\t\t\t2627,  // MS SQL Server",
        ),
        (
            "\t\t\t-239,  // Informix",
            "\t\t\t-239,  // Informix",
        ),
        (
            "\t\t\t-268   // Informix",
            "\t\t\t-268   // Informix",
        ),
        (
            "\t\t\t// Unwrap BatchUpdateException to expose contained exception\n\t\t\t// with potentially more specific SQL state.",
            "\t\t\t// 解包 BatchUpdateException 以暴露可能具有更具体 SQL state 的内部异常。",
        ),
        (
            "\t\t\t// Expose top-level exception but potentially use nested SQL state.",
            "\t\t\t// 暴露顶层异常，但可能使用嵌套 SQL state。",
        ),
        (
            "\t\t// The actual SQL state check...",
            "\t\t// 实际 SQL state 检查...",
        ),
        (
            "\t\t// For MySQL: exception class name indicating a timeout?\n\t\t// (since MySQL doesn't throw the JDBC 4 SQLTimeoutException)",
            "\t\t// MySQL：异常类名是否表示超时？\n\t\t// （MySQL 不抛出 JDBC 4 SQLTimeoutException）",
        ),
        (
            "\t\t// Couldn't resolve anything proper - resort to UncategorizedSQLException.",
            "\t\t// 无法解析——回退到 UncategorizedSQLException。",
        ),
        (
            "\t/**\n\t * Gets the SQL state code from the supplied {@link SQLException exception}.\n\t * <p>Some JDBC drivers nest the actual exception from a batched update, so we\n\t * might need to dig down into the nested exception.\n\t * @param ex the exception from which the {@link SQLException#getSQLState() SQL state}\n\t * is to be extracted\n\t * @return the SQL state code\n\t */",
            "\t/**\n\t * 从给定 {@link SQLException exception} 获取 SQL state 码。\n\t * <p>部分 JDBC 驱动将批更新中的实际异常嵌套，可能需要深入嵌套异常。\n\t * @param ex 要提取 {@link SQLException#getSQLState() SQL state} 的异常\n\t * @return SQL state 码\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given SQL state and the associated error code (in case\n\t * of a generic SQL state value) indicate a {@link DuplicateKeyException}:\n\t * either SQL state 23505 as a specific indication, or the generic SQL state\n\t * 23000 with a well-known vendor code.\n\t * @param sqlState the SQL state value\n\t * @param errorCode the error code\n\t */",
            "\t/**\n\t * 检查给定 SQL state 及关联错误码（通用 SQL state 时）是否表示 {@link DuplicateKeyException}：\n\t * 特定指示 SQL state 23505，或通用 SQL state 23000 加已知厂商码。\n\t * @param sqlState SQL state 值\n\t * @param errorCode 错误码\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given SQL state indicates a {@link CannotAcquireLockException},\n\t * with SQL state 40001 as a specific indication.\n\t * @param sqlState the SQL state value\n\t */",
            "\t/**\n\t * 检查给定 SQL state 是否表示 {@link CannotAcquireLockException}，\n\t * 特定指示为 SQL state 40001。\n\t * @param sqlState SQL state 值\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given SQL state indicates a {@link QueryTimeoutException},\n\t * with SQL state 57014 as a specific indication.\n\t * @param sqlState the SQL state value\n\t */",
            "\t/**\n\t * 检查给定 SQL state 是否表示 {@link QueryTimeoutException}，\n\t * 特定指示为 SQL state 57014。\n\t * @param sqlState SQL state 值\n\t */",
        ),
    ],
    "SqlArrayValue.java": [
        (
            "/**\n * Common {@link SqlValue} implementation for JDBC {@link Array} creation\n * based on the JDBC 4 {@link java.sql.Connection#createArrayOf} method.\n *\n * <p>Also serves as a template for custom {@link SqlValue} implementations\n * with cleanup demand.\n *\n * @author Juergen Hoeller\n * @author Philippe Marschall\n * @since 6.1\n */",
            "/**\n * 基于 JDBC 4 {@link java.sql.Connection#createArrayOf} 方法创建 JDBC {@link Array} 的通用 {@link SqlValue} 实现。\n *\n * <p>也可作为需要清理的自定义 {@link SqlValue} 实现的模板。\n *\n * @author Juergen Hoeller\n * @author Philippe Marschall\n * @since 6.1\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlArrayValue} for the given type name and elements.\n\t * @param typeName the SQL name of the type the elements of the array map to\n\t * @param elements the elements to populate the {@code Array} object with\n\t * @see java.sql.Connection#createArrayOf\n\t */",
            "\t/**\n\t * 为给定类型名和元素创建 {@code SqlArrayValue}。\n\t * @param typeName 数组元素映射到的 SQL 类型名\n\t * @param elements 用于填充 {@code Array} 对象的元素\n\t * @see java.sql.Connection#createArrayOf\n\t */",
        ),
    ],
    "SqlValue.java": [
        (
            "/**\n * Simple interface for complex types to be set as statement parameters.\n *\n * <p>Implementations perform the actual work of setting the actual values. They must\n * implement the callback method {@code setValue} which can throw SQLExceptions\n * that will be caught and translated by the calling code. This callback method has\n * access to the underlying Connection via the given PreparedStatement object, if that\n * should be needed to create any database-specific objects.\n *\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see org.springframework.jdbc.core.SqlTypeValue\n * @see org.springframework.jdbc.core.DisposableSqlTypeValue\n */",
            "/**\n * 将复杂类型设为语句参数的简单接口。\n *\n * <p>实现负责设置实际值，须实现回调方法 {@code setValue}，\n * 其抛出的 SQLException 由调用方捕获并翻译。\n * 若需创建数据库特定对象，可通过给定 PreparedStatement 访问底层 Connection。\n *\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see org.springframework.jdbc.core.SqlTypeValue\n * @see org.springframework.jdbc.core.DisposableSqlTypeValue\n */",
        ),
        (
            "\t/**\n\t * Set the value on the given PreparedStatement.\n\t * @param ps the PreparedStatement to work on\n\t * @param paramIndex the index of the parameter for which we need to set the value\n\t * @throws SQLException if an SQLException is encountered while setting parameter values\n\t */",
            "\t/**\n\t * 在给定 PreparedStatement 上设置值。\n\t * @param ps 要操作的 PreparedStatement\n\t * @param paramIndex 需要设置值的参数索引\n\t * @throws SQLException 设置参数值时遇到 SQLException\n\t */",
        ),
        (
            "\t/**\n\t * Clean up resources held by this value object.\n\t */",
            "\t/**\n\t * 清理本值对象持有的资源。\n\t */",
        ),
    ],
}
