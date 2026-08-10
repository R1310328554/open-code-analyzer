package org.keycloak.migration.migrators;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.DefaultAuthenticationFlows;

/**
 * 升级至 26.6.1 的域级迁移器：在浏览器认证流中插入组织（Organization）相关步骤。
 */
public class MigrateTo26_6_1 extends RealmMigration {

    /** 目标模型版本 26.6.1。 */
    public static final ModelVersion VERSION = new ModelVersion("26.6.1");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        DefaultAuthenticationFlows.addOrganizationBrowserFlowStep(realm, realm.getFlowByAlias(DefaultAuthenticationFlows.BROWSER_FLOW));
    }
}
