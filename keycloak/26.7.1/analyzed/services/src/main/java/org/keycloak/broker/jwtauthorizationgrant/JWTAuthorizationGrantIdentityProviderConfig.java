package org.keycloak.broker.jwtauthorizationgrant;

import org.keycloak.broker.oidc.IssuerValidation;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.RealmModel;

import static org.keycloak.broker.oidc.OIDCIdentityProviderConfig.JWKS_URL;
import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * JWT 授权授予 IdP 配置模型：组合 {@link JWTAuthorizationGrantConfig} 与 issuer 校验。
 */
public class JWTAuthorizationGrantIdentityProviderConfig extends IdentityProviderModel implements JWTAuthorizationGrantConfig, IssuerValidation {

    /** 默认构造。 */
    public JWTAuthorizationGrantIdentityProviderConfig() {
    }

    /** 从已有 {@link IdentityProviderModel} 复制配置。 */
    public JWTAuthorizationGrantIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    @Override
    /** 校验 JWKS URL 与 issuer 配置合法性。 */
    public void validate(RealmModel realm) {
        checkUrl(realm.getSslRequired(), getJwksUrl(), JWKS_URL);
        validateIssuer(realm, IdentityProviderType.JWT_AUTHORIZATION_GRANT);
    }
}
