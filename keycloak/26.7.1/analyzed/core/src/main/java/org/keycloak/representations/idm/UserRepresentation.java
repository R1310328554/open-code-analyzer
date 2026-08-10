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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.representations.idm.oid4vc.IssuedVerifiableCredentialRepresentation;
import org.keycloak.representations.idm.oid4vc.UserVerifiableCredentialRepresentation;

/**
 * 用户的 Admin REST API 表示，继承 {@link AbstractUserRepresentation} 并扩展凭据、角色、联合身份及授权同意等信息。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserRepresentation extends AbstractUserRepresentation{

    /** 用户资源的自引用链接。 */
    protected String self; // link
    /** @deprecated 用户来源（已废弃，请使用 federationLink）。 */
    protected String origin;
    /** 用户创建时间戳（毫秒）。 */
    protected Long createdTimestamp;
    /** @deprecated 是否启用 TOTP（已废弃）。 */
    protected Boolean totp;
    /** 加载该用户的 UserStorageProvider ID，本地用户为 null。 */
    protected String federationLink;
    /** 服务账户关联的 clientId（非数据库 ID）。 */
    protected String serviceAccountClientId; // For rep, it points to clientId (not DB ID)

    /** 用户凭据列表。 */
    protected List<CredentialRepresentation> credentials;
    /** 可禁用的凭据类型集合。 */
    protected Set<String> disableableCredentialTypes;
    /** 待执行的 Required Action 列表。 */
    protected List<String> requiredActions;
    /** 联合身份（IdP 关联）列表。 */
    protected List<FederatedIdentityRepresentation> federatedIdentities;
    /** realm 角色名称列表。 */
    protected List<String> realmRoles;
    /** 客户端 ID 到客户端角色名称列表的映射。 */
    protected Map<String, List<String>> clientRoles;
    /** 客户端授权同意记录列表。 */
    protected List<UserConsentRepresentation> clientConsents;
    /** 令牌生效起始时间（not-before）。 */
    protected Integer notBefore;

    /** 用户可验证凭据（Verifiable Credential）列表。 */
    protected List<UserVerifiableCredentialRepresentation> verifiableCredentials;
    /** 已签发的可验证凭据列表。 */
    protected List<IssuedVerifiableCredentialRepresentation> issuedVerifiableCredentials;

    /** @deprecated 应用角色映射（已废弃，由 clientRoles 替代）。 */
    @Deprecated
    protected Map<String, List<String>> applicationRoles;
    /** @deprecated 社交账号链接（已废弃）。 */
    @Deprecated
    protected List<SocialLinkRepresentation> socialLinks;

    /** 用户所属组路径列表。 */
    protected List<String> groups;
    /** 当前调用者对各管理操作的访问权限映射。 */
    private Map<String, Boolean> access;

    public UserRepresentation() {
    }

    /**
     * 从已有表示复制构造用户对象。
     *
     * @param rep 源用户表示
     */
    public UserRepresentation(UserRepresentation rep) {
        // AbstractUserRepresentation
        this.id = rep.getId();
        this.username = rep.getUsername();
        this.firstName = rep.getFirstName();
        this.lastName = rep.getLastName();
        this.email = rep.getEmail();
        this.emailVerified = rep.isEmailVerified();
        this.attributes = rep.getAttributes();
        this.setUserProfileMetadata(rep.getUserProfileMetadata());

        this.self = rep.getSelf();
        this.createdTimestamp = rep.getCreatedTimestamp();
        this.enabled = rep.isEnabled();
        this.totp = rep.isTotp();
        this.federationLink = rep.getFederationLink();
        this.serviceAccountClientId = rep.getServiceAccountClientId();
        this.credentials = rep.getCredentials();
        this.disableableCredentialTypes = rep.getDisableableCredentialTypes();
        this.requiredActions = rep.getRequiredActions();
        this.federatedIdentities = rep.getFederatedIdentities();
        this.realmRoles = rep.getRealmRoles();
        this.clientRoles = rep.getClientRoles();
        this.clientConsents = rep.getClientConsents();
        this.notBefore = rep.getNotBefore();
        this.verifiableCredentials = rep.getVerifiableCredentials();
        this.issuedVerifiableCredentials = rep.getIssuedVerifiableCredentials();

        this.applicationRoles = rep.getApplicationRoles();
        this.socialLinks = rep.getSocialLinks();

        this.groups = rep.getGroups();
        this.access = rep.getAccess();
    }

    /** @return 自引用链接 */
    public String getSelf() {
        return self;
    }

    /** @param self 自引用链接 */
    public void setSelf(String self) {
        this.self = self;
    }

    /** @return 创建时间戳 */
    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /** @param createdTimestamp 创建时间戳 */
    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /** @deprecated 是否启用 TOTP */
    @Deprecated
    public Boolean isTotp() {
        return totp;
    }

    /** @deprecated 设置 TOTP 状态 */
    @Deprecated
    public void setTotp(Boolean totp) {
        this.totp = totp;
    }

    /** @return 用户凭据列表 */
    public List<CredentialRepresentation> getCredentials() {
        return credentials;
    }

    /** @param credentials 用户凭据列表 */
    public void setCredentials(List<CredentialRepresentation> credentials) {
        this.credentials = credentials;
    }

    /** @return Required Action 列表 */
    public List<String> getRequiredActions() {
        return requiredActions;
    }

    /** @param requiredActions Required Action 列表 */
    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }

    /** @return 联合身份列表 */
    public List<FederatedIdentityRepresentation> getFederatedIdentities() {
        return federatedIdentities;
    }

    /** @param federatedIdentities 联合身份列表 */
    public void setFederatedIdentities(List<FederatedIdentityRepresentation> federatedIdentities) {
        this.federatedIdentities = federatedIdentities;
    }

    /** @return 社交链接列表 */
    public List<SocialLinkRepresentation> getSocialLinks() {
        return socialLinks;
    }

    /** @param socialLinks 社交链接列表 */
    public void setSocialLinks(List<SocialLinkRepresentation> socialLinks) {
        this.socialLinks = socialLinks;
    }

    /** @return realm 角色名称列表 */
    public List<String> getRealmRoles() {
        return realmRoles;
    }

    /** @param realmRoles realm 角色名称列表 */
    public void setRealmRoles(List<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    /** @return 客户端角色映射 */
    public Map<String, List<String>> getClientRoles() {
        return clientRoles;
    }

    /** @param clientRoles 客户端角色映射 */
    public void setClientRoles(Map<String, List<String>> clientRoles) {
        this.clientRoles = clientRoles;
    }

    /** @return 客户端授权同意列表 */
    public List<UserConsentRepresentation> getClientConsents() {
        return clientConsents;
    }

    /** @param clientConsents 客户端授权同意列表 */
    public void setClientConsents(List<UserConsentRepresentation> clientConsents) {
        this.clientConsents = clientConsents;
    }

    /** @return not-before 时间 */
    public Integer getNotBefore() {
        return notBefore;
    }

    /** @param notBefore not-before 时间 */
    public void setNotBefore(Integer notBefore) {
        this.notBefore = notBefore;
    }

    /** @return 可验证凭据列表 */
    public List<UserVerifiableCredentialRepresentation> getVerifiableCredentials() {
        return verifiableCredentials;
    }

    /** @param verifiableCredentials 可验证凭据列表 */
    public void setVerifiableCredentials(List<UserVerifiableCredentialRepresentation> verifiableCredentials) {
        this.verifiableCredentials = verifiableCredentials;
    }

    /** @return 已签发可验证凭据列表 */
    public List<IssuedVerifiableCredentialRepresentation> getIssuedVerifiableCredentials() {
        return issuedVerifiableCredentials;
    }

    /** @param issuedVerifiableCredentials 已签发可验证凭据列表 */
    public void setIssuedVerifiableCredentials(List<IssuedVerifiableCredentialRepresentation> issuedVerifiableCredentials) {
        this.issuedVerifiableCredentials = issuedVerifiableCredentials;
    }

    /** @deprecated 应用角色映射 */
    @Deprecated
    public Map<String, List<String>> getApplicationRoles() {
        return applicationRoles;
    }

    /** @return 联邦存储提供者链接 ID */
    public String getFederationLink() {
        return federationLink;
    }

    /** @param federationLink 联邦存储提供者链接 ID */
    public void setFederationLink(String federationLink) {
        this.federationLink = federationLink;
    }

    /** @return 服务账户 clientId */
    public String getServiceAccountClientId() {
        return serviceAccountClientId;
    }

    /** @param serviceAccountClientId 服务账户 clientId */
    public void setServiceAccountClientId(String serviceAccountClientId) {
        this.serviceAccountClientId = serviceAccountClientId;
    }

    /** @return 用户所属组路径列表 */
    public List<String> getGroups() {
        return groups;
    }

    /** @param groups 用户所属组路径列表 */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    /**
     * 返回加载该用户的 UserStorageProvider ID。
     *
     * @return 本地存储用户时为 NULL
     * @deprecated 请使用 {@link #getFederationLink()} 替代
     */
    @Deprecated
    public String getOrigin() {
        return federationLink;
    }

    /**
     * @param origin 用户来源
     * @deprecated 请使用 {@link #setFederationLink(String)} 替代
     */
    @Deprecated
    public void setOrigin(String origin) {
        // deprecated
    }

    /** @return 可禁用凭据类型集合 */
    public Set<String> getDisableableCredentialTypes() {
        return disableableCredentialTypes;
    }

    /** @param disableableCredentialTypes 可禁用凭据类型集合 */
    public void setDisableableCredentialTypes(Set<String> disableableCredentialTypes) {
        this.disableableCredentialTypes = disableableCredentialTypes;
    }

    /** @return 管理操作访问权限映射 */
    public Map<String, Boolean> getAccess() {
        return access;
    }

    /** @param access 管理操作访问权限映射 */
    public void setAccess(Map<String, Boolean> access) {
        this.access = access;
    }
}
