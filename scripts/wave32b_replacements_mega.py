"""Chinese OCA + JavaDoc replacements for Spring Framework 7.0.8 wave32b batch [9:18]."""

from __future__ import annotations

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "LazyConnectionDataSourceProxy.java": [
        (
            "/**\n * 目标数据源的代理，延迟获取实际的 JDBC 连接，即直到第一次创建语句时才获取。连接初始化属性（例如自动提交模式、事务隔离和只读模式）将被保留，并在获取实际连接（如果有）后立即\n * 应用于实际的 JDBC 连接。因此，如果没有创建任何语句，提交和回滚调用将被忽略。从 6.1.2 开始，除了常规目标数据源之外，还特别支持在只读事务期间使用 {@link #s\n * etReadOnlyDataSource read-only DataSource}。\n * <p> 此数据源代理允许避免从池中获取 JDBC 连接，除非确实有必要。 JDBC 事务控制无需从池中获取连接或与数据库通信即可进行；这将在第一次创建 JDBC 语句时延迟完成\n * 。作为奖励，这允许在路由数据源（例如 {@link org.springframework.jdbc.datasource.lookup.IsolationLevelDataS\n * ourceRouter}）中考虑事务同步只读标志和/或隔离级别。\n * <p><b>如果您同时配置 LazyConnectionDataSourceProxy 和\n * TransactionAwareDataSourceProxy，请确保后者是最外层的 DataSource。</b> 在这种情况下，数据访问代码将与事务感知\n * DataSource 通信，而事务感知 DataSource 将与 LazyConnectionDataSourceProxy 一起工作。从 6.1.2\n * 开始，LazyConnectionDataSourceProxy 将在第一次连接访问时初始化其默认连接特性；要在启动时强制执行此操作，请调用 {@link\n * #checkDefaultConnectionProperties()}。\n * <p>L 物理 JDBC 连接的快速获取在通用事务划分环境中特别有用。它允许您在可能执行数据访问的所有方法上划分事务，如果没有发生实际的数据访问，则不会造成性能损失。\n * <p>此数据源代理为您提供类似于 JTA 和事务性 JNDI 数据源（由 Jakarta EE 服务器提供）的行为，甚至使用\n * DataSourceTransactionManager 或 HibernateTransactionManager 等本地事务策略。它不会使用 Spring 的\n * JtaTransactionManager 作为事务策略来增加价值。\n * 对于 Hibernate 的只读操作，还建议使用 <p>Lazy 获取 JDBC 连接，特别是当在二级缓存中解析结果的机会很高时。这根本不需要与数据库进行此类只读操作的通信。对\n * 于非事务性读取，您将获得相同的效果，但 JDBC 连接的延迟获取允许您仍然在事务中执行读取。\n * 在 6.2.6 的 <p>A 中，此数据源代理还会在连接同时关闭的超时情况下抑制回滚尝试。\n * <p><b>NOTE:</b> 此数据源代理需要返回包装的连接（实现 {@link ConnectionProxy} 接口），以便处理实际 JDBC 连接的延迟获取。使用\n * {@link Connection#unwrap} 检索本机 JDBC 连接。",
            "/**\n * 目标 {@link DataSource} 的代理，延迟获取实际 JDBC Connection，即直到首次创建 Statement 时才获取。\n * 连接初始化属性（自动提交模式、事务隔离、只读模式）将被保留，并在获取实际 Connection 后立即应用。\n * 因此，若未创建任何 Statement，commit 与 rollback 调用将被忽略。\n * 自 6.1.2 起，除常规目标 DataSource 外，还支持在只读事务期间使用 {@link #setReadOnlyDataSource read-only DataSource}。\n *\n * <p>本代理可避免在确实不需要时从连接池获取 JDBC Connection：JDBC 事务控制可在不取连接、不与数据库通信的情况下进行，\n * 首次创建 JDBC Statement 时才真正完成。此外，可在路由 DataSource（如\n * {@link org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter}）中\n * 考虑事务同步的只读标志和/或隔离级别。\n *\n * <p><b>若同时配置 LazyConnectionDataSourceProxy 与 TransactionAwareDataSourceProxy，\n * 请确保后者为最外层 DataSource。</b> 此时数据访问代码与事务感知 DataSource 交互，\n * 后者再与 LazyConnectionDataSourceProxy 协作。自 6.1.2 起，LazyConnectionDataSourceProxy 在首次 Connection 访问时\n * 初始化默认连接特性；若要在启动时强制执行，请调用 {@link #checkDefaultConnectionProperties()}。\n *\n * <p>在通用事务划分环境中，延迟获取物理 JDBC Connection 尤其有益：可在所有可能执行数据访问的方法上划分事务，\n * 而实际未发生数据访问时不会产生性能损失。\n *\n * <p>本代理提供类似 JTA 与 Jakarta EE 服务器所提供的事务性 JNDI DataSource 的行为，\n * 即使使用 DataSourceTransactionManager 或 HibernateTransactionManager 等本地事务策略也适用。\n * 若使用 Spring 的 JtaTransactionManager 作为事务策略则收益有限。\n *\n * <p>对 Hibernate 只读操作也建议延迟获取 JDBC Connection，尤其当二级缓存命中概率较高时，\n * 此类只读操作完全无需与数据库通信。非事务性读取效果相同，但延迟获取仍允许在事务中执行读取。\n *\n * <p>自 6.2.6 起，若超时期间 Connection 已被关闭，本代理还会抑制 rollback 尝试。\n *\n * <p><b>注意：</b>本代理须返回包装 Connection（实现 {@link ConnectionProxy}），以支持延迟获取实际 JDBC Connection。\n * 使用 {@link Connection#unwrap} 获取原生 JDBC Connection。",
        ),
        (
            "\t/**\n\t * 获取 Log（`Log`）。\n\t */",
            "\t/** 日志记录器。 */",
        ),
        (
            "\t/** 来源相关状态（`readOnlyDataSource`）。 */",
            "\t/** 只读事务期间使用的目标 DataSource。 */",
        ),
        (
            "\t/** `defaultAutoCommit`：该类的成员状态。 */",
            "\t/** 默认自动提交模式（尚未获取目标 Connection 时使用）。 */",
        ),
        (
            "\t/** 事务相关状态（`defaultTransactionIsolation`）。 */",
            "\t/** 默认事务隔离级别（尚未获取目标 Connection 时使用）。 */",
        ),
    ],
    "ScriptUtils.java": [
        (
            "\t/**\n\t * 方法 `logSqlWarnings`：完成本类中与「log Sql Warnings」相关的职责。\n\t */",
            "\t/**\n\t * 以 debug 级别记录 Statement 上的 SQLWarning 链。\n\t */",
        ),
        (
            "\t/**\n\t * 方法 `readScript`：完成本类中与「read Script」相关的职责。\n\t */",
            "\t/**\n\t * 从 {@link LineNumberReader} 逐行读取脚本并拼接为字符串。\n\t */",
        ),
        (
            "\t/**\n\t * 方法 `appendSeparatorToScriptIfNecessary`：完成本类中与「append Separator To Script If Necessary」相关的职责。\n\t */",
            "\t/**\n\t * 若语句分隔符末尾含空白，则在脚本末尾追加必要部分。\n\t */",
        ),
        (
            "\t * 启动：s With Any（方法 `startsWithAny`）。",
            "\t * 检查脚本在指定偏移处是否以任一前缀开头。",
        ),
    ],
    "RdbmsOperation.java": [
        (
            "\t/**\n\t */\n\tprotected final Log logger = LogFactory.getLog(getClass());",
            "\t/** 子类可用的 Logger。 */\n\tprotected final Log logger = LogFactory.getLog(getClass());",
        ),
        (
            "\t/**\n\t */\n\tprivate JdbcTemplate jdbcTemplate = new JdbcTemplate();",
            "\t/** 用于执行 SQL 的底层 {@link JdbcTemplate}。 */\n\tprivate JdbcTemplate jdbcTemplate = new JdbcTemplate();",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean updatableResults = false;",
            "\t/** 是否返回可更新 ResultSet。 */\n\tprivate boolean updatableResults = false;",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean returnGeneratedKeys = false;",
            "\t/** 是否返回生成键。 */\n\tprivate boolean returnGeneratedKeys = false;",
        ),
        (
            "\t/** 名称相关状态（`generatedKeysColumnNames`）。 */",
            "\t/** 生成键列名数组。 */",
        ),
        (
            "\t/** `sql`：该类的成员状态。 */",
            "\t/** 本操作的 SQL 语句。 */",
        ),
    ],
    "SqlQuery.java": [
        (
            "\t/**\n\t * 方法 `queryByNamedParam`：完成本类中与「query By Named Param」相关的职责。\n\t */",
            "\t/**\n\t * 按命名参数执行查询的核心方法，所有命名参数执行均经由此方法。\n\t * @param paramMap 命名参数映射\n\t * @param context 传给 {@code mapRow} 回调的上下文\n\t * @param queryFunction 实际查询函数（list 或 stream）\n\t */",
        ),
    ],
    "SQLErrorCodeSQLExceptionTranslator.java": [
        (
            "\t/** `userProvidedErrorCodesFilePresent`：该类的成员状态。 */",
            "\t/** 是否已提供用户自定义错误码文件。 */",
        ),
        (
            "\t/** `sqlErrorCodes`：该类的成员状态。 */",
            "\t/** 本转换器使用的 {@link SQLErrorCodes}。 */",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Translate（方法 `doTranslate`）。\n\t */",
            "\t/**\n\t * 基于 SQL 错误码将 {@link SQLException} 翻译为 Spring {@link DataAccessException}。\n\t * @param task 发生异常的任务描述\n\t * @param sql 导致异常的 SQL（若有）\n\t * @param ex 原始 SQLException\n\t */",
        ),
        (
            "\t/**\n\t * 方法 `logTranslation`：完成本类中与「log Translation」相关的职责。\n\t */",
            "\t/**\n\t * 在 debug 级别记录 SQLException 翻译信息。\n\t * @param custom 是否为自定义翻译\n\t */",
        ),
    ],
    "DefaultLobHandler.java": [
        (
            "\t/**\n\t * 获取 Log（`Log`）。\n\t */",
            "\t/** 日志记录器。 */",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean wrapAsLob = false;",
            "\t/** 是否将字节数组/String 包装为 JDBC Blob/Clob 提交。 */\n\tprivate boolean wrapAsLob = false;",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean streamAsLob = false;",
            "\t/** 是否以 JDBC 4.0 LOB 流方式提交二进制/字符流。 */\n\tprivate boolean streamAsLob = false;",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean createTemporaryLob = false;",
            "\t/** 是否通过 JDBC 4.0 创建临时 Blob/Clob。 */\n\tprivate boolean createTemporaryLob = false;",
        ),
    ],
    "ResultSetWrappingSqlRowSet.java": [
        (
            "\t/** 结果相关状态（`resultSet`）。 */",
            "\t/** 被包装的 {@link ResultSet}（通常为断开连接的 CachedRowSet）。 */",
        ),
        (
            "\t/** `rowSetMetaData`：该类的成员状态。 */",
            "\t/** 本 RowSet 的元数据。 */",
        ),
        (
            "\t/** `columnLabelMap`：该类的成员状态。 */",
            "\t/** 列标签到列索引的映射。 */",
        ),
        (
            " * <p>Note：从 JDBC 4.0 开始",
            " * <p>注意：自 JDBC 4.0 起",
        ),
        (
            "\t/**\n\t */\n\tprivate static final long serialVersionUID",
            "\t/** 序列化版本 UID。 */\n\tprivate static final long serialVersionUID",
        ),
        (
            "\t/**\n\t * @see java.sql.ResultSetMetaData#getCatalogName(int)\n\t */",
            "\t/**\n\t * 返回本 RowSet 的元数据。\n\t * @see java.sql.ResultSetMetaData#getCatalogName(int)\n\t */",
        ),
    ],
}
