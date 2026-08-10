package org.keycloak.testframework.admin;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

/**
 * 针对固定 Keycloak 基址（及可选 TLS）创建 {@link AdminClientBuilder} 的工厂，
 * 并跟踪需自动关闭的 {@link Keycloak} 实例。
 */
public class AdminClientFactory {

    private final Supplier<KeycloakBuilder> delegateSupplier;

    private final List<Keycloak> instanceToClose = new LinkedList<>();

    /** @param serverUrl Admin REST 基址（HTTP） */
    AdminClientFactory(String serverUrl) {
        delegateSupplier = () -> KeycloakBuilder.builder().serverUrl(serverUrl);
    }

    /**
     * @param serverUrl Admin REST 基址（HTTPS）
     * @param sslContext 客户端 TLS 上下文
     */
    AdminClientFactory(String serverUrl, SSLContext sslContext) {
            delegateSupplier = () ->
                    KeycloakBuilder.builder()
                            .serverUrl(serverUrl)
                            .resteasyClient(Keycloak.getClientProvider().newRestEasyClient(null, sslContext, false));
    }

    /** 基于工厂配置创建新的 AdminClientBuilder。 */
    public AdminClientBuilder create() {
        return new AdminClientBuilder(this, delegateSupplier.get());
    }

    /** 登记测试结束时需关闭的客户端。 */
    void addToClose(Keycloak keycloak) {
        instanceToClose.add(keycloak);
    }

    /** 关闭所有已登记的 Keycloak 客户端。 */
    public void close() {
        instanceToClose.forEach(Keycloak::close);
    }

}
