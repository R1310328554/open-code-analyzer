package org.keycloak.protocol.oid4vc.issuance.requiredactions;

import org.keycloak.authentication.actiontoken.DefaultActionToken;
import org.keycloak.representations.idm.oid4vc.VerifiableCredentialOfferActionConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 凭证发放（Credential Offer）必需操作的动作令牌。
 * <p>携带 {@link VerifiableCredentialOfferActionConfig} 与可选重定向 URI，用户通过邮件/链接完成 offer 相关必需操作后回到 OID4VCI 流程。</p>
 */
public class CredentialOfferActionToken extends DefaultActionToken {

    /** 动作令牌类型标识。 */
    public static final String TOKEN_TYPE = "credential-offer";
    /** JSON 序列化字段：必需操作配置。 */
    private static final String JSON_FIELD_REQUIRED_ACTIONS = "acconf";
    /** JSON 序列化字段：操作完成后的重定向 URI。 */
    private static final String JSON_FIELD_REDIRECT_URI = "reduri";

    /** 凭证发放必需操作配置（序列化为 {@link #JSON_FIELD_REQUIRED_ACTIONS}）。 */
    @JsonProperty(JSON_FIELD_REQUIRED_ACTIONS)
    private VerifiableCredentialOfferActionConfig actionConfig;

    /** 操作完成后的重定向 URI。 */
    @JsonProperty(JSON_FIELD_REDIRECT_URI)
    private String redirectUri;

    /**
     * 构造凭证发放动作令牌。
     * @param userId 目标用户 ID
     * @param absoluteExpirationInSecs 绝对过期时间（Unix 秒）
     * @param actionConfig 必需操作配置
     * @param redirectUri 完成后重定向 URI
     * @param clientId 签发目标客户端 ID
     */
    public CredentialOfferActionToken(String userId, int absoluteExpirationInSecs, VerifiableCredentialOfferActionConfig actionConfig, String redirectUri, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        setActionConfig(actionConfig);
        setRedirectUri(redirectUri);
        this.issuedFor = clientId;
    }

    /**
     * 构造带邮箱的凭证发放动作令牌（用于邮件链接）。
     * @param userId 目标用户 ID
     * @param email 用户邮箱
     * @param absoluteExpirationInSecs 绝对过期时间（Unix 秒）
     * @param actionConfig 必需操作配置
     * @param redirectUri 完成后重定向 URI
     * @param clientId 签发目标客户端 ID
     */
    public CredentialOfferActionToken(String userId, String email, int absoluteExpirationInSecs, VerifiableCredentialOfferActionConfig actionConfig, String redirectUri, String clientId) {
        this(userId, absoluteExpirationInSecs, actionConfig, redirectUri, clientId);
        setEmail(email);
    }

    /** Jackson 反序列化用私有构造。 */
    private CredentialOfferActionToken() {
    }

    /** @return 凭证发放必需操作配置 */
    public VerifiableCredentialOfferActionConfig getActionConfig() {
        return actionConfig;
    }

    /** @param actionConfig 凭证发放必需操作配置 */
    public void setActionConfig(VerifiableCredentialOfferActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }

    /** @return 操作完成后的重定向 URI */
    public String getRedirectUri() {
        return redirectUri;
    }

    /** @param redirectUri 操作完成后的重定向 URI */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
