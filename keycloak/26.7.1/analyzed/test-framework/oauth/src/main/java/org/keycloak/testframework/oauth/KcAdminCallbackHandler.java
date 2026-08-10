package org.keycloak.testframework.oauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.keycloak.constants.AdapterConstants;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.adapters.action.LogoutAction;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.adapters.action.TestAvailabilityAction;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Keycloak 客户端管理回调 HTTP 处理器。
 * <p>
 * 解析 JWS 签名的管理动作（登出、推送 not-before、可用性探测）并写入 {@link KcAdminInvocations}。
 */
public class KcAdminCallbackHandler implements HttpHandler {

    private final KcAdminInvocations invocations;

    /** @param kcAdminInvocations 管理回调调用记录器 */
    KcAdminCallbackHandler(KcAdminInvocations kcAdminInvocations) {
        this.invocations = kcAdminInvocations;
    }

    /**
     * 根据请求路径解析 JWS 负载并分发到对应的管理动作队列。
     *
     * @param exchange HTTP 交换对象
     * @throws IOException JWS 解析失败时抛出
     */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            JWSInput adminToken = new JWSInput(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (path.endsWith(AdapterConstants.K_LOGOUT)) {
                invocations.add(adminToken.readJsonContent(LogoutAction.class));
            } else if (path.endsWith(AdapterConstants.K_PUSH_NOT_BEFORE)) {
                invocations.add(adminToken.readJsonContent(PushNotBeforeAction.class));
            } else if (path.endsWith(AdapterConstants.K_TEST_AVAILABLE)) {
                invocations.add(adminToken.readJsonContent(TestAvailabilityAction.class));
            }
            exchange.sendResponseHeaders(204, 0);
            exchange.close();
        } catch (JWSInputException e) {
            throw new IOException(e);
        }
    }

}
