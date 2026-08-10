package org.keycloak.admin.ui.rest.model;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 有效角色（含领域角色与客户端角色）在管理 UI 中的统一表示。
 */
public final class EffectiveRole {
    /** 角色内部 ID。 */
    @Schema(required = true)
    private final String id;
    /** 角色名称。 */
    @Schema(required = true)
    private final String name;
    /** 角色描述。 */
    private final String description;
    /** 是否为客户端角色（false 表示领域角色）。 */
    @Schema(required = true)
    private final boolean clientRole;
    /** 所属客户端可读 clientId，领域角色时为 null。 */
    private String client;
    /** 所属客户端内部 ID。 */
    private String clientId;

    public EffectiveRole(String id, String name, String description, boolean clientRole) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.clientRole = clientRole;
    }

    public EffectiveRole(String id, String name, String description, boolean clientRole, String client, String clientId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.clientRole = clientRole;
        this.client = client;
        this.clientId = clientId;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isClientRole() {
        return this.clientRole;
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

    @Override
    public String toString() {
        return "EffectiveRole{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", clientRole=" + clientRole +
                ", client='" + client + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EffectiveRole that = (EffectiveRole) o;
        return clientRole == that.clientRole &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                Objects.equals(client, that.client) &&
                Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, clientRole, client, clientId);
    }
}
