package org.keycloak.admin.client;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.client.ClientBuilder;

import org.keycloak.admin.client.spi.ResteasyClientClassicProvider;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

/**
 * RestEasy {@link ClientBuilder} 的包装工具类（已弃用）。
 * <p>
 * 请改用 {@link ResteasyClientClassicProvider#createClientBuilder()}。
 *
 * @deprecated 使用 {@link ResteasyClientClassicProvider#createClientBuilder()} 替代
 */
@Deprecated
public class ClientBuilderWrapper {

    /**
     * 创建配置了 SSL 上下文的 RestEasy 客户端构建器。
     *
     * @param sslContext SSL 上下文，可为 {@code null} 以使用默认上下文
     * @param disableTrustManager 是否禁用信任管理器（仅用于开发环境）
     * @return 配置完成的 {@link ClientBuilder} 实例
     */
    public static ClientBuilder create(SSLContext sslContext, boolean disableTrustManager) {
        ResteasyClientBuilderImpl result = ResteasyClientClassicProvider.createClientBuilder();
        result.sslContext(sslContext);
        if (disableTrustManager) {
            result.disableTrustManager();
        }
        return result;
    }

}
