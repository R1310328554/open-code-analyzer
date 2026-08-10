package org.keycloak.testframework.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * JDK 内置 {@link com.sun.net.httpserver.HttpServer} 的响应发送辅助类。
 */
public class HttpServerUtil {

    /**
     * 发送带可选响应头与二进制体的 HTTP 响应，并在 finally 中关闭 exchange。
     *
     * @param exchange 当前 HTTP 交换
     * @param statusCode HTTP 状态码
     * @param headers 响应头，可为 {@code null}
     * @param bodyBytes 响应体字节，可为 {@code null}
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, Map<String, List<String>> headers, byte[] bodyBytes) {

        try {
            long length = bodyBytes != null ? bodyBytes.length : 0;
            exchange.sendResponseHeaders(statusCode, length);
            if (headers != null) {
                Headers responseHeaders = exchange.getResponseHeaders();
                for (var entry : headers.entrySet()) {
                    responseHeaders.put(entry.getKey(), entry.getValue());
                }
            }

            if (bodyBytes != null) {
                try (var os = exchange.getResponseBody()) {
                    os.write(bodyBytes);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            exchange.close();
        }
    }

    /** 以 UTF-8 编码字符串响应体并委托给字节数组重载。 */
    public static void sendResponse(HttpExchange exchange, int statusCode, Map<String, List<String>> headers, String body) {
        byte[] bytes = body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
        sendResponse(exchange, statusCode, headers, bytes);
    }

    /** 发送无响应体的 HTTP 响应。 */
    public static void sendResponse(HttpExchange exchange, int statusCode, Map<String, List<String>> headers) {
        sendResponse(exchange, statusCode, headers, (byte[]) null);
    }
}
