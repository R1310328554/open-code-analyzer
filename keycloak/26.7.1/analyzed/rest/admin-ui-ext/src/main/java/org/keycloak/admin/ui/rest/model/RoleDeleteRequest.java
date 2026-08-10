package org.keycloak.admin.ui.rest.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 批量删除角色的请求体：携带角色 ID、名称及可选的客户端 ID。
 */
public class RoleDeleteRequest {
    /** 待删除角色的内部 ID。 */
    @Schema(required = true)
    private String roleId;

    /** 待删除角色的名称。 */
    @Schema(required = true)
    private String roleName;

    /** 客户端 ID；客户端角色时必填，领域角色为 null。 */
    @Schema(description = "Client ID if this is a client role, null for realm roles")
    private String clientId;

    public RoleDeleteRequest() {
    }

    public RoleDeleteRequest(String roleId, String roleName, String clientId) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.clientId = clientId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
