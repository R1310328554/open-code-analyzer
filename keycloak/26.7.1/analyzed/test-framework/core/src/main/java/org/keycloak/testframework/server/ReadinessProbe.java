package org.keycloak.testframework.server;

import java.net.HttpURLConnection;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 轮询服务器端点直至就绪，同时支持 HTTP 与 HTTPS 连接。
 * <p>
 * 测试环境通常未启用 {@code /health/ready}，因此以 master realm 可访问性作为就绪信号。
 */
public final class ReadinessProbe {

    /** 单次 HTTP 连接与读取超时（毫秒）。 */
    private static final int CONNECTION_TIMEOUT_MILLIS = Math.toIntExact(Duration.ofSeconds(5).toMillis());
    /** 就绪轮询间隔（毫秒）。 */
    private static final long POLL_INTERVAL_MILLIS = Duration.ofMillis(500).toMillis();

    private ReadinessProbe() {
    }

    /**
     * 等待单节点 {@link KeycloakServer} 就绪。
     *
     * @param server 托管服务器实例
     * @param timeout 超时时间（秒）
     */
    public static void waitUntilReady(KeycloakServer server, long timeout) {
        waitUntilReady(index -> server.getBaseUrl(), 1, timeout);
    }

    /**
     * 等待集群中每个节点的基址均可访问。
     *
     * @param baseUrlFunction 按节点索引返回基址的函数
     * @param clusterSize 集群节点数
     * @param timeout 每个节点的超时时间（秒）
     */
    public static void waitUntilReady(IntFunction<String> baseUrlFunction, int clusterSize, long timeout) {
        var sslContext = createTrustAllSslContext();
        for (int i = 0; i < clusterSize; i++) {
            // 多数测试未启用 /health/ready，改用 master realm 探测
            var url = baseUrlFunction.apply(i) + "/realms/master";
            waitUntilReady(url, sslContext, timeout);
        }
    }

    private static void waitUntilReady(String url, SSLContext sslContext, long timeout) {
        var deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeout);
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
                if (connection instanceof HttpsURLConnection https) {
                    https.setSSLSocketFactory(sslContext.getSocketFactory());
                    https.setHostnameVerifier((hostname, session) -> true);
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
                connection.setReadTimeout(CONNECTION_TIMEOUT_MILLIS);
                connection.setRequestMethod("GET");

                try {
                    if (connection.getResponseCode() == 200) {
                        return;
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                // 服务器尚未可用，稍后重试
            }

            try {
                //noinspection BusyWait
                Thread.sleep(POLL_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server readiness", e);
            }
        }
        throw new IllegalStateException("Server did not become ready within " + timeout + " seconds: " + url);
    }

    private static SSLContext createTrustAllSslContext() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, null);
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create trust-all SSLContext", e);
        }
    }
}
