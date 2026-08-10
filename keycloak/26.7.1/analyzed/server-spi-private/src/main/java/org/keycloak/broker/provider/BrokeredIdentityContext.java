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
package org.keycloak.broker.provider;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 联邦身份上下文：封装 {@link IdentityProvider} 认证成功后获得的用户标识与属性。
 * <p>包含远程用户名、邮箱、令牌、IdP 配置、映射器授予的角色/组及可写入 {@link UserModel} 的扩展属性。</p>
 *
 * @author Pedro Igor
 */
public class BrokeredIdentityContext {

    private String id;
    private String legacyId;
    private String username;
    private String modelUsername;
    private String email;
    private String firstName;
    private String lastName;
    private String brokerSessionId;
    private String brokerUserId;
    private String token;
    private IdentityProviderModel idpConfig;
    private UserAuthenticationIdentityProvider<?> idp;
    private Map<String, Object> contextData = new HashMap<>();
    private AuthenticationSessionModel authenticationSession;

    /** 以远程用户 ID 与 IdP 配置构造上下文；IdP 须已启用。 */
    public BrokeredIdentityContext(String id, IdentityProviderModel idpConfig) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(idpConfig, "Identity provider config must not be null");

        if (!idpConfig.isEnabled()) {
            throw new IdentityBrokerException("Identity provider is disabled");
        }

        this.id = id;
        this.idpConfig = idpConfig;
    }

    /** 无远程 ID 的构造方式，后续通过 {@link #setId(String)} 设置。 */
    public BrokeredIdentityContext(IdentityProviderModel idpConfig) {
        Objects.requireNonNull(idpConfig, "Identity provider config must not be null");

        if (!idpConfig.isEnabled()) {
            throw new IdentityBrokerException("Identity provider is disabled");
        }

        this.idpConfig = idpConfig;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 旧版 API 的用户 ID，用于迁移兼容。
     *
     * ID from older API version. For API migrations.
     *
     * @return legacy ID
     */
    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    /**
     * 远程 IdP 中的用户名；若配置非大小写敏感则转为小写。
     *
     * Username in remote idp
     *
     * @return
     */
    public String getUsername() {
        if (getIdpConfig().isCaseSensitiveOriginalUsername()) {
            return username;
        }

        return username == null ? null : username.toLowerCase();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 写入 {@link UserModel} 的用户名（可与远程用户名不同）。
     *
     * username to store in UserModel
     *
     * @return
     */
    public String getModelUsername() {
        return modelUsername;
    }

    public void setModelUsername(String modelUsername) {
        this.modelUsername = modelUsername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBrokerSessionId() {
        return brokerSessionId;
    }

    public void setBrokerSessionId(String brokerSessionId) {
        this.brokerSessionId = brokerSessionId;
    }

    public String getBrokerUserId() {
        return brokerUserId;
    }

    public void setBrokerUserId(String brokerUserId) {
        this.brokerUserId = brokerUserId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public IdentityProviderModel getIdpConfig() {
        return idpConfig;
    }

    public UserAuthenticationIdentityProvider<?> getIdp() {
        return idp;
    }

    public void setIdp(UserAuthenticationIdentityProvider<?> idp) {
        this.idp = idp;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData;
    }

    private Map<String, String> getSessionNotes() {
        HashMap<String, String> sessionNotes = (HashMap<String, String>) this.contextData.get(Constants.MAPPER_SESSION_NOTES);
        if (sessionNotes == null) {
            sessionNotes = new HashMap<>();
            this.contextData.put(Constants.MAPPER_SESSION_NOTES, sessionNotes);
        }
        return sessionNotes;
    }

    public void setSessionNote(String key, String value) {
        if(authenticationSession != null) {
            authenticationSession.setUserSessionNote(key, value);
        }
        else {
            getSessionNotes().put(key, value);
        }
    }

    public void addSessionNotesToUserSession(UserSessionModel userSession) {
        getSessionNotes().forEach((k, v) -> userSession.setNote(k, v));
    }

    // 设置用户属性，供「更新资料」页与认证器使用
    public void setUserAttribute(String attributeName, String attributeValue) {
        List<String> list = new ArrayList<>();
        list.add(attributeValue);
        getContextData().put(Constants.USER_ATTRIBUTES_PREFIX + attributeName, list);
    }

    // 移除否则会在「更新资料」页与认证器中可见的用户属性
    public void removeUserAttribute(String attributeName) {
        getContextData().remove(Constants.USER_ATTRIBUTES_PREFIX + attributeName);
    }

    public void setUserAttribute(String attributeName, List<String> attributeValues) {
        getContextData().put(Constants.USER_ATTRIBUTES_PREFIX + attributeName, attributeValues);
    }

    public String getUserAttribute(String attributeName) {
        List<String> userAttribute = (List<String>) getContextData().get(Constants.USER_ATTRIBUTES_PREFIX + attributeName);
        if (userAttribute == null || userAttribute.isEmpty()) {
            return null;
        } else {
            return userAttribute.get(0);
        }
    }

    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : this.contextData.entrySet()) {
            if (entry.getKey().startsWith(Constants.USER_ATTRIBUTES_PREFIX)) {
                String attrName = entry.getKey().substring(Constants.USER_ATTRIBUTES_PREFIX.length());
                List<String> asList = (List<String>) getContextData().get(Constants.USER_ATTRIBUTES_PREFIX + attrName);

                if (asList.isEmpty()) {
                    continue;
                }

                result.put(attrName, asList);
            }
        }

        return result;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    public void setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
        this.authenticationSession = authenticationSession;
    }

    /**
     * 获取映射器已授予的角色集合。
     *
     * Obtains the set of roles that were granted by mappers.
     *
     * @return a {@link Set} containing the roles.
     */
    private Set<String> getMapperGrantedRoles() {
        Set<String> roles = (Set<String>) this.contextData.get(Constants.MAPPER_GRANTED_ROLES);
        if (roles == null) {
            roles = new HashSet<>();
            this.contextData.put(Constants.MAPPER_GRANTED_ROLES, roles);
        }
        return roles;
    }

    /**
     * 获取映射器已分配的分组集合。
     *
     * Obtains the set of groups that were assigned by mappers.
     *
     * @return a {@link Set} containing the groups.
     */
    @SuppressWarnings("unchecked")
    private Set<String> getMapperAssignedGroups() {
        Set<String> groups = (Set<String>) this.contextData.get(Constants.MAPPER_GRANTED_GROUPS);
        if (groups == null) {
            groups = new HashSet<>();
            this.contextData.put(Constants.MAPPER_GRANTED_GROUPS, groups);
        }
        return groups;
    }

    /**
     * 检查映射器是否已授予指定角色。
     *
     * Verifies if a mapper has already granted the specified role.
     *
     * @param roleName the name of the role.
     * @return {@code true} if a mapper has already granted the role; {@code false} otherwise.
     */
    public boolean hasMapperGrantedRole(final String roleName) {
        return this.getMapperGrantedRoles().contains(roleName);
    }

    /**
     * 检查映射器是否已分配指定分组。
     *
     * Verifies if a mapper has already assigned the specified group.
     *
     * @param groupId the id of the group.
     * @return {@code true} if a mapper has already assigned the group; {@code false} otherwise.
     */
    public boolean hasMapperAssignedGroup(final String groupId) {
        return this.getMapperAssignedGroups().contains(groupId);
    }

    /**
     * 将角色加入映射器授予集合。
     *
     * Adds the specified role to the set of roles granted by mappers.
     *
     * @param roleName the name of the role.
     */
    public void addMapperGrantedRole(final String roleName) {
        this.getMapperGrantedRoles().add(roleName);
    }

    /**
     * 将分组加入映射器分配集合。
     *
     * Adds the specified group to the set of groups assigned by mappers.
     *
     * @param groupId the id of the group.
     */
    public void addMapperAssignedGroup(final String groupId) {
        this.getMapperAssignedGroups().add(groupId);
    }

    /**
     * @deprecated use {@link #setFirstName(String)} and {@link #setLastName(String)} instead
     * @param name
     */
    @Deprecated
    public void setName(String name) {
        if (name != null) {
            int i = name.lastIndexOf(' ');
            if (i != -1) {
                firstName  = name.substring(0, i);
                lastName = name.substring(i + 1);
            } else {
                firstName = name;
            }
        }
    }


    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
