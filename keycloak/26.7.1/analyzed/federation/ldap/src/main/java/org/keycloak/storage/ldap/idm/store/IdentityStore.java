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

package org.keycloak.storage.ldap.idm.store;

import java.util.List;
import java.util.Set;
import javax.naming.AuthenticationException;
import javax.naming.ldap.LdapName;

import org.keycloak.models.ModelException;
import org.keycloak.representations.idm.LDAPCapabilityRepresentation;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.LDAPOperationDecorator;

/**
 * 身份存储 SPI 最小抽象，封装 LDAP 条目的 CRUD、查询、凭证与组 membership 操作。
 *
 * TODO: Rather remove this abstraction
 *
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public interface IdentityStore {

    /**
     * 返回本 IdentityStore 实例的 LDAP 配置。
     *
     * @return
     */
    LDAPConfig getConfig();

    // General

    /**
     * 持久化指定 LDAP 条目。
     *
     * @param ldapObject
     */
    void add(LDAPObject ldapObject);

    /**
     * 更新指定 LDAP 条目。
     *
     * @param ldapObject
     */
    void update(LDAPObject ldapObject);

    /**
     * 删除指定 LDAP 条目。
     *
     * @param ldapObject
     */
    void remove(LDAPObject ldapObject);

    /**
     * 向组添加成员。
     * @param groupDn 组条目的 DN
     * @param memberAttrName 成员属性名
     * @param value 成员值（依组类型可为 uid 或 dn）
     */
    public void addMemberToGroup(LdapName groupDn, String memberAttrName, String value);

    /**
     * 从组移除成员。
     * @param groupDn 组条目的 DN
     * @param memberAttrName 成员属性名
     * @param value 成员值（依组类型可为 uid 或 dn）
     */
    public void removeMemberFromGroup(LdapName groupDn, String memberAttrName, String value);

    // Identity query

    /** 执行 {@link LDAPQuery} 并返回匹配条目列表。 */
    List<LDAPObject> fetchQueryResults(LDAPQuery LDAPQuery);

    /** 返回 {@link LDAPQuery} 匹配条目总数。 */
    int countQueryResults(LDAPQuery LDAPQuery);

//    // Relationship query
//
//    <V extends Relationship> List<V> fetchQueryResults(RelationshipQuery<V> query);
//
//    <V extends Relationship> int countQueryResults(RelationshipQuery<V> query);

    /**
     * 查询 LDAP 服务端 <a href="https://ldapwiki.com/wiki/RootDSE">RootDSE</a>，提取其宣告的全部
     * <i>extensions</i>、<i>controls</i> 与 <i>features</i> 对应的 {@link LDAPCapabilityRepresentation}。
     * LDAP Wiki 提供<a href="https://ldapwiki.com/wiki/LDAP%20Extensions%20and%20Controls%20Listing">已知能力列表</a>。
     *
     * <p>LDAP 错误或空搜索结果时抛出 {@link ModelException}。</p>
     *
     * @return 每项代表一项服务端能力（control、extension 或 feature）的集合
     */
    Set<LDAPCapabilityRepresentation> queryServerCapabilities();

    // Credentials

    /**
     * 校验指定凭证。
     *
     * @param user Keycloak 用户
     * @param password Ldap 密码
     * @throws AuthenticationException 认证失败时
     */
    void validatePassword(LDAPObject user, String password) throws AuthenticationException;

    /**
     * 更新指定用户的 LDAP 密码。
     *
     * @param user Keycloak 用户
     * @param password Ldap 密码
     * @param passwordUpdateDecorator 密码更新前后回调，可为 null
     */
    void updatePassword(LDAPObject user, String password, LDAPOperationDecorator passwordUpdateDecorator);

}
