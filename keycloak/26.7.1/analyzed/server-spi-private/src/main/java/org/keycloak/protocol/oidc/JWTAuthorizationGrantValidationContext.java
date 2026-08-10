package org.keycloak.protocol.oidc;

import java.util.Set;

import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.JsonWebToken;


/**
 * JWT 授权授予（{@code urn:ietf:params:oauth:grant-type:jwt-bearer}）校验上下文。
 * <p>封装断言字符串、解析后的 {@link JsonWebToken}、JWS 输入及 scope 限制。</p>
 */
public interface JWTAuthorizationGrantValidationContext {

    /** @return 原始 JWT 断言字符串 */
    String getAssertion();

    /** @return 解析后的 JWT 载荷 */
    JsonWebToken getJWT();

    /** @return JWS 解析输入（含签名） */
    JWSInput getJws();

    /** @return 请求中的 scope 参数字符串 */
    String getScopeParam();

    /** @return 受限 scope 集合 */
    Set<String> getRestrictedScopes();

    /** @param restrictedScopes 设置受限 scope 集合 */
    void setRestrictedScopes(Set<String> restrictedScopes);

    /** @return JWT {@code iss} 声明 */
    default String getIssuer() {
        return getJWT().getIssuer();
    }

    /** @return JWT {@code sub} 声明 */
    default String getSubject() {
        return getJWT().getSubject();
    }
}
