package org.keycloak.testframework.infinispan;

import java.util.Map;

import org.keycloak.testframework.logging.JBossContainerLogConsumer;
import org.keycloak.testframework.util.ContainerImages;

import org.infinispan.testcontainers.InfinispanContainer;
import org.jboss.logging.Logger;

/**
 * 基于 Testcontainers 启动外部 Infinispan 服务器的测试实现。
 * <p>
 * 固定暴露 11222 端口，并生成 Keycloak 远程缓存连接所需的 {@link #serverConfig()} 选项。
 */
public class InfinispanExternalServer extends InfinispanContainer implements InfinispanServer {

    /** Infinispan 服务器认证用户名。 */
    private static final String USER = "keycloak";
    /** Infinispan 服务器认证密码。 */
    private static final String PASSWORD = "Password1!";
    /** Keycloak 连接远程缓存时使用的主机地址。 */
    private static final String HOST = "127.0.0.1";

    /**
     * 使用 {@link org.keycloak.testframework.util.ContainerImages} 中的镜像名创建服务器实例。
     *
     * @return 已配置认证与端口映射的 Infinispan 容器
     */
    public static InfinispanExternalServer create() {
        return new InfinispanExternalServer(ContainerImages.getContainerImageName("infinispan"));
    }

    /**
     * 使用指定 Docker 镜像启动 Infinispan 并绑定固定端口。
     *
     * @param dockerImageName 完整 Docker 镜像引用
     */
    @SuppressWarnings("resource")
    private InfinispanExternalServer(String dockerImageName) {
        super(dockerImageName);
        withUser(USER);
        withPassword(PASSWORD);
        withLogConsumer(new JBossContainerLogConsumer(Logger.getLogger("managed.infinispan")));
        addFixedExposedPort(11222, 11222);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 返回远程缓存主机、凭据、TLS 与负载均衡轮询间隔等 Keycloak 启动选项。
     */
    @Override
    public Map<String, String> serverConfig() {
        return Map.of(
                "cache-remote-host", HOST,
                "cache-remote-username", USER,
                "cache-remote-password", PASSWORD,
                "cache-remote-tls-enabled", "false",
                "spi-cache-embedded-default-site-name", "ispn",
                "spi-load-balancer-check-remote-poll-interval", "500",
                "spi-cache-remote-default-client-intelligence", "BASIC"
        );
    }
}
