package org.keycloak.representations;

import org.keycloak.TokenCategory;

/**
 * 授权响应 JWT（如 PAR 或混合流返回的 authorization response token）。
 * <p>
 * 令牌类别为 {@link org.keycloak.TokenCategory#AUTHORIZATION_RESPONSE}。
 */
public class AuthorizationResponseToken extends JsonWebToken{

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.AUTHORIZATION_RESPONSE;
    }
}
