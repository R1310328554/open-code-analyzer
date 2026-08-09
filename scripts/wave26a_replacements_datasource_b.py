"""Chinese JavaDoc replacements for springframework wave26a datasource classes (part B)."""

DATASOURCE_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "IsolationLevelDataSourceAdapter.java": [
        (
            "/**\n * An adapter for a target {@link javax.sql.DataSource}, applying the current\n * Spring transaction's isolation level (and potentially specified user credentials)\n * to every {@code getConnection} call. Also applies the read-only flag,\n * if specified.\n *\n * <p>Can be used to proxy a target JNDI DataSource that does not have the\n * desired isolation level (and user credentials) configured. Client code\n * can work with this DataSource as usual, not worrying about such settings.\n *\n * <p>Inherits the capability to apply specific user credentials from its superclass\n * {@link UserCredentialsDataSourceAdapter}; see the latter's javadoc for details\n * on that functionality (for example, {@link #setCredentialsForCurrentThread}).\n *\n * <p><b>WARNING:</b> This adapter simply calls\n * {@link java.sql.Connection#setTransactionIsolation} and/or\n * {@link java.sql.Connection#setReadOnly} for every Connection obtained from it.\n * It does, however, <i>not</i> reset those settings; it rather expects the target\n * DataSource to perform such resetting as part of its connection pool handling.\n * <b>Make sure that the target DataSource properly cleans up such transaction state.</b>\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0.3\n * @see #setIsolationLevel\n * @see #setIsolationLevelName\n * @see #setUsername\n * @see #setPassword\n */",
            "/**\n * 目标 {@link javax.sql.DataSource} 的适配器，\n * 在每次 {@code getConnection} 调用时应用当前 Spring 事务的隔离级别\n * （以及可能指定的用户凭据），并在指定时应用只读标志。\n *\n * <p>可用于代理未配置所需隔离级别（及用户凭据）的目标 JNDI DataSource。\n * 客户端代码可照常使用本 DataSource，无需关心这些设置。\n *\n * <p>从超类 {@link UserCredentialsDataSourceAdapter} 继承应用特定用户凭据的能力；\n * 详见后者 javadoc（例如 {@link #setCredentialsForCurrentThread}）。\n *\n * <p><b>警告：</b> 本适配器仅对获取的每个 Connection 调用\n * {@link java.sql.Connection#setTransactionIsolation} 和/或\n * {@link java.sql.Connection#setReadOnly}。\n * 但<i>不会</i>重置这些设置，而是期望目标 DataSource\n * 在连接池处理中清理此类事务状态。\n * <b>请确保目标 DataSource 正确清理这些事务状态。</b>\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0.3\n * @see #setIsolationLevel\n * @see #setIsolationLevelName\n * @see #setUsername\n * @see #setPassword\n */",
        ),
        (
            "\t/**\n\t * Map of constant names to constant values for the isolation constants\n\t * defined in {@link TransactionDefinition}.\n\t */",
            "\t/**\n\t * {@link TransactionDefinition} 中定义的隔离级别常量名到常量值的映射。\n\t */",
        ),
        (
            "\t/**\n\t * Set the default isolation level by the name of the corresponding constant\n\t * in {@link org.springframework.transaction.TransactionDefinition} &mdash;\n\t * for example, {@code \"ISOLATION_SERIALIZABLE\"}.\n\t * <p>If not specified, the target DataSource's default will be used.\n\t * Note that a transaction-specific isolation value will always override\n\t * any isolation setting specified at the DataSource level.\n\t * @param constantName name of the constant\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see #setIsolationLevel\n\t */",
            "\t/**\n\t * 通过 {@link org.springframework.transaction.TransactionDefinition} 中对应常量名\n\t * 设置默认隔离级别，例如 {@code \"ISOLATION_SERIALIZABLE\"}。\n\t * <p>未指定时使用目标 DataSource 默认值。\n\t * 事务级隔离值始终覆盖 DataSource 级设置。\n\t * @param constantName 常量名\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see #setIsolationLevel\n\t */",
        ),
        (
            "\t/**\n\t * Specify the default isolation level to use for Connection retrieval,\n\t * according to the JDBC {@link java.sql.Connection} constants\n\t * (equivalent to the corresponding Spring\n\t * {@link org.springframework.transaction.TransactionDefinition} constants).\n\t * <p>If not specified, the target DataSource's default will be used.\n\t * Note that a transaction-specific isolation value will always override\n\t * any isolation setting specified at the DataSource level.\n\t * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED\n\t * @see java.sql.Connection#TRANSACTION_READ_COMMITTED\n\t * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ\n\t * @see java.sql.Connection#TRANSACTION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()\n\t */",
            "\t/**\n\t * 按 JDBC {@link java.sql.Connection} 常量（等价于 Spring\n\t * {@link org.springframework.transaction.TransactionDefinition} 常量）\n\t * 指定获取 Connection 时使用的默认隔离级别。\n\t * <p>未指定时使用目标 DataSource 默认值。\n\t * 事务级隔离值始终覆盖 DataSource 级设置。\n\t * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED\n\t * @see java.sql.Connection#TRANSACTION_READ_COMMITTED\n\t * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ\n\t * @see java.sql.Connection#TRANSACTION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()\n\t */",
        ),
        (
            "\t/**\n\t * Return the statically specified isolation level,\n\t * or {@code null} if none.\n\t */",
            "\t/**\n\t * 返回静态指定的隔离级别，未指定时返回 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Applies the current isolation level value and read-only flag\n\t * to the returned Connection.\n\t * @see #getCurrentIsolationLevel()\n\t * @see #getCurrentReadOnlyFlag()\n\t */",
            "\t/**\n\t * 将当前隔离级别值和只读标志应用到返回的 Connection。\n\t * @see #getCurrentIsolationLevel()\n\t * @see #getCurrentReadOnlyFlag()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the current isolation level: either the transaction's\n\t * isolation level or a statically defined isolation level.\n\t * @return the current isolation level, or {@code null} if none\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()\n\t * @see #setIsolationLevel\n\t */",
            "\t/**\n\t * 确定当前隔离级别：事务隔离级别或静态定义的隔离级别。\n\t * @return 当前隔离级别，无则返回 {@code null}\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionIsolationLevel()\n\t * @see #setIsolationLevel\n\t */",
        ),
        (
            "\t/**\n\t * Determine the current read-only flag: by default,\n\t * the transaction's read-only hint.\n\t * @return whether there is a read-only hint for the current scope\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()\n\t */",
            "\t/**\n\t * 确定当前只读标志：默认取事务的只读提示。\n\t * @return 当前作用域是否存在只读提示\n\t * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()\n\t */",
        ),
    ],
    "JdbcTransactionObjectSupport.java": [
        (
            "/**\n * Convenient base class for JDBC-aware transaction objects. Can contain a\n * {@link ConnectionHolder} with a JDBC {@code Connection}, and implements the\n * {@link SavepointManager} interface based on that {@code ConnectionHolder}.\n *\n * <p>Allows for programmatic management of JDBC {@link java.sql.Savepoint Savepoints}.\n * Spring's {@link org.springframework.transaction.support.DefaultTransactionStatus}\n * automatically delegates to this, as it autodetects transaction objects which\n * implement the {@link SavepointManager} interface.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see DataSourceTransactionManager\n */",
            "/**\n * JDBC 感知事务对象的便捷基类。可持有包含 JDBC {@code Connection} 的\n * {@link ConnectionHolder}，并基于该 {@code ConnectionHolder} 实现\n * {@link SavepointManager} 接口。\n *\n * <p>支持以编程方式管理 JDBC {@link java.sql.Savepoint 保存点}。\n * Spring 的 {@link org.springframework.transaction.support.DefaultTransactionStatus}\n * 会自动委托给实现 {@link SavepointManager} 的事务对象。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see DataSourceTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Set the ConnectionHolder for this transaction object.\n\t */",
            "\t/**\n\t * 设置本事务对象的 ConnectionHolder。\n\t */",
        ),
        (
            "\t/**\n\t * Return the ConnectionHolder for this transaction object.\n\t */",
            "\t/**\n\t * 返回本事务对象的 ConnectionHolder。\n\t */",
        ),
        (
            "\t/**\n\t * Check whether this transaction object has a ConnectionHolder.\n\t */",
            "\t/**\n\t * 检查本事务对象是否持有 ConnectionHolder。\n\t */",
        ),
        (
            "\t/**\n\t * Set the previous isolation level to retain, if any.\n\t */",
            "\t/**\n\t * 设置要保留的先前隔离级别（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return the retained previous isolation level, if any.\n\t */",
            "\t/**\n\t * 返回保留的先前隔离级别（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Set the read-only status of this transaction.\n\t * The default is {@code false}.\n\t * @since 5.2.1\n\t */",
            "\t/**\n\t * 设置本事务的只读状态。默认为 {@code false}。\n\t * @since 5.2.1\n\t */",
        ),
        (
            "\t/**\n\t * Return the read-only status of this transaction.\n\t * @since 5.2.1\n\t */",
            "\t/**\n\t * 返回本事务的只读状态。\n\t * @since 5.2.1\n\t */",
        ),
        (
            "\t/**\n\t * Set whether savepoints are allowed within this transaction.\n\t * The default is {@code false}.\n\t */",
            "\t/**\n\t * 设置本事务内是否允许保存点。默认为 {@code false}。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether savepoints are allowed within this transaction.\n\t */",
            "\t/**\n\t * 返回本事务内是否允许保存点。\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Implementation of SavepointManager\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// SavepointManager 实现\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * This implementation creates a JDBC Savepoint and returns it.\n\t * @see java.sql.Connection#setSavepoint\n\t */",
            "\t/**\n\t * 本实现创建 JDBC 保存点并返回。\n\t * @see java.sql.Connection#setSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * This implementation rolls back to the given JDBC Savepoint.\n\t * @see java.sql.Connection#rollback(java.sql.Savepoint)\n\t */",
            "\t/**\n\t * 本实现回滚到给定 JDBC 保存点。\n\t * @see java.sql.Connection#rollback(java.sql.Savepoint)\n\t */",
        ),
        (
            "\t/**\n\t * This implementation releases the given JDBC Savepoint.\n\t * @see java.sql.Connection#releaseSavepoint\n\t */",
            "\t/**\n\t * 本实现释放给定 JDBC 保存点。\n\t * @see java.sql.Connection#releaseSavepoint\n\t */",
        ),
    ],
    "ShardingKeyDataSourceAdapter.java": [
        (
            "/**\n * An adapter for a target {@link DataSource}, designed to apply sharding keys, if specified,\n * to every standard {@code #getConnection} call, returning a direct connection to the shard\n * corresponding to the specified sharding key value. All other methods simply delegate\n * to the corresponding methods of the target {@code DataSource}.\n *\n * <p>The target {@code DataSource} must implement the {@link #createConnectionBuilder} method;\n * otherwise, a {@link java.sql.SQLFeatureNotSupportedException} will be thrown when attempting\n * to acquire shard connections.\n *\n * <p>This adapter needs to be configured with a {@link ShardingKeyProvider} callback which is\n * used to get the current sharding keys for every {@code #getConnection} call, for example:\n *\n * <pre class=\"code\">\n * ShardingKeyDataSourceAdapter dataSourceAdapter = new ShardingKeyDataSourceAdapter(dataSource);\n * dataSourceAdapter.setShardingKeyProvider(() -&gt; dataSource.createShardingKeyBuilder()\n *     .subkey(SecurityContextHolder.getContext().getAuthentication().getName(), JDBCType.VARCHAR).build());\n * </pre>\n *\n * @author Mohamed Lahyane (Anir)\n * @author Juergen Hoeller\n * @since 6.1.2\n * @see #getConnection\n * @see #createConnectionBuilder()\n * @see UserCredentialsDataSourceAdapter\n */",
            "/**\n * 目标 {@link DataSource} 的适配器，在每次标准 {@code #getConnection} 调用时\n * （若已指定）应用分片键，返回对应分片的直连。\n * 其余方法均委托给目标 {@code DataSource} 的对应方法。\n *\n * <p>目标 {@code DataSource} 必须实现 {@link #createConnectionBuilder}；\n * 否则获取分片连接时将抛出 {@link java.sql.SQLFeatureNotSupportedException}。\n *\n * <p>须配置 {@link ShardingKeyProvider} 回调，\n * 在每次 {@code #getConnection} 调用时获取当前分片键，例如：\n *\n * <pre class=\"code\">\n * ShardingKeyDataSourceAdapter dataSourceAdapter = new ShardingKeyDataSourceAdapter(dataSource);\n * dataSourceAdapter.setShardingKeyProvider(() -&gt; dataSource.createShardingKeyBuilder()\n *     .subkey(SecurityContextHolder.getContext().getAuthentication().getName(), JDBCType.VARCHAR).build());\n * </pre>\n *\n * @author Mohamed Lahyane (Anir)\n * @author Juergen Hoeller\n * @since 6.1.2\n * @see #getConnection\n * @see #createConnectionBuilder()\n * @see UserCredentialsDataSourceAdapter\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of {@code ShardingKeyDataSourceAdapter}, wrapping the\n\t * given {@link DataSource}.\n\t * @param dataSource the target {@code DataSource} to be wrapped\n\t */",
            "\t/**\n\t * 创建新的 {@code ShardingKeyDataSourceAdapter} 实例，包装给定 {@link DataSource}。\n\t * @param dataSource 要包装的目标 {@code DataSource}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of {@code ShardingKeyDataSourceAdapter}, wrapping the\n\t * given {@link DataSource}.\n\t * @param dataSource the target {@code DataSource} to be wrapped\n\t * @param shardingKeyProvider the {@code ShardingKeyProvider} used to get the\n\t * sharding keys\n\t */",
            "\t/**\n\t * 创建新的 {@code ShardingKeyDataSourceAdapter} 实例，包装给定 {@link DataSource}。\n\t * @param dataSource 要包装的目标 {@code DataSource}\n\t * @param shardingKeyProvider 用于获取分片键的 {@code ShardingKeyProvider}\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ShardingKeyProvider} for this adapter.\n\t */",
            "\t/**\n\t * 设置本适配器的 {@link ShardingKeyProvider}。\n\t */",
        ),
        (
            "\t/**\n\t * Obtain a connection to the database shard using the provided sharding key\n\t * and super sharding key (if available).\n\t * <p>The sharding key is obtained from the configured\n\t * {@link #setShardingKeyProvider ShardingKeyProvider}.\n\t * @return a {@code Connection} object representing a direct shard connection\n\t * @throws SQLException if an error occurs while creating the connection\n\t * @see #createConnectionBuilder()\n\t */",
            "\t/**\n\t * 使用提供的分片键和上级分片键（若有）获取数据库分片连接。\n\t * <p>分片键来自已配置的 {@link #setShardingKeyProvider ShardingKeyProvider}。\n\t * @return 表示分片直连的 {@code Connection} 对象\n\t * @throws SQLException 创建连接时出错\n\t * @see #createConnectionBuilder()\n\t */",
        ),
        (
            "\t/**\n\t * Obtain a connection to the database shard using the provided username and password,\n\t * considering the sharding keys (if available) and the given credentials.\n\t * <p>The sharding key is obtained from the configured\n\t * {@link #setShardingKeyProvider ShardingKeyProvider}.\n\t * @param username the database user on whose behalf the connection is being made\n\t * @param password the user's password\n\t * @return a {@code Connection} object representing a direct shard connection\n\t * @throws SQLException if an error occurs while creating the connection\n\t */",
            "\t/**\n\t * 使用给定用户名和密码获取数据库分片连接，\n\t * 同时考虑分片键（若有）和给定凭据。\n\t * <p>分片键来自已配置的 {@link #setShardingKeyProvider ShardingKeyProvider}。\n\t * @param username 代表其建立连接的数据库用户\n\t * @param password 用户密码\n\t * @return 表示分片直连的 {@code Connection} 对象\n\t * @throws SQLException 创建连接时出错\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of {@link ConnectionBuilder} using the target {@code DataSource}'s\n\t * {@code createConnectionBuilder()} method and set the appropriate sharding keys\n\t * from the configured {@link #setShardingKeyProvider ShardingKeyProvider}.\n\t * @return a ConnectionBuilder object representing a builder for direct shard connections\n\t * @throws SQLException if an error occurs while creating the ConnectionBuilder\n\t */",
            "\t/**\n\t * 使用目标 {@code DataSource} 的 {@code createConnectionBuilder()} 方法\n\t * 创建新的 {@link ConnectionBuilder} 实例，并设置已配置\n\t * {@link #setShardingKeyProvider ShardingKeyProvider} 的相应分片键。\n\t * @return 用于构建分片直连的 ConnectionBuilder 对象\n\t * @throws SQLException 创建 ConnectionBuilder 时出错\n\t */",
        ),
    ],
    "ShardingKeyProvider.java": [
        (
            "/**\n * Strategy interface for determining sharding keys which are used to establish direct\n * shard connections in the context of sharded databases. This is used as a callback\n * for providing the current sharding key (plus optionally a super sharding key) in\n * {@link org.springframework.jdbc.datasource.ShardingKeyDataSourceAdapter}.\n *\n * <p>Can be used as a functional interface (for example, with a lambda expression) for a simple\n * sharding key, or as a two-method interface when including a super sharding key as well.\n *\n * @author Mohamed Lahyane (Anir)\n * @author Juergen Hoeller\n * @since 6.1.2\n * @see ShardingKeyDataSourceAdapter#setShardingKeyProvider\n */",
            "/**\n * 确定分片键的策略接口，用于在分片数据库场景下建立分片直连。\n * 作为回调在 {@link org.springframework.jdbc.datasource.ShardingKeyDataSourceAdapter}\n * 中提供当前分片键（以及可选的上级分片键）。\n *\n * <p>简单分片键可作为函数式接口（例如 lambda）使用；\n * 若需上级分片键，则作为双方法接口使用。\n *\n * @author Mohamed Lahyane (Anir)\n * @author Juergen Hoeller\n * @since 6.1.2\n * @see ShardingKeyDataSourceAdapter#setShardingKeyProvider\n */",
        ),
        (
            "\t/**\n\t * Determine the sharding key. This method returns the sharding key relevant to the current\n\t * context which will be used to obtain a direct shard connection.\n\t * @return the sharding key, or {@code null} if it is not available or cannot be determined\n\t * @throws SQLException if an error occurs while obtaining the sharding key\n\t */",
            "\t/**\n\t * 确定分片键。返回与当前上下文相关、用于获取分片直连的分片键。\n\t * @return 分片键，不可用或无法确定时返回 {@code null}\n\t * @throws SQLException 获取分片键时出错\n\t */",
        ),
        (
            "\t/**\n\t * Determine the super sharding key, if any. This method returns the super sharding key\n\t * relevant to the current context which will be used to obtain a direct shard connection.\n\t * @return the super sharding key, or {@code null} if it is not available or cannot be\n\t * determined (the default)\n\t * @throws SQLException if an error occurs while obtaining the super sharding key\n\t */",
            "\t/**\n\t * 确定上级分片键（若有）。返回与当前上下文相关、用于获取分片直连的上级分片键。\n\t * @return 上级分片键，不可用或无法确定时返回 {@code null}（默认）\n\t * @throws SQLException 获取上级分片键时出错\n\t */",
        ),
    ],
    "SimpleConnectionHandle.java": [
        (
            "/**\n * Simple implementation of the {@link ConnectionHandle} interface,\n * containing a given JDBC Connection.\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
            "/**\n * {@link ConnectionHandle} 接口的简单实现，持有给定 JDBC Connection。\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleConnectionHandle for the given Connection.\n\t * @param connection the JDBC Connection\n\t */",
            "\t/**\n\t * 为给定 Connection 创建新的 SimpleConnectionHandle。\n\t * @param connection JDBC Connection\n\t */",
        ),
        (
            "\t/**\n\t * Return the specified Connection as-is.\n\t */",
            "\t/**\n\t * 原样返回指定 Connection。\n\t */",
        ),
    ],
    "SimpleDriverDataSource.java": [
        (
            "/**\n * Simple implementation of the standard JDBC {@link javax.sql.DataSource} interface,\n * configuring a plain old JDBC {@link java.sql.Driver} via bean properties, and\n * returning a new {@link java.sql.Connection} from every {@code getConnection} call.\n *\n * <p><b>NOTE: This class is not an actual connection pool; it does not actually\n * pool Connections.</b> It just serves as simple replacement for a full-blown\n * connection pool, implementing the same standard interface, but creating new\n * Connections on every call.\n *\n * <p>In a Jakarta EE container, it is recommended to use a JNDI DataSource provided by\n * the container. Such a DataSource can be exposed as a DataSource bean in a Spring\n * ApplicationContext via {@link org.springframework.jndi.JndiObjectFactoryBean},\n * for seamless switching to and from a local DataSource bean like this class.\n *\n * <p>This {@code SimpleDriverDataSource} class was originally designed alongside\n * <a href=\"https://commons.apache.org/proper/commons-dbcp\">Apache Commons DBCP</a>\n * and <a href=\"https://sourceforge.net/projects/c3p0\">C3P0</a>, featuring bean-style\n * {@code BasicDataSource}/{@code ComboPooledDataSource} classes with configuration\n * properties for local resource setups. For a modern JDBC connection pool, consider\n * <a href=\"https://github.com/brettwooldridge/HikariCP\">HikariCP</a> instead,\n * exposing a corresponding {@code HikariDataSource} instance to the application.\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see DriverManagerDataSource\n */",
            "/**\n * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，\n * 通过 bean 属性配置传统 {@link java.sql.Driver}，\n * 每次 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。\n *\n * <p><b>注意：本类不是真正的连接池，不会复用 Connection。</b>\n * 它只是完整连接池的简单替代，实现相同标准接口，但每次调用都创建新 Connection。\n *\n * <p>在 Jakarta EE 容器中，建议使用容器提供的 JNDI DataSource。\n * 可通过 {@link org.springframework.jndi.JndiObjectFactoryBean} 将其暴露为 Spring\n * ApplicationContext 中的 DataSource bean，与本类本地 bean 无缝切换。\n *\n * <p>本 {@code SimpleDriverDataSource} 最初与\n * <a href=\"https://commons.apache.org/proper/commons-dbcp\">Apache Commons DBCP</a>\n * 和 <a href=\"https://sourceforge.net/projects/c3p0\">C3P0</a> 同期设计，\n * 提供 bean 风格的 {@code BasicDataSource}/{@code ComboPooledDataSource} 配置属性。\n * 现代 JDBC 连接池可考虑 <a href=\"https://github.com/brettwooldridge/HikariCP\">HikariCP</a>，\n * 向应用暴露对应的 {@code HikariDataSource} 实例。\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see DriverManagerDataSource\n */",
        ),
        (
            "\t/**\n\t * Constructor for bean-style configuration.\n\t */",
            "\t/**\n\t * 用于 bean 风格配置的构造函数。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given standard Driver parameters.\n\t * @param driver the JDBC Driver object\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。\n\t * @param driver JDBC Driver 对象\n\t * @param url 访问 Driver 使用的 JDBC URL\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given standard Driver parameters.\n\t * @param driver the JDBC Driver object\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param username the JDBC username to use for accessing the DriverManager\n\t * @param password the JDBC password to use for accessing the DriverManager\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。\n\t * @param driver JDBC Driver 对象\n\t * @param url 访问 Driver 使用的 JDBC URL\n\t * @param username 访问 Driver 使用的 JDBC 用户名\n\t * @param password 访问 Driver 使用的 JDBC 密码\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given standard Driver parameters.\n\t * @param driver the JDBC Driver object\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param conProps the JDBC connection properties\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 使用给定标准 Driver 参数创建新的 SimpleDriverDataSource。\n\t * @param driver JDBC Driver 对象\n\t * @param url 访问 Driver 使用的 JDBC URL\n\t * @param conProps JDBC 连接属性\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Specify the JDBC Driver implementation class to use.\n\t * <p>An instance of this Driver class will be created and held\n\t * within the SimpleDriverDataSource.\n\t * @see #setDriver\n\t */",
            "\t/**\n\t * 指定要使用的 JDBC Driver 实现类。\n\t * <p>将创建并持有该 Driver 类实例于 SimpleDriverDataSource 内。\n\t * @see #setDriver\n\t */",
        ),
        (
            "\t/**\n\t * Specify the JDBC Driver instance to use.\n\t * <p>This allows for passing in a shared, possibly pre-configured\n\t * Driver instance.\n\t * @see #setDriverClass\n\t */",
            "\t/**\n\t * 指定要使用的 JDBC Driver 实例。\n\t * <p>允许传入共享的、可能已预配置的 Driver 实例。\n\t * @see #setDriverClass\n\t */",
        ),
        (
            "\t/**\n\t * Return the JDBC Driver instance to use.\n\t */",
            "\t/**\n\t * 返回要使用的 JDBC Driver 实例。\n\t */",
        ),
    ],
}
