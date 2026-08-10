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

package org.keycloak.storage.ldap.mappers;

import java.util.List;
import java.util.Set;
import javax.naming.AuthenticationException;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.user.SynchronizationResult;

/**
 * LDAP 存储映射器 SPI：定义 LDAP 联邦与 Keycloak 用户模型之间的同步、导入与代理扩展点。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface LDAPStorageMapper extends Provider {

    /**
     * 从联邦存储同步数据到 Keycloak（例如预加载角色并写入 Keycloak 数据库）。
     * 仅当映射器支持同步时适用。
     *
     */
    SynchronizationResult syncDataFromFederationProviderToKeycloak(RealmModel realm);

    /**
     * 从 Keycloak 同步数据回联邦存储。
     *
     **/
    SynchronizationResult syncDataFromKeycloakToFederationProvider(RealmModel realm);

    /**
     * 返回属于指定组的用户；不支持组存储时返回空列表。
     */
    List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults);

    /**
     * 返回拥有指定角色的用户；不支持角色存储时返回空列表。
     * @param realm
     * @param role
     * @param firstResult
     * @param maxResults
     * @return
     */
    List<UserModel> getRoleMembers(RealmModel realm, RoleModel role, int firstResult, int maxResults);

    /**
     * 从 LDAP 导入用户到本地 Keycloak 数据库时调用。
     *
     * @param ldapUser
     * @param user
     * @param realm
     * @param isCreate true 表示首次从 LDAP 导入；false 表示用户已存在，正在从 LDAP 升级/同步
     */
    void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate);


    /**
     * 用户已在 Keycloak DB 创建、即将写入 LDAP 时调用。
     *
     * @param ldapUser
     * @param localUser
     * @param realm
     */
    void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm);

    /**
     * 返回此映射器要求 LDAP 条目必须包含的属性名集合。
     *
     * @return 必填属性列表，或 null
     */
    Set<String> mandatoryAttributeNames();

    /**
     * 返回此映射器映射到 Keycloak 用户的用户模型属性名集合。
     *
     * @return 用户属性集合；无属性时返回空集，永不为 null
     */
    Set<String> getUserAttributes();

    /**
     * 为 LDAP 联邦用户提供用户代理，可在读取/写入时注入映射逻辑。
     *
     * @param ldapUser
     * @param delegate
     * @param realm
     * @return
     */
    UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm);


    /**
     * 执行 LDAP 用户查询前调用，可修改返回属性、查询条件等。
     *
     * @param query
     */
    void beforeLDAPQuery(LDAPQuery query);

    /**
     * 指定用户 LDAP 认证失败时调用。若任一映射器返回 true，则不再重新抛出 {@link AuthenticationException}。
     *
     * @param user
     * @param ldapUser
     * @param ldapException
     * @return true 表示映射器已处理异常；此时不会重新抛出 AuthenticationException
     */
    boolean onAuthenticationFailure(LDAPObject ldapUser, UserModel user, AuthenticationException ldapException, RealmModel realm);

    /**
     * 获取与此映射器关联的 LDAP 联邦提供者。
     *
     * @return
     */
    public LDAPStorageProvider getLdapProvider();
}
