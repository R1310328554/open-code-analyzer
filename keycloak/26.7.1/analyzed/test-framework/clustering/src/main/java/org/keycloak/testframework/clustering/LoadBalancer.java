package org.keycloak.testframework.clustering;

import java.util.HashMap;

import org.keycloak.testframework.server.ClusteredKeycloakServer;
import org.keycloak.testframework.server.KeycloakUrls;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyResponse;
import org.jboss.logging.Logger;

/**
 * 基于 Vert.x {@link HttpProxy} 的简易负载均衡/反向代理，将请求转发到集群中指定 Keycloak 节点。
 * 默认监听 {@link #HOSTNAME}（localhost:9999）。
 */
public class LoadBalancer {

    private static final Logger LOGGER = Logger.getLogger(LoadBalancer.class);
    /** 负载均衡器对外暴露的基础 URL。 */
    public static final String HOSTNAME = "http://localhost:9999";

    private final ClusteredKeycloakServer server;
    private final HashMap<Integer, Origin> urls = new HashMap<>();
    private final Vertx vertx;
    private final HttpProxy proxy;

    private int currentNodeIndex = 0;

    /**
     * 为给定集群服务器启动本地反向代理。
     *
     * @param server 集群 Keycloak 服务器实例
     */
    public LoadBalancer(ClusteredKeycloakServer server) {
        this.server = server;

        this.vertx = Vertx.vertx();
        HttpClient proxyClient = vertx.createHttpClient();
        proxy = HttpProxy.reverseProxy(proxyClient);
        proxy.addInterceptor(new ProxyInterceptor() {
            @Override
            public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
                LOGGER.debugf("Proxy request intercepted: %s", context.request().getURI());
                return ProxyInterceptor.super.handleProxyRequest(context);
            }
        });
        node(currentNodeIndex);

        HttpServer proxyServer = vertx.createHttpServer();
        Future.await(proxyServer.requestHandler(proxy).listen(9999, "localhost"));
    }

    /** 将代理上游切换到指定节点索引。 */
    public void node(int index) {
        Origin origin = origin(index);
        currentNodeIndex = index;
        LOGGER.debugf("Setting proxy origin to: %s:%d", origin.host, origin.port);
        proxy.origin(origin.port, origin.host);
    }

    /** 返回指定节点的 {@link KeycloakUrls}（管理与应用 URL）。 */
    public KeycloakUrls nodeUrls(int index) {
        return origin(index).urls;
    }

    /** 返回集群节点数量。 */
    public int nodeCount() {
        return server.clusterSize();
    }

    /** 轮询切换到下一个集群节点。 */
    public void nextNode() {
        node((currentNodeIndex + 1) % nodeCount());
    }

    private Origin origin(int index) {
        if (index >= server.clusterSize()) {
            throw new IllegalArgumentException("Node index out of bounds. Requested nodeIndex: %d, cluster size: %d".formatted(index, server.clusterSize()));
        }
        return urls.computeIfAbsent(index, i ->
              new Origin("localhost", server.getBasePort(i), new KeycloakUrls(server.getBaseUrl(i), server.getManagementBaseUrl(i)))
        );
    }

    /** 关闭 Vert.x 实例并释放代理资源。 */
    public void close() {
        Future.await(vertx.close());
    }

    /** 代理上游节点的主机、端口与 Keycloak URL 集合。 */
    record Origin(String host, int port, KeycloakUrls urls) {
    }
}
