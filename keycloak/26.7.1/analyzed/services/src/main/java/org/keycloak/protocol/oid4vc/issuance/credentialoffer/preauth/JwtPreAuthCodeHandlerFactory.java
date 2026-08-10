package org.keycloak.protocol.oid4vc.issuance.credentialoffer.preauth;

import org.keycloak.models.KeycloakSession;

/**
 * {@link JwtPreAuthCodeHandler} 的 Provider 工厂。
 * <p>Provider ID 为 {@link #PROVIDER_ID}（{@code jwt-pre-auth-code-handler}）。</p>
 */
public class JwtPreAuthCodeHandlerFactory implements PreAuthCodeHandlerFactory {

    /** JWT 预授权码 Handler 的 Provider 标识。 */
    public static final String PROVIDER_ID = "jwt-pre-auth-code-handler";


    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 创建 JWT 预授权码 Handler 实例。 */
    @Override
    public JwtPreAuthCodeHandler create(KeycloakSession session) {
        return new JwtPreAuthCodeHandler(session);
    }
}
