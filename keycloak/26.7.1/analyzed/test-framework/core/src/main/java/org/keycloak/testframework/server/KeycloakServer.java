package org.keycloak.testframework.server;

import org.keycloak.testframework.config.Config;

/**
 * Keycloak 集成测试服务器的生命周期契约。
 * <p>
 * 实现类负责启动/停止服务器并提供 HTTP 与管理端基址；
 * 由 {@link AbstractKeycloakServerSupplier} 及其实现类在测试中托管。
 */
public interface KeycloakServer {

    /**
     * 按给定配置启动服务器。
     *
     * @param keycloakServerConfigBuilder 启动命令与选项构建器
     * @param tlsEnabled 是否启用 HTTPS
     */
    void start(KeycloakServerConfigBuilder keycloakServerConfigBuilder, boolean tlsEnabled);

    /** 停止服务器并释放资源。 */
    void stop();

    /** @return 面向客户端的 HTTP(S) 基址 */
    String getBaseUrl();

    /** @return Quarkus 管理/健康检查端点基址 */
    String getManagementBaseUrl();

    /**
     * 读取 {@code kc.test.keycloak-server.hot.deploy} 配置，是否启用 Provider 热部署。
     *
     * @return 配置为 {@code true} 时启用
     */
    static boolean getDependencyHotDeployEnabled() {
        return Boolean.parseBoolean(Config.getValueTypeConfig(KeycloakServer.class, "hot.deploy", "false", String.class));
    }

}
