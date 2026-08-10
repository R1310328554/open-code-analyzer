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

package org.keycloak.federation.kerberos;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.keycloak.common.Profile;
import org.keycloak.common.constants.KerberosConstants;
import org.keycloak.credential.CredentialAuthentication;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.federation.kerberos.impl.KerberosUsernamePasswordAuthenticator;
import org.keycloak.federation.kerberos.impl.SPNEGOAuthenticator;
import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.ImportedUserValidation;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.keycloak.userprofile.AttributeGroupMetadata;
import org.keycloak.userprofile.AttributeMetadata;
import org.keycloak.userprofile.UserProfileDecorator;
import org.keycloak.userprofile.UserProfileMetadata;
import org.keycloak.userprofile.UserProfileUtil;

import org.jboss.logging.Logger;

/**
 * 独立 Kerberos 用户联邦提供器，支持 SPNEGO 与可选密码认证，并负责用户导入与凭证校验。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosFederationProvider implements UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        CredentialInputUpdater,
        CredentialAuthentication,
        ImportedUserValidation,
        UserProfileDecorator,
        UserRegistrationProvider {

    private static final Logger logger = Logger.getLogger(KerberosFederationProvider.class);
    /** 用户属性中存储 Kerberos 主体名的键名。 */
    public static final String KERBEROS_PRINCIPAL = KerberosConstants.KERBEROS_PRINCIPAL;

    protected KeycloakSession session;
    protected UserStorageProviderModel model;
    protected KerberosConfig kerberosConfig;
    protected KerberosFederationProviderFactory factory;

    public KerberosFederationProvider(KeycloakSession session, UserStorageProviderModel model, KerberosFederationProviderFactory factory) {
        this.session = session;
        this.model = model;
        this.kerberosConfig = new KerberosConfig(model);
        this.factory = factory;
    }

    /** {@inheritDoc} 只读模式下返回 {@link ReadOnlyKerberosUserModelDelegate} 包装用户。 */
    @Override
    public UserModel validate(RealmModel realm, UserModel user) {
        if (kerberosConfig.getEditMode() == EditMode.READ_ONLY) {
            return new ReadOnlyKerberosUserModelDelegate(user, this);
        } else {
            return user;
        }
    }

    /** {@inheritDoc} 通过 Kerberos 查询用户名是否存在并导入/关联本地用户。 */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        KerberosUsernamePasswordAuthenticator authenticator = factory.createKerberosUsernamePasswordAuthenticator(kerberosConfig);
        if (authenticator.isUserAvailable(username)) {
            try {
                String kerberosPrincipal = authenticator.getKerberosPrincipal(username);
                return findOrCreateAuthenticatedUser(realm, new KerberosPrincipal(kerberosPrincipal));
            } catch (LoginException le) {
                throw new IllegalStateException("Should not happen", le);
            }
        } else {
            return null;
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        return null;
    }

    @Override
    public void preRemove(RealmModel realm) {

    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {

    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel) || !PasswordCredentialModel.TYPE.equals(input.getType())) return false;
        if (kerberosConfig.getEditMode() == EditMode.READ_ONLY) {
            throw new ReadOnlyException("Can't change password in Keycloak database. Change password with your Kerberos server");
        }
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    /** {@inheritDoc} 支持 KERBEROS 凭证，配置允许时还支持密码凭证。 */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(UserCredentialModel.KERBEROS) || (kerberosConfig.isAllowPasswordAuthentication() && credentialType.equals(PasswordCredentialModel.TYPE));
    }

    @Override
    public boolean supportsCredentialAuthenticationFor(String type) {
        return UserCredentialModel.KERBEROS.equals(type);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) return false;
        if (input.getType().equals(PasswordCredentialModel.TYPE) && !((UserCredentialManager) user.credentialManager()).isConfiguredLocally(PasswordCredentialModel.TYPE)) {
            return validPassword(user.getFirstAttribute(KERBEROS_PRINCIPAL), input.getChallengeResponse());
        } else {
            return false; // invalid cred type
        }
    }

    protected boolean validPassword(String kerberosPrincipal, String password) {
        if (kerberosConfig.isAllowPasswordAuthentication()) {
            KerberosUsernamePasswordAuthenticator authenticator = factory.createKerberosUsernamePasswordAuthenticator(kerberosConfig);
            return authenticator.validUser(kerberosPrincipal, password);
        } else {
            return false;
        }
    }

    /** {@inheritDoc} 执行 SPNEGO 握手与用户查找/创建，支持多步 CONTINUE 响应。 */
    @Override
    public CredentialValidationOutput authenticate(RealmModel realm, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) return null;
        UserCredentialModel credential = (UserCredentialModel)input;
        if (credential.getType().equals(UserCredentialModel.KERBEROS)) {
            SPNEGOAuthenticator spnegoAuthenticator = (SPNEGOAuthenticator) credential.getNote(KerberosConstants.AUTHENTICATED_SPNEGO_CONTEXT);
            if (spnegoAuthenticator != null) {
                logger.debugf("SPNEGO authentication already performed by previous provider. Provider '%s' will try to lookup user with kerberos principal '%s'", this, spnegoAuthenticator.getAuthenticatedKerberosPrincipal());
            } else {
                String spnegoToken = credential.getChallengeResponse();
                spnegoAuthenticator = factory.createSPNEGOAuthenticator(spnegoToken, kerberosConfig);

                spnegoAuthenticator.authenticate();
            }

            Map<String, String> state = new HashMap<>();
            if (spnegoAuthenticator.isAuthenticated()) {
                KerberosPrincipal kerberosPrincipal = spnegoAuthenticator.getAuthenticatedKerberosPrincipal();
                UserModel user = findOrCreateAuthenticatedUser(realm, kerberosPrincipal);
                if (user == null) {
                    // 将已认证的 SPNEGO 上下文附在凭证上，供链中其他 LDAP/Kerberos 提供器查找用户（如 MSAD 林信任）
                    // 注意：Kerberos 重放保护使其他提供器无法再次执行 SPNEGO
                    credential.setNote(KerberosConstants.AUTHENTICATED_SPNEGO_CONTEXT, spnegoAuthenticator);

                    return CredentialValidationOutput.fallback();
                } else {
                    String delegationCredential = spnegoAuthenticator.getSerializedDelegationCredential();
                    if (delegationCredential != null) {
                        state.put(KerberosConstants.GSS_DELEGATION_CREDENTIAL, delegationCredential);
                    }

                    return new CredentialValidationOutput(user, CredentialValidationOutput.Status.AUTHENTICATED, state);
                }
            }  else if (spnegoAuthenticator.getResponseToken() != null) {
                // SPNEGO 握手需多步完成时返回 CONTINUE
                logger.tracef("SPNEGO Handshake will continue");
                state.put(KerberosConstants.RESPONSE_TOKEN, spnegoAuthenticator.getResponseToken());
                return new CredentialValidationOutput(null, CredentialValidationOutput.Status.CONTINUE, state);
            } else {
                logger.tracef("SPNEGO Handshake not successful");
                return CredentialValidationOutput.fallback();
            }

        } else {
            return null;
        }
    }

    @Override
    public void close() {

    }

    /**
     * Kerberos 认证成功后查找或创建本地用户。
     *
     * @param realm realm
     * @param kerberosPrincipal Kerberos 主体
     * @return 找到或新建的用户；若同名用户已存在但未关联本提供器则返回 null
     */
    protected UserModel findOrCreateAuthenticatedUser(RealmModel realm, KerberosPrincipal kerberosPrincipal) {
        UserModel user = UserStoragePrivateUtil.userLocalStorage(session).searchForUserByUserAttributeStream(realm, KerberosConstants.KERBEROS_PRINCIPAL, kerberosPrincipal.toString())
                .findFirst().orElse(null);

        if (user != null) {
            user = session.users().getUserById(realm, user.getId());  // make sure we get a cached instance
            logger.debug("Kerberos authenticated user " + kerberosPrincipal + " found in Keycloak storage");

            if (!model.getId().equals(user.getFederationLink())) {
                logger.warn("User with username " + kerberosPrincipal + " already exists, but is not linked to provider [" + model.getName() + "]");
                return null;
            } else {
                UserModel proxied = validate(realm, user);
                if (proxied != null) {
                    return proxied;
                } else {
                    logger.warn("User with username " + kerberosPrincipal.getPrefix() + " already exists and is linked to provider [" + model.getName() +
                            "] but kerberos principal is not correct. Kerberos principal on user is: " + user.getFirstAttribute(KERBEROS_PRINCIPAL));
                    logger.warn("Will re-create user");
                    new UserManager(session).removeUser(realm, user, UserStoragePrivateUtil.userLocalStorage(session));
                }
            }
        }

        logger.debug("Kerberos authenticated user " + kerberosPrincipal + " not in Keycloak storage. Creating him");
        return importUserToKeycloak(realm, kerberosPrincipal);
    }

    protected UserModel importUserToKeycloak(RealmModel realm, KerberosPrincipal kerberosPrincipal) {
        // 根据 Kerberos realm 推测邮箱地址
        String email = kerberosPrincipal.getPrefix() + "@" + kerberosPrincipal.getRealm().toLowerCase();
        // realm 与配置一致时用户名取前缀；信任域场景使用完整主体作为用户名
        String username = (kerberosPrincipal.getRealm().equalsIgnoreCase(kerberosConfig.getKerberosRealm())) ? kerberosPrincipal.getPrefix() : email;

        logger.debugf("Creating kerberos user %s with username: %s, email: %s to local Keycloak storage", kerberosPrincipal, username, email);
        UserModel user = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, username);
        user.setEnabled(true);
        user.setEmail(email);
        user.setFederationLink(model.getId());
        user.setSingleAttribute(KERBEROS_PRINCIPAL, kerberosPrincipal.toString());

        if (kerberosConfig.isUpdateProfileFirstLogin()) {
            if (isUpdateEmailEnabled(realm)) {
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_EMAIL);
            }
            user.addRequiredAction(UserModel.RequiredAction.UPDATE_PROFILE);
        }

        return validate(realm, user);
    }

    private static boolean isUpdateEmailEnabled(RealmModel realm) {
        if (!Profile.isFeatureEnabled(Profile.Feature.UPDATE_EMAIL)) {
            return false;
        }

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(RequiredAction.UPDATE_EMAIL.name());

        return model != null && model.isEnabled();
    }

    @Override
    public String toString() {
        return "KerberosFederationProvider - " + model.getName();
    }

    @Override
    public List<AttributeMetadata> decorateUserProfile(String providerId, UserProfileMetadata metadata) {
        int guiOrder = (int) metadata.getAttributes().stream()
                .map(AttributeMetadata::getName)
                .distinct()
                .count();

        AttributeGroupMetadata metadataGroup = UserProfileUtil.lookupUserMetadataGroup(session);
        return Collections.singletonList(UserProfileUtil.createAttributeMetadata(KerberosConstants.KERBEROS_PRINCIPAL, metadata, metadataGroup, guiOrder++, model.getName()));
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        // no support for creating users
        return null;
    }
}
