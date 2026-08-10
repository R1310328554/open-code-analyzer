package org.keycloak.protocol.oid4vc.issuance.credentialoffer.preauth;

import org.keycloak.common.VerificationException;
import org.keycloak.protocol.oid4vc.model.PreAuthCodeCtx;
import org.keycloak.provider.Provider;

/**
 * 预授权码（pre-authorized code）的生成与校验处理器。
 * <p>实现方负责在生成时嵌入 {@link PreAuthCodeCtx}，并在校验时恢复该上下文。</p>
 */
public interface PreAuthCodeHandler extends Provider {

    /**
     * 根据非敏感字段上下文生成预授权码。
     * <p>实现须将上下文嵌入码中，以便校验时完整恢复。</p>
     *
     * @param ctx 预授权码上下文（凭证类型、发放参数等非敏感信息）
     * @return 预授权码字符串
     */
    String createPreAuthCode(PreAuthCodeCtx ctx);

    /**
     * 校验预授权码并返回关联上下文。
     *
     * @param preAuthCode 客户端提交的预授权码
     * @return 校验成功后的 {@link PreAuthCodeCtx}
     * @throws VerificationException 码无效、过期或签名失败时抛出
     */
    PreAuthCodeCtx verifyPreAuthCode(String preAuthCode) throws VerificationException;
}
