package org.keycloak.models.cache.infinispan.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.UserVerifiableCredentialModel;

/**
 * 单条用户可验证凭证（Verifiable Credential）的缓存值对象。
 * <p>
 * 缓存凭证 ID、关联客户端作用域、revision 版本、创建/更新时间与用户属性映射。
 */
public class CachedUserVerifiableCredential {

    /** 凭证唯一标识。 */
    private final String id;
    /** 关联的客户端作用域 ID。 */
    private final String clientScopeId;
    /** 凭证 revision 版本字符串。 */
    private final String revision;
    /** 凭证创建时间戳。 */
    private final Long createdDate;
    /** 凭证最后更新时间戳。 */
    private final Long updatedDate;
    /** 用户属性多值映射。 */
    private final MultivaluedHashMap<String, String> userAttributes;

    /** 从可验证凭证模型构造缓存值对象。 */
    public CachedUserVerifiableCredential(UserVerifiableCredentialModel credentialModel) {
        this.id = credentialModel.getId();
        this.clientScopeId = credentialModel.getClientScopeId();
        this.revision = credentialModel.getRevision();
        this.createdDate = credentialModel.getCreatedDate();
        this.updatedDate = credentialModel.getUpdatedDate();

        this.userAttributes = new MultivaluedHashMap<>();
        if (credentialModel.getUserAttributes() != null) {
            for (Map.Entry<String, List<String>> entry : credentialModel.getUserAttributes().entrySet()) {
                this.userAttributes.addAll(entry.getKey(), entry.getValue());
            }
        }
    }

    /** 返回凭证唯一标识。 */
    public String getId() {
        return id;
    }

    /** 返回关联的客户端作用域 ID。 */
    public String getClientScopeId() {
        return clientScopeId;
    }

    /** 返回凭证 revision 版本字符串。 */
    public String getRevision() {
        return revision;
    }

    /** 返回凭证创建时间戳。 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** 返回凭证最后更新时间戳。 */
    public Long getUpdatedDate() {
        return updatedDate;
    }

    /** 返回用户属性的防御性副本映射。 */
    public Map<String, List<String>> getUserAttributes() {
        return new HashMap<>(userAttributes);
    }
}
