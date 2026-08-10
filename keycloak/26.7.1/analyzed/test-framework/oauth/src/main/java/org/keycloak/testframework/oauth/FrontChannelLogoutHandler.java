package org.keycloak.testframework.oauth;

import java.io.IOException;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.UriUtils;
import org.keycloak.representations.LogoutToken;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * front-channel 登出 HTTP 处理器。
 * <p>
 * 解析查询参数中的 {@code sid} 与 {@code iss}，构造 {@link LogoutToken} 并记录到 {@link KcAdminInvocations}。
 */
class FrontChannelLogoutHandler implements HttpHandler {

    private final KcAdminInvocations invocations;

    /** @param invocations 用于记录登出令牌的调用收集器 */
    FrontChannelLogoutHandler(KcAdminInvocations invocations) {
        this.invocations = invocations;
    }

    /** {@inheritDoc} 解析 front-channel 登出参数并返回 HTTP 200。 */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        MultivaluedHashMap<String, String> params = UriUtils.decodeQueryString(exchange.getRequestURI().getRawQuery());

        LogoutToken token = new LogoutToken();
        token.setSid(params.getFirst("sid"));
        token.issuer(params.getFirst("iss"));
        invocations.add(token);

        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
