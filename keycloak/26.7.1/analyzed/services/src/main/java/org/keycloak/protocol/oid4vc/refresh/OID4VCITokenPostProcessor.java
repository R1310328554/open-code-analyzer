package org.keycloak.protocol.oid4vc.refresh;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;
import org.keycloak.protocol.oidc.encode.AccessTokenContext;
import org.keycloak.protocol.oidc.encode.TokenContextEncoderProvider;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.RefreshToken;

/**
 * OID4VCI 令牌后处理器。
 * <p>对 oid4vci refresh token 清除 sessionId，并将 access token audience 限制为凭证端点。</p>
 */
public class OID4VCITokenPostProcessor implements TokenPostProcessor {

    /** Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public OID4VCITokenPostProcessor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void process(TokenPostProcessorContext context) {
        AccessToken accessToken = context.accessToken();
        RefreshToken refreshToken = context.refreshToken();

        if (refreshToken == null || !OID4VCIRefreshTokenProviderFactory.PROVIDER_ID.equals(refreshToken.getProvider())) {
            return;
        }

        if (shouldUseTransientSession(accessToken)) {
            // 临时会话下 refresh/access token 不应携带 sessionId
            refreshToken.setSessionId(null);
            accessToken.setSessionId(null);
        }

        // 将 audience 限制为凭证端点 URL
        String credentialEndpoint = OID4VCIssuerWellKnownProvider.getCredentialsEndpoint(session.getContext());
        accessToken.audience(credentialEndpoint);
    }


    /** 判断是否使用临时会话（无 sessionId）。 */
    private boolean shouldUseTransientSession(AccessToken accessToken) {
        TokenContextEncoderProvider encoder = session.getProvider(TokenContextEncoderProvider.class);
        return (encoder.getTokenContextFromTokenId(accessToken.getId()).getSessionType() == AccessTokenContext.SessionType.TRANSIENT);
    }
}
