package org.keycloak.admin.ui.rest.model;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

/**
 * 将 {@link RoleModel} 映射为管理 UI 用的 {@link ClientRole} 或 {@link EffectiveRole} DTO。
 */
public class RoleMapper {
    /**
     * 把客户端角色模型转为 {@link ClientRole}，并解析所属客户端信息。
     *
     * @param roleModel 源角色模型
     * @param realm 所属领域
     */
    public static ClientRole convertToModel(RoleModel roleModel, RealmModel realm) {
        ClientModel clientModel = realm.getClientById(roleModel.getContainerId());
        if (clientModel==null) {
            throw new IllegalArgumentException("Could not find referenced client");
        }
        ClientRole clientRole = new ClientRole(roleModel.getId(), roleModel.getName(), roleModel.getDescription());
        clientRole.setClientId(clientModel.getId());
        clientRole.setClient(clientModel.getClientId());
        return clientRole;
    }

    /**
     * 把角色模型转为统一的 {@link EffectiveRole}，自动区分领域角色与客户端角色。
     *
     * @param roleModel 源角色模型
     * @param realm 所属领域
     */
    public static EffectiveRole convertToEffectiveRole(RoleModel roleModel, RealmModel realm) {
        if (roleModel.isClientRole()) {
            ClientModel clientModel = realm.getClientById(roleModel.getContainerId());
            if (clientModel == null) {
                throw new IllegalArgumentException("Could not find referenced client");
            }
            return new EffectiveRole(
                    roleModel.getId(),
                    roleModel.getName(),
                    roleModel.getDescription(),
                    true,
                    clientModel.getClientId(),
                    clientModel.getId()
            );
        } else {
            return new EffectiveRole(
                    roleModel.getId(),
                    roleModel.getName(),
                    roleModel.getDescription(),
                    false
            );
        }
    }
}
