package org.keycloak.testframework.realm;

/**
 * 托管测试客户端的声明式配置接口。
 * <p>
 * 实现类通过 {@link #configure(ClientBuilder)} 向构建器追加或覆盖客户端属性。
 */
public interface ClientConfig {

    /**
     * 将本配置应用到 {@link ClientBuilder}。
     *
     * @param client 客户端构建器
     * @return 配置后的构建器
     */
    ClientBuilder configure(ClientBuilder client);

}
