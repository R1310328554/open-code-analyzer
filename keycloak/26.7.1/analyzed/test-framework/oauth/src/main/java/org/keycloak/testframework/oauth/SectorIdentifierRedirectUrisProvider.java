package org.keycloak.testframework.oauth;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.Response;

import org.keycloak.util.JsonSerialization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 在 HTTP 服务器上暴露 sector identifier 重定向 URI 列表的模拟端点。
 * <p>
 * 响应 {@link #CONTEXT} 路径下的 JSON 数组，供 OIDC 客户端 sector identifier 校验使用。
 *
 * @author rmartinc
 */
public class SectorIdentifierRedirectUrisProvider implements Closeable {

    /** sector identifier 文档的 HTTP 上下文路径。 */
    public static final String CONTEXT = "/sector-identifier-redirect-uris";

    private final HttpServer httpServer;
    private final String[] sectorIdentifierRedirectUris;

    /**
     * 注册 HTTP 处理器并绑定重定向 URI 列表。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     * @param sectorIdentifierRedirectUris 要暴露的重定向 URI 数组
     */
    public SectorIdentifierRedirectUrisProvider(HttpServer httpServer, String[] sectorIdentifierRedirectUris) {
        this.httpServer = httpServer;
        this.sectorIdentifierRedirectUris = sectorIdentifierRedirectUris;
        this.httpServer.createContext(CONTEXT, new SectorIdentifierRedirectUrisHandler());
    }

    /** 从 HTTP 服务器移除本端点的上下文。 */
    @Override
    public void close() {
        httpServer.removeContext(CONTEXT);
    }

    private class SectorIdentifierRedirectUrisHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String metadata = JsonSerialization.writeValueAsString(sectorIdentifierRedirectUris);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(Response.Status.OK.getStatusCode(), metadata.length());
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(metadata.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
