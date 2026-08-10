package org.keycloak.testframework.database;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.keycloak.testframework.config.Config;

/**
 * 远程数据库 {@link TestDatabase} 实现。
 * <p>
 * 不本地启动数据库进程，仅从配置组装 Keycloak 服务器所需的 JDBC 连接参数。
 */
public class RemoteTestDatabase implements TestDatabase {

    /** {@inheritDoc} 远程库由外部托管，本地无需启动。 */
    @Override
    public void start(DatabaseConfiguration databaseConfiguration) {
        // 无操作
    }

    /** {@inheritDoc} 远程库由外部托管，本地无需停止。 */
    @Override
    public void stop() {
        // 无操作
    }

    /** 返回配置中的数据库厂商标识（{@code db}）。 */
    private String getDatabaseVendor() {
        return getRequiredDbConfigOption("vendor");
    }

    /** 返回 JDBC URL。 */
    private String getJdbcUrl() {
        return getRequiredDbConfigOption("url");
    }

    /** 返回数据库用户名。 */
    private String getUsername() {
        return getRequiredDbConfigOption("user");
    }

    /** 返回数据库密码。 */
    private String getPassword() {
        return getRequiredDbConfigOption("password");
    }

    /** 返回可选的 JDBC 驱动类名。 */
    private String getDatabaseDriver() {
        return Config.getValueTypeConfig(TestDatabase.class, "driver", null, String.class);
    }

    /**
     * 读取远程数据库必填配置项。
     *
     * @param option 配置键名
     * @return 配置值
     * @throws NoSuchElementException 缺少必填项时抛出
     */
    private static String getRequiredDbConfigOption(String option) {
        String value = Config.getValueTypeConfig(TestDatabase.class, option, null, String.class);
        if (value == null) {
            throw new NoSuchElementException("Missing required config for a remote DB: " + Config.getValueTypeFQN(TestDatabase.class, option));
        }
        return value;
    }

    /** {@inheritDoc} 组装 {@code db}、{@code db-url}、凭据及可选驱动配置。 */
    @Override
    public Map<String, String> serverConfig() {
        Map<String, String> serverConfig = new HashMap<>(Map.of(
                "db", getDatabaseVendor(),
                "db-url", getJdbcUrl(),
                "db-username", getUsername(),
                "db-password", getPassword()
        ));
        if (getDatabaseDriver() != null) {
            serverConfig.put("db-driver", getDatabaseDriver());
        }

        return serverConfig;
    }
}
