package org.keycloak.crypto;

import org.keycloak.common.VerificationException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 客户端 ECDSA JWS 签名验证提供者。
 * <p>为 ES256/ES384/ES512 等算法创建 {@link ClientECDSASignatureVerifierContext}。</p>
 */
public class ECDSAClientSignatureVerifierProvider implements ClientSignatureVerifierProvider {
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** ECDSA 签名算法标识（如 ES256）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm ECDSA 算法名 */
    public ECDSAClientSignatureVerifierProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 创建客户端 ECDSA 验签上下文。 */
    public SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException {
        return new ClientECDSASignatureVerifierContext(session, client, input);
    }

    @Override
    /** @return 绑定的 ECDSA 算法名 */
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    /** @return 恒为 true */
    public boolean isAsymmetricAlgorithm() {
        return true;
    }
}
