package org.keycloak.protocol.oid4vc.issuance.keybinding;

import java.util.Map;
import java.util.Optional;

import org.keycloak.OAuth2Constants;
import org.keycloak.broker.provider.TrustMaterialRequest;
import org.keycloak.broker.provider.TrustMaterialResolver;
import org.keycloak.constants.OID4VCIConstants;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 从客户端配置的 trust-material IdP 加载 attestation 校验所需可信密钥的 {@link AttestationKeyResolver} 实现。
 * <p>IdP 别名列表来自客户端属性 {@link OID4VCIConstants#OID4VCI_ATTESTER_TRUST_IDPS_ATTR}，再通过 {@link TrustMaterialResolver} 按 kid/算法/issuer 解析 JWK。</p>
 */
public class TrustedAttestationKeyResolver implements AttestationKeyResolver {

    private static final Logger logger = Logger.getLogger(TrustedAttestationKeyResolver.class);

    /** 当前 Keycloak 会话，用于访问客户端上下文与 broker 组件。 */
    private final KeycloakSession session;

    /** @param session 当前 Keycloak 会话 */
    public TrustedAttestationKeyResolver(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 从客户端配置的 trust-material IdP 加载 attestation 校验用可信密钥。
     * <p>IdP 别名通过客户端属性 {@link OID4VCIConstants#OID4VCI_ATTESTER_TRUST_IDPS_ATTR} 配置。</p>
     *
     * @return 解析到的可信 JWK；未找到时返回 {@code null}
     */
    @Override
    public JWK resolveKey(String kid, Map<String, Object> header, Map<String, Object> payload) {
        ClientModel client = session.getContext().getClient();
        if (client == null) {
            throw new IllegalStateException("Cannot load trust-material IdP aliases because client is null");
        }

        String trustIdpsConfig = client.getAttribute(OID4VCIConstants.OID4VCI_ATTESTER_TRUST_IDPS_ATTR);
        if (StringUtil.isBlank(trustIdpsConfig)) {
            logger.warnf("No trust-material IdP aliases configured for client: %s", client.getClientId());
            return null;
        }

        String algorithm = header != null ? (String) header.get(JWK.ALGORITHM) : null;
        String issuer = payload != null ? (String) payload.get(OAuth2Constants.ISSUER) : null;

        TrustMaterialRequest request = TrustMaterialRequest.builder()
                .kid(kid)
                .algorithm(algorithm)
                .issuer(issuer)
                .build();

        Optional<JWK> jwk = new TrustMaterialResolver().resolveKey(session, trustIdpsConfig, request);
        if (jwk.isEmpty()) {
            logger.debugf("Key with kid '%s' not found in configured trusted attester keys", kid);
        }

        return jwk.orElse(null);
    }
}
