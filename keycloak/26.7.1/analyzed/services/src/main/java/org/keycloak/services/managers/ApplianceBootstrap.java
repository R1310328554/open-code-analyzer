/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.managers;

import org.keycloak.Config;
import org.keycloak.common.Version;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.config.BootstrapAdminOptions;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.DefaultKeyProviders;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.services.ServicesLogger;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.utils.StringUtil;

import static org.keycloak.models.Constants.DEFAULT_ACCESS_CODE_LIFESPAN;
import static org.keycloak.models.Constants.DEFAULT_ACCESS_CODE_LIFESPAN_LOGIN;
import static org.keycloak.models.Constants.DEFAULT_ACCESS_CODE_LIFESPAN_USER_ACTION;
import static org.keycloak.models.Constants.DEFAULT_SESSION_IDLE_TIMEOUT;
import static org.keycloak.models.Constants.DEFAULT_SESSION_MAX_LIFESPAN;
import static org.keycloak.models.UserModel.IS_TEMP_ADMIN_ATTR_NAME;

/**
 * 首次安装引导工具。
 * <p>创建 master 领域、临时/初始管理员用户及服务账户客户端。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ApplianceBootstrap {

    /** Keycloak 会话 */
    private final KeycloakSession session;

    public ApplianceBootstrap(KeycloakSession session) {
        this.session = session;
    }

    /** @return 是否为全新安装（admin realm 尚不存在） */
    public boolean isNewInstall() {
        return session.realms().getRealmByName(Config.getAdminRealm()) == null;
    }

    /** @return master 领域是否尚无用户 */
    public boolean isNoMasterUser() {
        RealmModel realm = session.realms().getRealmByName(Config.getAdminRealm());
        session.getContext().setRealm(realm);
        return session.users().getUsersCount(realm, true) == 0;
    }

    /** 创建默认 master 领域及密钥 Provider。
     * @throws IllegalStateException 领域已存在时
     */
    public void createMasterRealm() {
        if (!isNewInstall()) {
            throw new IllegalStateException("Can't create default realm as realms already exists");
        }

        String adminRealmName = Config.getAdminRealm();
        ServicesLogger.LOGGER.initializingAdminRealm(adminRealmName);

        RealmManager manager = new RealmManager(session);
        RealmModel realm = manager.createRealm(adminRealmName);
        realm.setName(adminRealmName);
        realm.setDisplayName(Version.NAME);
        realm.setDisplayNameHtml(Version.NAME_HTML);
        realm.setEnabled(true);
        realm.addRequiredCredential(CredentialRepresentation.PASSWORD);
        realm.setDefaultSignatureAlgorithm(Constants.DEFAULT_SIGNATURE_ALGORITHM);
        realm.setSsoSessionIdleTimeout(DEFAULT_SESSION_IDLE_TIMEOUT);
        realm.setAccessTokenLifespan(60);
        realm.setAccessTokenLifespanForImplicitFlow(Constants.DEFAULT_ACCESS_TOKEN_LIFESPAN_FOR_IMPLICIT_FLOW_TIMEOUT);
        realm.setSsoSessionMaxLifespan(DEFAULT_SESSION_MAX_LIFESPAN);
        realm.setOfflineSessionIdleTimeout(Constants.DEFAULT_OFFLINE_SESSION_IDLE_TIMEOUT);
        // KEYCLOAK-7688：离线 Token 的离线会话最大寿命
        realm.setOfflineSessionMaxLifespanEnabled(false);
        realm.setOfflineSessionMaxLifespan(Constants.DEFAULT_OFFLINE_SESSION_MAX_LIFESPAN);
        realm.setAccessCodeLifespan(DEFAULT_ACCESS_CODE_LIFESPAN);
        realm.setAccessCodeLifespanUserAction(DEFAULT_ACCESS_CODE_LIFESPAN_USER_ACTION);
        realm.setAccessCodeLifespanLogin(DEFAULT_ACCESS_CODE_LIFESPAN_LOGIN);
        realm.setSslRequired(SslRequired.EXTERNAL);
        realm.setRegistrationAllowed(false);
        realm.setRegistrationEmailAsUsername(false);

        session.getContext().setRealm(realm);
        DefaultKeyProviders.createProviders(realm);

        // master 领域用户配置更宽松
        // 除 username 外 firstName/lastName/email 均非必填
        UserProfileProvider UserProfileProvider = session.getProvider(UserProfileProvider.class);
        UPConfig upConfig = UserProfileProvider.getConfiguration();
        for (UPAttribute attr : upConfig.getAttributes()) {
            if (!UserModel.USERNAME.equals(attr.getName())) {
                attr.setRequired(null);
            }
        }
        UserProfileProvider.setConfiguration(upConfig);
    }

    /**
     * 创建 master 领域管理员用户。
     * @param username 管理员用户名
     * @param password 密码
     * @param isTemporary 是否为临时管理员
     * @param initialUser 为 true 时仅在没有其他用户时创建
     * @return 创建失败返回 false
     */
    public boolean createMasterRealmAdminUser(String username, String password, boolean isTemporary, /*Integer expriationMinutes,*/ boolean initialUser) {
        return createMasterRealmAdminUser(username, password, null, null, null, isTemporary, initialUser);
    }

    /**
     * 创建带扩展资料的管理员用户。
     * @param username 用户名
     * @param password 密码
     * @param firstName 名（可选）
     * @param lastName 姓（可选）
     * @param email 邮箱（可选）
     * @param isTemporary 是否临时管理员
     * @param initialUser 为 true 时仅在没有其他用户时创建
     * @return 创建失败返回 false
     */
    public boolean createMasterRealmAdminUser(String username, String password, String firstName, String lastName, String email, boolean isTemporary, /*Integer expriationMinutes,*/ boolean initialUser) {
        RealmModel realm = session.realms().getRealmByName(Config.getAdminRealm());
        session.getContext().setRealm(realm);

        username = StringUtil.isBlank(username) ? BootstrapAdminOptions.DEFAULT_TEMP_ADMIN_USERNAME : username;
        //expriationMinutes = expriationMinutes == null ? DEFAULT_TEMP_ADMIN_EXPIRATION : expriationMinutes;

        if (initialUser && session.users().getUsersCount(realm, true) > 0) {
            ServicesLogger.LOGGER.addAdminUserFailedUsersExist(Config.getAdminRealm());
            return false;
        }

        try {
            UserModel adminUser = session.users().addUser(realm, username);
            adminUser.setEnabled(true);
            if (StringUtil.isNotBlank(firstName)) {
                adminUser.setFirstName(firstName);
            }
            if (StringUtil.isNotBlank(lastName)) {
                adminUser.setLastName(lastName);
            }
            if (StringUtil.isNotBlank(email)) {
                adminUser.setEmail(email);
            }
            if (isTemporary) {
                adminUser.setSingleAttribute(IS_TEMP_ADMIN_ATTR_NAME, Boolean.TRUE.toString());
                // also set the expiration - could be relative to a creation timestamp, or computed
            }

            UserCredentialModel usrCredModel = UserCredentialModel.password(password);
            adminUser.credentialManager().updateCredential(usrCredModel);

            RoleModel adminRole = realm.getRole(AdminRoles.ADMIN);
            adminUser.grantRole(adminRole);

            if (isTemporary)
                ServicesLogger.LOGGER.createdTemporaryAdminUser(username);
            else
                ServicesLogger.LOGGER.createdInitialAdminUser(username);
        } catch (ModelDuplicateException e) {
            ServicesLogger.LOGGER.addUserFailedUserExists(username, Config.getAdminRealm());
            return false;
        }
        return true;
    }

    /**
     * 创建临时管理员服务账户客户端。
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥
     * @return 创建失败返回 false
     */
    public boolean createTemporaryMasterRealmAdminService(String clientId, String clientSecret /*, Integer expriationMinutes*/) {
        RealmModel realm = session.realms().getRealmByName(Config.getAdminRealm());
        session.getContext().setRealm(realm);

        clientId = StringUtil.isBlank(clientId) ? BootstrapAdminOptions.DEFAULT_TEMP_ADMIN_SERVICE : clientId;
        //expriationMinutes = expriationMinutes == null ? DEFAULT_TEMP_ADMIN_EXPIRATION : expriationMinutes;

        ClientRepresentation adminClient = new ClientRepresentation();
        adminClient.setClientId(clientId);
        adminClient.setEnabled(true);
        adminClient.setServiceAccountsEnabled(true);
        adminClient.setStandardFlowEnabled(false);
        adminClient.setPublicClient(false);
        adminClient.setSecret(clientSecret);

        try {
            ClientModel adminClientModel = ClientManager.createClient(session, realm, adminClient);

            new ClientManager(new RealmManager(session)).enableServiceAccount(adminClientModel);
            UserModel serviceAccount = session.users().getServiceAccount(adminClientModel);
            RoleModel adminRole = realm.getRole(AdminRoles.ADMIN);
            serviceAccount.grantRole(adminRole);

            adminClientModel.setAttribute(Constants.USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED, Boolean.TRUE.toString());
            adminClientModel.setAttribute(IS_TEMP_ADMIN_ATTR_NAME, Boolean.TRUE.toString());
            // also set the expiration - could be relative to a creation timestamp, or computed

            ServicesLogger.LOGGER.createdTemporaryAdminService(clientId);
        } catch (ModelDuplicateException e) {
            ServicesLogger.LOGGER.addClientFailedClientExists(clientId, Config.getAdminRealm());
            return false;
        }
        return true;
    }

    public void createMasterRealmUser(String username, String password, boolean isTemporary) {
        createMasterRealmAdminUser(username, password, null, null, null, isTemporary, true);
    }

    public void createMasterRealmUser(String username, String password, String firstName, String lastName, String email, boolean isTemporary) {
        createMasterRealmAdminUser(username, password, firstName, lastName, email, isTemporary, true);
    }

}
