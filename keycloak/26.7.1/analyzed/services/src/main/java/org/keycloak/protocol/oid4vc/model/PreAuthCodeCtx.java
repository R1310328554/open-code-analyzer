package org.keycloak.protocol.oid4vc.model;

import java.util.List;
import java.util.Objects;

import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 预授权码 JWT 可嵌入的非敏感公开字段。
 * <p>作为 {@link org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState} 的部分公开视图，不含交易码等机密信息。</p>
 */
public class PreAuthCodeCtx implements Cloneable {

    /** 凭证发放 ID。 */
    @JsonProperty("credentials_offer_id")
    private String credentialsOfferId;

    /** 目标 OAuth 客户端 ID。 */
    @JsonProperty("target_client_id")
    private String targetClientId;

    /** 目标用户 ID。 */
    @JsonProperty("target_user_id")
    private String targetUserId;

    /** 发放流程 nonce。 */
    @JsonProperty("nonce")
    private String nonce;

    /** 过期时间（Unix 秒）。 */
    @JsonProperty("exp")
    private Long expiresAt;

    /** 授权详情列表（公开子集）。 */
    @JsonProperty("authorization_details")
    private List<OID4VCAuthorizationDetail> authorizationDetails;

    /** 无参构造，供 Jackson 反序列化使用。 */
    public PreAuthCodeCtx() {
    }

    /**
     * 从凭证发放状态提取可公开字段；交易码等敏感数据不会进入预授权码。
     * @param offerState 凭证发放状态
     */
    public PreAuthCodeCtx(CredentialOfferState offerState) {
        Objects.requireNonNull(offerState);

        this.credentialsOfferId = offerState.getCredentialsOfferId();
        this.targetClientId = offerState.getTargetClientId();
        this.targetUserId = offerState.getTargetUserId();
        this.nonce = offerState.getNonce();
        this.expiresAt = offerState.getExpiresAt();

        List<OID4VCAuthorizationDetail> details = offerState.getAuthorizationDetails();
        this.authorizationDetails = details == null ? null : details.stream()
                .map(OID4VCAuthorizationDetail::clone)
                .peek(d -> d.setCredentialsOfferId(null))
                .toList();
    }

    /**
     * 从授权详情汇总凭证配置 ID 列表（不参与 JSON 序列化）。
     * @return 凭证配置 ID 列表
     */
    @JsonIgnore
    public List<String> getCredentialConfigurationIds() {
        if (authorizationDetails == null) {
            return List.of();
        }

        return authorizationDetails.stream()
                .map(OID4VCAuthorizationDetail::getCredentialConfigurationId)
                .toList();
    }

    /** @return 凭证发放 ID */
    public String getCredentialsOfferId() {
        return credentialsOfferId;
    }

    /** @param credentialsOfferId 凭证发放 ID */
    public void setCredentialsOfferId(String credentialsOfferId) {
        this.credentialsOfferId = credentialsOfferId;
    }

    /** @return 授权详情列表 */
    public List<OID4VCAuthorizationDetail> getAuthorizationDetails() {
        return authorizationDetails;
    }

    /** @param authorizationDetails 授权详情列表 */
    public void setAuthorizationDetails(List<OID4VCAuthorizationDetail> authorizationDetails) {
        this.authorizationDetails = authorizationDetails;
    }

    /** @return 目标客户端 ID */
    public String getTargetClientId() {
        return targetClientId;
    }

    /** @param targetClientId 目标客户端 ID */
    public void setTargetClientId(String targetClientId) {
        this.targetClientId = targetClientId;
    }

    /** @return 目标用户 ID */
    public String getTargetUserId() {
        return targetUserId;
    }

    /** @param targetUserId 目标用户 ID */
    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    /** @return nonce */
    public String getNonce() {
        return nonce;
    }

    /** @param nonce nonce */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /** @return 过期时间 */
    public Long getExpiresAt() {
        return expiresAt;
    }

    /** @param expiresAt 过期时间 */
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PreAuthCodeCtx that = (PreAuthCodeCtx) o;
        return Objects.equals(getCredentialsOfferId(), that.getCredentialsOfferId()) && Objects.equals(getCredentialConfigurationIds(), that.getCredentialConfigurationIds()) && Objects.equals(getAuthorizationDetails(), that.getAuthorizationDetails()) && Objects.equals(getTargetClientId(), that.getTargetClientId()) && Objects.equals(getTargetUserId(), that.getTargetUserId()) && Objects.equals(getNonce(), that.getNonce()) && Objects.equals(getExpiresAt(), that.getExpiresAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCredentialsOfferId(), getCredentialConfigurationIds(), getAuthorizationDetails(), getTargetClientId(), getTargetUserId(), getNonce(), getExpiresAt());
    }

    /** @return 浅拷贝 */
    @Override
    public PreAuthCodeCtx clone() {
        try {
            return (PreAuthCodeCtx) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
