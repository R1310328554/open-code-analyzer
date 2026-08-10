package org.keycloak.migration.migrators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.Profile;
import org.keycloak.migration.MigrationProvider;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.storage.UserStorageProvider;

/**
 * 升级至 26.7.0 的域级迁移器：调整注册必选动作优先级、迁移参数化作用域与 LDAP 映射器、
 * 补充组织管理员角色，并在启用 SAML 阶梯认证时为域创建 AuthnContextClassRef 默认作用域。
 */
public class MigrateTo26_7_0 extends RealmMigration {

    /** 目标模型版本 26.7.0。 */
    public static final ModelVersion VERSION = new ModelVersion("26.7.0");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrate(KeycloakSession session) {
        // addOrganizationAdminRoles 会访问跨域实体（master 管理客户端），
        // 在 RealmMigration 逐域清理持久化上下文时会触发 LazyInitializationException，故单独一轮处理
        session.realms().getRealmsStream().forEach(this::addOrganizationAdminRoles);
        super.migrate(session);
    }

    @Override
    public void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep,
            boolean skipUserDependent) {
        addOrganizationAdminRoles(realm);
        super.migrateImport(session, realm, rep, skipUserDependent);
    }

    @Override
    public void migrateRealm(KeycloakSession session, RealmModel realm) {
        updatePasswordAfterEmailVerificationDuringRegistrationOfUsers(realm);
        updateAdminPermissionsSchema(session, realm);
        renameDynamicScopeAttributes(realm);
        migrateParameterizedScopeTypes(realm);
        migrateLdapBinaryAttributeMappers(realm);
        migrateLdapGroupAttributeMappers(realm);
        migrateSamlAuthnContextClassRefClientScope(session, realm);
    }

    /** 为缺少类型属性的参数化客户端作用域推断并写入 parameterizedScopeType 与 regexp。 */
    private void migrateParameterizedScopeTypes(RealmModel realm) {
        realm.getClientScopesStream()
                .filter(scope -> scope.isParameterizedScope())
                .filter(scope -> scope.getAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_TYPE) == null)
                .forEach(scope -> {
                    String regexp = scope.getParameterizedScopeRegexp();
                    if (regexp == null) {
                        LOG.warnf("Parameterized client scope '%s' has no regexp. Defaulting to 'string' type.", scope.getName());
                        scope.setAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_TYPE, "string");
                        return;
                    }

                    String prefix = scope.getName() + ":";
                    String defaultRegexp = prefix + "*";
                    if (defaultRegexp.equals(regexp)) {
                        scope.setAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_TYPE, "string");
                        scope.removeAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_REGEXP);
                    } else if (regexp.startsWith(prefix)) {
                        scope.setAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_TYPE, "custom");
                        scope.setAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_REGEXP, regexp.substring(prefix.length()));
                    } else {
                        LOG.warnf("Parameterized client scope '%s' has a regexp '%s' that does not start with the expected prefix '%s'. Please migrate this scope manually.", scope.getName(), regexp, prefix);
                    }
                });
    }

    /** 在邮箱验证之后重新排列 configure-totp 与 update-password 必选动作的优先级。 */
    private void updatePasswordAfterEmailVerificationDuringRegistrationOfUsers(RealmModel realm) {
        Map<String, RequiredActionProviderModel> reqActionsByAlias = new HashMap<>();
        List<Integer> reqActionPriorities = new ArrayList<>();
        realm.getRequiredActionProvidersStream().forEach((reqAction) -> {
            reqActionsByAlias.put(reqAction.getAlias(), reqAction);
            reqActionPriorities.add(reqAction.getPriority());
        });

        RequiredActionProviderModel verifyEmail = reqActionsByAlias.get(UserModel.RequiredAction.VERIFY_EMAIL.name());
        RequiredActionProviderModel configureTotp = reqActionsByAlias.get(UserModel.RequiredAction.CONFIGURE_TOTP.name());
        RequiredActionProviderModel updatePassword = reqActionsByAlias.get(UserModel.RequiredAction.UPDATE_PASSWORD.name());

        if (verifyEmail == null) {
            return;
        }

        // 管理员未改动优先级时的默认情况：与 DefaultRequiredActions 保持一致
        if (configureTotp != null && updatePassword != null &&
                verifyEmail.getPriority() == 50 && configureTotp.getPriority() == 10 && updatePassword.getPriority() == 30) {
            configureTotp.setPriority(54);
            realm.updateRequiredActionProvider(configureTotp);
            updatePassword.setPriority(57);
            realm.updateRequiredActionProvider(updatePassword);
        } else {
            // 管理员已自定义优先级：在 verifyEmail 之后依次占用首个可用优先级槽位
            int nextAvailablePriority = getFirstAvailablePriorityAfter(verifyEmail.getPriority(), reqActionPriorities);
            if (configureTotp != null) {
                configureTotp.setPriority(nextAvailablePriority);
                realm.updateRequiredActionProvider(configureTotp);
                nextAvailablePriority = getFirstAvailablePriorityAfter(nextAvailablePriority, reqActionPriorities);
            }
            if (updatePassword != null) {
                updatePassword.setPriority(nextAvailablePriority);
                realm.updateRequiredActionProvider(updatePassword);
            }
        }
    }

    /** 在指定优先级之后查找尚未被占用的最小优先级值。 */
    private int getFirstAvailablePriorityAfter(int priority, List<Integer> reqActionPriorities) {
        for (int i = priority + 1 ; i < (priority + reqActionPriorities.size() + 2) ; i++) {
            if (!reqActionPriorities.contains(i)) {
                return i;
            }
        }

        // 不应发生
        return reqActionPriorities.get(reqActionPriorities.size() - 1) + 1;
    }

    /** 为域补充组织相关管理员角色，并将 query-organizations 设为 view-organizations 的复合角色。 */
    private void addOrganizationAdminRoles(RealmModel realm) {
        MigrationUtils.addAdminRole(realm, AdminRoles.VIEW_ORGANIZATIONS);
        MigrationUtils.addAdminRole(realm, AdminRoles.MANAGE_ORGANIZATIONS);
        MigrationUtils.addAdminRole(realm, AdminRoles.QUERY_ORGANIZATIONS);

        addQueryCompositeRoles(realm.getMasterAdminClient());
        if (!realm.getName().equals(Config.getAdminRealm())) {
            addQueryCompositeRoles(realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID));
        }
    }

    /** 将 query-organizations 复合到 view-organizations 下（若两者均存在且尚未关联）。 */
    private void addQueryCompositeRoles(ClientModel client) {
        if (client == null) return;
        RoleModel viewOrganizations = client.getRole(AdminRoles.VIEW_ORGANIZATIONS);
        RoleModel queryOrganizations = client.getRole(AdminRoles.QUERY_ORGANIZATIONS);
        if (viewOrganizations != null && queryOrganizations != null && !viewOrganizations.hasRole(queryOrganizations)) {
            viewOrganizations.addCompositeRole(queryOrganizations);
        }
    }

    /** 若域已启用管理员权限客户端，则初始化 FGAP 模式结构。 */
    private void updateAdminPermissionsSchema(KeycloakSession session, RealmModel realm) {
        if (realm.getAdminPermissionsClient() != null) {
            AdminPermissionsSchema.SCHEMA.init(session, realm);
        }
    }

    /** 将旧版动态作用域属性名重命名为参数化作用域对应的新属性键。 */
    private void renameDynamicScopeAttributes(RealmModel realm) {
        realm.getClientScopesStream().forEach(clientScope -> {
            renameAttribute(clientScope, "is.dynamic.scope", ClientScopeModel.IS_PARAMETERIZED_SCOPE);
            renameAttribute(clientScope, "dynamic.scope.regexp", ClientScopeModel.PARAMETERIZED_SCOPE_REGEXP);
        });
    }

    /** 若旧属性存在则复制到新键并删除旧键。 */
    private void renameAttribute(ClientScopeModel clientScope, String oldName, String newName) {
        String value = clientScope.getAttribute(oldName);
        if (value != null) {
            clientScope.setAttribute(newName, value);
            clientScope.removeAttribute(oldName);
        }
    }

    /** 为标记为二进制但未配置解码器的 LDAP 用户属性映射器默认启用 base64 解码。 */
    private void migrateLdapBinaryAttributeMappers(RealmModel realm) {
        realm.getComponentsStream(realm.getId(), UserStorageProvider.class.getName())
                .filter(c -> LDAPConstants.LDAP_PROVIDER.equals(c.getProviderId()))
                .flatMap(ldap -> realm.getComponentsStream(ldap.getId(), "org.keycloak.storage.ldap.mappers.LDAPStorageMapper"))
                .filter(c -> "user-attribute-ldap-mapper".equals(c.getProviderId()))
                .filter(c -> "true".equals(c.getConfig().getFirst("is.binary.attribute")))
                .filter(c -> c.getConfig().getFirst("binary.attribute.decoder") == null)
                .forEach(c -> {
                    c.getConfig().putSingle("binary.attribute.decoder", "base64");
                    realm.updateComponent(c);
                });
    }

    /** 为缺少 decode.group.uuid.attribute 配置的 LDAP 组映射器写入默认值 false。 */
    private void migrateLdapGroupAttributeMappers(RealmModel realm) {
        realm.getComponentsStream(realm.getId(), UserStorageProvider.class.getName())
                .filter(c -> LDAPConstants.LDAP_PROVIDER.equals(c.getProviderId()))
                .flatMap(ldap -> realm.getComponentsStream(ldap.getId(), "org.keycloak.storage.ldap.mappers.LDAPStorageMapper"))
                .filter(c -> "group-ldap-mapper".equals(c.getProviderId()))
                .filter(c -> c.getConfig().getFirst("decode.group.uuid.attribute") == null)
                .forEach(c -> {
                    c.getConfig().putSingle("decode.group.uuid.attribute", "false");
                    realm.updateComponent(c);
                });
    }

    /** 在启用 SAML 阶梯认证特性时为域创建 AuthnContextClassRef 默认客户端作用域并关联到 SAML 客户端。 */
    private void migrateSamlAuthnContextClassRefClientScope(KeycloakSession session, RealmModel realm) {
        if (Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION_SAML)) {
            MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);

            ClientScopeModel samlAuthnContextClassRefScope = KeycloakModelUtils.getClientScopeByName(realm, "AuthnContextClassRef");
            if (samlAuthnContextClassRefScope == null) {
                // 在域中创建默认的 'AuthnContextClassRef' 客户端作用域
                samlAuthnContextClassRefScope = migrationProvider.addSamlAuthnContextClassRefClientScope(realm);

                // 将该作用域添加到所有现有 SAML 客户端
                session.clients().addClientScopeToAllClients(realm, samlAuthnContextClassRefScope, true);
            }
        }
    }
}
