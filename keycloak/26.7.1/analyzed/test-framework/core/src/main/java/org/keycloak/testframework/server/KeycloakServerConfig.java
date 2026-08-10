package org.keycloak.testframework.server;

/**
 * 托管 Keycloak 服务器的声明式配置接口。
 * <p>
 * 实现类通过 {@link KeycloakServerConfigBuilder} 以链式方式描述启动参数、特性与依赖。
 */
public interface KeycloakServerConfig {

    /**
     * 在共享构建器上应用本配置。
     *
     * @param config 服务器配置构建器
     * @return 修改后的构建器，便于继续链式调用
     */
    KeycloakServerConfigBuilder configure(KeycloakServerConfigBuilder config);

}
