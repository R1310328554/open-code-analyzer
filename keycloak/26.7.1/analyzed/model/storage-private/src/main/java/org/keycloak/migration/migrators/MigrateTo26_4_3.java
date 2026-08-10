package org.keycloak.migration.migrators;


import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 升级至 26.4.3 的域级迁移器：在细粒度管理权限 schema 中为 Users 资源类型补充 {@code reset-password} 作用域。
 */
public class MigrateTo26_4_3 extends RealmMigration {

    /** 目标模型版本 26.4.3。 */
    public static final ModelVersion VERSION = new ModelVersion("26.4.3");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        AdminPermissionsSchema.SCHEMA.addResourceTypeScope(session, realm, AdminPermissionsSchema.USERS_RESOURCE_TYPE, AdminPermissionsSchema.RESET_PASSWORD);
    }
}
