package org.keycloak.migration.migrators;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 升级至 20.0.0 的模型迁移器：为 account 客户端补充 {@link AccountRoles#VIEW_GROUPS} 角色并映射至 account-console。
 */
public class MigrateTo20_0_0 implements Migration {

    /** 目标模型版本 20.0.0。 */
    public static final ModelVersion VERSION = new ModelVersion("20.0.0");

    @Override
    public void migrate(KeycloakSession session) {

        session.realms().getRealmsStream().forEach(this::addViewGroupsRole);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        addViewGroupsRole(realm);
    }

    /** 若 account 客户端尚无 view-groups 角色，则创建并授予 account-console 客户端作用域映射。 */
    private void addViewGroupsRole(RealmModel realm) {
        ClientModel accountClient = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        if (accountClient != null && accountClient.getRole(AccountRoles.VIEW_GROUPS) == null) {
            RoleModel viewGroupsRole = accountClient.addRole(AccountRoles.VIEW_GROUPS);
            viewGroupsRole.setDescription("${role_" + AccountRoles.VIEW_GROUPS + "}");
            ClientModel accountConsoleClient = realm.getClientByClientId(Constants.ACCOUNT_CONSOLE_CLIENT_ID);
            accountConsoleClient.addScopeMapping(viewGroupsRole);
        }
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
