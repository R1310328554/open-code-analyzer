package org.keycloak.crypto;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;

/**
 * 服务端 ECDSA 签名提供者。
 * <p>支持按会话密钥或 {@link KeyWrapper} 创建 ES 系列签名与验签上下文。</p>
 */
public class ECDSASignatureProvider implements SignatureProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** ECDSA 算法标识（ES256/ES384/ES512）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm ECDSA 算法名 */
    public ECDSASignatureProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 使用 Realm 活动 EC 密钥创建签名上下文。 */
    public SignatureSignerContext signer() throws SignatureException {
        return new ServerECDSASignatureSignerContext(session, algorithm);
    }

    @Override
    /** 使用指定 EC 密钥创建签名上下文。 */
    public SignatureSignerContext signer(KeyWrapper key) throws SignatureException {
        SignatureProvider.checkKeyForSignature(key, algorithm, KeyType.EC);
        return new ServerECDSASignatureSignerContext(key);
    }

    @Override
    /** 按 kid 解析 EC 公钥并创建验签上下文。 */
    public SignatureVerifierContext verifier(String kid) throws VerificationException {
        return new ServerECDSASignatureVerifierContext(session, kid, algorithm);
    }

    @Override
    /** 使用指定 EC 公钥创建验签上下文。 */
    public SignatureVerifierContext verifier(KeyWrapper key) throws VerificationException {
        SignatureProvider.checkKeyForVerification(key, algorithm, KeyType.EC);
        return new ServerECDSASignatureVerifierContext(key);
    }

    @Override
    /** @return 恒为 true */
    public boolean isAsymmetricAlgorithm() {
        return true;
    }
}
