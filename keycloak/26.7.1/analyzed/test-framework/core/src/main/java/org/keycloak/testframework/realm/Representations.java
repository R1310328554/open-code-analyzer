package org.keycloak.testframework.realm;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * 测试框架内部使用的 IDM 表示对象工厂（包内可见）。
 * <p>
 * 提供角色、组与凭证等 {@link org.keycloak.representations.idm} 类型的便捷构造。
 */
class Representations {

    /** 工具类，禁止实例化。 */
    private Representations() {
    }

    /**
     * 构造角色表示。
     *
     * @param roleName 角色名
     * @param asClientRole 是否为客户端角色
     * @return 角色表示对象
     */
    static RoleRepresentation toRole(String roleName, boolean asClientRole) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setClientRole(asClientRole);
        return role;
    }

    /**
     * 构造组表示。
     *
     * @param groupName 组名
     * @return 组表示对象
     */
    static GroupRepresentation toGroup(String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        return group;
    }

    /**
     * 构造凭证表示。
     *
     * @param type 凭证类型（如 password）
     * @param value 凭证值
     * @return 凭证表示对象
     */
    static CredentialRepresentation toCredential(String type, String value) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(type);
        credential.setValue(value);
        return credential;
    }

}
