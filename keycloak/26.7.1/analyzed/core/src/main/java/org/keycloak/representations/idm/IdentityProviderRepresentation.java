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
package org.keycloak.representations.idm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 身份提供者（Identity Provider）的 REST 表示，用于 Broker 登录与联邦身份配置。
 *
 * @author Pedro Igor
 */
public class IdentityProviderRepresentation {

    /** IdP 别名，在 realm 内唯一。 */
    protected String alias;
    /** 管理控制台显示名称。 */
    protected String displayName;
    /** IdP 内部持久化 ID。 */
    protected String internalId;
    /** IdP 实现类型 ID（如 saml、oidc）。 */
    protected String providerId;
    /** 是否启用该 IdP。 */
    protected boolean enabled = true;

    /** 首次登录后始终展示资料更新页。 */
    public static final String UPFLM_ON = "on";
    /** 仅当必填资料字段缺失时展示资料更新页。 */
    public static final String UPFLM_MISSING = "missing";
    /** 首次登录后从不展示资料更新页。 */
    public static final String UPFLM_OFF = "off";

    /**
     * 通过该 IdP 首次登录创建用户后，资料更新页展示模式。可选值：
     * <ul>
     * <li><code>on</code> - 对所有用户展示资料更新页
     * <li><code>missing</code> - 仅对缺少必填用户资料字段的用户展示
     * <li><code>off</code> - 首次登录后从不展示资料更新页
     * </ul>
     *
     * @see #UPFLM_ON
     * @see #UPFLM_MISSING
     * @see #UPFLM_OFF
     */
    @Deprecated
    protected String updateProfileFirstLoginMode;

    /** 是否信任 IdP 提供的邮箱（跳过邮箱验证）。 */
    protected Boolean trustEmail;
    /** 是否存储 IdP 令牌以供后续使用。 */
    protected Boolean storeToken;
    /** 创建用户时是否添加 read-token 角色。 */
    protected Boolean addReadTokenRoleOnCreate;
    /** 是否为默认 IdP（用户可直接跳转登录）。 */
    protected Boolean authenticateByDefault;
    /** 是否仅用于账户链接（不在登录页展示）。 */
    protected Boolean linkOnly;
    /** 是否在登录页隐藏该 IdP。 */
    protected Boolean hideOnLogin;
    /** 首次 Broker 登录认证流别名。 */
    protected String firstBrokerLoginFlowAlias;
    /** Broker 登录后认证流别名。 */
    protected String postBrokerLoginFlowAlias;
    /** 关联组织 ID（Organization 功能）。 */
    protected String organizationId;
    /** IdP 实现特定的配置键值对。 */
    protected Map<String, String> config = new HashMap<>();
    /** IdP 支持的类型标签列表；默认为 null 以兼容 Keycloak 26.4 及更早版本。 */
    protected List<String> types;

    /** @return IdP 内部 ID */
    public String getInternalId() {
        return this.internalId;
    }

    /** @param internalId IdP 内部 ID */
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    /** @return IdP 别名 */
    public String getAlias() {
        return this.alias;
    }

    /** @param alias IdP 别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @return IdP 实现类型 ID */
    public String getProviderId() {
        return this.providerId;
    }

    /** @param providerId IdP 实现类型 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return IdP 配置 */
    public Map<String, String> getConfig() {
        return this.config;
    }

    /** @param config IdP 配置 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /** @return 是否启用 */
    public boolean isEnabled() {
        return this.enabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 是否仅用于账户链接 */
    public Boolean isLinkOnly() {
        return linkOnly;
    }

    /** @param linkOnly 是否仅用于账户链接 */
    public void setLinkOnly(Boolean linkOnly) {
        this.linkOnly = linkOnly;
    }

    /** @return 是否在登录页隐藏 */
    public Boolean isHideOnLogin() {
        return this.hideOnLogin;
    }

    /** @param hideOnLogin 是否在登录页隐藏 */
    public void setHideOnLogin(Boolean hideOnLogin) {
        this.hideOnLogin = hideOnLogin;
    }

    /**
     * 已弃用，由 {@link #updateProfileFirstLoginMode} 替代。保留以支持旧 realm 导入。
     *
     * @deprecated {@link #setUpdateProfileFirstLoginMode(String)}
     */
    @Deprecated
    public void setUpdateProfileFirstLogin(Boolean updateProfileFirstLogin) {
        this.updateProfileFirstLoginMode = updateProfileFirstLogin == null ? null : (updateProfileFirstLogin ? UPFLM_ON : UPFLM_OFF);
    }

    /**
     * @deprecated 已弃用，由 IdpReviewProfileAuthenticator 配置替代
     */
    @Deprecated
    public String getUpdateProfileFirstLoginMode() {
        return updateProfileFirstLoginMode;
    }

    /**
     * @deprecated 已弃用，由 IdpReviewProfileAuthenticator 配置替代
     */
    @Deprecated
    public void setUpdateProfileFirstLoginMode(String updateProfileFirstLoginMode) {
        this.updateProfileFirstLoginMode = updateProfileFirstLoginMode;
    }

    /**
     * @deprecated 已由身份提供者认证器配置选项替代
     */
    @Deprecated
    public Boolean isAuthenticateByDefault() {
        return authenticateByDefault;
    }

    /** @param authenticateByDefault 是否为默认 IdP */
    @Deprecated
    public void setAuthenticateByDefault(Boolean authenticateByDefault) {
        this.authenticateByDefault = authenticateByDefault;
    }

    /** @return 首次 Broker 登录流别名 */
    public String getFirstBrokerLoginFlowAlias() {
        return firstBrokerLoginFlowAlias;
    }

    /** @param firstBrokerLoginFlowAlias 首次 Broker 登录流别名 */
    public void setFirstBrokerLoginFlowAlias(String firstBrokerLoginFlowAlias) {
        this.firstBrokerLoginFlowAlias = firstBrokerLoginFlowAlias;
    }

    /** @return Broker 登录后流别名 */
    public String getPostBrokerLoginFlowAlias() {
        return postBrokerLoginFlowAlias;
    }

    /** @param postBrokerLoginFlowAlias Broker 登录后流别名 */
    public void setPostBrokerLoginFlowAlias(String postBrokerLoginFlowAlias) {
        this.postBrokerLoginFlowAlias = postBrokerLoginFlowAlias;
    }

    /** @return 是否存储 IdP 令牌 */
    public Boolean isStoreToken() {
        return this.storeToken;
    }

    /** @param storeToken 是否存储 IdP 令牌 */
    public void setStoreToken(Boolean storeToken) {
        this.storeToken = storeToken;
    }

    /** @return 创建时是否添加 read-token 角色 */
    public Boolean isAddReadTokenRoleOnCreate() {
        return addReadTokenRoleOnCreate;
    }

    /** @param addReadTokenRoleOnCreate 创建时是否添加 read-token 角色 */
    public void setAddReadTokenRoleOnCreate(Boolean addReadTokenRoleOnCreate) {
        this.addReadTokenRoleOnCreate = addReadTokenRoleOnCreate;
    }

    /** @return 是否信任 IdP 邮箱 */
    public Boolean isTrustEmail() {
        return trustEmail;
    }

    /** @param trustEmail 是否信任 IdP 邮箱 */
    public void setTrustEmail(Boolean trustEmail) {
        this.trustEmail = trustEmail;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @return 关联组织 ID */
    public String getOrganizationId() {
        return this.organizationId;
    }

    /** @param organizationId 关联组织 ID */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /** @return IdP 类型标签列表 */
    public List<String> getTypes() {
        return this.types;
    }
    /** @param types IdP 类型标签列表 */
    public void setTypes(List<String> types) {
        this.types = types;
    }

}
