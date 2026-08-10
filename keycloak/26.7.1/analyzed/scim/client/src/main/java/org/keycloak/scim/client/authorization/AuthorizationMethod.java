package org.keycloak.scim.client.authorization;

import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;

/**
 * SCIM 客户端授权策略接口。
 * <p>在 HTTP 请求发出前注入认证信息（如 Bearer Token）。</p>
 */
public interface AuthorizationMethod {

    /**
     * 在请求发送前设置授权头或其他认证信息。
     *
     * @param http    {@link SimpleHttp} 实例，用于获取令牌等辅助操作
     * @param request 即将发送的 {@link SimpleHttpRequest}
     */
    void onBefore(SimpleHttp http, SimpleHttpRequest request);
}
