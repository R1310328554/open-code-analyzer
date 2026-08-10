package org.keycloak.testframework.server;

import java.util.concurrent.TimeoutException;

import org.keycloak.Keycloak;
import org.keycloak.common.Version;
import org.keycloak.testframework.util.MavenProjectUtil;

/**
 * 在测试 JVM 内嵌启动 Quarkus Keycloak 的 {@link KeycloakServer} 实现。
 * <p>
 * 使用 {@link Keycloak#builder()} 同进程启动，适合快速集成测试。
 */
public class EmbeddedKeycloakServer implements KeycloakServer {

    /** 就绪探针超时（秒）。 */
    private final long startTimeout;
    /** 内嵌 {@link Keycloak} 运行时句柄。 */
    private Keycloak keycloak;
    /** 当前是否启用 TLS。 */
    private boolean tlsEnabled = false;

    /** @param startTimeout 启动就绪等待超时（秒） */
    public EmbeddedKeycloakServer(long startTimeout) {
        this.startTimeout = startTimeout;
    }

    /** 装配 Provider 依赖、同进程启动 Quarkus 并等待就绪探针。 */
    @Override
    public void start(KeycloakServerConfigBuilder keycloakServerConfigBuilder, boolean tlsEnabled) {
        Keycloak.Builder builder = Keycloak.builder().setVersion(Version.VERSION);
        this.tlsEnabled = tlsEnabled;

        for(KeycloakDependency dependency : keycloakServerConfigBuilder.toDependencies()) {
            KeycloakDependency updatedDependency = MavenProjectUtil.updateDependencyDetails(dependency);
            builder.addDependency(updatedDependency.getGroupId(), updatedDependency.getArtifactId(), updatedDependency.getVersion());
        }

        keycloak = builder.start(keycloakServerConfigBuilder.toArgs());
        if (!isRunning()) {
            throw new RuntimeException("Keycloak failed to start");
        }

        ReadinessProbe.waitUntilReady(this, startTimeout);
    }

    /** 停止内嵌 Keycloak 运行时。 */
    @Override
    public void stop() {
        try {
            keycloak.stop();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} 根据 TLS 返回 8443 或 8080 基址。 */
    @Override
    public String getBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:8443";
        } else {
            return "http://localhost:8080";
        }
    }

    /** {@inheritDoc} 内嵌模式管理端口为 9001。 */
    @Override
    public String getManagementBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:9001";
        } else {
            return "http://localhost:9001";
        }
    }

    /** 通过是否存在 "Quarkus Main Thread" 判断 Quarkus 是否已启动。 */
    private boolean isRunning() {
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t.getName().equals("Quarkus Main Thread")) {
                return true;
            }
        }
        return false;
    }

}
