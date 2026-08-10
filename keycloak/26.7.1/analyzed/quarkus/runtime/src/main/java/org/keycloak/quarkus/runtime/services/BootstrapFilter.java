package org.keycloak.quarkus.runtime.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

/**
 * Pre-matching request filter that returns a 503 Service Unavailable response while the server bootstrap is in progress.
 */
 * 服务器引导（bootstrap）完成前拦截所有请求并返回 503 Service Unavailable 的预匹配过滤器。

@ApplicationScoped
public class BootstrapFilter {
    private static final Logger LOG = Logger.getLogger(BootstrapFilter.class);

    /** 实例创建时的启动时间戳，用于计算 Retry-After。 */
    private final long startup;
    /** 引导完成后置为 true，后续请求快速放行。 */
    private boolean ready;
    /** 是否已记录“引导期间收到请求”的警告日志（仅一次）。 */
    private volatile boolean warningLogged;

    @Inject
    QuarkusKeycloakSessionFactory factory;

    public BootstrapFilter() {
        startup = System.currentTimeMillis();
    }

    /** 引导未完成时返回 503 及 Retry-After；完成后返回 null 继续过滤链。 */
    @ServerRequestFilter(priority = 1, preMatching = true)
    public Response filter(ContainerRequestContext ignored) {
        if (ready) {
            // JVM 分支预测可优化此路径，避免反复读取 volatile 字段
            return null;
        }
        if (factory.isBootstrapCompleted()) {
            // 返回 null 表示继续正常处理请求
            ready = true;
            return null;
        }
        if (!warningLogged) {
            synchronized (this) {
                if (!warningLogged) {
                    // 最多记录一次警告，提醒管理员在客户端投诉前检查就绪探针
                    LOG.warn("Request received during bootstrapping, returning a 503 error. Use the readiness health endpoint to ensure the service is ready before forwarding requests to the service.");
                }
                warningLogged = true;
            }
        }

        // 退避重试：等待时间与当前已启动时长相当，最短 1 秒、最长 60 秒
        long retry = Math.min(Math.max((System.currentTimeMillis() - startup) / 1000, 1), 60);
        // 返回 503 Service Unavailable
        return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.TEXT_PLAIN)
                .entity("Bootstrap in progress. Retry in " + retry + " seconds.")
                .header(HttpHeaders.RETRY_AFTER, retry)
                .header("Refresh", retry)
                .build();

    }
}
