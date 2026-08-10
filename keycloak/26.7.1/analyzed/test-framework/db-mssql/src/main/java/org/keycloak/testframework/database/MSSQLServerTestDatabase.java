package org.keycloak.testframework.database;

import java.util.List;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.LogManager;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * SQL Server Testcontainers 测试数据库实现。
 * <p>
 * 启动 Express 版容器、创建 UTF-8 排序规则数据库，并启用 READ_COMMITTED_SNAPSHOT。
 */
class MSSQLServerTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(MSSQLServerTestDatabase.class);

    /** 配置别名与容器镜像逻辑名。 */
    public static final String NAME = "mssql";

    @SuppressWarnings("resource")
    /** {@inheritDoc} 创建接受许可协议的 MSSQL Express 容器并设置 SA 密码。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new MSSQLServerContainer<>(DockerImageName.parse(ContainerImages.getContainerImageName(NAME))).withPassword(getPassword()).withEnv("MSSQL_PID", "Express").acceptLicense();
    }

    /** {@inheritDoc} MSSQL 容器不支持 {@code withUsername}/{@code withDatabase}，留空实现。 */
    @Override
    public void withDatabaseAndUser(String database, String username, String password) {
        // MSSQLServerContainer 不支持 withUsername 与 withDatabase
    }

    @Override
    public String getDatabaseVendor() {
        return NAME;
    }

    /** {@inheritDoc} SQL Server 默认系统管理员账户 {@code sa}。 */
    @Override
    public String getUsername() {
        return "sa";
    }

    /** {@inheritDoc} 与容器启动时一致的 SA 密码。 */
    @Override
    public String getPassword() {
        return "vEry$tron9Pwd";
    }

    /** {@inheritDoc} 追加禁用集成安全/加密及 Unicode 参数，并指定 databaseName。 */
    @Override
    public String getJdbcUrl(boolean internal) {
        return super.getJdbcUrl(internal) + ";integratedSecurity=false;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=false;databaseName=" + getDatabase();
    }

    /** {@inheritDoc} 启动前临时将 JDBC 连接日志级别降为 ERROR，避免预登录告警刷屏。 */
    @Override
    public void start(DatabaseConfiguration config) {
        // 避免 SQL Server JDBC 预登录阶段 WARNING 日志干扰测试输出
        java.util.logging.Logger mssqlLogger = LogManager.getLogManager().getLogger("com.microsoft.sqlserver.jdbc.internals.SQLServerConnection");
        java.util.logging.Level level = mssqlLogger.getLevel();
        try {
            mssqlLogger.setLevel(Level.ERROR);
            super.start(config);
        } finally {
            mssqlLogger.setLevel(level);
        }
    }

    /** {@inheritDoc} 容器就绪后执行 sqlcmd 创建数据库并开启 READ_COMMITTED_SNAPSHOT。 */
    @Override
    public List<String> getPostStartCommand() {
        return List.of("/opt/mssql-tools18/bin/sqlcmd", "-U", "sa", "-P", getPassword(), "-No", "-Q", "CREATE DATABASE " + getDatabase() + " COLLATE Latin1_General_100_CI_AS_SC_UTF8; " +
                // MSSQL 建议开启 READ_COMMITTED_SNAPSHOT 以减少死锁
                "ALTER DATABASE " + getDatabase() + " SET READ_COMMITTED_SNAPSHOT ON;");
    }

    /** {@inheritDoc} 返回本模块 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
