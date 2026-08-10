package org.keycloak.scim.client.authorization;

import java.io.IOException;

import org.keycloak.OAuth2Constants;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.representations.AccessTokenResponse;

import org.apache.http.HttpHeaders;

/**
 * {@link AuthorizationMethod} 的实现：通过 OAuth 2.0 客户端凭据（Client Credentials）授权类型
 * 获取访问令牌，并在请求中设置 {@code Authorization: Bearer} 头。
 *
 * @param tokenEndpoint 用于获取访问令牌的令牌端点 URL
 * @param clientId      客户端 ID
 * @param clientSecret  客户端密钥
 */
public record OAuth2Bearer(String tokenEndpoint, String clientId, String clientSecret) implements AuthorizationMethod {

    /** 获取访问令牌并写入 Bearer 授权头。 */
    @Override
    public void onBefore(SimpleHttp http, SimpleHttpRequest request) {
        try {
            AccessTokenResponse response = http.doPost(tokenEndpoint)
                    .param(OAuth2Constants.GRANT_TYPE, OAuth2Constants.CLIENT_CREDENTIALS)
                    .param(OAuth2Constants.CLIENT_ID, clientId)
                    .param(OAuth2Constants.CLIENT_SECRET, clientSecret)
                    .asJson(AccessTokenResponse.class);
            String token = response.getToken();

            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain access token", e);
        }

    }
}
