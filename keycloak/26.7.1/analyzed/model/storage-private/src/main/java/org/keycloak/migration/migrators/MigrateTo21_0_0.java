package org.keycloak.migration.migrators;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 升级至 21.0.0 的模型迁移器：将旧版 {@code keycloak}/{@code rh-sso} 管理控制台主题切换为 {@code keycloak.v2}。
 */
public class MigrateTo21_0_0 implements Migration {

    /** 目标模型版本 21.0.0。 */
    public static final ModelVersion VERSION = new ModelVersion("21.0.0");

    @Override
    public void migrate(KeycloakSession session) {
        session.realms().getRealmsStream().forEach(this::updateAdminTheme);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        updateAdminTheme(realm);
    }

    /** 将 legacy 管理主题名替换为 v2 主题。 */
    private void updateAdminTheme(RealmModel realm) {
        String adminTheme = realm.getAdminTheme();
        if ("keycloak".equals(adminTheme) || "rh-sso".equals(adminTheme)) {
            realm.setAdminTheme("keycloak.v2");
        }
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
