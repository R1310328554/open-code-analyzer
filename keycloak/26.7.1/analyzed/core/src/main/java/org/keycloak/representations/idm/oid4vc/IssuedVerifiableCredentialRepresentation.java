package org.keycloak.representations.idm.oid4vc;

import java.util.Objects;

/**
 * 已签发可验证凭证（Verifiable Credential）的 REST 表示，记录签发元数据与关联钱包客户端信息。
 */
public class IssuedVerifiableCredentialRepresentation {

    /** 凭证记录 ID。 */
    private String id;
    /** 持有该凭证的用户 ID。 */
    private String userId;
    /** 凭证类型标识。 */
    private String credentialType;
    /** 签发时间戳（毫秒）。 */
    private Long issuedAt;
    /** 过期时间戳（毫秒）。 */
    private Long expiresAt;
    // 作为 OID4VCI 钱包的客户端 UUID
    private String clientId;
    /** 钱包客户端显示名称。 */
    private String clientName;
    /** 钱包客户端基础 URL。 */
    private String clientBaseUrl;

    /** @return 钱包客户端名称 */
    public String getClientName() {
        return clientName;
    }

    /** @param clientName 钱包客户端名称 */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /** @return 钱包客户端基础 URL */
    public String getClientBaseUrl() {
        return clientBaseUrl;
    }

    /** @param clientBaseUrl 钱包客户端基础 URL */
    public void setClientBaseUrl(String clientBaseUrl) {
        this.clientBaseUrl = clientBaseUrl;
    }

    /** 凭证修订版本号。 */
    private String revision;

    /** @return 凭证记录 ID */
    public String getId() {
        return id;
    }

    /** @param id 凭证记录 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 用户 ID */
    public String getUserId() {
        return userId;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return 凭证类型 */
    public String getCredentialType() {
        return credentialType;
    }

    /** @param credentialType 凭证类型 */
    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    /** @return 签发时间戳 */
    public Long getIssuedAt() {
        return issuedAt;
    }

    /** @param issuedAt 签发时间戳 */
    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    /** @return 过期时间戳 */
    public Long getExpiresAt() {
        return expiresAt;
    }

    /** @param expiresAt 过期时间戳 */
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** @return 钱包客户端 UUID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 钱包客户端 UUID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 修订版本号 */
    public String getRevision() {
        return revision;
    }

    /** @param revision 修订版本号 */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IssuedVerifiableCredentialRepresentation that = (IssuedVerifiableCredentialRepresentation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(credentialType, that.credentialType) &&
                Objects.equals(issuedAt, that.issuedAt) &&
                Objects.equals(expiresAt, that.expiresAt) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, credentialType, issuedAt, expiresAt, clientId, revision);
    }
}
