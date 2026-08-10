package org.keycloak.models;

/**
 * 已签发可验证凭证（VC）记录模型，用于 OID4VCI 场景。
 */
public class IssuedVerifiableCredentialModel {

    private String id;
    private String userId;
    private String verifiableCredentialId;
    private Long issuedAt;
    private Long expiresAt;
    // 表示作为 OID4VCI 钱包的客户端 UUID
    private String clientId;
    private String revision;

    public IssuedVerifiableCredentialModel() {
    }

    public IssuedVerifiableCredentialModel(String userId, String verifiableCredentialId, String clientId) {
        this.userId = userId;
        this.verifiableCredentialId = verifiableCredentialId;
        this.clientId = clientId;
    }

    /** @return 记录唯一标识符 */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** @return 持有该 VC 的用户 ID */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return 可验证凭证定义 ID */
    public String getVerifiableCredentialId() {
        return verifiableCredentialId;
    }

    public void setVerifiableCredentialId(String verifiableCredentialId) {
        this.verifiableCredentialId = verifiableCredentialId;
    }

    /** @return 签发时间戳（毫秒） */
    public Long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    /** @return 过期时间戳（毫秒） */
    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** @return OID4VCI 钱包客户端 UUID */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 记录修订版本号 */
    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
