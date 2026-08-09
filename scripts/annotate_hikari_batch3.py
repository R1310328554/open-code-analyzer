#!/usr/bin/env python3
"""Chinese-annotate HikariCP batch-3 files (24 files)."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
SRC = Path("/tmp/hikari_cp_src")
VER = ROOT / "hikaricp/dev-a4d93f4f8551"
ANALYZED = VER / "analyzed"
BATCH = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"]

# Global inline-comment translations (order matters)
INLINE_COMMENTS: list[tuple[str, str]] = [
    ("// Properties changeable at runtime through the HikariConfigMXBean", "// 可通过 HikariConfigMXBean 在运行时修改的属性"),
    ("// Properties NOT changeable at runtime", "// 运行时不可修改的属性"),
    ("// ***********************************************************************", "// ***********************************************************************"),
    ("//                       HikariConfigMXBean methods", "//                       HikariConfigMXBean 方法"),
    ("//                     All other configuration methods", "//                     其余配置方法"),
    ("//                          Private methods", "//                          私有方法"),
    ("// ***********************************************************************\n   //                           JDBC methods", "// ***********************************************************************\n   //                           JDBC 方法"),
    ("//                        IBagStateListener callback", "//                        IBagStateListener 回调"),
    ("//                        HikariPoolMBean methods", "//                        HikariPoolMXBean 方法"),
    ("//                           Package methods", "//                           包级方法"),
    ("//                           Private methods", "//                           私有方法"),
    ("//                      Non-anonymous Inner-classes", "//                      具名内部类"),
    ("//                      IConcurrentBagEntry methods", "//                      IConcurrentBagEntry 方法"),
    ("// treat empty property as null", "// 空字符串属性视为 null"),
    ("// Check Data Source Options", "// 校验数据源配置选项"),
    ("// keepalive time must larger than 30 seconds", "// keepalive 间隔必须大于 30 秒"),
    ("// keepalive time must be less than maxLifetime (if maxLifetime is enabled)", "// keepalive 必须小于 maxLifetime（若已启用 maxLifetime）"),
    ("// Pool number is global to the VM to avoid overlapping pool numbers in classloader scoped environments", "// 池编号在 JVM 内全局递增，避免类加载器隔离环境下编号冲突"),
    ("// The SecurityManager didn't allow us to read/write system properties", "// SecurityManager 禁止读写系统属性"),
    ("// so just generate a random pool number instead", "// 改为生成随机池名"),
    ("// continue", "// 忽略并继续"),
    ("// continue with the close even if setNetworkTimeout() throws", "// 即使 setNetworkTimeout() 抛异常也继续关闭"),
    ("// ignore", "// 忽略"),
    ("// variance% = 100 / factor", "// 方差百分比 = 100 / factor"),
    ("// default variance upto 25% of the maxLifetime (random)", "// 默认在 maxLifetime 内随机方差（最多约 25%）"),
    ("// variance up to 20% of the heartbeat time", "// 心跳间隔最多 20% 随机方差"),
    ("// we check POOL_NORMAL to avoid a flood of messages if shutdown() is running concurrently", "// 检查 POOL_NORMAL，避免与 shutdown() 并发时刷屏日志"),
    ("// refresh values in case they changed via MBean", "// 刷新可能经 MBean 修改的运行时配置"),
    ("// Detect retrograde time, allowing +128ms as per NTP spec.", "// 检测时钟回拨（按 NTP 规范允许 +128ms 容差）"),
    ("// No point evicting for forward clock motion, this merely accelerates connection retirement anyway", "// 时钟前跳无需驱逐，只会加速连接退役"),
    ("// Try to maintain minimum connections", "// 尝试维持最小空闲连接数"),
    ("// Pool is suspended, shutdown, or at max size", "// 池已挂起、关闭或已达最大容量"),
    ("// failed to get connection from db, sleep and retry", "// 从数据库获取连接失败，退避后重试"),
    ("// static initializer", "// 静态初始化块"),
    ("// unreachable in HikariCP, but we're still forced to catch it", "// HikariCP 中不会到达，但接口仍要求捕获"),
    ("// pool never started", "// 池从未启动"),
    ("// NOTE: This exception text is referenced by a Spring Boot FailureAnalyzer, it should not be", "// 注意：此异常文本被 Spring Boot FailureAnalyzer 引用，不应"),
    ("// changed without first notifying the Spring Boot developers.", "// 在未通知 Spring Boot 开发者的情况下修改。"),
    ("// We timed out... break and throw exception", "// 等待超时，跳出循环并抛出异常"),
    ("// ok", "// 配置有效"),
]

# Per-file: (old, new) javadoc / text replacements — longest first within each file
FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "HikariConfig.java": [
        (
            "@SuppressWarnings({\"SameParameterValue\", \"unused\"})\npublic class HikariConfig",
            "/**\n * HikariCP 连接池配置类，实现 {@link HikariConfigMXBean}。\n * <p>\n * 可通过属性文件、{@link Properties} 或 setter 配置；\n * 连接池启动后配置被“封存”，运行时仅可通过 MXBean 修改部分属性。\n *\n * @author Brett Wooldridge\n */\n@SuppressWarnings({\"SameParameterValue\", \"unused\"})\npublic class HikariConfig",
        ),
        (
            "   /**\n    * Default constructor\n    * <p>\n    * If the System property {@code hikari.configurationFile} is set,\n    * then the default constructor will attempt to load the specified configuration file\n    * <p>\n    * {@link #HikariConfig(String propertyFileName)} can be similarly used\n    * instead of using the system property\n    */",
            "   /**\n    * 默认构造器。\n    * <p>\n    * 若系统属性 {@code hikari.configurationFile} 已设置，\n    * 将尝试加载指定配置文件。\n    * <p>\n    * 也可使用 {@link #HikariConfig(String propertyFileName)} 代替系统属性。\n    */",
        ),
        (
            "   /**\n    * Construct a HikariConfig from the specified properties object.\n    *\n    * @param properties the name of the property file\n    */",
            "   /**\n    * 从指定 {@link Properties} 对象构造配置。\n    *\n    * @param properties 属性集合\n    */",
        ),
        (
            "   /**\n    * Construct a HikariConfig from the specified property file name.  <code>propertyFileName</code>\n    * will first be treated as a path in the file-system, and if that fails the\n    * Class.getResourceAsStream(propertyFileName) will be tried.\n    *\n    * @param propertyFileName the name of the property file\n    */",
            "   /**\n    * 从指定属性文件名构造配置。{@code propertyFileName} 先按文件系统路径解析，\n    * 失败则尝试 {@code Class.getResourceAsStream(propertyFileName)}。\n    *\n    * @param propertyFileName 属性文件名或路径\n    */",
        ),
        (
            "   /**\n    * Get the default password to use for DataSource.getConnection(username, password) calls.\n    * @return the password\n    */",
            "   /**\n    * 获取 {@code DataSource.getConnection(username, password)} 的默认密码。\n    *\n    * @return 密码\n    */",
        ),
        (
            "   /**\n    * Set the default password to use for DataSource.getConnection(username, password) calls.\n    * @param password the password\n    */",
            "   /**\n    * 设置 {@code DataSource.getConnection(username, password)} 的默认密码。\n    *\n    * @param password 密码\n    */",
        ),
        (
            "   /**\n    * Get the default username used for DataSource.getConnection(username, password) calls.\n    *\n    * @return the username\n    */",
            "   /**\n    * 获取 {@code DataSource.getConnection(username, password)} 的默认用户名。\n    *\n    * @return 用户名\n    */",
        ),
        (
            "   /**\n    * Set the default username used for DataSource.getConnection(username, password) calls.\n    *\n    * @param username the username\n    */",
            "   /**\n    * 设置 {@code DataSource.getConnection(username, password)} 的默认用户名。\n    *\n    * @param username 用户名\n    */",
        ),
        (
            "   /**\n    * Atomically set the default username and password to use for DataSource.getConnection(username, password) calls.\n    *\n    * @param credentials the username and password pair\n    */",
            "   /**\n    * 原子地设置 {@code DataSource.getConnection(username, password)} 的默认用户名与密码。\n    *\n    * @param credentials 用户名与密码对\n    */",
        ),
        (
            "   /**\n    * Atomically get the default username and password to use for DataSource.getConnection(username, password) calls.\n    *\n    * @return the username and password pair\n    */",
            "   /**\n    * 原子地获取 {@code DataSource.getConnection(username, password)} 的默认用户名与密码。\n    *\n    * @return 用户名与密码对\n    */",
        ),
        (
            "   /**\n    * Get the SQL query to be executed to test the validity of connections.\n    *\n    * @return the SQL query string, or null\n    */",
            "   /**\n    * 获取用于测试连接有效性的 SQL 查询。\n    *\n    * @return SQL 查询字符串，或 {@code null}\n    */",
        ),
        (
            "   /**\n    * Set the SQL query to be executed to test the validity of connections. Using\n    * the JDBC4 <code>Connection.isValid()</code> method to test connection validity can\n    * be more efficient on some databases and is recommended.\n    *\n    * @param connectionTestQuery a SQL query string\n    */",
            "   /**\n    * 设置用于测试连接有效性的 SQL 查询。\n    * 部分数据库上使用 JDBC4 {@code Connection.isValid()} 更高效，推荐使用。\n    *\n    * @param connectionTestQuery SQL 查询字符串\n    */",
        ),
        (
            "   /**\n    * Get the SQL string that will be executed on all new connections when they are\n    * created, before they are added to the pool.\n    *\n    * @return the SQL to execute on new connections, or null\n    */",
            "   /**\n    * 获取新建连接加入池之前执行的初始化 SQL。\n    *\n    * @return 新连接初始化 SQL，或 {@code null}\n    */",
        ),
        (
            "   /**\n    * Set the SQL string that will be executed on all new connections when they are\n    * created, before they are added to the pool.  If this query fails, it will be\n    * treated as a failed connection attempt.\n    *\n    * @param connectionInitSql the SQL to execute on new connections\n    */",
            "   /**\n    * 设置新建连接加入池之前执行的初始化 SQL；若执行失败视为连接创建失败。\n    *\n    * @param connectionInitSql 新连接初始化 SQL\n    */",
        ),
        (
            "   /**\n    * Get the {@link DataSource} that has been explicitly specified to be wrapped by the\n    * pool.\n    *\n    * @return the {@link DataSource} instance, or null\n    */",
            "   /**\n    * 获取显式指定由池包装的 {@link DataSource}。\n    *\n    * @return {@link DataSource} 实例，或 {@code null}\n    */",
        ),
        (
            "   /**\n    * Set a {@link DataSource} for the pool to explicitly wrap.  This setter is not\n    * available through property file based initialization.\n    *\n    * @param dataSource a specific {@link DataSource} to be wrapped by the pool\n    */",
            "   /**\n    * 设置池显式包装的 {@link DataSource}；此 setter 不可通过属性文件初始化。\n    *\n    * @param dataSource 要由池包装的 {@link DataSource}\n    */",
        ),
        (
            "   /**\n    * Get the name of the JDBC {@link DataSource} class used to create Connections.\n    *\n    * @return the fully qualified name of the JDBC {@link DataSource} class\n    */",
            "   /**\n    * 获取用于创建连接的 JDBC {@link DataSource} 类全名。\n    *\n    * @return JDBC {@link DataSource} 类全限定名\n    */",
        ),
        (
            "   /**\n    * Set the fully qualified class name of the JDBC {@link DataSource} that will be used create Connections.\n    *\n    * @param className the fully qualified name of the JDBC {@link DataSource} class\n    */",
            "   /**\n    * 设置用于创建连接的 JDBC {@link DataSource} 类全限定名。\n    *\n    * @param className JDBC {@link DataSource} 类全限定名\n    */",
        ),
        (
            "   /**\n    * Add a property (name/value pair) that will be used to configure the {@link DataSource}/{@link java.sql.Driver}.\n    * <p>\n    * In the case of a {@link DataSource}, the property names will be translated to Java setters following the Java Bean\n    * naming convention.  For example, the property {@code cachePrepStmts} will translate into {@code setCachePrepStmts()}\n    * with the {@code value} passed as a parameter.\n    * <p>\n    * In the case of a {@link java.sql.Driver}, the property will be added to a {@link Properties} instance that will\n    * be passed to the driver during {@link java.sql.Driver#connect(String, Properties)} calls.\n    *\n    * @param propertyName the name of the property\n    * @param value the value to be used by the DataSource/Driver\n    */",
            "   /**\n    * 添加用于配置 {@link DataSource}/{@link java.sql.Driver} 的属性（名/值对）。\n    * <p>\n    * 对 {@link DataSource}，属性名按 Java Bean 约定映射为 setter，\n    * 例如 {@code cachePrepStmts} 对应 {@code setCachePrepStmts(value)}。\n    * <p>\n    * 对 {@link java.sql.Driver}，属性加入 {@link Properties}，\n    * 在 {@link java.sql.Driver#connect(String, Properties)} 时传给驱动。\n    *\n    * @param propertyName 属性名\n    * @param value 传给 DataSource/Driver 的值\n    */",
        ),
        (
            "   /**\n    * Get the default auto-commit behavior of connections in the pool.\n    *\n    * @return the default auto-commit behavior of connections\n    */",
            "   /**\n    * 获取池中连接的默认自动提交行为。\n    *\n    * @return 默认自动提交设置\n    */",
        ),
        (
            "   /**\n    * Set the default auto-commit behavior of connections in the pool.\n    *\n    * @param isAutoCommit the desired auto-commit default for connections\n    */",
            "   /**\n    * 设置池中连接的默认自动提交行为。\n    *\n    * @param isAutoCommit 期望的自动提交默认值\n    */",
        ),
        (
            "   /**\n    * Get the pool suspension behavior (allowed or disallowed).\n    *\n    * @return the pool suspension behavior\n    */",
            "   /**\n    * 获取是否允许挂起连接池。\n    *\n    * @return 是否允许挂起\n    */",
        ),
        (
            "   /**\n    * Set whether or not pool suspension is allowed.  There is a performance\n    * impact when pool suspension is enabled.  Unless you need it (for a\n    * redundancy system for example) do not enable it.\n    *\n    * @param isAllowPoolSuspension the desired pool suspension allowance\n    */",
            "   /**\n    * 设置是否允许挂起连接池。启用挂起有性能开销；\n    * 除非需要（例如冗余系统），否则不要启用。\n    *\n    * @param isAllowPoolSuspension 是否允许挂起\n    */",
        ),
        (
            "   /**\n    * Get the pool initialization failure timeout.  See {@code #setInitializationFailTimeout(long)}\n    * for details.\n    *\n    * @return the number of milliseconds before the pool initialization fails\n    * @see HikariConfig#setInitializationFailTimeout(long)\n    */",
            "   /**\n    * 获取池初始化失败超时。详见 {@code #setInitializationFailTimeout(long)}。\n    *\n    * @return 初始化失败前的等待毫秒数\n    * @see HikariConfig#setInitializationFailTimeout(long)\n    */",
        ),
        (
            "   /**\n    * Set the pool initialization failure timeout.  This setting applies to pool\n    * initialization when {@link HikariDataSource} is constructed with a {@link HikariConfig},\n    * or when {@link HikariDataSource} is constructed using the no-arg constructor\n    * and {@link HikariDataSource#getConnection()} is called.\n    * <ul>\n    *   <li>Any value greater than zero will be treated as a timeout for pool initialization.\n    *       The calling thread will be blocked from continuing until a successful connection\n    *       to the database, or until the timeout is reached.  If the timeout is reached, then\n    *       a {@code PoolInitializationException} will be thrown. </li>\n    *   <li>A value of zero will <i>not</i>  prevent the pool from starting in the\n    *       case that a connection cannot be obtained. However, upon start the pool will\n    *       attempt to obtain a connection and validate that the {@code connectionTestQuery}\n    *       and {@code connectionInitSql} are valid.  If those validations fail, an exception\n    *       will be thrown.  If a connection cannot be obtained, the validation is skipped\n    *       and the the pool will start and continue to try to obtain connections in the\n    *       background.  This can mean that callers to {@code DataSource#getConnection()} may\n    *       encounter exceptions. </li>\n    *   <li>A value less than zero will bypass any connection attempt and validation during\n    *       startup, and therefore the pool will start immediately.  The pool will continue to\n    *       try to obtain connections in the background. This can mean that callers to\n    *       {@code DataSource#getConnection()} may encounter exceptions. </li>\n    * </ul>\n    * Note that if this timeout value is greater than or equal to zero (0), and therefore an\n    * initial connection validation is performed, this timeout does not override the\n    * {@code connectionTimeout} or {@code validationTimeout}; they will be honored before this\n    * timeout is applied.  The default value is one millisecond.\n    *\n    * @param initializationFailTimeout the number of milliseconds before the\n    *        pool initialization fails, or 0 to validate connection setup but continue with\n    *        pool start, or less than zero to skip all initialization checks and start the\n    *        pool without delay.\n    */",
            "   /**\n    * 设置池初始化失败超时。适用于以 {@link HikariConfig} 构造 {@link HikariDataSource}，\n    * 或无参构造后首次调用 {@link HikariDataSource#getConnection()} 的场景。\n    * <ul>\n    *   <li>大于 0：作为初始化超时；调用线程阻塞直至成功连库或超时，\n    *       超时则抛出 {@code PoolInitializationException}。</li>\n    *   <li>等于 0：无法获取连接时不阻止启动；启动时尝试获取连接并校验\n    *       {@code connectionTestQuery} 与 {@code connectionInitSql}，校验失败则抛异常；\n    *       无法获取连接则跳过校验，池在后台继续尝试，\n    *       {@code DataSource#getConnection()} 调用方可能遇到异常。</li>\n    *   <li>小于 0：启动时不尝试连接与校验，池立即启动并在后台获取连接，\n    *       调用方可能遇到异常。</li>\n    * </ul>\n    * 当该值 ≥ 0 且执行初始连接校验时，不覆盖 {@code connectionTimeout} 或 {@code validationTimeout}；\n    * 默认值为 1 毫秒。\n    *\n    * @param initializationFailTimeout 初始化失败超时毫秒数；0 表示校验后继续启动；\n    *        小于 0 表示跳过所有初始化检查并立即启动\n    */",
        ),
        (
            "   /**\n    * Determine whether internal pool queries, principally aliveness checks, will be isolated in their own transaction\n    * via {@link Connection#rollback()}.  Defaults to {@code false}.\n    *\n    * @return {@code true} if internal pool queries are isolated, {@code false} if not\n    */",
            "   /**\n    * 判断内部池查询（主要是存活检测）是否通过 {@link Connection#rollback()} 隔离在独立事务中。\n    * 默认 {@code false}。\n    *\n    * @return 是否隔离内部查询\n    */",
        ),
        (
            "   /**\n    * Configure whether internal pool queries, principally aliveness checks, will be isolated in their own transaction\n    * via {@link Connection#rollback()}.  Defaults to {@code false}.\n    *\n    * @param isolate {@code true} if internal pool queries should be isolated, {@code false} if not\n    */",
            "   /**\n    * 配置内部池查询是否通过 {@link Connection#rollback()} 隔离在独立事务中。\n    * 默认 {@code false}。\n    *\n    * @param isolate 是否隔离内部查询\n    */",
        ),
        (
            "   /**\n    * Get the MetricRegistry instance to use for registration of metrics used by HikariCP.  Default is {@code null}.\n    *\n    * @return the MetricRegistry instance that will be used\n    */",
            "   /**\n    * 获取 HikariCP 注册指标所用的 MetricRegistry 实例。默认 {@code null}。\n    *\n    * @return 将使用的 MetricRegistry 实例\n    */",
        ),
        (
            "   /**\n    * Set a MetricRegistry instance to use for registration of metrics used by HikariCP.\n    *\n    * @param metricRegistry the MetricRegistry instance to use\n    */",
            "   /**\n    * 设置 HikariCP 注册指标所用的 MetricRegistry 实例。\n    *\n    * @param metricRegistry MetricRegistry 实例\n    */",
        ),
        (
            "   /**\n    * Get the HealthCheckRegistry that will be used for registration of health checks by HikariCP.  Currently only\n    * Codahale/DropWizard is supported for health checks.\n    *\n    * @return the HealthCheckRegistry instance that will be used\n    */",
            "   /**\n    * 获取 HikariCP 注册健康检查所用的 HealthCheckRegistry。\n    * 目前仅支持 Codahale/DropWizard。\n    *\n    * @return 将使用的 HealthCheckRegistry 实例\n    */",
        ),
        (
            "   /**\n    * Set the HealthCheckRegistry that will be used for registration of health checks by HikariCP.  Currently only\n    * Codahale/DropWizard is supported for health checks.  Default is {@code null}.\n    *\n    * @param healthCheckRegistry the HealthCheckRegistry to be used\n    */",
            "   /**\n    * 设置 HikariCP 注册健康检查所用的 HealthCheckRegistry。\n    * 目前仅支持 Codahale/DropWizard。默认 {@code null}。\n    *\n    * @param healthCheckRegistry 要使用的 HealthCheckRegistry\n    */",
        ),
        (
            "   /**\n    * This property controls the keepalive interval for a connection in the pool. An in-use connection will never be\n    * tested by the keepalive thread, only when it is idle will it be tested.\n    *\n    * @return the interval in which connections will be tested for aliveness, thus keeping them alive by the act of checking. Value is in milliseconds, default is 0 (disabled).\n    */",
            "   /**\n    * 控制池中连接的 keepalive 检测间隔。使用中的连接不会被 keepalive 线程检测，仅空闲时检测。\n    *\n    * @return 存活检测间隔（毫秒），默认 0（禁用）\n    */",
        ),
        (
            "   /**\n    * This property controls the keepalive interval for a connection in the pool. An in-use connection will never be\n    * tested by the keepalive thread, only when it is idle will it be tested.\n    *\n    * @param keepaliveTimeMs the interval in which connections will be tested for aliveness, thus keeping them alive by the act of checking. Value is in milliseconds, default is 0 (disabled).\n    */",
            "   /**\n    * 控制池中连接的 keepalive 检测间隔。使用中的连接不会被 keepalive 线程检测，仅空闲时检测。\n    *\n    * @param keepaliveTimeMs 存活检测间隔（毫秒），默认 0（禁用）\n    */",
        ),
        (
            "   /**\n    * Determine whether the Connections in the pool are in read-only mode.\n    *\n    * @return {@code true} if the Connections in the pool are read-only, {@code false} if not\n    */",
            "   /**\n    * 判断池中连接是否为只读模式。\n    *\n    * @return 只读返回 {@code true}\n    */",
        ),
        (
            "   /**\n    * Configures the Connections to be added to the pool as read-only Connections.\n    *\n    * @param readOnly {@code true} if the Connections in the pool are read-only, {@code false} if not\n    */",
            "   /**\n    * 配置加入池的连接是否为只读。\n    *\n    * @param readOnly 是否只读\n    */",
        ),
        (
            "   /**\n    * Determine whether HikariCP will self-register {@link HikariConfigMXBean} and {@link HikariPoolMXBean} instances\n    * in JMX.\n    *\n    * @return {@code true} if HikariCP will register MXBeans, {@code false} if it will not\n    */",
            "   /**\n    * 判断 HikariCP 是否在 JMX 中自注册 {@link HikariConfigMXBean} 与 {@link HikariPoolMXBean}。\n    *\n    * @return 是否注册 MXBean\n    */",
        ),
        (
            "   /**\n    * Configures whether HikariCP self-registers the {@link HikariConfigMXBean} and {@link HikariPoolMXBean} in JMX.\n    *\n    * @param register {@code true} if HikariCP should register MXBeans, {@code false} if it should not\n    */",
            "   /**\n    * 配置 HikariCP 是否在 JMX 中自注册 {@link HikariConfigMXBean} 与 {@link HikariPoolMXBean}。\n    *\n    * @param register 是否注册 MXBean\n    */",
        ),
        (
            "   /**\n    * Set the name of the connection pool.  This is primarily used in logging and JMX management consoles\n    * to identify pools and pool configurations\n    *\n    * @param poolName the name of the connection pool to use\n    */",
            "   /**\n    * 设置连接池名称，主要用于日志与 JMX 管理控制台识别池及配置。\n    *\n    * @param poolName 连接池名称\n    */",
        ),
        (
            "   /**\n    * Get the ScheduledExecutorService used for housekeeping.\n    *\n    * @return the executor\n    */",
            "   /**\n    * 获取用于 housekeeping 的 {@link ScheduledExecutorService}。\n    *\n    * @return 调度执行器\n    */",
        ),
        (
            "   /**\n    * Set the ScheduledExecutorService used for housekeeping.\n    *\n    * @param executor the ScheduledExecutorService\n    */",
            "   /**\n    * 设置用于 housekeeping 的 {@link ScheduledExecutorService}。\n    *\n    * @param executor 调度执行器\n    */",
        ),
        (
            "   /**\n    * Get the default schema name to be set on connections.\n    *\n    * @return the default schema name\n    */",
            "   /**\n    * 获取连接上设置的默认 schema 名称。\n    *\n    * @return 默认 schema 名称\n    */",
        ),
        (
            "   /**\n    * Set the default schema name to be set on connections.\n    *\n    * @param schema the name of the default schema\n    */",
            "   /**\n    * 设置连接上使用的默认 schema 名称。\n    *\n    * @param schema 默认 schema 名称\n    */",
        ),
        (
            "   /**\n    * Get the class name of the {@link HikariCredentialsProvider} that will be used to get credentials at runtime.\n    *\n    * @return the class name of the credentials provider\n    * @see HikariCredentialsProvider\n    */",
            "   /**\n    * 获取运行时获取凭据的 {@link HikariCredentialsProvider} 类名。\n    *\n    * @return 凭据提供者类名\n    * @see HikariCredentialsProvider\n    */",
        ),
        (
            "   /**\n    * Set the class name of the {@link HikariCredentialsProvider} that will be used to get credentials at runtime. Use this method\n    * or provide a {@link HikariCredentialsProvider} instance via the {@link #setCredentialsProvider(HikariCredentialsProvider)} method.\n    *\n    * @param credentialsProviderClassName the class name of the credentials provider\n    * @see HikariCredentialsProvider\n    */",
            "   /**\n    * 设置运行时获取凭据的 {@link HikariCredentialsProvider} 类名。\n    * 也可通过 {@link #setCredentialsProvider(HikariCredentialsProvider)} 直接提供实例。\n    *\n    * @param credentialsProviderClassName 凭据提供者类名\n    * @see HikariCredentialsProvider\n    */",
        ),
        (
            "   /**\n    * Get the {@link HikariCredentialsProvider} instance created by {@link #setCredentialsProviderClassName(String)} or specified by\n    * {@link #setCredentialsProvider(HikariCredentialsProvider)}.\n    *\n    * @return the HikariCredentialsProvider instance, or null\n    * @see HikariCredentialsProvider\n    */",
            "   /**\n    * 获取由 {@link #setCredentialsProviderClassName(String)} 创建\n    * 或 {@link #setCredentialsProvider(HikariCredentialsProvider)} 指定的实例。\n    *\n    * @return {@link HikariCredentialsProvider} 实例，或 {@code null}\n    * @see HikariCredentialsProvider\n    */",
        ),
        (
            "   /**\n    * Set a user supplied {@link HikariCredentialsProvider} instance. If this method is used, then the {@link #setCredentialsProviderClassName(String)}\n    * method should not be used. The {@link HikariCredentialsProvider} instance will be used to get credentials at runtime.\n    *\n    * @param credentialsProvider a user supplied HikariCredentialsProvider instance\n    * @see HikariCredentialsProvider\n    */",
            "   /**\n    * 设置用户提供的 {@link HikariCredentialsProvider} 实例；\n    * 使用此方法时不应再调用 {@link #setCredentialsProviderClassName(String)}。\n    *\n    * @param credentialsProvider 用户提供的凭据提供者\n    * @see HikariCredentialsProvider\n    */",
        ),
        (
            "   /**\n    * Get the user supplied SQLExceptionOverride class name.\n    *\n    * @return the user supplied SQLExceptionOverride class name\n    * @see SQLExceptionOverride\n    */",
            "   /**\n    * 获取用户提供的 {@link SQLExceptionOverride} 类名。\n    *\n    * @return 类名\n    * @see SQLExceptionOverride\n    */",
        ),
        (
            "   /**\n    * Set the user supplied SQLExceptionOverride class name.\n    *\n    * @param exceptionOverrideClassName the user supplied SQLExceptionOverride class name\n    * @see SQLExceptionOverride\n    */",
            "   /**\n    * 设置用户提供的 {@link SQLExceptionOverride} 类名。\n    *\n    * @param exceptionOverrideClassName 类名\n    * @see SQLExceptionOverride\n    */",
        ),
        (
            "   /**\n    * Get the SQLExceptionOverride instance created by {@link #setExceptionOverrideClassName(String)} or specified by\n    * {@link #setExceptionOverride(SQLExceptionOverride)}.\n    *\n    * @return the SQLExceptionOverride instance, or null\n    * @see SQLExceptionOverride\n    */",
            "   /**\n    * 获取由 {@link #setExceptionOverrideClassName(String)} 创建\n    * 或 {@link #setExceptionOverride(SQLExceptionOverride)} 指定的实例。\n    *\n    * @return {@link SQLExceptionOverride} 实例，或 {@code null}\n    * @see SQLExceptionOverride\n    */",
        ),
        (
            "   /**\n    * Set the user supplied SQLExceptionOverride instance.\n    *\n    * @param exceptionOverride the user supplied SQLExceptionOverride instance\n    * @see SQLExceptionOverride\n    */",
            "   /**\n    * 设置用户提供的 {@link SQLExceptionOverride} 实例。\n    *\n    * @param exceptionOverride 异常覆盖实现\n    * @see SQLExceptionOverride\n    */",
        ),
        (
            "   /**\n    * Set the default transaction isolation level.  The specified value is the\n    * constant name from the <code>Connection</code> class, eg.\n    * <code>TRANSACTION_REPEATABLE_READ</code>.\n    *\n    * @param isolationLevel the name of the isolation level\n    */",
            "   /**\n    * 设置默认事务隔离级别。值为 {@code Connection} 类中的常量名，\n    * 例如 {@code TRANSACTION_REPEATABLE_READ}。\n    *\n    * @param isolationLevel 隔离级别名称\n    */",
        ),
        (
            "   /**\n    * Get the thread factory used to create threads.\n    *\n    * @return the thread factory (may be null, in which case the default thread factory is used)\n    */",
            "   /**\n    * 获取创建线程的 {@link ThreadFactory}。\n    *\n    * @return 线程工厂，{@code null} 时使用默认工厂\n    */",
        ),
        (
            "   /**\n    * Set the thread factory to be used to create threads.\n    *\n    * @param threadFactory the thread factory (setting to null causes the default thread factory to be used)\n    */",
            "   /**\n    * 设置创建线程的 {@link ThreadFactory}。\n    *\n    * @param threadFactory 线程工厂，{@code null} 时使用默认工厂\n    */",
        ),
        (
            "   /**\n    * Copies the state of {@code this} into {@code other}.\n    *\n    * @param other Other {@link HikariConfig} to copy the state to.\n    */",
            "   /**\n    * 将 {@code this} 的状态复制到 {@code other}。\n    *\n    * @param other 目标 {@link HikariConfig}\n    */",
        ),
    ],
    "HikariPool.java": [
        (
            "/**\n * This is the primary connection pool class that provides the basic\n * pooling behavior for HikariCP.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * HikariCP 核心连接池实现，提供借还连接、维护空闲连接、\n * 驱逐/关闭连接及池生命周期管理等基本池化行为。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        (
            "   /**\n    * Construct a HikariPool with the specified configuration.\n    *\n    * @param config a HikariConfig instance\n    */",
            "   /**\n    * 使用指定配置构造连接池。\n    *\n    * @param config {@link HikariConfig} 实例\n    */",
        ),
        (
            "   /**\n    * Get a connection from the pool, or timeout after connectionTimeout milliseconds.\n    *\n    * @return a java.sql.Connection instance\n    * @throws SQLException thrown if a timeout occurs trying to obtain a connection\n    */",
            "   /**\n    * 从池中获取连接，超时时间为 {@code connectionTimeout} 毫秒。\n    *\n    * @return {@link java.sql.Connection} 实例\n    * @throws SQLException 获取连接超时\n    */",
        ),
        (
            "   /**\n    * Get a connection from the pool, or timeout after the specified number of milliseconds.\n    *\n    * @param hardTimeout the maximum time to wait for a connection from the pool\n    * @return a java.sql.Connection instance\n    * @throws SQLException thrown if a timeout occurs trying to obtain a connection\n    */",
            "   /**\n    * 从池中获取连接，在指定毫秒内超时。\n    *\n    * @param hardTimeout 最大等待毫秒数\n    * @return {@link java.sql.Connection} 实例\n    * @throws SQLException 获取连接超时\n    */",
        ),
        (
            "   /**\n    * Shutdown the pool, closing all idle connections and aborting or closing\n    * active connections.\n    *\n    * @throws InterruptedException thrown if the thread is interrupted during shutdown\n    */",
            "   /**\n    * 关闭连接池：关闭所有空闲连接，中止或关闭活跃连接。\n    *\n    * @throws InterruptedException 关闭过程中线程被中断\n    */",
        ),
        (
            "   /**\n    * Evict a Connection from the pool.\n    *\n    * @param connection the Connection to evict (actually a {@link ProxyConnection})\n    */",
            "   /**\n    * 从池中驱逐连接。\n    *\n    * @param connection 要驱逐的连接（实际为 {@link ProxyConnection}）\n    */",
        ),
        (
            "   /**\n    * Set a metrics registry to be used when registering metrics collectors.  The HikariDataSource prevents this\n    * method from being called more than once.\n    *\n    * @param metricRegistry the metrics registry instance to use\n    */",
            "   /**\n    * 设置注册指标收集器所用的 metrics 注册表；{@link HikariDataSource} 禁止多次调用。\n    *\n    * @param metricRegistry 指标注册表实例\n    */",
        ),
        (
            "   /**\n    * Set the MetricsTrackerFactory to be used to create the IMetricsTracker instance used by the pool.\n    *\n    * @param metricsTrackerFactory an instance of a class that subclasses MetricsTrackerFactory\n    */",
            "   /**\n    * 设置用于创建池内 {@link IMetricsTracker} 的 {@link MetricsTrackerFactory}。\n    *\n    * @param metricsTrackerFactory {@link MetricsTrackerFactory} 子类实例\n    */",
        ),
        (
            "   /**\n    * Set the health check registry to be used when registering health checks.  Currently only Codahale health\n    * checks are supported.\n    *\n    * @param healthCheckRegistry the health check registry instance to use\n    */",
            "   /**\n    * 设置注册健康检查所用的注册表；目前仅支持 Codahale 健康检查。\n    *\n    * @param healthCheckRegistry 健康检查注册表实例\n    */",
        ),
        (
            "   /**\n    * Log the current pool state at debug level.\n    *\n    * @param prefix an optional prefix to prepend the log message\n    */",
            "   /**\n    * 在 DEBUG 级别记录当前池状态。\n    *\n    * @param prefix 日志消息可选前缀\n    */",
        ),
        (
            "   /**\n    * Recycle PoolEntry (add back to the pool)\n    *\n    * @param poolEntry the PoolEntry to recycle\n    */",
            "   /**\n    * 回收 {@link PoolEntry}（归还到池中）。\n    *\n    * @param poolEntry 要回收的条目\n    */",
        ),
        (
            "   /**\n    * Permanently close the real (underlying) connection (eat any exception).\n    *\n    * @param poolEntry poolEntry having the connection to close\n    * @param closureReason reason to close\n    */",
            "   /**\n    * 永久关闭底层真实连接（吞掉异常）。\n    *\n    * @param poolEntry 持有待关闭连接的条目\n    * @param closureReason 关闭原因\n    */",
        ),
        (
            "   /**\n    * Creating new poolEntry. If maxLifetime is configured, create a future End-of-life task with variance from\n    * the maxLifetime time to ensure there is no massive die-off of Connections in the pool.\n    */",
            "   /**\n    * 创建新的 {@link PoolEntry}。若配置了 maxLifetime，\n    * 会按带随机方差的寿命调度退役任务，避免池中连接同时大量失效。\n    */",
        ),
        (
            "   /**\n    * Fill pool up from current idle connections (as they are perceived at the point of execution) to minimumIdle connections.\n    */",
            "   /**\n    * 根据当前空闲连接数补充到 {@code minimumIdle}。\n    */",
        ),
        (
            "   /**\n    * Attempt to abort or close active connections.\n    *\n    * @param assassinExecutor the ExecutorService to pass to Connection.abort()\n    */",
            "   /**\n    * 尝试中止或关闭所有活跃连接。\n    *\n    * @param assassinExecutor 传给 {@link Connection#abort(Executor)} 的执行器\n    */",
        ),
        (
            "   /**\n    * If {@code initializationFailTimeout} is configured, check that we have DB connectivity.\n    *\n    * @throws PoolInitializationException if fails to create or validate connection\n    * @see HikariConfig#setInitializationFailTimeout(long)\n    */",
            "   /**\n    * 若配置了 {@code initializationFailTimeout}，校验数据库连通性。\n    *\n    * @throws PoolInitializationException 创建或校验连接失败\n    * @see HikariConfig#setInitializationFailTimeout(long)\n    */",
        ),
        (
            "   /**\n    * Log the Throwable that caused pool initialization to fail, and then throw a PoolInitializationException with\n    * that cause attached.\n    *\n    * @param t the Throwable that caused the pool to fail to initialize (possibly null)\n    */",
            "   /**\n    * 记录导致池初始化失败的异常，并抛出附带原因的 {@link PoolInitializationException}。\n    *\n    * @param t 失败原因（可为 {@code null}）\n    */",
        ),
        (
            "   /**\n    * \"Soft\" evict a Connection (/PoolEntry) from the pool.  If this method is being called by the user directly\n    * through {@link HikariDataSource#evictConnection(Connection)} then {@code owner} is {@code true}.\n    * <p>\n    * If the caller is the owner, or if the Connection is idle (i.e. can be \"reserved\" in the {@link ConcurrentBag}),\n    * then we can close the connection immediately.  Otherwise, we leave it \"marked\" for eviction so that it is evicted\n    * the next time someone tries to acquire it from the pool.\n    * <p>\n    * @param poolEntry the PoolEntry (/Connection) to \"soft\" evict from the pool\n    * @param reason the reason that the connection is being evicted\n    * @param owner true if the caller is the owner of the connection, false otherwise\n    * @return true if the connection was evicted (closed), false if it was merely marked for eviction\n    */",
            "   /**\n    * 对连接/{@link PoolEntry} 执行“软”驱逐。\n    * 若由用户经 {@link HikariDataSource#evictConnection(Connection)} 调用，则 {@code owner} 为 {@code true}。\n    * <p>\n    * 若调用方是持有者，或连接空闲（可在 {@link ConcurrentBag} 中 reserve），则立即关闭；\n    * 否则仅标记待驱逐，下次借出时再关闭。\n    * <p>\n    * @param poolEntry 要软驱逐的条目\n    * @param reason 驱逐原因\n    * @param owner 调用方是否为连接持有者\n    * @return 已关闭返回 {@code true}，仅标记返回 {@code false}\n    */",
        ),
        (
            "   /**\n    * Create/initialize the Housekeeping service {@link ScheduledExecutorService}.  If the user specified an Executor\n    * to be used in the {@link HikariConfig}, then we use that.  If no Executor was specified (typical), then create\n    * an Executor and configure it.\n    *\n    * @return either the user specified {@link ScheduledExecutorService}, or the one we created\n    */",
            "   /**\n    * 创建/初始化 housekeeping 用的 {@link ScheduledExecutorService}。\n    * 若 {@link HikariConfig} 中已指定则使用用户提供的，否则创建并配置默认实例。\n    *\n    * @return 用户指定或内部创建的调度执行器\n    */",
        ),
        (
            "   /**\n    * Destroy (/shutdown) the Housekeeping service Executor, if it was the one that we created.\n    */",
            "   /**\n    * 销毁（shutdown）housekeeping 执行器（仅当由本池创建时）。\n    */",
        ),
        (
            "   /**\n    * Create a PoolStats instance that will be used by metrics tracking, with a pollable resolution of 1 second.\n    *\n    * @return a PoolStats instance\n    */",
            "   /**\n    * 创建指标追踪用的 {@link PoolStats} 实例，采样分辨率 1 秒。\n    *\n    * @return {@link PoolStats} 实例\n    */",
        ),
        (
            "   /**\n    * Create a timeout exception (specifically, {@link SQLTransientConnectionException}) to be thrown, because a\n    * timeout occurred when trying to acquire a Connection from the pool.  If there was an underlying cause for the\n    * timeout, e.g. a SQLException thrown by the driver while trying to create a new Connection, then use the\n    * SQL State from that exception as our own and additionally set that exception as the \"next\" SQLException inside\n    * our exception.\n    * <p>\n    * As a side effect, log the timeout failure at DEBUG, and record the timeout failure in the metrics tracker.\n    *\n    * @param startTime the start time (timestamp) of the acquisition attempt\n    * @return a SQLException to be thrown from {@link #getConnection()}\n    */",
            "   /**\n    * 创建获取连接超时异常（{@link SQLTransientConnectionException}）。\n    * 若有底层原因（如驱动创建连接时抛出的 {@link SQLException}），\n    * 复用其 SQLState 并链为 nextException。\n    * <p>\n    * 副作用：DEBUG 记录超时，并向指标追踪器上报。\n    *\n    * @param startTime 获取尝试的起始时间戳\n    * @return 从 {@link #getConnection()} 抛出的 {@link SQLException}\n    */",
        ),
        (
            "   /**\n    * Creating and adding poolEntries (connections) to the pool.\n    */",
            "   /**\n    * 创建 {@link PoolEntry}（连接）并加入池。\n    */",
        ),
        (
            "      /**\n       * We only create connections if we need another idle connection or have threads still waiting\n       * for a new connection. Otherwise, we bail out of the request to create.\n       *\n       * @return true if we should create a connection, false if the need has disappeared\n       */",
            "      /**\n       * 仅当需要更多空闲连接或有线程等待时才继续创建；否则退出。\n       *\n       * @return 是否应继续创建连接\n       */",
        ),
        (
            "   /**\n    * The housekeeping task to retire and maintain minimum idle connections.\n    */",
            "   /**\n    * Housekeeping 任务：退役过期空闲连接并维持最小空闲数。\n    */",
        ),
        (
            "   /**\n    * Exception thrown when the pool fails to initialize, typically due to a failure to create or validate a connection.\n    * @hidden\n    */",
            "   /**\n    * 池初始化失败时抛出，通常因无法创建或校验连接。\n    * @hidden\n    */",
        ),
        (
            "      /**\n       * Construct an exception, possibly wrapping the provided Throwable as the cause.\n       * @param t the Throwable to wrap\n       */",
            "      /**\n       * 构造异常，可将给定 {@link Throwable} 作为原因。\n       * @param t 要包装的原因\n       */",
        ),
    ],
    "PoolEntry.java": [
        (
            "/**\n * Entry used in the ConcurrentBag to track Connection instances.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * {@link ConcurrentBag} 中跟踪 {@link Connection} 实例的条目。\n *\n * @author Brett Wooldridge\n */",
        ),
        ("   /**\n    * Release this entry back to the pool.\n    */", "   /**\n    * 将此条目归还到连接池。\n    */"),
        (
            "   /**\n    * Set the end of life {@link ScheduledFuture}.\n    *\n    * @param endOfLife this PoolEntry/Connection's end of life {@link ScheduledFuture}\n    */",
            "   /**\n    * 设置寿命结束的 {@link ScheduledFuture}。\n    *\n    * @param endOfLife 本条目/连接的寿命结束任务\n    */",
        ),
        ("   /** Returns millis since lastBorrowed */", "   /** 返回自上次借出以来的毫秒数 */"),
    ],
    "ProxyConnection.java": [
        (
            "/**\n * This is the proxy class for {@link Connection}.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * {@link Connection} 的代理实现，拦截 JDBC 调用并在归还时重置连接状态。\n *\n * @author Brett Wooldridge\n */",
        ),
    ],
    "ProxyFactory.java": [
        (
            "/**\n * A factory class that produces proxies around instances of the standard\n * JDBC interfaces.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * 为标准 JDBC 接口实例创建代理对象的工厂。\n *\n * @author Brett Wooldridge\n */",
        ),
        (
            "   /**\n    * Create a proxy for the specified {@link Connection} instance.\n    * @param poolEntry the PoolEntry holding pool state\n    * @param connection the raw database Connection\n    * @param openStatements a reusable list to track open Statement instances\n    * @param leakTask the ProxyLeakTask for this connection\n    * @param isReadOnly the default readOnly state of the connection\n    * @param isAutoCommit the default autoCommit state of the connection\n    * @return a proxy that wraps the specified {@link Connection}\n    */",
            "   /**\n    * 为指定 {@link Connection} 创建代理。\n    * @param poolEntry 持有池状态的 {@link PoolEntry}\n    * @param connection 原始数据库连接\n    * @param openStatements 跟踪已打开 {@link Statement} 的可复用列表\n    * @param leakTask 本连接的 {@link ProxyLeakTask}\n    * @param isReadOnly 连接默认只读状态\n    * @param isAutoCommit 连接默认自动提交状态\n    * @return 包装指定 {@link Connection} 的代理\n    */",
        ),
        ("      // unconstructable", "      // 不可实例化"),
        ("      // Body is replaced (injected) by JavassistProxyFactory", "      // 方法体由 JavassistProxyFactory 在构建时注入"),
    ],
    "ProxyLeakTask.java": [
        (
            "/**\n * A Runnable that is scheduled in the future to report leaks.  The ScheduledFuture is\n * cancelled if the connection is closed before the leak time expires.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * 定时报告连接泄漏的 {@link Runnable}。\n * 若在泄漏检测阈值前归还连接，则取消对应的 {@link ScheduledFuture}。\n *\n * @author Brett Wooldridge\n */",
        ),
    ],
    "ProxyLeakTaskFactory.java": [
        (
            "/**\n * A factory for {@link ProxyLeakTask} Runnables that are scheduled in the future to report leaks.\n *\n * @author Brett Wooldridge\n * @author Andreas Brenk\n */",
            "/**\n * 创建并调度 {@link ProxyLeakTask} 的工厂，用于连接泄漏检测。\n *\n * @author Brett Wooldridge\n * @author Andreas Brenk\n */",
        ),
    ],
    "ProxyStatement.java": [
        (
            "/**\n * This is the proxy class for {@link Statement}.\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
            "/**\n * {@link Statement} 的代理实现，跟踪打开的语句并在关闭时清理。\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
        ),
    ],
    "ProxyPreparedStatement.java": [
        (
            "/**\n * This is the proxy class for {@link PreparedStatement}.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * {@link PreparedStatement} 的代理实现。\n *\n * @author Brett Wooldridge\n */",
        ),
    ],
    "ProxyCallableStatement.java": [
        (
            "/**\n * This is the proxy class for {@link CallableStatement}.\n *\n * @author Brett Wooldridge\n */",
            "/**\n * {@link CallableStatement} 的代理实现。\n *\n * @author Brett Wooldridge\n */",
        ),
    ],
    "ProxyResultSet.java": [
        (
            "/**\n * This is the proxy class for {@link ResultSet}.\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
            "/**\n * {@link ResultSet} 的代理实现。\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
        ),
    ],
    "ProxyDatabaseMetaData.java": [
        (
            "/**\n * This is the proxy class for {@link DatabaseMetaData}.\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
            "/**\n * {@link DatabaseMetaData} 的代理实现。\n *\n * @author Brett Wooldridge\n * @author Yanming Zhou\n */",
        ),
    ],
    "PoolBase.java": [
        (
            "abstract class PoolBase",
            "/**\n * 连接池抽象基类：管理底层 {@link DataSource}、创建/校验连接、\n * 重置连接状态及 JMX 注册等共用逻辑。\n */\nabstract class PoolBase",
        ),
        (
            "   /**\n    * Register MBeans for HikariConfig and HikariPool.\n    *\n    * @param hikariPool a HikariPool instance\n    */",
            "   /**\n    * 注册或注销 {@link HikariConfig} 与 {@link HikariPool} 的 MBean。\n    *\n    * @param hikariPool {@link HikariPool} 实例\n    */",
        ),
        (
            "   /**\n    * Create/initialize the underlying DataSource.\n    */",
            "   /**\n    * 创建/初始化底层 {@link DataSource}。\n    */",
        ),
        (
            "   /**\n    * Obtain connection from data source.\n    *\n    * @return a Connection\n    */",
            "   /**\n    * 从数据源获取连接。\n    *\n    * @return {@link Connection}\n    */",
        ),
        (
            "   /**\n    * Set up a connection initial state.\n    *\n    * @param connection a Connection\n    * @throws ConnectionSetupException thrown if any exception is encountered\n    */",
            "   /**\n    * 设置连接的初始状态（只读、自动提交、隔离级别、schema 等）。\n    *\n    * @param connection 连接\n    * @throws ConnectionSetupException 设置过程中发生异常\n    */",
        ),
        (
            "   /**\n    * Execute isValid() or connection test query.\n    *\n    * @param connection a Connection to check\n    */",
            "   /**\n    * 执行 {@code isValid()} 或连接测试查询。\n    *\n    * @param connection 待检查的连接\n    */",
        ),
        ("   //                       JMX methods", "   //                       JMX 方法"),
        ("         // tracker will be null during failFast check", "         // failFast 检查时 tracker 可能为 null"),
        ("         // fall thru (continue)", "         // 忽略并继续"),
        (
            "   /**\n    * Set the query timeout, if it is supported by the driver.\n    *\n    * @param statement a Statement to set the query timeout on\n    * @param timeoutSec timeout in seconds\n    * @throws SQLException if the driver does not support query timeouts\n    */",
            "   /**\n    * 若驱动支持，设置查询超时。\n    *\n    * @param statement 要设置超时的 {@link Statement}\n    * @param timeoutSec 超时秒数\n    * @throws SQLException 驱动不支持查询超时时抛出\n    */",
        ),
        (
            "   /**\n    * Set the network timeout, if <code>isUseNetworkTimeout</code> is <code>true</code> and the\n    * driver supports it.\n    *\n    * @param connection a Connection to set the network timeout on\n    * @param timeoutMs timeout in milliseconds\n    */",
            "   /**\n    * 若启用网络超时且驱动支持，设置网络超时。\n    *\n    * @param connection 要设置超时的连接\n    * @param timeoutMs 超时毫秒数\n    */",
        ),
        (
            "   /**\n    * Execute the user-specified init SQL.\n    *\n    * @param connection a Connection\n    * @param sql the SQL to execute\n    * @param isAutoCommit whether to restore auto-commit after execution\n    * @throws SQLException if the SQL fails\n    */",
            "   /**\n    * 执行用户指定的初始化 SQL。\n    *\n    * @param connection 连接\n    * @param sql 要执行的 SQL\n    * @param isAutoCommit 执行后是否恢复自动提交\n    * @throws SQLException SQL 执行失败\n    */",
        ),
        (
            "   /**\n    * Set the loginTimeout on the specified DataSource.\n    *\n    * @param dataSource the DataSource on which to set the login timeout\n    */",
            "   /**\n    * 在指定 {@link DataSource} 上设置登录超时。\n    *\n    * @param dataSource 数据源\n    */",
        ),
    ],
    "DriverDataSource.java": [
        (
            "/**\n * A DataSource implementation that uses a JDBC Driver to create connections.\n * This class is used to provide a DataSource that can be configured with a\n * specific JDBC driver and connection properties.\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 通过 JDBC {@link Driver} 创建连接的 {@link javax.sql.DataSource} 实现。\n * 可使用指定驱动与连接属性进行配置。\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
    ],
    "FastList.java": [
        (
            "/**\n * Fast list without range checking.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 无边界检查的快速列表实现。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        (
            "   /**\n    * Construct a FastList with a default size of 32.\n    * @param clazz the Class stored in the collection\n    */",
            "   /**\n    * 构造默认容量 32 的 {@link FastList}。\n    * @param clazz 集合存储的元素类型\n    */",
        ),
        (
            "   /**\n    * Construct a FastList with a specified size.\n    * @param clazz the Class stored in the collection\n    * @param size the initial size of the FastList\n    */",
            "   /**\n    * 构造指定初始容量的 {@link FastList}。\n    * @param clazz 集合存储的元素类型\n    * @param size 初始容量\n    */",
        ),
        (
            "   /**\n    * Add an element to the tail of the FastList.\n    *\n    * @param element the element to add\n    * @return <code>true</code> (as specified by the Collection interface)\n    */",
            "   /**\n    * 在 {@link FastList} 尾部添加元素。\n    *\n    * @param element 要添加的元素\n    * @return 恒为 {@code true}（符合 {@link Collection} 约定）\n    */",
        ),
        (
            "   /**\n    * Get the element at the specified index.\n    *\n    * @param index the index of the element to return\n    * @return the element at the specified index\n    */",
            "   /**\n    * 获取指定索引处的元素。\n    *\n    * @param index 索引\n    * @return 该索引处的元素\n    */",
        ),
        (
            "   /**\n    * Remove the last element from the list.  No bound check is performed, so if this\n    * method is called when the list is empty, an ArrayIndexOutOfBoundsException will be\n    * thrown.\n    */",
            "   /**\n    * 移除列表最后一个元素；不做边界检查，空列表调用将抛出 {@link ArrayIndexOutOfBoundsException}。\n    */",
        ),
        (
            "   /**\n    * Get the current number of elements in the FastList.\n    *\n    * @return the number of elements in the FastList\n    */",
            "   /**\n    * 获取 {@link FastList} 当前元素个数。\n    *\n    * @return 元素个数\n    */",
        ),
    ],
    "PropertyElf.java": [
        (
            "/**\n * A class that reflectively sets bean properties on a target object.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 通过反射将属性设置到目标 Bean 上的辅助类。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        (
            "   /**\n    * Get the bean-style property names for the specified object.\n    *\n    * @param targetClass the target object\n    * @return a set of property names\n    */",
            "   /**\n    * 获取指定类的 Bean 风格属性名集合。\n    *\n    * @param targetClass 目标类\n    * @return 属性名集合\n    */",
        ),
        ("      // cannot be constructed", "      // 不可实例化"),
    ],
    "SuspendResumeLock.java": [
        (
            "/**\n * This class implements a lock that can be used to suspend and resume the pool.  It\n * also provides a faux implementation that is used when the feature is disabled that\n * hopefully gets fully \"optimized away\" by the JIT.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 用于挂起/恢复连接池访问的锁。\n * 功能禁用时提供 FAUX_LOCK 空实现，期望被 JIT 完全优化掉。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        ("   /**\n    * Default constructor\n    */", "   /** 默认构造器。 */"),
    ],
    "JavassistProxyFactory.java": [
        (
            "public final class JavassistProxyFactory",
            "/**\n * 基于 Javassist 的 JDBC 代理类生成器（较 {@link java.lang.reflect.Proxy} 更快）。\n */\npublic final class JavassistProxyFactory",
        ),
    ],
    "Credentials.java": [
        (
            "/**\n * A simple class to hold connection credentials and is designed to be immutable.\n */",
            "/**\n * 不可变的连接凭据（用户名/密码）值对象。\n */",
        ),
        (
            "   /**\n    * Construct an immutable Credentials object with the supplied username and password.\n    *\n    * @param username the username\n    * @param password the password\n    * @return a new Credentials object\n    */",
            "   /**\n    * 使用给定用户名与密码创建不可变 {@link Credentials}。\n    *\n    * @param username 用户名\n    * @param password 密码\n    * @return 新的 {@link Credentials} 实例\n    */",
        ),
        (
            "   /**\n    * Construct an immutable Credentials object with the supplied username and password.\n    *\n    * @param username the username\n    * @param password the password\n    */",
            "   /**\n    * 使用给定用户名与密码构造不可变凭据。\n    *\n    * @param username 用户名\n    * @param password 密码\n    */",
        ),
        ("   /**\n    * Get the username.\n    *\n    * @return the username\n    */", "   /**\n    * 获取用户名。\n    *\n    * @return 用户名\n    */"),
        ("   /**\n    * Get the password.\n    *\n    * @return the password\n    */", "   /**\n    * 获取密码。\n    *\n    * @return 密码\n    */"),
    ],
    "IsolationLevel.java": [
        (
            "/**\n * An enumeration representing the various isolation levels for database transactions.\n * Each isolation level corresponds to a specific behavior regarding visibility of changes made by other transactions.\n * The levels are defined according to the SQL standard and may vary in implementation across different databases.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 数据库事务隔离级别枚举。\n * 各级别对应其他事务已提交/未提交变更的可见性语义；\n * 按 SQL 标准定义，各数据库实现可能略有差异。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
    ],
    "ConcurrentBag.java": [
        (
            "/**\n * This is a specialized concurrent bag that achieves superior performance\n * to LinkedBlockingQueue and LinkedTransferQueue for the purposes of a\n * connection pool.  It uses ThreadLocal storage when possible to avoid\n * locks, but resorts to scanning a common collection if there are no\n * available items in the ThreadLocal list.  Not-in-use items in the\n * ThreadLocal lists can be \"stolen\" when the borrowing thread has none\n * of its own.  It is a \"lock-less\" implementation using a specialized\n * AbstractQueuedLongSynchronizer to manage cross-thread signaling.\n * <p>\n * Note that items that are \"borrowed\" from the bag are not actually\n * removed from any collection, so garbage collection will not occur\n * even if the reference is abandoned.  Thus care must be taken to\n * \"requite\" borrowed objects otherwise a memory leak will result.  Only\n * the \"remove\" method can completely remove an object from the bag.\n *\n * @author Brett Wooldridge\n *\n * @param <T> the templated type to store in the bag\n * @hidden\n */",
            "/**\n * 专为连接池设计的高性能并发“袋子”结构，\n * 借还性能优于 {@link java.util.concurrent.LinkedBlockingQueue} 与\n * {@link java.util.concurrent.LinkedTransferQueue}。\n * 优先使用 {@link ThreadLocal} 避免锁；本地无可用项时扫描共享列表，\n * 并可“窃取”其他线程 ThreadLocal 中的空闲项。\n * 通过定制的 AbstractQueuedLongSynchronizer 实现近乎无锁的跨线程信号。\n * <p>\n * 注意：借出项不会从集合中物理移除，遗弃引用也不会被 GC 回收；\n * 必须调用 {@link #requite(IConcurrentBagEntry)} 归还，否则内存泄漏。\n * 只有 {@link #remove(IConcurrentBagEntry)} 能彻底移除项。\n *\n * @author Brett Wooldridge\n * @param <T> 袋中存储的类型\n * @hidden\n */",
        ),
        (
            "   /**\n    * This interface defines the contract for an entry in the ConcurrentBag.\n    * It provides methods to manage the state of the entry, which can be\n    * \"not in use\", \"in use\", \"removed\", or \"reserved\".\n    * @hidden\n    */",
            "   /**\n    * {@link ConcurrentBag} 条目的状态契约。\n    * 状态可为：未使用、使用中、已移除、已预留。\n    * @hidden\n    */",
        ),
        (
            "   /**\n    * Construct a ConcurrentBag with the specified listener.\n    *\n    * @param listener the listener to notify when add() is called\n    */",
            "   /**\n    * 使用指定监听器构造 {@link ConcurrentBag}。\n    *\n    * @param listener 调用 {@link #add(IConcurrentBagEntry)} 时通知的监听器\n    */",
        ),
        (
            "   /**\n    * Add a new object to the bag for others to borrow.\n    *\n    * @param bagEntry the object to add to the bag\n    */",
            "   /**\n    * 向袋中添加新对象供其他线程借出。\n    *\n    * @param bagEntry 要添加的条目\n    */",
        ),
        (
            "   /**\n    * Remove a value from the bag.  This method should only be called\n    * when the object is not in use (borrowed).\n    *\n    * @param bagEntry the value to remove\n    * @return true if the object was removed, false otherwise\n    */",
            "   /**\n    * 从袋中移除条目；仅应在对象未被借出时调用。\n    *\n    * @param bagEntry 要移除的条目\n    * @return 移除成功返回 {@code true}\n    */",
        ),
        (
            "   /**\n    * Get the number of threads pending (waiting) for an item from the\n    * bag.\n    *\n    * @return the number of waiting threads\n    */",
            "   /**\n    * 获取等待从袋中借出条目的线程数。\n    *\n    * @return 等待线程数\n    */",
        ),
        (
            "   /**\n    * Get a count of the number of items in the specified state at the time of this call.\n    *\n    * @param state the state to count\n    * @return the number of items in the specified state\n    */",
            "   /**\n    * 获取指定状态下条目数量（调用时刻快照）。\n    *\n    * @param state 要统计的状态\n    * @return 该状态下的条目数\n    */",
        ),
        (
            "   /**\n    * Get the total number of items in the bag.\n    *\n    * @return the number of items in the bag\n    */",
            "   /**\n    * 获取袋中条目总数。\n    *\n    * @return 条目总数\n    */",
        ),
    ],
    "ClockSource.java": [
        (
            "/**\n * A resolution-independent provider of current time-stamps and elapsed time\n * calculations.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * 与具体时钟分辨率无关的时间戳与耗时计算提供者。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        (
            "   /**\n    * Get the current time-stamp (resolution is opaque).\n    *\n    * @return the current time-stamp\n    */",
            "   /**\n    * 获取当前时间戳（分辨率由实现决定）。\n    *\n    * @return 当前时间戳\n    */",
        ),
        (
            "   /**\n    * Convert an opaque time-stamp returned by currentTime() into\n    * milliseconds.\n    *\n    * @param time an opaque time-stamp returned by an instance of this class\n    * @return the time-stamp in milliseconds\n    */",
            "   /**\n    * 将 {@link #currentTime()} 返回的不透明时间戳转换为毫秒。\n    *\n    * @param time 不透明时间戳\n    * @return 毫秒值\n    */",
        ),
        (
            "   /**\n    * Convert an opaque time-stamp returned by currentTime() into\n    * nanoseconds.\n    *\n    * @param time an opaque time-stamp returned by an instance of this class\n    * @return the time-stamp in nanoseconds\n    */",
            "   /**\n    * 将 {@link #currentTime()} 返回的不透明时间戳转换为纳秒。\n    *\n    * @param time 不透明时间戳\n    * @return 纳秒值\n    */",
        ),
        (
            "   /**\n    * Get the difference in milliseconds between two opaque time-stamps returned\n    * by currentTime().\n    *\n    * @param startTime an opaque start time-stamp returned by currentTime()\n    * @param endTime an opaque end time-stamp returned by currentTime()\n    * @return the difference in milliseconds\n    */",
            "   /**\n    * 计算两个 {@link #currentTime()} 时间戳之间的毫秒差。\n    *\n    * @param startTime 起始时间戳\n    * @param endTime 结束时间戳\n    * @return 毫秒差\n    */",
        ),
        (
            "   /**\n    * Get the difference in nanoseconds between two opaque time-stamps returned\n    * by currentTime().\n    *\n    * @param startTime an opaque start time-stamp returned by currentTime()\n    * @param endTime an opaque end time-stamp returned by currentTime()\n    * @return the difference in nanoseconds\n    */",
            "   /**\n    * 计算两个 {@link #currentTime()} 时间戳之间的纳秒差。\n    *\n    * @param startTime 起始时间戳\n    * @param endTime 结束时间戳\n    * @return 纳秒差\n    */",
        ),
        (
            "   /**\n    * Return the specified opaque time-stamp plus the specified number of milliseconds.\n    *\n    * @param time an opaque time-stamp returned by currentTime()\n    * @param millis milliseconds to add\n    * @return an opaque time-stamp\n    */",
            "   /**\n    * 返回指定时间戳加上给定毫秒数后的新时间戳。\n    *\n    * @param time 不透明时间戳\n    * @param millis 要增加的毫秒数\n    * @return 新时间戳\n    */",
        ),
        (
            "   /**\n    * Get the TimeUnit the ClockSource is denominated in.\n    *\n    * @return the TimeUnit\n    */",
            "   /**\n    * 获取 {@link ClockSource} 使用的时间单位。\n    *\n    * @return 时间单位\n    */",
        ),
        (
            "   /**\n    * Get a String representation of the elapsed time in appropriate magnitude terminology.\n    *\n    * @param startTime an opaque start time-stamp returned by currentTime()\n    * @param endTime an opaque end time-stamp returned by currentTime()\n    * @return a String representation of the elapsed time\n    */",
            "   /**\n    * 以合适量级返回耗时的字符串表示。\n    *\n    * @param startTime 起始时间戳\n    * @param endTime 结束时间戳\n    * @return 耗时描述字符串\n    */",
        ),
    ],
    "UtilityElf.java": [
        (
            "/**\n * Utilities methods.\n *\n * @author Brett Wooldridge\n * @hidden\n */",
            "/**\n * HikariCP 通用工具方法集合。\n *\n * @author Brett Wooldridge\n * @hidden\n */",
        ),
        (
            "   /**\n    * Get a trimmed string or null if the string is null or empty.\n    *\n    * @param value the string to trim\n    * @return a trimmed string, or null\n    */",
            "   /**\n    * 返回去首尾空白的字符串；{@code null} 或空串则返回 {@code null}。\n    *\n    * @param value 原字符串\n    * @return 去空白后的字符串或 {@code null}\n    */",
        ),
        (
            "   /**\n    * Create and instance of the specified class using the constructor matching the specified\n    * argument types.\n    *\n    * @param className the name of the class to instantiate\n    * @param interfaceClass an interface that the specified class implements\n    * @param args arguments to pass to the constructor\n    * @return a newly instantiated class\n    */",
            "   /**\n    * 使用匹配参数类型的构造器创建指定类实例。\n    *\n    * @param className 要实例化的类名\n    * @param interfaceClass 指定类应实现的接口（用于校验）\n    * @param args 构造器参数\n    * @return 新实例\n    */",
        ),
        (
            "   /**\n    * Create a ThreadPoolExecutor.\n    *\n    * @param poolSize the size of the pool\n    * @param poolName the name of the pool\n    * @param threadFactory the ThreadFactory to use\n    * @param rejectedExecutionHandler the RejectedExecutionHandler to use\n    * @return a ThreadPoolExecutor\n    */",
            "   /**\n    * 创建 {@link ThreadPoolExecutor}。\n    *\n    * @param poolSize 线程池大小\n    * @param poolName 线程池名称\n    * @param threadFactory 线程工厂\n    * @param rejectedExecutionHandler 拒绝策略\n    * @return 线程池执行器\n    */",
        ),
        (
            "   /**\n    * Get the int value of a transaction isolation level by name.\n    *\n    * @param isolationLevelName the name of the isolation level\n    * @return the int value of the isolation level\n    */",
            "   /**\n    * 按名称获取事务隔离级别的整型常量值。\n    *\n    * @param isolationLevelName 隔离级别名称\n    * @return 隔离级别整型值\n    */",
        ),
    ],
    "module-info.java": [
        (
            "module com.zaxxer.hikari\n{",
            "/**\n * HikariCP 高性能 JDBC 连接池模块。\n */\nmodule com.zaxxer.hikari\n{",
        ),
    ],
}

# Step comments for complex pool logic
HIKARI_POOL_STEPS: list[tuple[str, str]] = [
    (
        "      super(config);\n\n      this.connectionBag = new ConcurrentBag<>(this);",
        "      super(config);\n\n      // 步骤1：创建 ConcurrentBag 作为连接存储\n      this.connectionBag = new ConcurrentBag<>(this);",
    ),
    (
        "      checkFailFast();\n\n      if (config.getMetricsTrackerFactory()",
        "      // 步骤2：按 initializationFailTimeout 快速失败校验\n      checkFailFast();\n\n      // 步骤3：配置指标追踪\n      if (config.getMetricsTrackerFactory()",
    ),
    (
        "      suspendResumeLock.acquire();\n      final var startTime = currentTime();\n\n      try {\n         var timeout = hardTimeout;\n         do {\n            var poolEntry = connectionBag.borrow(timeout, MILLISECONDS);",
        "      // 步骤1：获取挂起/恢复锁，防止池挂起期间借连接\n      suspendResumeLock.acquire();\n      final var startTime = currentTime();\n\n      try {\n         var timeout = hardTimeout;\n         // 步骤2：循环从 bag 借出 PoolEntry，直到成功或超时\n         do {\n            var poolEntry = connectionBag.borrow(timeout, MILLISECONDS);",
    ),
    (
        "            if (poolEntry.isMarkedEvicted() || (elapsedMillis(poolEntry.lastAccessed, now) > aliveBypassWindowMs && isConnectionDead(poolEntry.connection))) {",
        "            // 步骤3：若已标记驱逐或（超过存活绕过窗口且连接已死）则关闭并重试\n            if (poolEntry.isMarkedEvicted() || (elapsedMillis(poolEntry.lastAccessed, now) > aliveBypassWindowMs && isConnectionDead(poolEntry.connection))) {",
    ),
    (
        "               return poolEntry.createProxyConnection(leakTaskFactory.schedule(poolEntry));",
        "               // 步骤4：连接可用，包装为 ProxyConnection 并调度泄漏检测\n               return poolEntry.createProxyConnection(leakTaskFactory.schedule(poolEntry));",
    ),
    (
        "         poolState = POOL_SHUTDOWN;\n\n         if (addConnectionExecutor == null)",
        "         // 步骤1：标记池为关闭状态\n         poolState = POOL_SHUTDOWN;\n\n         if (addConnectionExecutor == null)",
    ),
    (
        "         softEvictConnections();\n\n         addConnectionExecutor.shutdown();",
        "         // 步骤2：软驱逐所有连接\n         softEvictConnections();\n\n         // 步骤3：停止添加连接的线程池\n         addConnectionExecutor.shutdown();",
    ),
    (
        "      poolEntry.markEvicted();\n      if (owner || connectionBag.reserve(poolEntry)) {",
        "      // 步骤1：标记条目待驱逐\n      poolEntry.markEvicted();\n      // 步骤2：持有者可立即关闭；空闲连接可 reserve 后关闭\n      if (owner || connectionBag.reserve(poolEntry)) {",
    ),
    (
        "      final var initializationFailTimeout = config.getInitializationFailTimeout();\n      if (initializationFailTimeout < 0) {\n         return;\n      }\n\n      final var startTime = currentTime();\n      do {\n         final var poolEntry = createPoolEntry();",
        "      final var initializationFailTimeout = config.getInitializationFailTimeout();\n      if (initializationFailTimeout < 0) {\n         return;\n      }\n\n      // 在超时窗口内重试创建首条连接\n      final var startTime = currentTime();\n      do {\n         final var poolEntry = createPoolEntry();",
    ),
    (
        "            while (shouldContinueCreating()) {\n               final var poolEntry = createPoolEntry();",
        "            // 仅在池正常且仍需要连接时循环创建\n            while (shouldContinueCreating()) {\n               final var poolEntry = createPoolEntry();",
    ),
]

HIKARI_CONFIG_STEPS: list[tuple[str, str]] = [
    (
        "   public void validate()\n   {\n      if (poolName == null) {",
        "   /**\n    * 校验并规范化配置，在启动池之前调用。\n    */\n   public void validate()\n   {\n      // 步骤1：生成默认池名并校验 JMX 池名\n      if (poolName == null) {",
    ),
    (
        "      validateNumerics();\n\n      if (LOGGER.isDebugEnabled()",
        "      // 步骤2：校验数值型超时/池大小等参数\n      validateNumerics();\n\n      if (LOGGER.isDebugEnabled()",
    ),
]


def has_chinese(text: str) -> bool:
    return any("\u4e00" <= c <= "\u9fff" for c in text)


def apply_replacements(text: str, pairs: list[tuple[str, str]]) -> str:
    for old, new in sorted(pairs, key=lambda x: -len(x[0])):
        if old in text:
            text = text.replace(old, new)
    return text


def annotate_file(rel: str) -> str:
    src = SRC / rel
    text = src.read_text(encoding="utf-8")
    name = Path(rel).name

    for old, new in INLINE_COMMENTS:
        text = text.replace(old, new)

    text = apply_replacements(text, FILE_REPLACEMENTS.get(name, []))

    if name == "HikariPool.java":
        text = apply_replacements(text, HIKARI_POOL_STEPS)
    elif name == "HikariConfig.java":
        text = apply_replacements(text, HIKARI_CONFIG_STEPS)

    # Generic @param/@return cleanup for remaining English fragments
    generic = [
        (r"@return the ", "@return "),
        (r"@param (\w+) the ", r"@param \1 "),
        ("@return true if", "@return 满足条件返回 {@code true}，"),
        ("@return false if", "@return 满足条件返回 {@code false}，"),
    ]
    for old, new in generic:
        text = re.sub(old, new, text)

    return text


def main() -> int:
    missing = []
    for rel in BATCH:
        out = ANALYZED / rel
        out.parent.mkdir(parents=True, exist_ok=True)
        text = annotate_file(rel)
        out.write_text(text, encoding="utf-8")
        if not has_chinese(text):
            missing.append(rel)
    print(json.dumps({"total": len(BATCH), "ok": len(BATCH) - len(missing), "missing": missing}, ensure_ascii=False))
    return 1 if missing else 0


if __name__ == "__main__":
    raise SystemExit(main())
