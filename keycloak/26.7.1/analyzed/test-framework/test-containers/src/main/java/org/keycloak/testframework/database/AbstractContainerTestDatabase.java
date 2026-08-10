package org.keycloak.testframework.database;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.logging.JBossContainerLogConsumer;

import org.jboss.logging.Logger;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * 基于 Testcontainers 的 JDBC 测试数据库抽象实现。
 * <p>
 * 负责启动容器、应用初始化脚本、执行启动后命令，并向 Keycloak 提供 {@code db-*} 配置项。
 */
public abstract class AbstractContainerTestDatabase implements TestDatabase {

    protected boolean reuse;

    protected JdbcDatabaseContainer<?> container;
    protected DatabaseConfiguration config;

    /**
     * 根据配置启动数据库容器，可选执行启动后命令。
     *
     * @param config 数据库名称、初始化脚本等配置
     */
    public void start(DatabaseConfiguration config) {
        this.config = config;

        String reuseProp = Config.getValueTypeFQN(TestDatabase.class, "reuse");
        boolean reuseConfigured = Config.get(reuseProp, false, Boolean.class);
        if (config.isPreventReuse() && reuseConfigured) {
            getLogger().warnf("Ignoring '%s' as test explicitly prevents it", reuseProp);
            this.reuse = false;
        } else {
            this.reuse = reuseConfigured;
        }

        container = createContainer();
        container = container.withStartupTimeout(Duration.ofMinutes(10))
                .withLogConsumer(new JBossContainerLogConsumer(Logger.getLogger("managed.db." + getDatabaseVendor())))
                .withReuse(reuse)
                .withInitScript(config.getInitScript());
        withDatabaseAndUser(getDatabase(), getUsername(), getPassword());
        container.start();

        try {
            List<String> postStartCommand = getPostStartCommand();
            if (postStartCommand != null) {
                getLogger().tracev("Running post start command: {0}", String.join(" ", postStartCommand));
                Container.ExecResult execResult = container.execInContainer(postStartCommand.toArray(new String[0]));
                String stdout = execResult.getStdout();
                String stderr = execResult.getStderr();
                getLogger().tracev(stdout);
                getLogger().tracev(stderr);
                if (execResult.getExitCode() != 0) {
                    throw new RuntimeException("Post start command failed with exit code: " + execResult.getExitCode() + ". stdout: " + stdout + ". stderr: " + stderr);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 配置容器内的数据库名、用户名与密码。
     *
     * @param database 数据库名称
     * @param username JDBC 用户名
     * @param password JDBC 密码
     */
    public void withDatabaseAndUser(String database, String username, String password) {
        container.withDatabaseName(database);
        container.withUsername(username);
        container.withPassword(password);
    }

    /** 停止容器；若启用了 reuse 则保留容器供后续测试复用。 */
    public void stop() {
        if (!reuse) {
            container.stop();
        }
    }

    /** {@inheritDoc} 返回主机侧可访问的 JDBC 连接配置。 */
    @Override
    public Map<String, String> serverConfig() {
        return serverConfig(false);
    }

    /**
     * 生成 Keycloak 服务器所需的数据库配置项。
     *
     * @param internal 为 {@code true} 时使用 Docker 网络内部 IP 与端口
     * @return 包含 {@code db}、{@code db-url}、{@code db-username}、{@code db-password} 的映射
     */
    public Map<String, String> serverConfig(boolean internal) {
        return Map.of(
                "db", getDatabaseVendor(),
                "db-url", getJdbcUrl(internal),
                "db-username", getUsername(),
                "db-password", getPassword()
        );
    }

    /** 创建具体数据库厂商的 Testcontainers 实例。 */
    public abstract JdbcDatabaseContainer<?> createContainer();

    /** 容器启动后可选执行的 shell 命令，默认不执行。 */
    public List<String> getPostStartCommand() {
        return null;
    }

    /** 返回逻辑数据库名，未配置时默认为 {@code keycloak}。 */
    public String getDatabase() {
        return config.getDatabase() == null ? "keycloak" : config.getDatabase();
    }

    /** 返回 JDBC 用户名，默认为 {@code keycloak}。 */
    public String getUsername() {
        return "keycloak";
    }

    /** 返回 JDBC 密码，默认为 {@code keycloak}。 */
    public String getPassword() {
        return "keycloak";
    }

    /**
     * 构建 JDBC URL。
     *
     * @param internal 为 {@code true} 时将主机映射地址替换为容器网络 IP
     * @return JDBC 连接 URL
     */
    public String getJdbcUrl(boolean internal) {
        var url = container.getJdbcUrl();
        if (internal) {
            var ip = container.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
            return url.replace(container.getHost() + ":" + container.getFirstMappedPort(), ip + ":" + container.getExposedPorts().get(0));
        }
        return url;
    }

    /** 返回 Keycloak {@code db} 配置项对应的数据库厂商标识。 */
    public abstract String getDatabaseVendor();

    /** 返回本实现使用的 JBoss Logging 日志器。 */
    public abstract Logger getLogger();
}
