package org.keycloak.testframework.server;

import java.net.ConnectException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;

import org.keycloak.it.utils.Maven;
import org.keycloak.testframework.config.Config;

import static java.lang.System.out;

/**
 * 连接本地已手动或脚本启动的 Keycloak 进程的 {@link KeycloakServer} 实现。
 * <p>
 * 不负责启动或停止进程，仅在未检测到监听端口时打印启动命令并等待。
 */
public class RemoteKeycloakServer implements KeycloakServer {

    private final long startTimeout;

    private boolean tlsEnabled = false;

    private String kcwCommand;

    /**
     * @param startTimeout 就绪探测超时时间（秒）
     */
    public RemoteKeycloakServer(long startTimeout) {
        this.startTimeout = startTimeout;
    }

    /** {@inheritDoc} — 检测远程进程，必要时输出启动说明并等待就绪。 */
    @Override
    public void start(KeycloakServerConfigBuilder keycloakServerConfigBuilder, boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
        kcwCommand = Config.getValueTypeConfig(KeycloakServer.class, "kcw", null, String.class);
        if (!verifyRunningKeycloak()) {
            if (kcwCommand != null) {
                printStartupInstructionsKcw(keycloakServerConfigBuilder);
            } else {
                printStartupInstructionsManual(keycloakServerConfigBuilder);
            }
            waitForStartup();
        }
        ReadinessProbe.waitUntilReady(this, startTimeout);
    }

    /** {@inheritDoc} — 远程模式不停止外部进程。 */
    @Override
    public void stop() {
    }

    /** {@inheritDoc} — 返回本地默认 HTTP/HTTPS 应用端口。 */
    @Override
    public String getBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:8443";
        } else {
            return "http://localhost:8080";
        }
    }

    /** {@inheritDoc} — 返回本地默认管理/指标端口。 */
    @Override
    public String getManagementBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:9000";
        } else {
            return "http://localhost:9000";
        }
    }

    /** 打印手动启动 Keycloak 所需的 CLI 命令与 provider 列表。 */
    private void printStartupInstructionsManual(KeycloakServerConfigBuilder config) {
        out.println("Remote Keycloak server is not running on " + getBaseUrl() + ", please start Keycloak with:");
        out.println();
        out.println(String.join(" \\\n", config.toArgs()));
        out.println();

        Set<KeycloakDependency> dependencies = config.toDependencies();
        if (!dependencies.isEmpty()) {
            out.println("Requested providers:");
            for (KeycloakDependency d : dependencies) {
                out.println("* " + d.getGroupId() + ":" + d.getArtifactId());
            }
            out.println();
        }
    }

    /** 打印使用 {@code kcw} 包装脚本启动时的命令与环境变量提示。 */
    private void printStartupInstructionsKcw(KeycloakServerConfigBuilder config) {
        out.println("Remote Keycloak server is not running on " + getBaseUrl() + ", please start Keycloak with:");
        out.println();

        Set<KeycloakDependency> dependencies = config.toDependencies();
        if (!dependencies.isEmpty()) {
            String dependencyPaths = dependencies.stream().map(d -> Maven.resolveArtifact(d.getGroupId(), d.getArtifactId()).toString()).collect(Collectors.joining(","));
            out.println("KCW_PROVIDERS=" + dependencyPaths + " \\");
        }

        out.println("kcw " + kcwCommand + " " + String.join(" \\\n", config.toArgs()));
        out.println();
    }

    /** 尝试连接基址以判断 Keycloak 是否已在监听。 */
    private boolean verifyRunningKeycloak() {
        try {
            new URL(getBaseUrl()).openConnection().connect();
            return true;
        } catch (ConnectException e) {
            return false;
        } catch (SSLException ignored) {
            // if the kc server is running with https, it is not this class' responsibility to check the certificate
            // we're just checking that keycloak is running
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 在固定窗口内轮询，直至检测到 Keycloak 开始监听。 */
    private void waitForStartup() {
        long waitUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        while (!verifyRunningKeycloak() && System.currentTimeMillis() < waitUntil) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
