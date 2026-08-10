package org.keycloak.admin.ui.rest.model;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 客户端角色在管理 UI 中的精简表示，含角色与所属客户端信息。
 */
public final class ClientRole {
    /** 角色内部 ID。 */
    @Schema(required = true)
    private final String id;
    /** 角色名称。 */
    @Schema(required = true)
    private final String role;
    /** 所属客户端的可读 clientId。 */
    @Schema(required = true)
    private String client;
    /** 所属客户端内部 ID。 */
    @Schema(required = true)
    private String clientId;
    /** 角色描述。 */
    private String description;

    public String getId() {
        return this.id;
    }

    public String getRole() {
        return this.role;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ClientRole(String id, String role, String description) {
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public ClientRole(String id, String role, String client, String clientId, String description) {
        this.id = id;
        this.role = role;
        this.client = client;
        this.clientId = clientId;
        this.description = description;
    }

    /** 基于当前字段创建副本，可覆盖部分属性。 */
    public ClientRole copy(String id, String role, String client, String clientId, String description) {
        return new ClientRole(id, role, client, clientId, description);
    }

    @Override public String toString() {
        return "ClientRole{" + "id='" + id + '\'' + ", role='" + role + '\'' + ", client='" + client + '\'' + ", clientId='" + clientId + '\'' + ", description='" + description + '\'' + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClientRole that = (ClientRole) o;
        return id.equals(that.id) && role.equals(that.role) && client.equals(that.client) && clientId.equals(that.clientId);
    }

    @Override public int hashCode() {
        return Objects.hash(id, role, client, clientId);
    }
}
