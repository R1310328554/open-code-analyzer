package org.keycloak.testframework.oauth;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.Response.Status;

import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.util.JsonSerialization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 在 {@code /cimd/metadata} 路径上暴露 CIMD（客户端标识元数据）文档的模拟端点。
 * <p>
 * 返回 {@link OIDCClientRepresentation} JSON，支持自定义 HTTP 状态与 Cache-Control 头。
 *
 * @author rmartinc
 */
public class CimdProvider implements Closeable {

    /** CIMD 元数据文档的 HTTP 上下文路径。 */
    public static final String CONTEXT = "/cimd/metadata";

    private final HttpServer httpServer;
    private final OIDCClientRepresentation client;
    private Status responseStatus;
    private String cacheControlHeader;

    /**
     * 注册 HTTP 处理器并绑定客户端元数据表示。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     * @param client 要暴露的 OIDC 客户端元数据
     */
    public CimdProvider(HttpServer httpServer, OIDCClientRepresentation client) {
        this.httpServer = httpServer;
        this.client = client;
        this.responseStatus = Status.OK;
        httpServer.createContext(CONTEXT, new OIDCClientRepresentationHandler());
    }

    /** @return 当前绑定的客户端元数据表示 */
    public OIDCClientRepresentation getRepresentation() {
        return client;
    }

    /** 设置元数据端点返回的 HTTP 状态。 */
    public void setStatus(Status status) {
        this.responseStatus = status;
    }

    /** 设置成功响应中的 Cache-Control 头。 */
    public void setCacheControlHeader(String cacheControlHeader) {
        this.cacheControlHeader = cacheControlHeader;
    }

    /** 从 HTTP 服务器移除 CIMD 元数据上下文。 */
    @Override
    public void close() {
        httpServer.removeContext(CONTEXT);
    }

    /** 根据当前状态与缓存头返回 OIDC 客户端元数据 JSON。 */
    private class OIDCClientRepresentationHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (exchange) {
                switch (responseStatus) {
                    case OK -> {
                        String metadata = JsonSerialization.writeValueAsString(client);
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        if (cacheControlHeader != null) {
                            exchange.getResponseHeaders().add("Cache-Control", cacheControlHeader);
                        }
                        exchange.sendResponseHeaders(Status.OK.getStatusCode(), metadata.length());
                        try (OutputStream out = exchange.getResponseBody()) {
                            out.write(metadata.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    case NOT_MODIFIED -> exchange.sendResponseHeaders(Status.NOT_MODIFIED.getStatusCode(), -1);
                    case NOT_FOUND -> exchange.sendResponseHeaders(Status.NOT_FOUND.getStatusCode(), -1);
                    default -> exchange.sendResponseHeaders(Status.BAD_REQUEST.getStatusCode(), -1);
                }
            }
        }
    }
}
