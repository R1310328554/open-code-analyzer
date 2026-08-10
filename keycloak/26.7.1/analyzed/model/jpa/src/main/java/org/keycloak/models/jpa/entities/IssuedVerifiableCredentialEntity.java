package org.keycloak.models.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 已签发可验证凭据（VC）JPA 实体，映射 {@code ISSUED_VER_CREDENTIAL} 表。
 * <p>记录 OID4VCI 流程中向用户钱包签发的 VC 实例，含签发/过期时间及关联客户端。</p>
 */
@Entity
@Table(name="ISSUED_VER_CREDENTIAL")
@NamedQueries({
        @NamedQuery(name="issuedVcsByUser", query="select vc from IssuedVerifiableCredentialEntity vc where vc.user.id = :userId order by vc.issuedAt desc"),
        @NamedQuery(name="deleteIssuedVcsByRealm", query="delete from IssuedVerifiableCredentialEntity vc where vc.user IN (select u from UserEntity u where u.realmId = :realmId)"),
        @NamedQuery(name="deleteIssuedVcsByUser",  query="delete from IssuedVerifiableCredentialEntity vc where vc.user.id = :userId"),
        @NamedQuery(name="deleteIssuedVcsByClientScope", query="delete from IssuedVerifiableCredentialEntity vc where vc.verifiableCredentialId in (select uvc.id from UserVerifiableCredentialEntity uvc where uvc.clientScopeId = :scopeId)"),
        @NamedQuery(name="deleteIssuedVcsByClient", query="delete from IssuedVerifiableCredentialEntity vc where vc.clientId = :clientId"),
        @NamedQuery(name="deleteExpiredIssuedVcs", query="delete from IssuedVerifiableCredentialEntity vc where vc.expiresAt IS NOT NULL and vc.expiresAt < :currentTime"),
        @NamedQuery(name="deleteIssuedVcsByUserAndType", query="delete from IssuedVerifiableCredentialEntity vc where vc.user.id = :userId and vc.verifiableCredentialId = :verifiableCredentialId"),
})
public class IssuedVerifiableCredentialEntity {

    /** 签发记录 UUID。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY)
    protected String id;

    /** 凭据所属用户。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    protected UserEntity user;

    /** 关联的 {@link UserVerifiableCredentialEntity} 模板 ID。 */
    @Column(name="VER_CREDENTIAL_ID")
    protected String verifiableCredentialId;

    /** 签发时间戳（毫秒）。 */
    @Column(name="ISSUED_AT")
    protected Long issuedAt;

    /** 过期时间戳（毫秒）；null 表示不过期。 */
    @Column(name="EXPIRES_AT")
    protected Long expiresAt;

    // 客户端 UUID，作为 OID4VCI 钱包使用
    @Column(name="CLIENT_ID")
    protected String clientId;

    /** VC 内容修订/版本标识。 */
    @Column(name="REVISION")
    protected String revision;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
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
        IssuedVerifiableCredentialEntity that = (IssuedVerifiableCredentialEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
