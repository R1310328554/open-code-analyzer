package org.keycloak.storage.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;


/**
 * 联邦用户已签发可验证凭证（VC）记录 JPA 实体，映射 FED_ISSUED_VER_CREDENTIAL 表。
 * <p>
 * 记录某用户在某客户端下签发的 VC 实例，含签发/过期时间及关联的 VC 定义 ID。
 */
@Entity
@Table(name="FED_ISSUED_VER_CREDENTIAL")
@NamedQueries({
        @NamedQuery(name="federatedIssuedVcsByUser",
                    query="select vc from FederatedUserIssuedVerifiableCredentialEntity vc where vc.userId = :userId order by vc.issuedAt desc"),
        @NamedQuery(name="deleteFederatedIssuedVcsByRealm",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.realmId = :realmId"),
        @NamedQuery(name="deleteFederatedIssuedVcsByUser",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.userId = :userId and vc.realmId = :realmId"),
        @NamedQuery(name="deleteFederatedIssuedVcsByClientScope",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.verifiableCredentialId in (select fuvc.id from FederatedUserVerifiableCredentialEntity fuvc where fuvc.clientScopeId = :scopeId)"),
        @NamedQuery(name="deleteExpiredFederatedIssuedVcs",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.expiresAt IS NOT NULL and vc.expiresAt < :currentTime"),
        @NamedQuery(name="deleteFederatedIssuedVcsByUserAndType",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.userId = :userId and vc.realmId = :realmId and vc.verifiableCredentialId = :verifiableCredentialId"),
        @NamedQuery(name="deleteFederatedIssuedVcsByStorageProvider",
                    query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.storageProviderId = :storageProviderId"),
        @NamedQuery(name="deleteFederatedIssuedVcsByClient",
                query="delete from FederatedUserIssuedVerifiableCredentialEntity vc where vc.clientId = :clientId")
})
public class FederatedUserIssuedVerifiableCredentialEntity {

    /** 签发记录 UUID（主键）。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY)
    protected String id;

    /** 联邦用户 ID。 */
    @Column(name="USER_ID", nullable = false)
    protected String userId;

    /** 所属 realm ID。 */
    @Column(name="REALM_ID", nullable = false, length = 36)
    protected String realmId;

    /** 用户存储提供者组件 ID。 */
    @Column(name="STORAGE_PROVIDER_ID", length = 36, nullable = false)
    protected String storageProviderId;

    /** 关联的可验证凭证定义 ID。 */
    @Column(name="VER_CREDENTIAL_ID", nullable = false)
    protected String verifiableCredentialId;

    /** 签发时间（毫秒）。 */
    @Column(name="ISSUED_AT", nullable = false)
    protected Long issuedAt;

    /** 过期时间（毫秒，可为 null 表示不过期）。 */
    @Column(name="EXPIRES_AT")
    protected Long expiresAt;

    /** 签发时的客户端 ID（可选）。 */
    @Column(name="CLIENT_ID")
    protected String clientId;

    /** VC 定义 revision，用于并发控制。 */
    @Column(name="REVISION", nullable = false)
    protected String revision;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getStorageProviderId() {
        return storageProviderId;
    }

    public void setStorageProviderId(String storageProviderId) {
        this.storageProviderId = storageProviderId;
    }

    public String getVerifiableCredentialId() {
        return verifiableCredentialId;
    }

    public void setVerifiableCredentialId(String verifiableCredentialId) {
        this.verifiableCredentialId = verifiableCredentialId;
    }

    public Long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FederatedUserIssuedVerifiableCredentialEntity that = (FederatedUserIssuedVerifiableCredentialEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
