"""Chinese JavaDoc replacements for springframework wave26b datasource classes."""

DATASOURCE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SmartDataSource.java": [
        (
            "/**\n * Extension of the {@code javax.sql.DataSource} interface, to be\n * implemented by special DataSources that return JDBC Connections\n * in an unwrapped fashion.\n *\n * <p>Classes using this interface can query whether the Connection\n * should be closed after an operation. Spring's DataSourceUtils and\n * JdbcTemplate classes automatically perform such a check.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see SingleConnectionDataSource#shouldClose\n * @see DataSourceUtils#releaseConnection\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
            "/**\n * {@code javax.sql.DataSource} 接口的扩展，\n * 由以未包装方式返回 JDBC 连接的特殊数据源实现。\n *\n * <p>使用此接口的类可查询操作后是否应关闭 Connection。\n * Spring 的 DataSourceUtils 和 JdbcTemplate 会自动执行此类检查。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see SingleConnectionDataSource#shouldClose\n * @see DataSourceUtils#releaseConnection\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
        ),
        (
            "\t/**\n\t * Should we close this Connection, obtained from this DataSource?\n\t * <p>Code that uses Connections from a SmartDataSource should always\n\t * perform a check via this method before invoking {@code close()}.\n\t * <p>Note that the JdbcTemplate class in the 'jdbc.core' package takes care of\n\t * releasing JDBC Connections, freeing application code of this responsibility.\n\t * @param con the Connection to check\n\t * @return whether the given Connection should be closed\n\t * @see java.sql.Connection#close()\n\t */",
            "\t/**\n\t * 是否应关闭从此数据源获取的 Connection？\n\t * <p>使用 SmartDataSource 连接的代码在调用 {@code close()} 前\n\t * 应始终通过本方法检查。\n\t * <p>注意 jdbc.core 包中的 JdbcTemplate 负责释放 JDBC 连接，\n\t * 应用代码无需承担此职责。\n\t * @param con 待检查的 Connection\n\t * @return 给定 Connection 是否应关闭\n\t * @see java.sql.Connection#close()\n\t */",
        ),
    ],
    "SingleConnectionDataSource.java": [
        (
            "/**\n * Implementation of {@link SmartDataSource} that wraps a single JDBC Connection\n * which is not closed after use. Obviously, this is not multi-threading capable.\n *\n * <p>Note that at shutdown, someone should close the underlying Connection\n * via the {@code close()} method. Client code will never call close\n * on the Connection handle if it is SmartDataSource-aware (for example, uses\n * {@code DataSourceUtils.releaseConnection}).\n *\n * <p>If client code will call {@code close()} in the assumption of a pooled\n * Connection, like when using persistence tools, set \"suppressClose\" to \"true\".\n * This will return a close-suppressing proxy instead of the physical Connection.\n *\n * <p>This is primarily intended for testing. For example, it enables easy testing\n * outside an application server, for code that expects to work on a DataSource.\n * In contrast to {@link DriverManagerDataSource}, it reuses the same Connection\n * all the time, avoiding excessive creation of physical Connections.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #getConnection()\n * @see java.sql.Connection#close()\n * @see DataSourceUtils#releaseConnection\n */",
            "/**\n * {@link SmartDataSource} 的实现，包装单个 JDBC 连接，\n * 使用后不关闭。显然，它不支持多线程。\n *\n * <p>注意关闭时应有人通过 {@code close()} 方法关闭底层连接。\n * 若客户端代码感知 SmartDataSource（例如使用 {@code DataSourceUtils.releaseConnection}），\n * 则不会对 Connection 句柄调用 close。\n *\n * <p>若客户端代码会像使用池化连接一样调用 {@code close()}（如使用持久化工具），\n * 请将 \"suppressClose\" 设为 \"true\"，\n * 此时将返回抑制 close 的代理而非物理 Connection。\n *\n * <p>主要用于测试。例如可在应用服务器外轻松测试期望 DataSource 的代码。\n * 与 {@link DriverManagerDataSource} 不同，它始终复用同一连接，\n * 避免频繁创建物理连接。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #getConnection()\n * @see java.sql.Connection#close()\n * @see DataSourceUtils#releaseConnection\n */",
        ),
        (
            "\t/** Create a close-suppressing proxy? */",
            "\t/** 是否创建抑制 close 的代理？ */",
        ),
        (
            "\t/** Explicit rollback before close? */",
            "\t/** 关闭前是否显式回滚？ */",
        ),
        (
            "\t/** Override auto-commit state? */",
            "\t/** 是否覆盖自动提交状态？ */",
        ),
        (
            "\t/** Wrapped Connection. */",
            "\t/** 被包装的 Connection。 */",
        ),
        (
            "\t/** Proxy Connection. */",
            "\t/** 代理 Connection。 */",
        ),
        (
            "\t/** Lifecycle lock for the shared Connection. */",
            "\t/** 共享 Connection 的生命周期锁。 */",
        ),
        (
            "\t/**\n\t * Constructor for bean-style configuration.\n\t */",
            "\t/**\n\t * 用于 Bean 风格配置的构造器。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SingleConnectionDataSource with the given standard\n\t * DriverManager parameters.\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param username the JDBC username to use for accessing the DriverManager\n\t * @param password the JDBC password to use for accessing the DriverManager\n\t * @param suppressClose if the returned Connection should be a\n\t * close-suppressing proxy or the physical Connection\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
            "\t/**\n\t * 使用给定标准 DriverManager 参数创建 SingleConnectionDataSource。\n\t * @param url 访问 DriverManager 的 JDBC URL\n\t * @param username 访问 DriverManager 的 JDBC 用户名\n\t * @param password 访问 DriverManager 的 JDBC 密码\n\t * @param suppressClose 返回的 Connection 是抑制 close 的代理还是物理 Connection\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SingleConnectionDataSource with the given standard\n\t * DriverManager parameters.\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param suppressClose if the returned Connection should be a\n\t * close-suppressing proxy or the physical Connection\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
            "\t/**\n\t * 使用给定标准 DriverManager 参数创建 SingleConnectionDataSource。\n\t * @param url 访问 DriverManager 的 JDBC URL\n\t * @param suppressClose 返回的 Connection 是抑制 close 的代理还是物理 Connection\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SingleConnectionDataSource with a given Connection.\n\t * @param target underlying target Connection\n\t * @param suppressClose if the Connection should be wrapped with a Connection that\n\t * suppresses {@code close()} calls (to allow for normal {@code close()}\n\t * usage in applications that expect a pooled Connection but do not know our\n\t * SmartDataSource interface)\n\t */",
            "\t/**\n\t * 使用给定 Connection 创建 SingleConnectionDataSource。\n\t * @param target 底层目标 Connection\n\t * @param suppressClose 是否用抑制 {@code close()} 调用的 Connection 包装\n\t * （以便期望池化连接但不了解 SmartDataSource 接口的应用正常调用 {@code close()}）\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether the returned Connection should be a close-suppressing proxy\n\t * or the physical Connection.\n\t */",
            "\t/**\n\t * 指定返回的 Connection 是抑制 close 的代理还是物理 Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the returned Connection will be a close-suppressing proxy\n\t * or the physical Connection.\n\t */",
            "\t/**\n\t * 返回返回的 Connection 是抑制 close 的代理还是物理 Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether the shared Connection should be explicitly rolled back\n\t * before close (if not in auto-commit mode).\n\t * <p>This is recommended for the Oracle JDBC driver in testing scenarios.\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 指定关闭前是否显式回滚共享 Connection（非自动提交模式下）。\n\t * <p>测试场景下建议对 Oracle JDBC 驱动启用此选项。\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the shared Connection should be explicitly rolled back\n\t * before close (if not in auto-commit mode).\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 返回关闭前是否显式回滚共享 Connection（非自动提交模式下）。\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether the returned Connection's \"autoCommit\" setting should be overridden.\n\t */",
            "\t/**\n\t * 指定是否覆盖返回 Connection 的 \"autoCommit\" 设置。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the returned Connection's \"autoCommit\" setting should be overridden.\n\t * @return the \"autoCommit\" value, or {@code null} if none to be applied\n\t */",
            "\t/**\n\t * 返回是否覆盖返回 Connection 的 \"autoCommit\" 设置。\n\t * @return \"autoCommit\" 值，无需应用时为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Specifying a custom username and password doesn't make sense\n\t * with a single Connection. Returns the single Connection if given\n\t * the same username and password; throws an SQLException else.\n\t */",
            "\t/**\n\t * 单连接模式下指定自定义用户名和密码无意义。\n\t * 用户名和密码相同时返回该连接，否则抛出 SQLException。\n\t */",
        ),
        (
            "\t/**\n\t * This is a single Connection: Do not close it when returning to the \"pool\".\n\t */",
            "\t/**\n\t * 这是单连接：归还到\"池\"时不关闭。\n\t */",
        ),
        (
            "\t/**\n\t * Close the underlying Connection.\n\t * The provider of this DataSource needs to care for proper shutdown.\n\t * <p>As this class implements {@link AutoCloseable}, it can be used\n\t * with a try-with-resource statement.\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 关闭底层 Connection。\n\t * 此 DataSource 的提供者需负责正确关闭。\n\t * <p>本类实现 {@link AutoCloseable}，可用于 try-with-resources。\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Close the underlying Connection.\n\t * The provider of this DataSource needs to care for proper shutdown.\n\t * <p>As this bean implements {@link DisposableBean}, a bean factory\n\t * will automatically invoke this on destruction of the bean.\n\t */",
            "\t/**\n\t * 关闭底层 Connection。\n\t * 此 DataSource 的提供者需负责正确关闭。\n\t * <p>本 Bean 实现 {@link DisposableBean}，Bean 工厂销毁时会自动调用。\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the underlying Connection via the DriverManager.\n\t */",
            "\t/**\n\t * 通过 DriverManager 初始化底层 Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Reset the underlying shared Connection, to be reinitialized on next access.\n\t */",
            "\t/**\n\t * 重置底层共享 Connection，下次访问时重新初始化。\n\t */",
        ),
        (
            "\t/**\n\t * Prepare the given Connection before it is exposed.\n\t * <p>The default implementation applies the auto-commit flag, if necessary.\n\t * Can be overridden in subclasses.\n\t * @param con the Connection to prepare\n\t * @see #setAutoCommit\n\t */",
            "\t/**\n\t * 在暴露前准备给定 Connection。\n\t * <p>默认实现按需应用 auto-commit 标志。\n\t * 子类可覆盖。\n\t * @param con 待准备的 Connection\n\t * @see #setAutoCommit\n\t */",
        ),
        (
            "\t/**\n\t * Close the underlying shared Connection.\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 关闭底层共享 Connection。\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Wrap the given Connection with a proxy that delegates every method call to it\n\t * but suppresses close calls.\n\t * @param target the original Connection to wrap\n\t * @return the wrapped Connection\n\t */",
            "\t/**\n\t * 用代理包装给定 Connection，将所有方法调用委托给它但抑制 close 调用。\n\t * @param target 待包装的原始 Connection\n\t * @return 包装后的 Connection\n\t */",
        ),
        (
            "\t/**\n\t * Invocation handler that suppresses close calls on JDBC Connections.\n\t */",
            "\t/**\n\t * 抑制 JDBC Connection 上 close 调用的调用处理器。\n\t */",
        ),
        (
            "\t\t\t// Invocation on ConnectionProxy interface coming in...",
            "\t\t\t// 来自 ConnectionProxy 接口的调用...",
        ),
        (
            "\t\t\t\t// Only consider equal when proxies are identical.",
            "\t\t\t\t// 仅当代理相同时视为相等。",
        ),
        (
            "\t\t\t\t// Use hashCode of Connection proxy.",
            "\t\t\t\t// 使用 Connection 代理的 hashCode。",
        ),
        (
            "\t\t\t\t// Handle close method: don't pass the call on.",
            "\t\t\t\t// 处理 close 方法：不继续传递调用。",
        ),
        (
            "\t\t\t\t// Handle getTargetConnection method: return underlying Connection.",
            "\t\t\t\t// 处理 getTargetConnection 方法：返回底层 Connection。",
        ),
        (
            "\t\t\t\t\t\t// Invoke method on target Connection.",
            "\t\t\t\t\t\t// 在目标 Connection 上调用方法。",
        ),
        (
            "\t\t\t\t// No underlying Connection -> lazy init via DriverManager.",
            "\t\t\t\t// 无底层 Connection -> 通过 DriverManager 懒初始化。",
        ),
    ],
    "TransactionAwareDataSourceProxy.java": [
        (
            "/**\n * Proxy for a target JDBC {@link javax.sql.DataSource}, adding awareness of\n * Spring-managed transactions. Similar to a transactional JNDI DataSource\n * as provided by a Jakarta EE server.\n *\n * <p>Data access code that should remain unaware of Spring's data access support\n * can work with this proxy to seamlessly participate in Spring-managed transactions.\n * Note that the transaction manager, for example {@link DataSourceTransactionManager},\n * still needs to work with the underlying DataSource, <i>not</i> with this proxy.\n *\n * <p><b>Make sure that TransactionAwareDataSourceProxy is the outermost DataSource\n * of a chain of DataSource proxies/adapters.</b> TransactionAwareDataSourceProxy\n * can delegate either directly to the target connection pool or to some\n * intermediary proxy/adapter like {@link LazyConnectionDataSourceProxy} or\n * {@link UserCredentialsDataSourceAdapter}.\n *\n * <p>Delegates to {@link DataSourceUtils} for automatically participating in\n * thread-bound transactions, for example managed by {@link DataSourceTransactionManager}.\n * {@code getConnection} calls and {@code close} calls on returned Connections\n * will behave properly within a transaction, i.e. always operate on the transactional\n * Connection. If not within a transaction, normal DataSource behavior applies.\n *\n * <p>This proxy allows data access code to work with the plain JDBC API and still\n * participate in Spring-managed transactions, similar to JDBC code in a Jakarta EE/JTA\n * environment. However, if possible, use Spring's DataSourceUtils, JdbcTemplate or\n * JDBC operation objects to get transaction participation even without a proxy for\n * the target DataSource, avoiding the need to define such a proxy in the first place.\n *\n * <p>As a further effect, using a transaction-aware DataSource will apply remaining\n * transaction timeouts to all created JDBC (Prepared/Callable)Statement. This means\n * that all operations performed through standard JDBC will automatically participate\n * in Spring-managed transaction timeouts.\n *\n * <p><b>NOTE:</b> This DataSource proxy needs to return wrapped Connections (which\n * implement the {@link ConnectionProxy} interface) in order to handle close calls\n * properly. Use {@link Connection#unwrap} to retrieve the native JDBC Connection.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see javax.sql.DataSource#getConnection()\n * @see java.sql.Connection#close()\n * @see DataSourceUtils#doGetConnection\n * @see DataSourceUtils#applyTransactionTimeout\n * @see DataSourceUtils#doReleaseConnection\n */",
            "/**\n * 目标 JDBC {@link javax.sql.DataSource} 的代理，增加对 Spring 管理事务的感知。\n * 类似于 Jakarta EE 服务器提供的带事务 JNDI DataSource。\n *\n * <p>应保持对 Spring 数据访问支持无感的数据访问代码\n * 可通过此代理无缝参与 Spring 管理的事务。\n * 注意事务管理器（如 {@link DataSourceTransactionManager}）\n * 仍需与底层 DataSource 协作，<i>而非</i>此代理。\n *\n * <p><b>请确保 TransactionAwareDataSourceProxy 是 DataSource 代理/适配器链的最外层。</b>\n * 它可直接委托目标连接池，或委托 {@link LazyConnectionDataSourceProxy}、\n * {@link UserCredentialsDataSourceAdapter} 等中间代理/适配器。\n *\n * <p>委托 {@link DataSourceUtils} 自动参与线程绑定事务\n * （例如由 {@link DataSourceTransactionManager} 管理）。\n * 事务内 {@code getConnection} 及返回 Connection 上的 {@code close}\n * 将正确行为，即始终操作事务 Connection；非事务时使用普通 DataSource 行为。\n *\n * <p>此代理使数据访问代码使用纯 JDBC API 仍可参与 Spring 管理事务，\n * 类似 Jakarta EE/JTA 环境中的 JDBC 代码。但如有可能，应使用 Spring 的\n * DataSourceUtils、JdbcTemplate 或 JDBC 操作对象获取事务参与，\n * 无需为目标 DataSource 定义此类代理。\n *\n * <p>此外，使用事务感知 DataSource 会将剩余事务超时应用于\n * 所有创建的 JDBC (Prepared/Callable)Statement，\n * 即标准 JDBC 操作自动参与 Spring 管理的事务超时。\n *\n * <p><b>注意：</b> 此 DataSource 代理需返回包装 Connection（实现 {@link ConnectionProxy}），\n * 以正确处理 close 调用。使用 {@link Connection#unwrap} 获取原生 JDBC Connection。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see javax.sql.DataSource#getConnection()\n * @see java.sql.Connection#close()\n * @see DataSourceUtils#doGetConnection\n * @see DataSourceUtils#applyTransactionTimeout\n * @see DataSourceUtils#doReleaseConnection\n */",
        ),
        (
            "\t/**\n\t * Create a new TransactionAwareDataSourceProxy.\n\t * @see #setTargetDataSource\n\t */",
            "\t/**\n\t * 创建新的 TransactionAwareDataSourceProxy。\n\t * @see #setTargetDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Create a new TransactionAwareDataSourceProxy.\n\t * @param targetDataSource the target DataSource\n\t */",
            "\t/**\n\t * 创建新的 TransactionAwareDataSourceProxy。\n\t * @param targetDataSource 目标 DataSource\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to obtain the transactional target Connection lazily on\n\t * actual data access.\n\t * <p>The default is \"true\". Specify \"false\" to immediately obtain a target\n\t * Connection when a transaction-aware Connection handle is retrieved.\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 指定是否在实际数据访问时懒获取事务目标 Connection。\n\t * <p>默认为 \"true\"。设为 \"false\" 则在获取事务感知 Connection 句柄时立即获取目标 Connection。\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to reobtain the target Connection for each operation\n\t * performed within a transaction.\n\t * <p>The default is \"false\". Specify \"true\" to reobtain transactional\n\t * Connections for every call on the Connection proxy; this is advisable\n\t * on JBoss if you hold on to a Connection handle across transaction boundaries.\n\t * <p>The effect of this setting is similar to the\n\t * \"hibernate.connection.release_mode\" value \"after_statement\".\n\t */",
            "\t/**\n\t * 指定事务内每次操作是否重新获取目标 Connection。\n\t * <p>默认为 \"false\"。设为 \"true\" 则 Connection 代理每次调用都重新获取事务 Connection；\n\t * 若在 JBoss 上跨事务边界持有 Connection 句柄，建议启用。\n\t * <p>此设置效果类似 \"hibernate.connection.release_mode\" 的 \"after_statement\"。\n\t */",
        ),
        (
            "\t/**\n\t * Delegates to DataSourceUtils for automatically participating in Spring-managed\n\t * transactions. Throws the original SQLException, if any.\n\t * <p>The returned Connection handle implements the ConnectionProxy interface,\n\t * allowing to retrieve the underlying target Connection.\n\t * @return a transactional Connection if any, a new one else\n\t * @see DataSourceUtils#doGetConnection\n\t * @see ConnectionProxy#getTargetConnection\n\t */",
            "\t/**\n\t * 委托 DataSourceUtils 自动参与 Spring 管理的事务。如有则抛出原始 SQLException。\n\t * <p>返回的 Connection 句柄实现 ConnectionProxy 接口，\n\t * 可获取底层目标 Connection。\n\t * @return 有事务时返回事务 Connection，否则返回新连接\n\t * @see DataSourceUtils#doGetConnection\n\t * @see ConnectionProxy#getTargetConnection\n\t */",
        ),
        (
            "\t/**\n\t * Wraps the given Connection with a proxy that delegates every method call to it\n\t * but delegates {@code close()} calls to DataSourceUtils.\n\t * @param targetDataSource the DataSource that the Connection came from\n\t * @return the wrapped Connection\n\t * @see java.sql.Connection#close()\n\t * @see DataSourceUtils#doReleaseConnection\n\t */",
            "\t/**\n\t * 用代理包装给定 Connection，将所有方法调用委托给它，\n\t * 但将 {@code close()} 委托给 DataSourceUtils。\n\t * @param targetDataSource Connection 来源的 DataSource\n\t * @return 包装后的 Connection\n\t * @see java.sql.Connection#close()\n\t * @see DataSourceUtils#doReleaseConnection\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether to obtain a fixed target Connection for the proxy\n\t * or to reobtain the target Connection for each operation.\n\t * <p>The default implementation returns {@code true} for all\n\t * standard cases. This can be overridden through the\n\t * {@link #setReobtainTransactionalConnections \"reobtainTransactionalConnections\"}\n\t * flag, which enforces a non-fixed target Connection within an active transaction.\n\t * Note that non-transactional access will always use a fixed Connection.\n\t * @param targetDataSource the target DataSource\n\t */",
            "\t/**\n\t * 确定代理是获取固定目标 Connection，还是每次操作重新获取。\n\t * <p>默认实现对所有标准情况返回 {@code true}。\n\t * 可通过 {@link #setReobtainTransactionalConnections \"reobtainTransactionalConnections\"}\n\t * 标志覆盖，在活动事务内强制非固定目标 Connection。\n\t * 注意非事务访问始终使用固定 Connection。\n\t * @param targetDataSource 目标 DataSource\n\t */",
        ),
        (
            "\t/**\n\t * Invocation handler that delegates close calls on JDBC Connections\n\t * to DataSourceUtils for being aware of thread-bound transactions.\n\t */",
            "\t/**\n\t * 将 JDBC Connection 上的 close 调用委托给 DataSourceUtils，\n\t * 以感知线程绑定事务的调用处理器。\n\t */",
        ),
        (
            "\t\t\t// Invocation on ConnectionProxy interface coming in...",
            "\t\t\t// 来自 ConnectionProxy 接口的调用...",
        ),
        (
            "\t\t\t\t\t// Only considered as equal when proxies are identical.",
            "\t\t\t\t\t// 仅当代理相同时视为相等。",
        ),
        (
            "\t\t\t\t\t// Use hashCode of Connection proxy.",
            "\t\t\t\t\t// 使用 Connection 代理的 hashCode。",
        ),
        (
            "\t\t\t\t\t// Allow for differentiating between the proxy and the raw Connection.",
            "\t\t\t\t\t// 便于区分代理与原始 Connection。",
        ),
        (
            "\t\t\t\t\t// Handle close method: only close if not within a transaction.",
            "\t\t\t\t\t// 处理 close 方法：仅非事务内才关闭。",
        ),
        (
            "\t\t\t\t\t\t\t// It's the transactional Connection: Don't close it.",
            "\t\t\t\t\t\t\t// 这是事务 Connection：不关闭。",
        ),
        (
            "\t\t\t\t\t// Avoid creation of target Connection on pre-close cleanup (for example, Hibernate Session)",
            "\t\t\t\t\t// 关闭前清理时避免创建目标 Connection（例如 Hibernate Session）",
        ),
        (
            "\t\t\t\t// Handle getTargetConnection method: return underlying Connection.",
            "\t\t\t\t// 处理 getTargetConnection 方法：返回底层 Connection。",
        ),
        (
            "\t\t\t// Invoke method on target Connection.",
            "\t\t\t// 在目标 Connection 上调用方法。",
        ),
        (
            "\t\t\t\t// If return value is a Statement, apply transaction timeout.",
            "\t\t\t\t// 若返回值为 Statement，应用事务超时。",
        ),
        (
            "\t\t\t\t// Applies to createStatement, prepareStatement, prepareCall.",
            "\t\t\t\t// 适用于 createStatement、prepareStatement、prepareCall。",
        ),
    ],
    "UserCredentialsDataSourceAdapter.java": [
        (
            "/**\n * An adapter for a target JDBC {@link javax.sql.DataSource}, applying the specified\n * user credentials to every standard {@code getConnection()} call, implicitly\n * invoking {@code getConnection(username, password)} on the target.\n * All other methods simply delegate to the corresponding methods of the\n * target DataSource.\n *\n * <p>Can be used to proxy a target JNDI DataSource that does not have user\n * credentials configured. Client code can work with this DataSource as usual,\n * using the standard {@code getConnection()} call.\n *\n * <p>In the following example, client code can simply transparently work with\n * the preconfigured \"myDataSource\", implicitly accessing \"myTargetDataSource\"\n * with the specified user credentials.\n *\n * <pre class=\"code\">\n * &lt;bean id=\"myTargetDataSource\" class=\"org.springframework.jndi.JndiObjectFactoryBean\"&gt;\n *   &lt;property name=\"jndiName\" value=\"java:comp/env/jdbc/myds\"/&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"myDataSource\" class=\"org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter\"&gt;\n *   &lt;property name=\"targetDataSource\" ref=\"myTargetDataSource\"/&gt;\n *   &lt;property name=\"username\" value=\"myusername\"/&gt;\n *   &lt;property name=\"password\" value=\"mypassword\"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * <p>If the \"username\" is empty, this proxy will simply delegate to the\n * standard {@code getConnection()} method of the target DataSource.\n * This can be used to keep a UserCredentialsDataSourceAdapter bean definition\n * just for the <i>option</i> of implicitly passing in user credentials if\n * the particular target DataSource requires it.\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @see #getConnection\n */",
            "/**\n * 目标 JDBC {@link javax.sql.DataSource} 的适配器，\n * 将指定用户凭据应用于每次标准 {@code getConnection()} 调用，\n * 隐式在目标上调用 {@code getConnection(username, password)}。\n * 其他方法均委托给目标 DataSource 的对应方法。\n *\n * <p>可用于代理未配置用户凭据的目标 JNDI DataSource。\n * 客户端代码可照常使用标准 {@code getConnection()} 调用。\n *\n * <p>下例中，客户端代码可透明地使用预配置的 \"myDataSource\"，\n * 以指定凭据隐式访问 \"myTargetDataSource\"。\n *\n * <pre class=\"code\">\n * &lt;bean id=\"myTargetDataSource\" class=\"org.springframework.jndi.JndiObjectFactoryBean\"&gt;\n *   &lt;property name=\"jndiName\" value=\"java:comp/env/jdbc/myds\"/&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id=\"myDataSource\" class=\"org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter\"&gt;\n *   &lt;property name=\"targetDataSource\" ref=\"myTargetDataSource\"/&gt;\n *   &lt;property name=\"username\" value=\"myusername\"/&gt;\n *   &lt;property name=\"password\" value=\"mypassword\"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * <p>若 \"username\" 为空，此代理直接委托目标 DataSource 的标准 {@code getConnection()}。\n * 可保留 UserCredentialsDataSourceAdapter Bean 定义，\n * 以便在特定目标 DataSource 需要时<i>可选</i>隐式传入用户凭据。\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @see #getConnection\n */",
        ),
        (
            "\t/**\n\t * Set the default username that this adapter should use for retrieving Connections.\n\t * <p>Default is no specific user. Note that an explicitly specified username\n\t * will always override any username/password specified at the DataSource level.\n\t * @see #setPassword\n\t * @see #setCredentialsForCurrentThread(String, String)\n\t * @see #getConnection(String, String)\n\t */",
            "\t/**\n\t * 设置此适配器获取 Connection 时使用的默认用户名。\n\t * <p>默认无特定用户。显式指定的用户名始终覆盖 DataSource 级别的用户名/密码。\n\t * @see #setPassword\n\t * @see #setCredentialsForCurrentThread(String, String)\n\t * @see #getConnection(String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Set the default user's password that this adapter should use for retrieving Connections.\n\t * <p>Default is no specific password. Note that an explicitly specified username\n\t * will always override any username/password specified at the DataSource level.\n\t * @see #setUsername\n\t * @see #setCredentialsForCurrentThread(String, String)\n\t * @see #getConnection(String, String)\n\t */",
            "\t/**\n\t * 设置此适配器获取 Connection 时使用的默认用户密码。\n\t * <p>默认无特定密码。显式指定的用户名始终覆盖 DataSource 级别的用户名/密码。\n\t * @see #setUsername\n\t * @see #setCredentialsForCurrentThread(String, String)\n\t * @see #getConnection(String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Specify a database catalog to be applied to each retrieved Connection.\n\t * @since 4.3.2\n\t * @see Connection#setCatalog\n\t */",
            "\t/**\n\t * 指定应用于每个获取 Connection 的数据库 catalog。\n\t * @since 4.3.2\n\t * @see Connection#setCatalog\n\t */",
        ),
        (
            "\t/**\n\t * Specify a database schema to be applied to each retrieved Connection.\n\t * @since 4.3.2\n\t * @see Connection#setSchema\n\t */",
            "\t/**\n\t * 指定应用于每个获取 Connection 的数据库 schema。\n\t * @since 4.3.2\n\t * @see Connection#setSchema\n\t */",
        ),
        (
            "\t/**\n\t * Set user credentials for this proxy and the current thread.\n\t * The given username and password will be applied to all subsequent\n\t * {@code getConnection()} calls on this DataSource proxy.\n\t * <p>This will override any statically specified user credentials,\n\t * that is, values of the \"username\" and \"password\" bean properties.\n\t * @param username the username to apply\n\t * @param password the password to apply\n\t * @see #removeCredentialsFromCurrentThread\n\t */",
            "\t/**\n\t * 为此代理及当前线程设置用户凭据。\n\t * 给定用户名和密码将应用于此后此 DataSource 代理的所有 {@code getConnection()} 调用。\n\t * <p>将覆盖静态指定的用户凭据，即 \"username\" 和 \"password\" Bean 属性值。\n\t * @param username 要应用的用户名\n\t * @param password 要应用的密码\n\t * @see #removeCredentialsFromCurrentThread\n\t */",
        ),
        (
            "\t/**\n\t * Remove any user credentials for this proxy from the current thread.\n\t * Statically specified user credentials apply again afterwards.\n\t * @see #setCredentialsForCurrentThread\n\t */",
            "\t/**\n\t * 从当前线程移除此代理的用户凭据。\n\t * 之后重新应用静态指定的用户凭据。\n\t * @see #setCredentialsForCurrentThread\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether there are currently thread-bound credentials,\n\t * using them if available, falling back to the statically specified\n\t * username and password (i.e. values of the bean properties) otherwise.\n\t * <p>Delegates to {@link #doGetConnection(String, String)} with the\n\t * determined credentials as parameters.\n\t * @see #doGetConnection\n\t */",
            "\t/**\n\t * 确定当前是否有线程绑定凭据，有则使用，\n\t * 否则回退到静态指定的用户名和密码（即 Bean 属性值）。\n\t * <p>以确定的凭据为参数委托 {@link #doGetConnection(String, String)}。\n\t * @see #doGetConnection\n\t */",
        ),
        (
            "\t/**\n\t * Simply delegates to {@link #doGetConnection(String, String)},\n\t * keeping the given user credentials as-is.\n\t */",
            "\t/**\n\t * 直接委托 {@link #doGetConnection(String, String)}，\n\t * 保持给定用户凭据不变。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to the {@code getConnection(username, password)}\n\t * method of the target DataSource, passing in the specified user credentials.\n\t * If the specified username is empty, it will simply delegate to the standard\n\t * {@code getConnection()} method of the target DataSource.\n\t * @param username the username to use\n\t * @param password the password to use\n\t * @return the Connection\n\t * @see javax.sql.DataSource#getConnection(String, String)\n\t * @see javax.sql.DataSource#getConnection()\n\t */",
            "\t/**\n\t * 本实现委托目标 DataSource 的 {@code getConnection(username, password)}，\n\t * 传入指定用户凭据。\n\t * 若用户名为空，则直接委托目标 DataSource 的标准 {@code getConnection()}。\n\t * @param username 要使用的用户名\n\t * @param password 要使用的密码\n\t * @return Connection\n\t * @see javax.sql.DataSource#getConnection(String, String)\n\t * @see javax.sql.DataSource#getConnection()\n\t */",
        ),
        (
            "\t/**\n\t * Inner class used as ThreadLocal value.\n\t */",
            "\t/**\n\t * 用作 ThreadLocal 值的内部类。\n\t */",
        ),
    ],
}
