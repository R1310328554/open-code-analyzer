package org.keycloak.migration.migrators;


import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 升级至 26.6.2 的域级迁移器：初始化细粒度管理员权限（FGAP）模式并为组资源类型补充成员管理作用域。
 */
public class MigrateTo26_6_2 extends RealmMigration {

    /** 目标模型版本 26.6.2。 */
    public static final ModelVersion VERSION = new ModelVersion("26.6.2");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        // 若域已启用管理员权限客户端，则初始化 FGAP 模式结构
        if (realm.getAdminPermissionsClient() != null) {
            AdminPermissionsSchema.SCHEMA.init(session, realm);
        }
        // 为组资源类型添加“管理成员归属”作用域
        AdminPermissionsSchema.SCHEMA.addResourceTypeScope(session, realm, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.MANAGE_MEMBERSHIP_OF_MEMBERS);
    }
}
