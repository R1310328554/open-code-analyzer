package org.keycloak.models;

import java.util.List;
import java.util.Map;

/**
 * 用户可验证凭据模型：存储与客户端范围关联的可验证凭据元数据与用户属性快照。
 */
public class UserVerifiableCredentialModel {

    private String id;
    private String clientScopeId;
    private String revision;
    private Long createdDate;
    private Long updatedDate;
    private Map<String, List<String>> userAttributes;

    /** @param id 凭据 ID
     * @param clientScopeId 客户端范围 ID */
    public UserVerifiableCredentialModel(String id, String clientScopeId) {
        this.id = id;
        this.clientScopeId = clientScopeId;
    }

    /** @return 凭据 ID */
    public String getId() {
        return id;
    }

    /** @param id 凭据 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 客户端范围 ID */
    public String getClientScopeId() {
        return clientScopeId;
    }

    /** @param clientScopeId 客户端范围 ID */
    public void setClientScopeId(String clientScopeId) {
        this.clientScopeId = clientScopeId;
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

    /** @return 更新时间戳 */
    public Long getUpdatedDate() { return updatedDate; }

    /** @param updatedDate 更新时间戳 */
    public void setUpdatedDate(Long updatedDate) { this.updatedDate = updatedDate; }
}
