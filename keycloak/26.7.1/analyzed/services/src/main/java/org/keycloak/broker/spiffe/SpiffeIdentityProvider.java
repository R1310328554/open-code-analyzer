package org.keycloak.broker.spiffe;

import java.nio.charset.StandardCharsets;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.authenticators.client.AbstractJWTClientValidator;
import org.keycloak.authentication.authenticators.client.FederatedJWTClientValidator;
import org.keycloak.broker.provider.ClientAssertionIdentityProvider;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.JsonWebToken;

import org.jboss.logging.Logger;

/**
 * SPIFFE 联邦客户端断言身份代理：实现 OAuth SPIFFE JWT SVID 客户端认证草案。
 * <p>参见 https://datatracker.ietf.org/doc/draft-schwenkschuster-oauth-spiffe-client-auth/</p>
 * <p>与常规 client assertion 的主要差异：</p>
 * <ul>
 * <li>使用 {@code jwt-spiffe} 客户端断言类型</li>
 * <li>{@code iss} 可选，SPIFFE ID 含 trust domain 替代 issuer</li>
 * <li>{@code jti} 可选，SPIFFE 实现可能复用/缓存 token</li>
 * <li>{@code sub} 为 {@code spiffe://trust-domain/workload-identity} 格式</li>
 * <li>公钥从 SPIFFE bundle 端点获取，JWKS 含 spiffe_sequence/spiffe_refresh_hint，JWK 可能无 alg</li>
 * </ul>
 */
public class SpiffeIdentityProvider implements ClientAssertionIdentityProvider<SpiffeIdentityProviderConfig> {

    private static final Logger LOGGER = Logger.getLogger(SpiffeIdentityProvider.class);

    private final KeycloakSession session;
    private final SpiffeIdentityProviderConfig config;

    /** @param session Keycloak 会话 @param config SPIFFE 身份代理配置 */
    public SpiffeIdentityProvider(KeycloakSession session, SpiffeIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    /** @return SPIFFE 配置 */
    @Override
    public SpiffeIdentityProviderConfig getConfig() {
        return config;
    }

    /** 校验 SPIFFE JWT SVID：trust domain 前缀、签名与可选过期上限。 */
    @Override
    public boolean verifyClientAssertion(ClientAuthenticationFlowContext context) throws Exception {
        FederatedJWTClientValidator validator = new FederatedJWTClientValidator(context, this::verifySignature,
                    null, config.getAllowedClockSkew(), true);
        validator.setExpectedClientAssertionType(SpiffeConstants.CLIENT_ASSERTION_TYPE);

        if (config.getFederatedClientAssertionMaxExpiration() != 0) {
            validator.setMaximumExpirationTime(config.getFederatedClientAssertionMaxExpiration());
        }

        String trustedDomain = config.getTrustDomain();

        JsonWebToken token = validator.getState().getToken();
        if (!token.getSubject().startsWith(trustedDomain + "/")) {
            throw new RuntimeException("Invalid trust-domain");
        }

        return validator.validate();
    }

    /** 从 SPIFFE bundle 端点加载公钥并验证 JWS 签名。 */
    private boolean verifySignature(AbstractJWTClientValidator validator) {
        try {
            String bundleEndpoint = config.getBundleEndpoint();
            JWSInput jws = validator.getState().getJws();
            JWSHeader header = jws.getHeader();
            String kid = header.getKeyId();
            String alg = header.getRawAlgorithm();

            String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(validator.getContext().getRealm().getId(), config.getInternalId());

            PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
            KeyWrapper publicKey = keyStorage.getPublicKey(modelKey, kid, alg, new SpiffeBundleEndpointLoader(session, bundleEndpoint));

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, alg);
            if (signatureProvider == null) {
                LOGGER.debugf("Failed to verify token, signature provider not found for algorithm %s", alg);
                return false;
            }

            return signatureProvider.verifier(publicKey).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());
        } catch (Exception e) {
            LOGGER.debug("Failed to verify token signature", e);
            return false;
        }
    }

    @Override
    public void close() {
    }

}
