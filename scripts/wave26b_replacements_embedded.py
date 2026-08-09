"""Chinese JavaDoc replacements for springframework wave26b embedded classes (small)."""

EMBEDDED_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractEmbeddedDatabaseConfigurer.java": [
        (
            "/**\n * Base class for {@link EmbeddedDatabaseConfigurer} implementations\n * providing common shutdown behavior through a \"SHUTDOWN\" statement.\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * {@link EmbeddedDatabaseConfigurer} 实现的基类，\n * 通过执行 \"SHUTDOWN\" 语句提供通用的关闭行为。\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
    ],
    "ConnectionProperties.java": [
        (
            "/**\n * {@code ConnectionProperties} serves as a simple data container that allows\n * essential JDBC connection properties to be configured consistently,\n * independent of the actual {@link javax.sql.DataSource DataSource}\n * implementation.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n * @see DataSourceFactory\n */",
            "/**\n * {@code ConnectionProperties} 作为简单数据容器，\n * 允许以一致方式配置基本 JDBC 连接属性，\n * 与实际 {@link javax.sql.DataSource DataSource} 实现无关。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n * @see DataSourceFactory\n */",
        ),
        (
            "\t/**\n\t * Set the JDBC driver class to use to connect to the database.\n\t * @param driverClass the jdbc driver class\n\t */",
            "\t/**\n\t * 设置用于连接数据库的 JDBC 驱动类。\n\t * @param driverClass JDBC 驱动类\n\t */",
        ),
        (
            "\t/**\n\t * Set the JDBC connection URL for the database.\n\t * @param url the connection url\n\t */",
            "\t/**\n\t * 设置数据库的 JDBC 连接 URL。\n\t * @param url 连接 URL\n\t */",
        ),
        (
            "\t/**\n\t * Set the username to use to connect to the database.\n\t * @param username the username\n\t */",
            "\t/**\n\t * 设置连接数据库所用的用户名。\n\t * @param username 用户名\n\t */",
        ),
        (
            "\t/**\n\t * Set the password to use to connect to the database.\n\t * @param password the password\n\t */",
            "\t/**\n\t * 设置连接数据库所用的密码。\n\t * @param password 密码\n\t */",
        ),
    ],
    "DataSourceFactory.java": [
        (
            "/**\n * {@code DataSourceFactory} encapsulates the creation of a particular\n * {@link DataSource} implementation such as a non-pooling\n * {@link org.springframework.jdbc.datasource.SimpleDriverDataSource}\n * or a HikariCP pool setup in the shape of a {@code HikariDataSource}.\n *\n * <p>Call {@link #getConnectionProperties()} to configure normalized\n * {@code DataSource} properties before calling {@link #getDataSource()}\n * to actually get the configured {@code DataSource} instance.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
            "/**\n * {@code DataSourceFactory} 封装特定 {@link DataSource} 实现的创建，\n * 例如非池化的 {@link org.springframework.jdbc.datasource.SimpleDriverDataSource}\n * 或 HikariCP 池化形式的 {@code HikariDataSource}。\n *\n * <p>调用 {@link #getConnectionProperties()} 配置标准化的\n * {@code DataSource} 属性，再调用 {@link #getDataSource()}\n * 获取已配置的 {@code DataSource} 实例。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Get the {@linkplain ConnectionProperties connection properties}\n\t * of the {@link #getDataSource DataSource} to be configured.\n\t */",
            "\t/**\n\t * 获取待配置 {@link #getDataSource DataSource} 的\n\t * {@linkplain ConnectionProperties 连接属性}。\n\t */",
        ),
        (
            "\t/**\n\t * Get the {@link DataSource} with the\n\t * {@linkplain #getConnectionProperties connection properties} applied.\n\t */",
            "\t/**\n\t * 获取已应用 {@linkplain #getConnectionProperties 连接属性} 的 {@link DataSource}。\n\t */",
        ),
    ],
    "DerbyEmbeddedDatabaseConfigurer.java": [
        (
            "/**\n * {@link EmbeddedDatabaseConfigurer} for the Apache Derby database.\n *\n * <p>Call {@link #getInstance()} to get the singleton instance of this class.\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * Apache Derby 数据库的 {@link EmbeddedDatabaseConfigurer}。\n *\n * <p>调用 {@link #getInstance()} 获取本类的单例实例。\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Get the singleton {@link DerbyEmbeddedDatabaseConfigurer} instance.\n\t * @return the configurer instance\n\t */",
            "\t/**\n\t * 获取 {@link DerbyEmbeddedDatabaseConfigurer} 单例实例。\n\t * @return 配置器实例\n\t */",
        ),
        (
            "\t\t\t// disable log file",
            "\t\t\t// 禁用日志文件",
        ),
        (
            "\t\t\t// Error code that indicates successful shutdown",
            "\t\t\t// 表示成功关闭的错误码",
        ),
    ],
    "EmbeddedDatabase.java": [
        (
            "/**\n * {@code EmbeddedDatabase} serves as a handle to an embedded database instance.\n *\n * <p>An {@code EmbeddedDatabase} is also a {@link DataSource} and adds a\n * {@link #shutdown} operation so that the embedded database instance can be\n * shut down gracefully.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
            "/**\n * {@code EmbeddedDatabase} 作为嵌入式数据库实例的句柄。\n *\n * <p>{@code EmbeddedDatabase} 同时是 {@link DataSource}，\n * 并提供 {@link #shutdown} 操作以优雅关闭嵌入式数据库实例。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Shut down this embedded database.\n\t */",
            "\t/**\n\t * 关闭此嵌入式数据库。\n\t */",
        ),
    ],
    "EmbeddedDatabaseConfigurer.java": [
        (
            "/**\n * {@code EmbeddedDatabaseConfigurer} encapsulates the configuration required to\n * create, connect to, and shut down a specific type of embedded database such as\n * HSQL, H2, or Derby.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
            "/**\n * {@code EmbeddedDatabaseConfigurer} 封装创建、连接和关闭\n * 特定类型嵌入式数据库（如 HSQL、H2 或 Derby）所需的配置。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Configure the properties required to create and connect to the embedded database.\n\t * @param properties connection properties to configure\n\t * @param databaseName the name of the embedded database\n\t */",
            "\t/**\n\t * 配置创建并连接嵌入式数据库所需的属性。\n\t * @param properties 待配置的连接属性\n\t * @param databaseName 嵌入式数据库名称\n\t */",
        ),
        (
            "\t/**\n\t * Shut down the embedded database instance that backs the supplied {@link DataSource}.\n\t * @param dataSource the corresponding {@link DataSource}\n\t * @param databaseName the name of the database being shut down\n\t */",
            "\t/**\n\t * 关闭支撑给定 {@link DataSource} 的嵌入式数据库实例。\n\t * @param dataSource 对应的 {@link DataSource}\n\t * @param databaseName 待关闭的数据库名称\n\t */",
        ),
    ],
    "EmbeddedDatabaseConfigurerDelegate.java": [
        (
            "/**\n * An {@link EmbeddedDatabaseConfigurer} delegate that can be used to customize\n * the embedded database.\n *\n * @author Stephane Nicoll\n * @since 6.2\n */",
            "/**\n * 可用于自定义嵌入式数据库的 {@link EmbeddedDatabaseConfigurer} 委托类。\n *\n * @author Stephane Nicoll\n * @since 6.2\n */",
        ),
    ],
    "EmbeddedDatabaseConfigurers.java": [
        (
            "/**\n * Maps well-known {@linkplain EmbeddedDatabaseType embedded database types}\n * to {@link EmbeddedDatabaseConfigurer} strategies.\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 6.2\n */",
            "/**\n * 将常见 {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}\n * 映射到 {@link EmbeddedDatabaseConfigurer} 策略。\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @author Sam Brannen\n * @author Stephane Nicoll\n * @since 6.2\n */",
        ),
        (
            "\t/**\n\t * Return a configurer instance for the given embedded database type.\n\t * @param type the {@linkplain EmbeddedDatabaseType embedded database type}\n\t * @return the configurer instance\n\t * @throws IllegalStateException if the driver for the specified database type is not available\n\t */",
            "\t/**\n\t * 返回给定嵌入式数据库类型对应的配置器实例。\n\t * @param type {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}\n\t * @return 配置器实例\n\t * @throws IllegalStateException 指定数据库类型的驱动不可用时\n\t */",
        ),
        (
            "\t/**\n\t * Customize the default configurer for the given embedded database type.\n\t * <p>The {@code customizer} typically uses\n\t * {@link EmbeddedDatabaseConfigurerDelegate} to customize things as necessary.\n\t * @param type the {@linkplain EmbeddedDatabaseType embedded database type}\n\t * @param customizer the customizer to return based on the default\n\t * @return the customized configurer instance\n\t * @throws IllegalStateException if the driver for the specified database type is not available\n\t */",
            "\t/**\n\t * 自定义给定嵌入式数据库类型的默认配置器。\n\t * <p>{@code customizer} 通常使用\n\t * {@link EmbeddedDatabaseConfigurerDelegate} 按需定制。\n\t * @param type {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}\n\t * @param customizer 基于默认配置器返回定制结果的函数\n\t * @return 定制后的配置器实例\n\t * @throws IllegalStateException 指定数据库类型的驱动不可用时\n\t */",
        ),
    ],
    "EmbeddedDatabaseFactoryBean.java": [
        (
            "/**\n * A subclass of {@link EmbeddedDatabaseFactory} that implements {@link FactoryBean}\n * for registration as a Spring bean. Returns the actual {@link DataSource} that\n * provides connectivity to the embedded database to Spring.\n *\n * <p>The target {@link DataSource} is returned instead of an {@link EmbeddedDatabase}\n * proxy since the {@link FactoryBean} will manage the initialization and destruction\n * lifecycle of the embedded database instance.\n *\n * <p>Implements {@link DisposableBean} to shut down the embedded database when the\n * managing Spring container is being closed.\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * {@link EmbeddedDatabaseFactory} 的子类，实现 {@link FactoryBean}\n * 以便注册为 Spring Bean。向 Spring 返回提供嵌入式数据库连接的\n * 实际 {@link DataSource}。\n *\n * <p>返回目标 {@link DataSource} 而非 {@link EmbeddedDatabase} 代理，\n * 因为 {@link FactoryBean} 将管理嵌入式数据库实例的初始化与销毁生命周期。\n *\n * <p>实现 {@link DisposableBean}，在 Spring 容器关闭时关闭嵌入式数据库。\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Set a script execution to be run in the bean destruction callback,\n\t * cleaning up the database and leaving it in a known state for others.\n\t * @param databaseCleaner the database script executor to run on destroy\n\t * @see #setDatabasePopulator\n\t * @see org.springframework.jdbc.datasource.init.DataSourceInitializer#setDatabaseCleaner\n\t */",
            "\t/**\n\t * 设置在 Bean 销毁回调中执行的脚本，\n\t * 清理数据库并使其处于已知状态供后续使用。\n\t * @param databaseCleaner 销毁时执行的数据库脚本执行器\n\t * @see #setDatabasePopulator\n\t * @see org.springframework.jdbc.datasource.init.DataSourceInitializer#setDatabaseCleaner\n\t */",
        ),
    ],
    "EmbeddedDatabaseFactoryRuntimeHints.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} implementation that registers reflection hints\n * for {@code EmbeddedDataSourceProxy#shutdown} in order to allow it to be used\n * as a bean destroy method.\n *\n * @author Sebastien Deleuze\n * @since 6.0\n */",
            "/**\n * {@link RuntimeHintsRegistrar} 实现，为 {@code EmbeddedDataSourceProxy#shutdown}\n * 注册反射提示，以便将其用作 Bean 销毁方法。\n *\n * @author Sebastien Deleuze\n * @since 6.0\n */",
        ),
    ],
    "EmbeddedDatabaseType.java": [
        (
            "/**\n * A supported embedded database type.\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @since 3.0\n */",
            "/**\n * 支持的嵌入式数据库类型。\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @since 3.0\n */",
        ),
        (
            "\t/** The <a href=\"https://hsqldb.org\">Hypersonic</a> Embedded Java SQL Database. */",
            "\t/** <a href=\"https://hsqldb.org\">Hypersonic</a> 嵌入式 Java SQL 数据库。 */",
        ),
        (
            "\t/** The <a href=\"https://h2database.com\">H2</a> Embedded Java SQL Database Engine. */",
            "\t/** <a href=\"https://h2database.com\">H2</a> 嵌入式 Java SQL 数据库引擎。 */",
        ),
        (
            "\t/** The <a href=\"https://db.apache.org/derby\">Apache Derby</a> Embedded SQL Database. */",
            "\t/** <a href=\"https://db.apache.org/derby\">Apache Derby</a> 嵌入式 SQL 数据库。 */",
        ),
    ],
    "H2EmbeddedDatabaseConfigurer.java": [
        (
            "/**\n * {@link EmbeddedDatabaseConfigurer} for an H2 embedded database instance.\n *\n * <p>Call {@link #getInstance()} to get the singleton instance of this class.\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0\n */",
            "/**\n * H2 嵌入式数据库实例的 {@link EmbeddedDatabaseConfigurer}。\n *\n * <p>调用 {@link #getInstance()} 获取本类的单例实例。\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Get the singleton {@code H2EmbeddedDatabaseConfigurer} instance.\n\t * @return the configurer instance\n\t * @throws ClassNotFoundException if H2 is not on the classpath\n\t */",
            "\t/**\n\t * 获取 {@code H2EmbeddedDatabaseConfigurer} 单例实例。\n\t * @return 配置器实例\n\t * @throws ClassNotFoundException H2 不在类路径上时\n\t */",
        ),
    ],
    "HsqlEmbeddedDatabaseConfigurer.java": [
        (
            "/**\n * {@link EmbeddedDatabaseConfigurer} for an HSQL embedded database instance.\n *\n * <p>Call {@link #getInstance()} to get the singleton instance of this class.\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @since 3.0\n */",
            "/**\n * HSQL 嵌入式数据库实例的 {@link EmbeddedDatabaseConfigurer}。\n *\n * <p>调用 {@link #getInstance()} 获取本类的单例实例。\n *\n * @author Keith Donald\n * @author Oliver Gierke\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Get the singleton {@link HsqlEmbeddedDatabaseConfigurer} instance.\n\t * @return the configurer instance\n\t * @throws ClassNotFoundException if HSQL is not on the classpath\n\t */",
            "\t/**\n\t * 获取 {@link HsqlEmbeddedDatabaseConfigurer} 单例实例。\n\t * @return 配置器实例\n\t * @throws ClassNotFoundException HSQL 不在类路径上时\n\t */",
        ),
    ],
    "OutputStreamFactory.java": [
        (
            "/**\n * Internal helper for exposing dummy OutputStreams to embedded databases\n * such as Derby, preventing the creation of a log file.\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 内部辅助类，向 Derby 等嵌入式数据库提供空 OutputStream，\n * 防止创建日志文件。\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Returns an {@link java.io.OutputStream} that ignores all data given to it.\n\t */",
            "\t/**\n\t * 返回忽略所有写入数据的 {@link java.io.OutputStream}。\n\t */",
        ),
        (
            "\t\t\t\t// ignore the output",
            "\t\t\t\t// 忽略输出",
        ),
    ],
}
