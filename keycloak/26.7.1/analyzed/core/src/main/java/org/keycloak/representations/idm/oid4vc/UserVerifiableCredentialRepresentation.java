package org.keycloak.representations.idm.oid4vc;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 用户可验证凭证（Verifiable Credential）的 REST 表示，描述凭证配置与用户属性快照。
 */
public class UserVerifiableCredentialRepresentation {

    /** 凭证作用域名称。 */
    private String credentialScopeName;
    /** OID4VCI 凭证配置 ID。 */
    private String credentialConfigurationId;
    /** 凭证修订版本号。 */
    private String revision;
    /** 创建时间戳（毫秒）。 */
    private Long createdDate;
    /** 最后更新时间戳（毫秒）。 */
    private Long updatedDate;
    /** 关联的用户属性快照。 */
    private Map<String, List<String>> userAttributes;

    /** @return 凭证配置 ID */
    public String getCredentialConfigurationId() {
        return credentialConfigurationId;
    }

    /** @param credentialConfigurationId 凭证配置 ID */
    public void setCredentialConfigurationId(String credentialConfigurationId) {
        this.credentialConfigurationId = credentialConfigurationId;
    }

    /** @return 凭证作用域名称 */
    public String getCredentialScopeName() {
        return credentialScopeName;
    }

    /** @param credentialScopeName 凭证作用域名称 */
    public void setCredentialScopeName(String credentialScopeName) {
        this.credentialScopeName = credentialScopeName;
    }

    /** @return 修订版本号 */
    public String getRevision() {
        return revision;
    }

    /** @param revision 修订版本号 */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /** @return 创建时间戳 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** @param createdDate 创建时间戳 */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    /** @return 用户属性快照 */
    public Map<String, List<String>> getUserAttributes() { return userAttributes; }

    /** @param userAttributes 用户属性快照 */
    public void setUserAttributes(Map<String, List<String>> userAttributes) { this.userAttributes = userAttributes; }

    /** @return 最后更新时间戳 */
    public Long getUpdatedDate() { return updatedDate; }

    /** @param updatedDate 最后更新时间戳 */
    public void setUpdatedDate(Long updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserVerifiableCredentialRepresentation that = (UserVerifiableCredentialRepresentation) o;
        return Objects.equals(credentialScopeName, that.credentialScopeName)
                && Objects.equals(credentialConfigurationId, that.getCredentialConfigurationId())
                && Objects.equals(revision, that.revision)
                && Objects.equals(createdDate, that.createdDate)
                && Objects.equals(userAttributes, that.userAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialScopeName, credentialConfigurationId, revision, createdDate, userAttributes);
    }
}
