package org.keycloak.crypto;

import org.keycloak.models.KeycloakSession;

/**
 * 服务端 ECDSA JWS 签名上下文。
 * <p>复用 {@link ServerAsymmetricSignatureSignerContext#getKey} 解析 Realm 活动 EC 密钥。</p>
 */
public class ServerECDSASignatureSignerContext extends ECDSASignatureSignerContext {

    /** @param session 当前会话 @param algorithm ECDSA 算法名（如 ES256） */
    public ServerECDSASignatureSignerContext(KeycloakSession session, String algorithm) throws SignatureException {
        super(ServerAsymmetricSignatureSignerContext.getKey(session, algorithm));
    }

    /** @param key 已解析的 EC 签名私钥包装 */
    public ServerECDSASignatureSignerContext(KeyWrapper key) {
        super(key);
    }
}
