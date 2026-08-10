package org.keycloak.crypto;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;

/**
 * 服务端 ECDSA JWS 验签上下文。
 * <p>验签前将 JWS 常用的 R||S 拼接格式转换为 ASN.1 DER，再委托父类校验。</p>
 */
public class ServerECDSASignatureVerifierContext extends  AsymmetricSignatureVerifierContext {
    /** @param session 当前会话 @param kid 密钥 ID @param algorithm ECDSA 算法名 */
    public ServerECDSASignatureVerifierContext(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        super(ServerAsymmetricSignatureVerifierContext.getKey(session, kid, algorithm));
    }

    /** @param key 已解析的 EC 验签公钥包装 */
    public ServerECDSASignatureVerifierContext(KeyWrapper key) {
        super(key);
    }

    @Override
    /** 将 concatenated R||S 签名转为 DER 后执行 ECDSA 验签。 */
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            int expectedSize = ECDSAAlgorithm.getSignatureLength(getAlgorithm());
            byte[] derSignature = ECDSAAlgorithm.concatenatedRSToASN1DER(signature, expectedSize);
            return super.verify(data, derSignature);
        } catch (Exception e) {
            throw new VerificationException("Signing failed", e);
        }
    }
}
