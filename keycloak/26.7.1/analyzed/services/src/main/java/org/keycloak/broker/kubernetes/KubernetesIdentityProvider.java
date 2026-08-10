package org.keycloak.broker.kubernetes;

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

import org.jboss.logging.Logger;

/**
 * Kubernetes 客户端断言身份提供者：验证集群 ServiceAccount JWT 客户端断言。
 */
public class KubernetesIdentityProvider implements ClientAssertionIdentityProvider<KubernetesIdentityProviderConfig> {

    /** 日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(KubernetesIdentityProvider.class);

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** Kubernetes IdP 配置。 */
    private final KubernetesIdentityProviderConfig config;

    /** @param session Keycloak 会话
     * @param config 身份提供者配置 */
    public KubernetesIdentityProvider(KeycloakSession session, KubernetesIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    @Override
    /** 使用 {@link FederatedJWTClientValidator} 校验 Kubernetes 客户端 JWT 断言。 */
    public boolean verifyClientAssertion(ClientAuthenticationFlowContext context) throws Exception {
        FederatedJWTClientValidator validator = new FederatedJWTClientValidator(context, this::verifySignature, config.getIssuer(), config.getAllowedClockSkew(), true);
        if (config.getFederatedClientAssertionMaxExpiration() != 0) {
            validator.setMaximumExpirationTime(config.getFederatedClientAssertionMaxExpiration());
        } else {
            validator.setMaximumExpirationTime(3600); // Kubernetes 默认令牌有效期 1 小时
        }
        return validator.validate();
    }

    /** 从 Kubernetes JWKS 加载公钥并验证 JWS 签名。 */
    private boolean verifySignature(AbstractJWTClientValidator validator) {
        try {
            JWSInput jws = validator.getState().getJws();
            JWSHeader header = jws.getHeader();
            String kid = header.getKeyId();
            String alg = header.getRawAlgorithm();

            String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(validator.getContext().getRealm().getId(), config.getInternalId());
            PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
            KeyWrapper publicKey = keyStorage.getPublicKey(modelKey, kid, alg, new KubernetesJwksEndpointLoader(session, config.getIssuer()));

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
    /** @return 强类型配置 */
    public KubernetesIdentityProviderConfig getConfig() {
        return config;
    }

    @Override
    /** 无资源需释放。 */
    public void close() {

    }
}
