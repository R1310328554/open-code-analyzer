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

/**
 * 已弃用的客户端模板 REST 表示，请改用 {@link ClientScopeRepresentation}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Deprecated // 请改用 ClientScopeRepresentation
public class ClientTemplateRepresentation {
    /**
     * 在 {@link ClientRepresentation#setClientTemplate} 中使用此值以清除模板关联。
     */
    public static final String NONE = "NONE";
    /** 模板内部 UUID。 */
    protected String id;
    /** 模板名称。 */
    protected String name;
    /** 模板描述。 */
    protected String description;
    /** 关联协议。 */
    protected String protocol;
    /** 是否允许完整 Scope。 */
    protected Boolean fullScopeAllowed;
    /** 是否为仅 Bearer 客户端。 */
    protected Boolean bearerOnly;
    /** 是否需要用户同意。 */
    protected Boolean consentRequired;
    /** 是否启用标准授权码流。 */
    protected Boolean standardFlowEnabled;
    /** 是否启用隐式流。 */
    protected Boolean implicitFlowEnabled;
    /** 是否启用资源所有者密码凭据流。 */
    protected Boolean directAccessGrantsEnabled;
    /** 是否启用服务账户。 */
    protected Boolean serviceAccountsEnabled;
    /** 是否为公开客户端。 */
    protected Boolean publicClient;
    /** 是否启用前端通道登出。 */
    protected Boolean frontchannelLogout;
    /** 模板自定义属性。 */
    protected Map<String, String> attributes;

    /** 协议映射器列表。 */
    protected List<ProtocolMapperRepresentation> protocolMappers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public List<ProtocolMapperRepresentation> getProtocolMappers() {
        return protocolMappers;
    }

    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        this.protocolMappers = protocolMappers;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Boolean isFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public void setFullScopeAllowed(Boolean fullScopeAllowed) {
        this.fullScopeAllowed = fullScopeAllowed;
    }

    public Boolean isBearerOnly() {
        return bearerOnly;
    }

    public void setBearerOnly(Boolean bearerOnly) {
        this.bearerOnly = bearerOnly;
    }

    public Boolean isConsentRequired() {
        return consentRequired;
    }

    public void setConsentRequired(Boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

    public Boolean isStandardFlowEnabled() {
        return standardFlowEnabled;
    }

    public void setStandardFlowEnabled(Boolean standardFlowEnabled) {
        this.standardFlowEnabled = standardFlowEnabled;
    }

    public Boolean isImplicitFlowEnabled() {
        return implicitFlowEnabled;
    }

    public void setImplicitFlowEnabled(Boolean implicitFlowEnabled) {
        this.implicitFlowEnabled = implicitFlowEnabled;
    }

    public Boolean isDirectAccessGrantsEnabled() {
        return directAccessGrantsEnabled;
    }

    public void setDirectAccessGrantsEnabled(Boolean directAccessGrantsEnabled) {
        this.directAccessGrantsEnabled = directAccessGrantsEnabled;
    }

    public Boolean isServiceAccountsEnabled() {
        return serviceAccountsEnabled;
    }

    public void setServiceAccountsEnabled(Boolean serviceAccountsEnabled) {
        this.serviceAccountsEnabled = serviceAccountsEnabled;
    }

    public Boolean isPublicClient() {
        return publicClient;
    }

    public void setPublicClient(Boolean publicClient) {
        this.publicClient = publicClient;
    }

    public Boolean isFrontchannelLogout() {
        return frontchannelLogout;
    }

    public void setFrontchannelLogout(Boolean frontchannelLogout) {
        this.frontchannelLogout = frontchannelLogout;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
