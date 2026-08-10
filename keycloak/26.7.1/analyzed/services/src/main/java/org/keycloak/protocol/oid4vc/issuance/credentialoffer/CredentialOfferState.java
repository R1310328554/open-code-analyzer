/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.Time;
import org.keycloak.protocol.oid4vc.model.CredentialsOffer;
import org.keycloak.protocol.oid4vc.model.OID4VCAuthorizationDetail;
import org.keycloak.saml.RandomSecret;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * 凭证发放（Credential Offer）的运行时状态。
 * <p>包含 {@link CredentialsOffer} 载荷、目标客户端/用户、nonce、交易码及授权详情等，
 * 序列化后存入 {@link CredentialOfferStorage}。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialOfferState {

    /** 凭证发放内部唯一标识（同时作为 singleUseObjects 键）。 */
    private String credentialsOfferId;
    /** OID4VCI Credential Offer JSON 模型。 */
    private CredentialsOffer credentialsOffer;
    /** 目标客户端 ID（可选绑定）。 */
    private String targetClientId;
    /** 目标用户内部 ID（可选绑定）。 */
    private String targetUserId;
    /** 嵌入 offerId 的 nonce，用于 authorization_code 流程关联。 */
    private String nonce;
    /** 预授权流程可选的交易码（第二因素）。 */
    private String txCode;
    /** 过期时间（Unix 秒）。 */
    private long expiresAt;
    /** 各 credential_configuration_id 对应的 authorization_details。 */
    private List<OID4VCAuthorizationDetail> authDetails;

    /**
     * 创建新的 CredentialOfferState。
     * <p>通常仅由已配置的 {@code CredentialOfferProvider} 调用；
     * 构造函数公开仅供测试使用。</p>
     *
     * @param credOffer 凭证发放模型
     * @param clientId 目标 client_id
     * @param userId 目标用户 ID
     * @param expiresAt 过期时间（Unix 秒）
     * @param authDetailsProvider 按 credential_configuration_id 生成 authorization_details 的函数
     */
    public CredentialOfferState(
            CredentialsOffer credOffer,
            String clientId,
            String userId,
            long expiresAt,
            Function<String, List<OID4VCAuthorizationDetail>> authDetailsProvider
    ) {
        this.credentialsOfferId = Base64Url.encode(RandomSecret.createRandomSecret(64));
        this.credentialsOffer = credOffer;
        this.targetClientId = clientId;
        this.targetUserId = userId;
        this.expiresAt = expiresAt;
        String nonceSecret = Base64Url.encode(RandomSecret.createRandomSecret(64));
        this.nonce = CredentialOfferLookupKey.embed(nonceSecret, credentialsOfferId);
        if (authDetailsProvider != null) {
            this.authDetails = authDetailsProvider.apply(credentialsOfferId);
        }
    }

    /** @return 预授权码（若 grant 类型为 pre-authorized_code） */
    @JsonIgnore
    public Optional<String> getPreAuthorizedCode() {
        return Optional.ofNullable(credentialsOffer.getPreAuthorizedCode());
    }

    /** @return 凭证发放内部 ID */
    public String getCredentialsOfferId() {
        return credentialsOfferId;
    }

    /** @return Credential Offer JSON 模型 */
    public CredentialsOffer getCredentialsOffer() {
        return credentialsOffer;
    }

    /** @return 目标客户端 ID */
    public String getTargetClientId() {
        return targetClientId;
    }

    /** @return 目标用户 ID */
    public String getTargetUserId() {
        return targetUserId;
    }

    /** @return 嵌入 offerId 的 nonce */
    public String getNonce() {
        return nonce;
    }

    /** @return 交易码（若已设置） */
    public String getTxCode() {
        return txCode;
    }

    /** @return 过期时间（Unix 秒） */
    public long getExpiresAt() {
        return expiresAt;
    }

    /** @return authorization_details 的防御性拷贝列表 */
    public List<OID4VCAuthorizationDetail> getAuthorizationDetails() {
        return Optional.ofNullable(authDetails).orElse(List.of()).stream()
                .map(OID4VCAuthorizationDetail::clone)
                .toList();
    }

    /**
     * 按 credential_configuration_id 查找单条 authorization_details。
     * @param credConfigId 凭证配置标识
     * @return 匹配的详情克隆，未找到时 {@code null}
     */
    public OID4VCAuthorizationDetail getAuthorizationDetails(String credConfigId) {
        return getAuthorizationDetails().stream()
                .filter(it -> it.getCredentialConfigurationId().equals(credConfigId))
                .findFirst()
                .map(OID4VCAuthorizationDetail::clone)
                .orElse(null);
    }

    /**
     * 比较 authorization_details 是否与给定列表等价（忽略 issued_credential_id）。
     * @param otherAuthDetails 待比较的详情列表
     * @return 等价返回 {@code true}
     */
    public boolean matchAuthorizationDetails(List<OID4VCAuthorizationDetail> otherAuthDetails) {
        if (authDetails == null && otherAuthDetails == null) { return true; }
        if (authDetails == null || otherAuthDetails == null) { return false; }
        if (authDetails.size() != otherAuthDetails.size()) { return false; }
        for (int i = 0; i < authDetails.size(); i++) {
            var authDetail = authDetails.get(i);
            var otherDetail = otherAuthDetails.get(i);
            if (otherDetail.getIssuedCredentialId() != null) {
                otherDetail = otherDetail.clone();
                otherDetail.setIssuedCredentialId(null);
            }
            // authorization_details 中不应出现 issued_credential_id
            assert authDetail.getIssuedCredentialId() == null;
            if (!authDetail.equals(otherDetail)) {
                return false;
            }
        }
        return true;
    }

    /** @return 当前时间是否已超过 expiresAt */
    @JsonIgnore
    public boolean isExpired() {
        int currentTime = Time.currentTime();
        return expiresAt <= currentTime;
    }

    // 私有 ---------------------------------------------------------------------------------------------------------

    // 供 JSON 反序列化：无参构造
    private CredentialOfferState() {
    }

    // 供 JSON 反序列化：设置 authorization_details
    private void setAuthorizationDetails(List<OID4VCAuthorizationDetail> authDetails) {
        this.authDetails = authDetails;
    }
}
