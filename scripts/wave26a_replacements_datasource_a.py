"""Chinese JavaDoc replacements for springframework wave26a datasource classes (part A)."""

DATASOURCE_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractDataSource.java": [
        (
            "/**\n * Abstract base class for Spring's {@link javax.sql.DataSource}\n * implementations, taking care of the padding.\n *\n * <p>'Padding' in the context of this class means default implementations\n * for certain methods from the {@code DataSource} interface, such as\n * {@link #getLoginTimeout()}, {@link #setLoginTimeout(int)}, and so forth.\n *\n * @author Juergen Hoeller\n * @since 07.05.2003\n * @see DriverManagerDataSource\n */",
            "/**\n * Spring {@link javax.sql.DataSource} 实现的抽象基类，负责接口“填充”逻辑。\n *\n * <p>本类语境下的“填充”指为 {@code DataSource} 接口的若干方法\n * 提供默认实现，例如 {@link #getLoginTimeout()}、{@link #setLoginTimeout(int)} 等。\n *\n * @author Juergen Hoeller\n * @since 07.05.2003\n * @see DriverManagerDataSource\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的 Logger。 */",
        ),
        (
            "\t/**\n\t * Returns 0, indicating the default system timeout is to be used.\n\t */",
            "\t/**\n\t * 返回 0，表示使用系统默认超时。\n\t */",
        ),
        (
            "\t/**\n\t * Setting a login timeout is not supported.\n\t */",
            "\t/**\n\t * 不支持设置登录超时。\n\t */",
        ),
        (
            "\t/**\n\t * LogWriter methods are not supported.\n\t */",
            "\t/**\n\t * 不支持 LogWriter 相关方法。\n\t */",
        ),
    ],
    "AbstractDriverBasedDataSource.java": [
        (
            "/**\n * Abstract base class for JDBC {@link javax.sql.DataSource} implementations\n * that operate on a JDBC {@link java.sql.Driver}.\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see SimpleDriverDataSource\n * @see DriverManagerDataSource\n */",
            "/**\n * 基于 JDBC {@link java.sql.Driver} 的 {@link javax.sql.DataSource} 实现抽象基类。\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see SimpleDriverDataSource\n * @see DriverManagerDataSource\n */",
        ),
        (
            "\t/**\n\t * Set the JDBC URL to use for connecting through the Driver.\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 设置通过 Driver 连接时使用的 JDBC URL。\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Return the JDBC URL to use for connecting through the Driver.\n\t */",
            "\t/**\n\t * 返回通过 Driver 连接时使用的 JDBC URL。\n\t */",
        ),
        (
            "\t/**\n\t * Set the JDBC username to use for connecting through the Driver.\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 设置通过 Driver 连接时使用的 JDBC 用户名。\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Return the JDBC username to use for connecting through the Driver.\n\t */",
            "\t/**\n\t * 返回通过 Driver 连接时使用的 JDBC 用户名。\n\t */",
        ),
        (
            "\t/**\n\t * Set the JDBC password to use for connecting through the Driver.\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 设置通过 Driver 连接时使用的 JDBC 密码。\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Return the JDBC password to use for connecting through the Driver.\n\t */",
            "\t/**\n\t * 返回通过 Driver 连接时使用的 JDBC 密码。\n\t */",
        ),
        (
            "\t/**\n\t * Specify a database catalog to be applied to each Connection.\n\t * @since 4.3.2\n\t * @see Connection#setCatalog\n\t */",
            "\t/**\n\t * 指定要应用到每个 Connection 的数据库 catalog。\n\t * @since 4.3.2\n\t * @see Connection#setCatalog\n\t */",
        ),
        (
            "\t/**\n\t * Return the database catalog to be applied to each Connection, if any.\n\t * @since 4.3.2\n\t */",
            "\t/**\n\t * 返回要应用到每个 Connection 的数据库 catalog（若有）。\n\t * @since 4.3.2\n\t */",
        ),
        (
            "\t/**\n\t * Specify a database schema to be applied to each Connection.\n\t * @since 4.3.2\n\t * @see Connection#setSchema\n\t */",
            "\t/**\n\t * 指定要应用到每个 Connection 的数据库 schema。\n\t * @since 4.3.2\n\t * @see Connection#setSchema\n\t */",
        ),
        (
            "\t/**\n\t * Return the database schema to be applied to each Connection, if any.\n\t * @since 4.3.2\n\t */",
            "\t/**\n\t * 返回要应用到每个 Connection 的数据库 schema（若有）。\n\t * @since 4.3.2\n\t */",
        ),
        (
            "\t/**\n\t * Specify arbitrary connection properties as key/value pairs,\n\t * to be passed to the Driver.\n\t * <p>Can also contain \"user\" and \"password\" properties. However,\n\t * any \"username\" and \"password\" bean properties specified on this\n\t * DataSource will override the corresponding connection properties.\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 以键值对形式指定任意连接属性，传递给 Driver。\n\t * <p>也可包含 \"user\" 和 \"password\" 属性。但本 DataSource 上\n\t * 指定的 username/password bean 属性会覆盖对应连接属性。\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Return the connection properties to be passed to the Driver, if any.\n\t */",
            "\t/**\n\t * 返回要传递给 Driver 的连接属性（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to {@code getConnectionFromDriver},\n\t * using the default username and password of this DataSource.\n\t * @see #getConnectionFromDriver(String, String)\n\t * @see #setUsername\n\t * @see #setPassword\n\t */",
            "\t/**\n\t * 本实现委托 {@code getConnectionFromDriver}，\n\t * 使用本 DataSource 的默认用户名和密码。\n\t * @see #getConnectionFromDriver(String, String)\n\t * @see #setUsername\n\t * @see #setPassword\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to {@code getConnectionFromDriver},\n\t * using the given username and password.\n\t * @see #getConnectionFromDriver(String, String)\n\t */",
            "\t/**\n\t * 本实现委托 {@code getConnectionFromDriver}，\n\t * 使用给定用户名和密码。\n\t * @see #getConnectionFromDriver(String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Build properties for the Driver, including the given username and password (if any),\n\t * and obtain a corresponding Connection.\n\t * @param username the name of the user\n\t * @param password the password to use\n\t * @return the obtained Connection\n\t * @throws SQLException in case of failure\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 构建 Driver 所需属性（含给定用户名和密码），并获取对应 Connection。\n\t * @param username 用户名\n\t * @param password 密码\n\t * @return 获取到的 Connection\n\t * @throws SQLException 失败时\n\t * @see java.sql.Driver#connect(String, java.util.Properties)\n\t */",
        ),
        (
            "\t/**\n\t * Obtain a Connection using the given properties.\n\t * <p>Template method to be implemented by subclasses.\n\t * @param props the merged connection properties\n\t * @return the obtained Connection\n\t * @throws SQLException in case of failure\n\t */",
            "\t/**\n\t * 使用给定属性获取 Connection。\n\t * <p>由子类实现的模板方法。\n\t * @param props 合并后的连接属性\n\t * @return 获取到的 Connection\n\t * @throws SQLException 失败时\n\t */",
        ),
    ],
    "ConnectionHandle.java": [
        (
            "/**\n * Simple interface to be implemented by handles for a JDBC Connection.\n * Used by JpaDialect, for example.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see SimpleConnectionHandle\n * @see ConnectionHolder\n */",
            "/**\n * JDBC Connection 句柄的简单接口。\n * 例如 JpaDialect 会使用它。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see SimpleConnectionHandle\n * @see ConnectionHolder\n */",
        ),
        (
            "\t/**\n\t * Fetch the JDBC Connection that this handle refers to.\n\t */",
            "\t/**\n\t * 获取本句柄引用的 JDBC Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Release the JDBC Connection that this handle refers to.\n\t * <p>The default implementation is empty, assuming that the lifecycle\n\t * of the connection is managed externally.\n\t * @param con the JDBC Connection to release\n\t */",
            "\t/**\n\t * 释放本句柄引用的 JDBC Connection。\n\t * <p>默认实现为空，假定连接生命周期由外部管理。\n\t * @param con 要释放的 JDBC Connection\n\t */",
        ),
    ],
    "ConnectionHolder.java": [
        (
            "/**\n * Resource holder wrapping a JDBC {@link Connection}.\n * {@link DataSourceTransactionManager} binds instances of this class\n * to the thread, for a specific {@link javax.sql.DataSource}.\n *\n * <p>Inherits rollback-only support for nested JDBC transactions\n * and reference count functionality from the base class.\n *\n * <p>Note: This is an SPI class, not intended to be used by applications.\n *\n * @author Juergen Hoeller\n * @since 06.05.2003\n * @see DataSourceTransactionManager\n * @see DataSourceUtils\n */",
            "/**\n * 包装 JDBC {@link Connection} 的资源持有者。\n * {@link DataSourceTransactionManager} 会将本类实例\n * 绑定到特定 {@link javax.sql.DataSource} 的线程上下文。\n *\n * <p>从基类继承嵌套 JDBC 事务的 rollback-only 支持与引用计数功能。\n *\n * <p>注意：这是 SPI 类，不供应用程序直接使用。\n *\n * @author Juergen Hoeller\n * @since 06.05.2003\n * @see DataSourceTransactionManager\n * @see DataSourceUtils\n */",
        ),
        (
            "\t/**\n\t * Prefix for savepoint names.\n\t */",
            "\t/**\n\t * 保存点名称前缀。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConnectionHolder for the given ConnectionHandle.\n\t * @param connectionHandle the ConnectionHandle to hold\n\t */",
            "\t/**\n\t * 为给定 ConnectionHandle 创建新的 ConnectionHolder。\n\t * @param connectionHandle 要持有的 ConnectionHandle\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConnectionHolder for the given JDBC Connection,\n\t * wrapping it with a {@link SimpleConnectionHandle},\n\t * assuming that there is no ongoing transaction.\n\t * @param connection the JDBC Connection to hold\n\t * @see SimpleConnectionHandle\n\t * @see #ConnectionHolder(java.sql.Connection, boolean)\n\t */",
            "\t/**\n\t * 为给定 JDBC Connection 创建新的 ConnectionHolder，\n\t * 使用 {@link SimpleConnectionHandle} 包装，假定当前无进行中的事务。\n\t * @param connection 要持有的 JDBC Connection\n\t * @see SimpleConnectionHandle\n\t * @see #ConnectionHolder(java.sql.Connection, boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConnectionHolder for the given JDBC Connection,\n\t * wrapping it with a {@link SimpleConnectionHandle}.\n\t * @param connection the JDBC Connection to hold\n\t * @param transactionActive whether the given Connection is involved\n\t * in an ongoing transaction\n\t * @see SimpleConnectionHandle\n\t */",
            "\t/**\n\t * 为给定 JDBC Connection 创建新的 ConnectionHolder，\n\t * 使用 {@link SimpleConnectionHandle} 包装。\n\t * @param connection 要持有的 JDBC Connection\n\t * @param transactionActive 给定 Connection 是否参与进行中的事务\n\t * @see SimpleConnectionHandle\n\t */",
        ),
        (
            "\t/**\n\t * Return the ConnectionHandle held by this ConnectionHolder.\n\t */",
            "\t/**\n\t * 返回本 ConnectionHolder 持有的 ConnectionHandle。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder currently has a Connection.\n\t */",
            "\t/**\n\t * 返回本持有者当前是否持有 Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether this holder represents an active, JDBC-managed transaction.\n\t * @see DataSourceTransactionManager\n\t */",
            "\t/**\n\t * 设置本持有者是否表示活跃的、由 JDBC 管理的事务。\n\t * @see DataSourceTransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder represents an active, JDBC-managed transaction.\n\t */",
            "\t/**\n\t * 返回本持有者是否表示活跃的、由 JDBC 管理的事务。\n\t */",
        ),
        (
            "\t/**\n\t * Override the existing Connection handle with the given Connection.\n\t * Reset the handle if given {@code null}.\n\t * <p>Used for releasing the Connection on suspend (with a {@code null}\n\t * argument) and setting a fresh Connection on resume.\n\t */",
            "\t/**\n\t * 用给定 Connection 覆盖现有 Connection 句柄。\n\t * 若传入 {@code null} 则重置句柄。\n\t * <p>用于挂起时释放 Connection（传入 {@code null}）\n\t * 以及恢复时设置新 Connection。\n\t */",
        ),
        (
            "\t/**\n\t * Return the current Connection held by this ConnectionHolder.\n\t * <p>This will be the same Connection until {@code released}\n\t * gets called on the ConnectionHolder, which will reset the\n\t * held Connection, fetching a new Connection on demand.\n\t * @see ConnectionHandle#getConnection()\n\t * @see #released()\n\t */",
            "\t/**\n\t * 返回本 ConnectionHolder 当前持有的 Connection。\n\t * <p>在调用 {@code released} 重置之前，将始终是同一 Connection；\n\t * 重置后按需获取新 Connection。\n\t * @see ConnectionHandle#getConnection()\n\t * @see #released()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether JDBC Savepoints are supported.\n\t * Caches the flag for the lifetime of this ConnectionHolder.\n\t * @throws SQLException if thrown by the JDBC driver\n\t */",
            "\t/**\n\t * 返回是否支持 JDBC 保存点。\n\t * 在本 ConnectionHolder 生命周期内缓存该标志。\n\t * @throws SQLException JDBC 驱动抛出时\n\t */",
        ),
        (
            "\t/**\n\t * Create a new JDBC Savepoint for the current Connection,\n\t * using generated savepoint names that are unique for the Connection.\n\t * @return the new Savepoint\n\t * @throws SQLException if thrown by the JDBC driver\n\t */",
            "\t/**\n\t * 为当前 Connection 创建新的 JDBC 保存点，\n\t * 使用对该 Connection 唯一的生成名称。\n\t * @return 新的 Savepoint\n\t * @throws SQLException JDBC 驱动抛出时\n\t */",
        ),
        (
            "\t/**\n\t * Releases the current Connection held by this ConnectionHolder.\n\t * <p>This is necessary for ConnectionHandles that expect \"Connection borrowing\",\n\t * where each returned Connection is only temporarily leased and needs to be\n\t * returned once the data operation is done, to make the Connection available\n\t * for other operations within the same transaction.\n\t */",
            "\t/**\n\t * 释放本 ConnectionHolder 当前持有的 Connection。\n\t * <p>对于期望“连接借用”的 ConnectionHandle 而言这是必要的：\n\t * 每次返回的 Connection 仅临时租借，数据操作完成后须归还，\n\t * 以便同一事务内其他操作继续使用。\n\t */",
        ),
    ],
    "ConnectionProxy.java": [
        (
            "/**\n * Subinterface of {@link java.sql.Connection} to be implemented by\n * Connection proxies. Allows access to the underlying target Connection.\n *\n * <p>This interface can be checked when there is a need to cast to a\n * native JDBC Connection such as Oracle's OracleConnection. Alternatively,\n * all such connections also support JDBC 4.0's {@link Connection#unwrap}.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see TransactionAwareDataSourceProxy\n * @see LazyConnectionDataSourceProxy\n * @see DataSourceUtils#getTargetConnection(java.sql.Connection)\n */",
            "/**\n * Connection 代理应实现的 {@link java.sql.Connection} 子接口。\n * 允许访问底层目标 Connection。\n *\n * <p>需要转换为原生 JDBC Connection（如 Oracle 的 OracleConnection）时可检查本接口。\n * 或者，此类连接也支持 JDBC 4.0 的 {@link Connection#unwrap}。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see TransactionAwareDataSourceProxy\n * @see LazyConnectionDataSourceProxy\n * @see DataSourceUtils#getTargetConnection(java.sql.Connection)\n */",
        ),
        (
            "\t/**\n\t * Return the target Connection of this proxy.\n\t * <p>This will typically be the native driver Connection\n\t * or a wrapper from a connection pool.\n\t * @return the underlying Connection (never {@code null})\n\t */",
            "\t/**\n\t * 返回本代理的目标 Connection。\n\t * <p>通常是原生驱动 Connection 或连接池包装器。\n\t * @return 底层 Connection（永不为 {@code null}）\n\t */",
        ),
    ],
    "DelegatingDataSource.java": [
        (
            "/**\n * JDBC {@link javax.sql.DataSource} implementation that delegates all calls\n * to a given target {@link javax.sql.DataSource}.\n *\n * <p>This class is meant to be subclassed, with subclasses overriding only\n * those methods (such as {@link #getConnection()}) that should not simply\n * delegate to the target DataSource.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see #getConnection\n */",
            "/**\n * 将所有调用委托给给定目标 {@link javax.sql.DataSource} 的 JDBC {@link javax.sql.DataSource} 实现。\n *\n * <p>本类供子类化：子类仅覆盖不应简单委托的方法（如 {@link #getConnection()}）。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see #getConnection\n */",
        ),
        (
            "\t/**\n\t * Create a new DelegatingDataSource.\n\t * @see #setTargetDataSource\n\t */",
            "\t/**\n\t * 创建新的 DelegatingDataSource。\n\t * @see #setTargetDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DelegatingDataSource.\n\t * @param targetDataSource the target DataSource\n\t */",
            "\t/**\n\t * 创建新的 DelegatingDataSource。\n\t * @param targetDataSource 目标 DataSource\n\t */",
        ),
        (
            "\t/**\n\t * Set the target DataSource that this DataSource should delegate to.\n\t */",
            "\t/**\n\t * 设置本 DataSource 应委托的目标 DataSource。\n\t */",
        ),
        (
            "\t/**\n\t * Return the target DataSource that this DataSource should delegate to.\n\t */",
            "\t/**\n\t * 返回本 DataSource 应委托的目标 DataSource。\n\t */",
        ),
        (
            "\t/**\n\t * Obtain the target {@code DataSource} for actual use (never {@code null}).\n\t * @since 5.0\n\t */",
            "\t/**\n\t * 获取实际使用的目标 {@code DataSource}（永不为 {@code null}）。\n\t * @since 5.0\n\t */",
        ),
    ],
    "DriverManagerDataSource.java": [
        (
            "/**\n * Simple implementation of the standard JDBC {@link javax.sql.DataSource} interface,\n * configuring the plain old JDBC {@link java.sql.DriverManager} via bean properties, and\n * returning a new {@link java.sql.Connection} from every {@code getConnection} call.\n *\n * <p><b>NOTE: This class is not an actual connection pool; it does not actually\n * pool Connections.</b> It just serves as simple replacement for a full-blown\n * connection pool, implementing the same standard interface, but creating new\n * Connections on every call.\n *\n * <p>Useful for test or standalone environments outside a Jakarta EE container, either\n * as a DataSource bean in a corresponding ApplicationContext or in conjunction with\n * a simple JNDI environment. Pool-assuming {@code Connection.close()} calls will\n * simply close the Connection, so any DataSource-aware persistence code should work.\n *\n * <p><b>NOTE: Within special class loading environments such as OSGi, this class\n * is effectively superseded by {@link SimpleDriverDataSource} due to general class\n * loading issues with the JDBC DriverManager that be resolved through direct Driver\n * usage (which is exactly what SimpleDriverDataSource does).</b>\n *\n * <p>In a Jakarta EE container, it is recommended to use a JNDI DataSource provided by\n * the container. Such a DataSource can be exposed as a DataSource bean in a Spring\n * ApplicationContext via {@link org.springframework.jndi.JndiObjectFactoryBean},\n * for seamless switching to and from a local DataSource bean like this class.\n * For tests, you can then either set up a mock JNDI environment through complete\n * solutions from third parties such as <a href=\"https://github.com/h-thurow/Simple-JNDI\">Simple-JNDI</a>,\n * or switch the bean definition to a local DataSource (which is simpler and thus recommended).\n *\n * <p>This {@code DriverManagerDataSource} class was originally designed alongside\n * <a href=\"https://commons.apache.org/proper/commons-dbcp\">Apache Commons DBCP</a>\n * and <a href=\"https://sourceforge.net/projects/c3p0\">C3P0</a>, featuring bean-style\n * {@code BasicDataSource}/{@code ComboPooledDataSource} classes with configuration\n * properties for local resource setups. For a modern JDBC connection pool, consider\n * <a href=\"https://github.com/brettwooldridge/HikariCP\">HikariCP</a> instead,\n * exposing a corresponding {@code HikariDataSource} instance to the application.\n *\n * @author Juergen Hoeller\n * @since 14.03.2003\n * @see SimpleDriverDataSource\n */",
            "/**\n * 标准 JDBC {@link javax.sql.DataSource} 接口的简单实现，\n * 通过 bean 属性配置传统 {@link java.sql.DriverManager}，\n * 每次 {@code getConnection} 调用返回新的 {@link java.sql.Connection}。\n *\n * <p><b>注意：本类不是真正的连接池，不会复用 Connection。</b>\n * 它只是完整连接池的简单替代，实现相同标准接口，但每次调用都创建新 Connection。\n *\n * <p>适用于 Jakarta EE 容器外的测试或独立环境，\n * 可作为 ApplicationContext 中的 DataSource bean，或配合简单 JNDI 环境使用。\n * 假定连接池的 {@code Connection.close()} 调用会直接关闭连接，\n * 因此任何 DataSource 感知的持久化代码均可正常工作。\n *\n * <p><b>注意：在 OSGi 等特殊类加载环境中，由于 DriverManager 的类加载问题，\n * 本类实质上已被 {@link SimpleDriverDataSource} 取代；\n * 后者通过直接使用 Driver 解决该问题。</b>\n *\n * <p>在 Jakarta EE 容器中，建议使用容器提供的 JNDI DataSource。\n * 可通过 {@link org.springframework.jndi.JndiObjectFactoryBean} 将其暴露为 Spring\n * ApplicationContext 中的 DataSource bean，与本类本地 bean 无缝切换。\n * 测试时可使用第三方完整方案（如 <a href=\"https://github.com/h-thurow/Simple-JNDI\">Simple-JNDI</a>）\n * 搭建模拟 JNDI 环境，或改用本地 DataSource bean（更简单，推荐）。\n *\n * <p>本 {@code DriverManagerDataSource} 最初与\n * <a href=\"https://commons.apache.org/proper/commons-dbcp\">Apache Commons DBCP</a>\n * 和 <a href=\"https://sourceforge.net/projects/c3p0\">C3P0</a> 同期设计，\n * 提供 bean 风格的 {@code BasicDataSource}/{@code ComboPooledDataSource} 配置属性。\n * 现代 JDBC 连接池可考虑 <a href=\"https://github.com/brettwooldridge/HikariCP\">HikariCP</a>，\n * 向应用暴露对应的 {@code HikariDataSource} 实例。\n *\n * @author Juergen Hoeller\n * @since 14.03.2003\n * @see SimpleDriverDataSource\n */",
        ),
        (
            "\t/**\n\t * Constructor for bean-style configuration.\n\t */",
            "\t/**\n\t * 用于 bean 风格配置的构造函数。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given JDBC URL,\n\t * not specifying a username or password for JDBC access.\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @see java.sql.DriverManager#getConnection(String)\n\t */",
            "\t/**\n\t * 使用给定 JDBC URL 创建新的 DriverManagerDataSource，\n\t * 不指定 JDBC 访问的用户名或密码。\n\t * @param url 访问 DriverManager 使用的 JDBC URL\n\t * @see java.sql.DriverManager#getConnection(String)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given standard\n\t * DriverManager parameters.\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param username the JDBC username to use for accessing the DriverManager\n\t * @param password the JDBC password to use for accessing the DriverManager\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
            "\t/**\n\t * 使用给定标准 DriverManager 参数创建新的 DriverManagerDataSource。\n\t * @param url 访问 DriverManager 使用的 JDBC URL\n\t * @param username 访问 DriverManager 使用的 JDBC 用户名\n\t * @param password 访问 DriverManager 使用的 JDBC 密码\n\t * @see java.sql.DriverManager#getConnection(String, String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DriverManagerDataSource with the given JDBC URL,\n\t * not specifying a username or password for JDBC access.\n\t * @param url the JDBC URL to use for accessing the DriverManager\n\t * @param conProps the JDBC connection properties\n\t * @see java.sql.DriverManager#getConnection(String)\n\t */",
            "\t/**\n\t * 使用给定 JDBC URL 创建新的 DriverManagerDataSource，\n\t * 不指定 JDBC 访问的用户名或密码。\n\t * @param url 访问 DriverManager 使用的 JDBC URL\n\t * @param conProps JDBC 连接属性\n\t * @see java.sql.DriverManager#getConnection(String)\n\t */",
        ),
        (
            "\t/**\n\t * Set the JDBC driver class name. This driver will get initialized\n\t * on startup, registering itself with the JDK's DriverManager.\n\t * <p><b>NOTE: DriverManagerDataSource is primarily intended for accessing\n\t * <i>pre-registered</i> JDBC drivers.</b> If you need to register a new driver,\n\t * consider using {@link SimpleDriverDataSource} instead. Alternatively, consider\n\t * initializing the JDBC driver yourself before instantiating this DataSource.\n\t * The \"driverClassName\" property is mainly preserved for backwards compatibility,\n\t * as well as for migrating between Commons DBCP and this DataSource.\n\t * @see java.sql.DriverManager#registerDriver(java.sql.Driver)\n\t * @see SimpleDriverDataSource\n\t */",
            "\t/**\n\t * 设置 JDBC 驱动类名。启动时将初始化该驱动并向 JDK DriverManager 注册。\n\t * <p><b>注意：DriverManagerDataSource 主要用于访问<i>已注册</i>的 JDBC 驱动。</b>\n\t * 若需注册新驱动，建议使用 {@link SimpleDriverDataSource}。\n\t * 或者在本 DataSource 实例化前自行初始化 JDBC 驱动。\n\t * \"driverClassName\" 属性主要为向后兼容及 Commons DBCP 与本 DataSource 迁移保留。\n\t * @see java.sql.DriverManager#registerDriver(java.sql.Driver)\n\t * @see SimpleDriverDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Getting a Connection using the nasty static from DriverManager is extracted\n\t * into a protected method to allow for easy unit testing.\n\t * @see java.sql.DriverManager#getConnection(String, java.util.Properties)\n\t */",
            "\t/**\n\t * 将通过 DriverManager 静态方法获取 Connection 的逻辑\n\t * 提取为 protected 方法，便于单元测试。\n\t * @see java.sql.DriverManager#getConnection(String, java.util.Properties)\n\t */",
        ),
    ],
}
