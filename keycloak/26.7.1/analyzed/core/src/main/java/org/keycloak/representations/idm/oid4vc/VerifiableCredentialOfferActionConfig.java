package org.keycloak.representations.idm.oid4vc;

import java.io.IOException;

import org.keycloak.common.util.Base64Url;
import org.keycloak.util.JsonSerialization;

/**
 * 可验证凭证发放（Credential Offer）操作的动作配置，用于 OID4VCI 凭证邀约流程。
 */
public class VerifiableCredentialOfferActionConfig {

    /**
     * 对应 OID4VCI 凭证的凭证配置 ID，与关联 OID4VCI 客户端作用域的
     * "Credential configuration ID" 属性相同。
     */
    private String credentialConfigurationId;

    /**
     * 目标客户端 ID（UUID）。未填写时（常见情况）凭证邀约可被任意客户端/钱包使用。
     */
    private String clientId;

    /**
     * 为 {@code true} 时凭证邀约面向 OID4VCI 预授权码授权；为 {@code false}（默认）时面向授权码授权。
     */
    private Boolean preAuthorized;

    /** @return 凭证配置 ID */
    public String getCredentialConfigurationId() {
        return credentialConfigurationId;
    }

    /** @param credentialConfigurationId 凭证配置 ID */
    public void setCredentialConfigurationId(String credentialConfigurationId) {
        this.credentialConfigurationId = credentialConfigurationId;
    }

    /** @return 目标客户端 ID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 目标客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 是否使用预授权码授权 */
    public Boolean getPreAuthorized() {
        return preAuthorized;
    }

    /** @param preAuthorized 是否使用预授权码授权 */
    public void setPreAuthorized(Boolean preAuthorized) {
        this.preAuthorized = preAuthorized;
    }

    @Override
    public String toString() {
        return "VerifiableCredentialOfferActionConfig{" +
                "credentialConfigurationId='" + credentialConfigurationId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", preAuthorized='" + preAuthorized + '\'' +
                '}';
    }

    /** 编码为可用于 AIA 参数的 Base64Url 字符串。 */
    public String asEncodedParameter() throws IOException {
        byte[] bytes = JsonSerialization.writeValueAsBytes(this);
        return Base64Url.encode(bytes);
    }

    /** 从 AIA 参数的 Base64Url 字符串解码配置。 */
    public static VerifiableCredentialOfferActionConfig decodeConfig(String configStr) throws IOException {
        byte[] bytes = Base64Url.decode(configStr);
        return JsonSerialization.readValue(bytes, VerifiableCredentialOfferActionConfig.class);
    }
}
