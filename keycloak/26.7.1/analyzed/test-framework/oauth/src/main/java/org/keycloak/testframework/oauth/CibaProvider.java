package org.keycloak.testframework.oauth;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuth2Constants;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.protocol.oidc.grants.ciba.channel.AuthenticationChannelRequest;
import org.keycloak.protocol.oidc.grants.ciba.channel.HttpAuthenticationChannelProvider;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.ClientNotificationEndpointRequest;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * CIBA（客户端发起的反向认证）测试模拟端点提供者。
 * <p>
 * 在嵌入式 {@link HttpServer} 上注册认证通道请求与客户端推送通知处理器，
 * 供集成测试断言 Keycloak 发出的 CIBA 相关 HTTP 调用。
 *
 * @author rmartinc
 */
public class CibaProvider implements Closeable {

    /** 接收 CIBA 认证通道请求的 HTTP 上下文路径。 */
    public static final String CONTEXT_REQUEST_AUTH_CHANNEL = "/ciba/request-authentication-channel";
    /** 接收 CIBA 客户端推送通知的 HTTP 上下文路径。 */
    public static final String CONTEXT_PUSH_NOTIFICATION = "/ciba/push-ciba-client-notification";
    /** 无 binding_message 时用于索引认证通道请求的内部占位键。 */
    public static final String DUMMY_KEY = "channel_request_dummy_key";

    private final HttpServer httpServer;
    private final ConcurrentMap<String, CibaAuthenticationChannelRequest> authenticationChannelRequests = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications = new ConcurrentHashMap<>();

    /**
     * 在指定 HTTP 服务器上注册 CIBA 认证通道与推送通知端点。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     */
    public CibaProvider(HttpServer httpServer) {
        this.httpServer = httpServer;
        httpServer.createContext(CONTEXT_REQUEST_AUTH_CHANNEL, (HttpExchange exchange) -> this.handleRequestAuthChannel(exchange));
        httpServer.createContext(CONTEXT_PUSH_NOTIFICATION, (HttpExchange exchange) -> this.handlePushNotification(exchange));
    }

    /**
     * 按 binding message 检索已记录的认证通道请求。
     *
     * @param bindingMessage 绑定消息；为 {@code null} 时使用 {@link #DUMMY_KEY}
     * @return 对应的认证通道请求，若尚未收到则返回 {@code null}
     */
    public CibaAuthenticationChannelRequest getAuthChannel(String bindingMessage) {
        if (bindingMessage == null) {
            bindingMessage = DUMMY_KEY;
        }
        return authenticationChannelRequests.get(bindingMessage);
    }

    /**
     * 取出并移除指定令牌对应的 CIBA 客户端推送通知。
     *
     * @param clientNotificationToken 客户端通知令牌
     * @return 已存储的通知请求；不存在时返回空对象
     */
    public ClientNotificationEndpointRequest getPushedCibaClientNotification(String clientNotificationToken) {
        ClientNotificationEndpointRequest request = cibaClientNotifications.remove(clientNotificationToken);
        if (request == null) {
            request = new ClientNotificationEndpointRequest();
        }
        return request;
    }

    /** 从 HTTP 服务器移除认证通道上下文。 */
    @Override
    public void close() {
        httpServer.removeContext(CONTEXT_REQUEST_AUTH_CHANNEL);
    }

    private int handleRequestAuthChannel(HttpExchange exchange) throws IOException {
        String token = extractTokenStringFromAuthHeader(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "Failed to parse bearer token");
        }

        AccessToken bearerToken;
        try {
            bearerToken = new JWSInput(token).readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "Failed to parse bearer token");
        }

        // 必填参数校验
        String authenticationChannelId = bearerToken.getId();
        if (authenticationChannelId == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "missing parameter : " + HttpAuthenticationChannelProvider.AUTHENTICATION_CHANNEL_ID);
        }

        // 解析请求体为认证通道请求对象
        AuthenticationChannelRequest request;
        try (InputStream is = exchange.getRequestBody()) {
            request = JsonSerialization.readValue(is, AuthenticationChannelRequest.class);
        } catch (IOException e) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "Invalid request");
        }

        String loginHint = request.getLoginHint();
        if (loginHint == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "missing parameter : " + CibaGrantType.LOGIN_HINT);
        }

        if (request.getConsentRequired() == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "missing parameter : " + CibaGrantType.IS_CONSENT_REQUIRED);
        }

        String scope = request.getScope();
        if (scope == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "missing parameter : " + OAuth2Constants.SCOPE);
        }

        // 可选参数；用于触发测试用故意错误
        String bindingMessage = request.getBindingMessage();
        if (bindingMessage != null && bindingMessage.equals("GODOWN")) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "intentional error : GODOWN");
        }

        // binding_message 可选；每个测试方法仅支持一条无 binding_message 的 CIBA 流程
        if (bindingMessage == null) {
            bindingMessage = DUMMY_KEY;
        }

        authenticationChannelRequests.put(bindingMessage, new CibaAuthenticationChannelRequest(request, token));

        exchange.sendResponseHeaders(Status.CREATED.getStatusCode(), -1);
        return Status.CREATED.getStatusCode();
    }

    private int handlePushNotification(HttpExchange exchange) throws IOException {
        String token = extractTokenStringFromAuthHeader(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "Failed to parse bearer token");
        }

        // 解析请求体为客户端通知端点请求
        ClientNotificationEndpointRequest request;
        try (InputStream is = exchange.getRequestBody()) {
            request = JsonSerialization.readValue(is, ClientNotificationEndpointRequest.class);
        } catch (IOException e) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "Invalid request");
        }

        ClientNotificationEndpointRequest existing = cibaClientNotifications.putIfAbsent(token, request);
        if (existing != null) {
            return errorRespose(exchange, Status.BAD_REQUEST.getStatusCode(), "There is already entry for clientNotification "
                    + token + ". Make sure to cleanup after previous tests.");
        }

        exchange.sendResponseHeaders(Status.NO_CONTENT.getStatusCode(), -1);
        return Status.NO_CONTENT.getStatusCode();
    }

    private int errorRespose(HttpExchange exchange, int code, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        return code;
    }

    private String extractTokenStringFromAuthHeader(String authHeader) {
        if (authHeader == null) {
            return null;
        }

        int indexOfSpace = authHeader.indexOf(' ');

        if (indexOfSpace <= 0) {
            return null;
        }

        return authHeader.substring(indexOfSpace + 1);
    }

    /** 封装 CIBA 认证通道 HTTP 请求及其 Bearer 令牌。 */
    public static class CibaAuthenticationChannelRequest {

        private String bearerToken;
        private AuthenticationChannelRequest request;

        /**
         * @param request 解析后的认证通道请求体
         * @param bearerToken Authorization 头中的 Bearer 令牌
         */
        public CibaAuthenticationChannelRequest(AuthenticationChannelRequest request, String bearerToken) {
            this.request = request;
            this.bearerToken = bearerToken;
        }

        /** 设置 Bearer 令牌。 */
        public void setBearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
        }

        /** @return Bearer 令牌字符串 */
        public String getBearerToken() {
            return bearerToken;
        }

        /** 设置认证通道请求体。 */
        public void setRequest(AuthenticationChannelRequest request) {
            this.request = request;
        }

        /** @return 认证通道请求体 */
        public AuthenticationChannelRequest getRequest() {
            return request;
        }
    }
}
