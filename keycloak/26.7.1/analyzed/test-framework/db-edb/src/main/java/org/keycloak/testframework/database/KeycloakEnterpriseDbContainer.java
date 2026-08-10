package org.keycloak.testframework.database;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Keycloak 测试用 EnterpriseDB（PostgreSQL 协议）Testcontainers 容器。
 * <p>
 * 配置默认库名、凭据与 5432 端口，并通过 PostgreSQL JDBC 驱动连接。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class KeycloakEnterpriseDbContainer extends JdbcDatabaseContainer<KeycloakEnterpriseDbContainer> {
    /** 容器内 PostgreSQL 数据库名。 */
    private String databaseName = "keycloak";
    /** 默认数据库用户名。 */
    private String username = "enterprisedb";
    /** 默认数据库密码。 */
    private String password = "password";
    /** PostgreSQL 默认监听端口。 */
    private static final int PORT = 5432;

    /** @param dockerImageName EnterpriseDB Docker 镜像引用 */
    public KeycloakEnterpriseDbContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    /** {@inheritDoc} 使用 PostgreSQL JDBC 驱动。 */
    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    /** {@inheritDoc} 构建指向映射端口的 JDBC URL。 */
    @Override
    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", getHost(), getMappedPort(PORT), getDatabaseName());
    }

    /** 设置 PG 环境变量并暴露数据库端口。 */
    @Override
    protected void configure() {
        addEnv("PGDATABASE", getDatabaseName());
        addEnv("PGUSER", getUsername());
        addEnv("PGPASSWORD", getPassword());
        addExposedPort(PORT);
    }

    /** {@inheritDoc} 健康检查 SQL。 */
    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public KeycloakEnterpriseDbContainer withUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public KeycloakEnterpriseDbContainer withPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public KeycloakEnterpriseDbContainer withDatabaseName(String dbName) {
        this.databaseName = dbName;
        return this;
    }

    /** {@inheritDoc} EDB 容器不支持 URL 参数，始终抛出异常。 */
    @Override
    public KeycloakEnterpriseDbContainer withUrlParam(String paramName, String paramValue) {
        throw new UnsupportedOperationException();
    }
}
