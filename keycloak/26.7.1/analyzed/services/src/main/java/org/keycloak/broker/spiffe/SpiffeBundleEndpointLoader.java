package org.keycloak.broker.spiffe;

import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JWKSUtils;

/**
 * SPIFFE Bundle 端点公钥加载器：从 trust bundle URL 获取 JWKS 并提取 JWT-SVID 签名密钥。
 * <p>优先使用 {@code use=JWT-SVID} 的 JWK，回退至 {@code use=SIG}；支持 spiffe_refresh_hint 缓存。</p>
 */
public class SpiffeBundleEndpointLoader implements PublicKeyLoader {

    private final KeycloakSession session;
    private final String bundleEndpoint;

    /** @param session Keycloak 会话 @param bundleEndpoint SPIFFE trust bundle URL */
    public SpiffeBundleEndpointLoader(KeycloakSession session, String bundleEndpoint) {
        this.session = session;
        this.bundleEndpoint = bundleEndpoint;
    }

    /** GET bundle 端点，解析 JWKS 并返回带 refresh hint 的公钥包装。 */
    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        SpiffeJSONWebKeySet jwks = SimpleHttp.create(session).doGet(bundleEndpoint).asJson(SpiffeJSONWebKeySet.class);
        PublicKeysWrapper keysWrapper = JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.JWT_SVID, true);
        if (keysWrapper.getKeys().isEmpty()) {
            keysWrapper = JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
        }
        return jwks.getSpiffeRefreshHint() == null ? keysWrapper : new PublicKeysWrapper(keysWrapper.getKeys(), jwks.getSpiffeRefreshHint());
    }

}
