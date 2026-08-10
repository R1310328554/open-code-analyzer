/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.ipatuura_user_spi;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialAuthentication;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.ipatuura_user_spi.authenticator.IpatuuraAuthenticator;
import org.keycloak.ipatuura_user_spi.schemas.SCIMError;
import org.keycloak.ipatuura_user_spi.schemas.SCIMUser;
import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.ImportedUserValidation;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

/**
 * Ipatuura SCIM 用户联邦存储提供器：用户查找、密码/Kerberos 认证与 SCIM 同步。
 *
 * @author <a href="mailto:jstephen@redhat.com">Justin Stephenson</a>
 * @version $Revision: 1 $
 */
public class IpatuuraUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator,
        CredentialAuthentication, UserRegistrationProvider, UserQueryProvider, ImportedUserValidation {
    protected KeycloakSession session;
    protected ComponentModel model;
    protected Ipatuura ipatuura;
    private static final Logger logger = Logger.getLogger(IpatuuraUserStorageProvider.class);
    protected final Set<String> supportedCredentialTypes = new HashSet<>();
    protected IpatuuraUserStorageProviderFactory factory;

    /** 构造提供器并注册支持的密码凭证类型。 */
    public IpatuuraUserStorageProvider(KeycloakSession session, ComponentModel model, Ipatuura ipatuura,
                                       IpatuuraUserStorageProviderFactory factory) {
        this.session = session;
        this.model = model;
        this.ipatuura = ipatuura;
        this.factory = factory;

        supportedCredentialTypes.add(PasswordCredentialModel.TYPE);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realm, username);
    }

    /** {@inheritDoc} 查找或从 SCIM 导入用户（剥离 @realm 后缀）。 */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        /* GSSAPI 用户可能以 user@realm 形式传入，需剥离 realm 后缀 */
        int idx = username.indexOf("@");
        if (idx != -1) {
            username = username.substring(0, idx);
        }

        UserModel user = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, username);
        if (user != null) {
            logger.debug("User already exists in keycloak");
            return user;
        } else {
            return createUserInKeycloak(realm, username);
        }
    }

    /** 从 SCIM 拉取用户属性并在 Keycloak 本地存储创建/关联。 */
    protected UserModel createUserInKeycloak(RealmModel realm, String username) {
        SCIMUser scimuser = ipatuura.getUserByUsername(username);
        if (scimuser.getTotalResults() == 0) {
            return null;
        }
        UserModel user = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, username);
        user.setEmail(ipatuura.getEmail(scimuser));
        user.setFirstName(ipatuura.getFirstName(scimuser));
        user.setLastName(ipatuura.getLastName(scimuser));
        user.setFederationLink(model.getId());
        user.setEnabled(ipatuura.getActive(scimuser));

        for (String name : ipatuura.getGroupsList(scimuser)) {
            Stream<GroupModel> groupsStream = session.groups().searchForGroupByNameStream(realm, name, false, null, null);
            GroupModel group = groupsStream.findFirst().orElse(null);

            if (group == null) {
                logger.debugv("No group found, creating group: {0}", name);
                group = session.groups().createGroup(realm, name);
            }
            user.joinGroup(group);
        }

        logger.debugv("Creating SCIM user {0} in keycloak", username);
        return new IpatuuraUserModelDelegate(ipatuura, user, model);
    }

    @Override
    public void close() {

    }

    public Set<String> getSupportedCredentialTypes() {
        return new HashSet<String>(this.supportedCredentialTypes);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return getSupportedCredentialTypes().contains(credentialType);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getSupportedCredentialTypes().contains(credentialType);
    }

    /** {@inheritDoc} 本地未配置密码时委托 SCIM 校验。 */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel))
            return false;

        /* 密码可在 Keycloak 本地或 SCIM 服务端校验（本地优先） */
        if (((UserCredentialManager) user.credentialManager()).isConfiguredLocally(input.getType())) {
            logger.debugv("Local password validation for {0}", user.getUsername());
            /* 返回 false 以回退到下一验证器 */
            return false;
        } else {
            logger.debugv("Delegated password validation for {0}", user.getUsername());
            Ipatuura ipatuura = this.ipatuura;
            return ipatuura.isValid(user.getUsername(), input.getChallengeResponse());
        }
    }

    /** {@inheritDoc} 同步 SCIM 中的姓名/邮箱到本地用户。 */
    @Override
    public UserModel validate(RealmModel realm, UserModel local) {
        Ipatuura ipatuura = this.ipatuura;

        SCIMUser scimuser = ipatuura.getUserByUsername(local.getUsername());
        String fname = ipatuura.getFirstName(scimuser);
        String lname = ipatuura.getLastName(scimuser);
        String email = ipatuura.getEmail(scimuser);

        if (!local.getFirstName().equals(fname)) {
            local.setFirstName(fname);
        }
        if (!local.getLastName().equals(lname)) {
            local.setLastName(lname);
        }
        if (!local.getEmail().equals(email)) {
            local.setEmail(email);
        }

        return new IpatuuraUserModelDelegate(this.ipatuura, local, model);
    }

    /** {@inheritDoc} 在 SCIM 创建用户后导入 Keycloak。 */
    @Override
    public UserModel addUser(RealmModel realm, String username) {
        Ipatuura ipatuura = this.ipatuura;

        SimpleHttpResponse resp = ipatuura.createUser(username);

        try {
            if (resp.getStatus() != HttpStatus.SC_CREATED) {
                logger.warn("Unexpected create status code returned");
                SCIMError error = resp.asJson(SCIMError.class);
                logger.warn(error.getDetail());
                resp.close();
                return null;
            }
            resp.close();
        } catch (IOException e) {
            logger.errorv("Error: {0}", e.getMessage());
            throw new RuntimeException(e);
        }

        return createUserInKeycloak(realm, username);
    }

    /** {@inheritDoc} 从 SCIM 删除对应用户。 */
    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        logger.debugv("Removing user: {0}", user.getUsername());
        Ipatuura ipatuura = this.ipatuura;

        SimpleHttpResponse resp = ipatuura.deleteUser(user.getUsername());
        Boolean status = false;
        try {
            status = resp.getStatus() == HttpStatus.SC_NO_CONTENT;
            resp.close();
        } catch (IOException e) {
            logger.errorv("Error: {0}", e.getMessage());
            throw new RuntimeException(e);
        }
        return status;
    }

    /** 按用户名在 SCIM 搜索并导入尚未存在的用户。 */
    private Stream<UserModel> performSearch(RealmModel realm, String search) {
        List<UserModel> users = new LinkedList<>();
        Ipatuura ipatuura = this.ipatuura;

        SCIMUser scimuser = ipatuura.getUserByUsername(search);
        if (scimuser.getTotalResults() > 0) {
            logger.debug("User found by username!");
            if (UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, search) == null) {
                UserModel user = getUserByUsername(realm, ipatuura.getUserName(scimuser));
                users.add(user);
            } else {
                logger.debug("User exists!");
            }

            return users.stream();
        }

        return users.stream();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel arg0, GroupModel arg1, Integer arg2, Integer arg3) {
        return Stream.empty();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        Ipatuura ipatuura = this.ipatuura;

        SCIMUser user = null;
        SimpleHttpResponse response;
        try {
            response = ipatuura.clientRequest("/Users", "GET", null);
            user = response.asJson(SCIMUser.class);
            response.close();
        } catch (Exception e) {
            logger.errorv("Error: {0}", e.getMessage());
            throw new RuntimeException(e);
        }

        return user.getTotalResults();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
            Integer maxResults) {
        String search = params.get(UserModel.SEARCH);
        /* 当前仅支持按用户名搜索 */
        if (search == null)
            return Stream.empty();
        return performSearch(realm, search);
    }

    @Override
    public boolean supportsCredentialAuthenticationFor(String type) {
        return UserCredentialModel.KERBEROS.equals(type);
    }

    /** {@inheritDoc} 处理 Kerberos SPNEGO 凭证并查找/导入用户。 */
    @Override
    public CredentialValidationOutput authenticate(RealmModel realm, CredentialInput input) {
        Map<String, String> state = new HashMap<>();
        String username = null;
        IpatuuraAuthenticator ipatuuraAuthenticator = factory.createSCIMAuthenticator();

        String token = ipatuuraAuthenticator.getToken(session);
        if (token != null) {
            username = ipatuura.gssAuth(token);

            /* 剥离 Kerberos 主体中的 @realm */
            int idx = username.indexOf("@");
            if (idx != -1) {
                username = username.substring(0, idx);
            }
            logger.debug("GSSAPI authenticating with user " + username);
        }

        UserModel user = getUserByUsername(realm, username);
        if (user == null) {
            logger.debug("CredentialValidationOutput failed");
            return CredentialValidationOutput.failed();
        }
        logger.debug("CredentialValidationOutput success!");
        return new CredentialValidationOutput(user, CredentialValidationOutput.Status.AUTHENTICATED, state);
    }
}
