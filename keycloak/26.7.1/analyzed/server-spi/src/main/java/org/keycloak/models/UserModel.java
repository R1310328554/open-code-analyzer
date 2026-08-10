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

package org.keycloak.models;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.provider.ProviderEvent;

import static org.keycloak.utils.StringUtil.isNotBlank;

/**
 * 用户模型：realm 内用户的核心 SPI，涵盖属性、组、必需操作与凭据管理。
 * <p>继承 {@link RoleMapperModel}，是认证与授权流程中的主体表示。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserModel extends RoleMapperModel, Model {
    String ID = "id";
    String USERNAME = "username";
    String FIRST_NAME = "firstName";
    String LAST_NAME = "lastName";
    String EMAIL = "email";
    String EMAIL_PENDING = "kc.email.pending";
    String EMAIL_VERIFIED = "emailVerified";
    String DID = "did";
    String LOCALE = "locale";
    String ENABLED = "enabled";
    String IDP_ALIAS = "keycloak.session.realm.users.query.idp_alias";
    String IDP_USER_ID = "keycloak.session.realm.users.query.idp_user_id";
    String INCLUDE_SERVICE_ACCOUNT = "keycloak.session.realm.users.query.include_service_account";
    String GROUPS = "keycloak.session.realm.users.query.groups";
    String SEARCH = "keycloak.session.realm.users.query.search";
    String EXACT = "keycloak.session.realm.users.query.exact";
    String CREATED_AFTER = "keycloak.session.realm.users.query.created_after";
    String CREATED_BEFORE = "keycloak.session.realm.users.query.created_before";
    String DISABLED_REASON = "disabledReason";
    // 标记临时管理员/服务账户的属性名
    //attribute name used to mark a temporary admin user/service account as temporary
    String IS_TEMP_ADMIN_ATTR_NAME = "is_temporary_admin";
    String CREATED_TIMESTAMP = "createdTimestamp";

    /** 按用户名（不区分大小写）排序的比较器。 */
    Comparator<UserModel> COMPARE_BY_USERNAME = Comparator.comparing(UserModel::getUsername, String.CASE_INSENSITIVE_ORDER);

    /** 用户删除事件。 */
    interface UserRemovedEvent extends ProviderEvent {
        RealmModel getRealm();
        UserModel getUser();
        KeycloakSession getKeycloakSession();
    }

    /** 用户删除前事件。 */
    interface UserPreRemovedEvent extends ProviderEvent {
        RealmModel getRealm();
        UserModel getUser();
        KeycloakSession getKeycloakSession();
    }

    // 无默认实现，以便抽象子类以不同方式提供用户名
    // No default method here to allow Abstract subclasses where the username is provided in a different manner
    /** @return 用户名 */
    String getUsername();

    /**
     * 设置用户名。
     * Sets username for this user.
     *
     * No default method here to allow Abstract subclasses where the username is provided in a different manner
     *
     * @param username username string
     */
    void setUsername(String username);

    /**
     * 获取用户创建时间戳；旧用户可能为 null。
     * Get timestamp of user creation. May be null for old users created before this feature introduction.
     */
    Long getCreatedTimestamp();

    /** @param timestamp 创建时间戳 */
    void setCreatedTimestamp(Long timestamp);

    /**
     * 获取最后修改时间戳；未修改过的用户可能为 null。
     * Get timestamp of last user modification. May be null for users that have not been modified
     * since this feature was introduced.
     */
    default Long getLastModifiedTimestamp() {
        return null;
    }

    default void setLastModifiedTimestamp(Long timestamp) {
    }

    /** @return 用户是否启用 */
    boolean isEnabled();

    /** @param enabled 是否启用 */
    void setEnabled(boolean enabled);

    /**
     * 设置属性单值并移除该属性的其他已有值。
     * Set single value of specified attribute. Remove all other existing values of this attribute
     *
     * @param name
     * @param value
     */
    void setSingleAttribute(String name, String value);

    /** @param name 属性名
     * @param values 属性值列表 */
    void setAttribute(String name, List<String> values);

    /** @param name 属性名 */
    void removeAttribute(String name);

    /**
     * @param name
     * @return null if there is not any value of specified attribute or first value otherwise. Don't throw exception if there are more values of the attribute
     */
    String getFirstAttribute(String name);

    /**
     * 获取指定属性名的全部值。
     * Obtains all values associated with the specified attribute name.
     *
     * @param name the name of the attribute.
     * @return a non-null {@link Stream} of attribute values.
     */
    Stream<String> getAttributeStream(final String name);

    /** @return 全部用户属性映射 */
    Map<String, List<String>> getAttributes();

    /**
     * 获取用户关联的必需操作别名。
     * Obtains the aliases of required actions associated with the user.
     *
     * @return a non-null {@link Stream} of required action aliases.
     */
    Stream<String> getRequiredActionsStream();

    /** @param action 必需操作别名 */
    void addRequiredAction(String action);

    /** @param action 必需操作别名 */
    void removeRequiredAction(String action);

    default void addRequiredAction(RequiredAction action) {
        if (action == null) return;
        String actionName = action.name();
        addRequiredAction(actionName);
    }

    default void removeRequiredAction(RequiredAction action) {
        if (action == null) return;
        String actionName = action.name();
        removeRequiredAction(actionName);
    }

    /** @return 名 */
    String getFirstName();

    /** @param firstName 名 */
    void setFirstName(String firstName);

    /** @return 姓 */
    String getLastName();

    /** @param lastName 姓 */
    void setLastName(String lastName);

    /** @return 邮箱 */
    String getEmail();

    /**
     * 设置用户邮箱。
     * Sets email for this user.
     *
     * @param email the email
     */
    void setEmail(String email);

    /** @return 邮箱是否已验证 */
    boolean isEmailVerified();

    /** @param verified 是否已验证 */
    void setEmailVerified(boolean verified);

    /**
     * 获取用户所属组。
     * Obtains the groups associated with the user.
     *
     * @return a non-null {@link Stream} of groups.
     */
    Stream<GroupModel> getGroupsStream();

    /**
     * 返回 realm 内按名称搜索的分页组流。
     * Returns a paginated stream of groups within this realm with search in the name
     *
     * @param search Case insensitive string which will be searched for. Ignored if null.
     * @param first Index of first group to return. Ignored if negative or {@code null}.
     * @param max Maximum number of records to return. Ignored if negative or {@code null}.
     * @return Stream of desired groups. Never returns {@code null}.
     */
    default Stream<GroupModel> getGroupsStream(String search, Integer first, Integer max) {
        if (search != null) search = search.toLowerCase();
        final String finalSearch = search;
        Stream<GroupModel> groupModelStream = getGroupsStream()
                .filter(group -> finalSearch == null || group.getName().toLowerCase().contains(finalSearch));

        if (first != null && first > 0) {
            groupModelStream = groupModelStream.skip(first);
        }

        if (max != null && max >= 0) {
            groupModelStream = groupModelStream.limit(max);
        }

        return groupModelStream;
    }

    default long getGroupsCount() {
        return getGroupsCountByNameContaining(null);
    }

    default long getGroupsCountByNameContaining(String search) {
        if (search == null) {
            return getGroupsStream().count();
        }

        String s = search.toLowerCase();
        return getGroupsStream().filter(group -> group.getName().toLowerCase().contains(s)).count();
    }

    /** @param group 待加入的组 */
    void joinGroup(GroupModel group);
    default void joinGroup(GroupModel group, MembershipMetadata metadata) {
        joinGroup(group);
    }
    /** @param group 待离开的组 */
    void leaveGroup(GroupModel group);
    /** @param group 组
     * @return 是否为该组成员 */
    boolean isMemberOf(GroupModel group);

    /** @return 联邦存储 Provider 链接 ID */
    String getFederationLink();
    /** @param link 联邦链接 ID */
    void setFederationLink(String link);

    /** @return 服务账户关联客户端内部 ID */
    String getServiceAccountClientLink();
    /** @param clientInternalId 客户端内部 ID */
    void setServiceAccountClientLink(String clientInternalId);

    /**
     * 指示用户是本地账户还是外部联邦账户。
     * Indicates if this {@link UserModel} maps to a local account or an account
     * federated from an external user storage.
     *
     * @return {@code true} if a federated account. Otherwise, {@code false}.
     */
    default boolean isFederated() {
        return isNotBlank(getFederationLink());
    }

    /**
     * 返回用于验证与更新该用户凭据的 {@link SubjectCredentialManager}。
     * Instance of a user credential manager to validate and update the credentials of this user.
     */
    SubjectCredentialManager credentialManager();

    /** 用户必需操作枚举。 */
    enum RequiredAction {
        VERIFY_EMAIL,
        UPDATE_PROFILE,
        CONFIGURE_TOTP,
        CONFIGURE_RECOVERY_AUTHN_CODES,
        UPDATE_PASSWORD,
        TERMS_AND_CONDITIONS,
        VERIFY_PROFILE,
        UPDATE_EMAIL
    }
}
