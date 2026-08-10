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
package org.keycloak.representations.idm.authorization;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * 资源服务器授权配置的 REST 表示，聚合资源、策略、作用域及决策策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceServerRepresentation {

    /** 资源服务器 ID。 */
    private String id;

    /** 关联客户端 ID。 */
    private String clientId;
    /** 资源服务器名称。 */
    private String name;
    /** 是否允许远程资源管理。 */
    private boolean allowRemoteResourceManagement = true;
    /** 策略执行模式。 */
    private PolicyEnforcementMode policyEnforcementMode = PolicyEnforcementMode.ENFORCING;
    /** 受保护资源列表。 */
    private List<ResourceRepresentation> resources = emptyList();
    /** 授权策略列表。 */
    private List<PolicyRepresentation> policies = emptyList();
    /** 可用作用域列表。 */
    private List<ScopeRepresentation> scopes = emptyList();
    /** 多策略冲突时的决策策略。 */
    private DecisionStrategy decisionStrategy;
    /** 授权模式（schema）定义。 */
    private AuthorizationSchema authorizationSchema;

    /** @param id 资源服务器 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 资源服务器 ID */
    public String getId() {
        return this.id;
    }

    /** @return 客户端 ID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 资源服务器名称 */
    public String getName() {
        return this.name;
    }

    /** @param name 资源服务器名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 是否允许远程资源管理 */
    public boolean isAllowRemoteResourceManagement() {
        return this.allowRemoteResourceManagement;
    }

    /** @param allowRemoteResourceManagement 是否允许远程资源管理 */
    public void setAllowRemoteResourceManagement(boolean allowRemoteResourceManagement) {
        this.allowRemoteResourceManagement = allowRemoteResourceManagement;
    }

    /** @return 策略执行模式 */
    public PolicyEnforcementMode getPolicyEnforcementMode() {
        return this.policyEnforcementMode;
    }

    /** @param policyEnforcementMode 策略执行模式 */
    public void setPolicyEnforcementMode(PolicyEnforcementMode policyEnforcementMode) {
        this.policyEnforcementMode = policyEnforcementMode;
    }

    /** @param resources 资源列表 */
    public void setResources(List<ResourceRepresentation> resources) {
        this.resources = resources;
    }

    /** @return 资源列表 */
    public List<ResourceRepresentation> getResources() {
        return resources;
    }

    /** @param policies 策略列表 */
    public void setPolicies(List<PolicyRepresentation> policies) {
        this.policies = policies;
    }

    /** @return 策略列表 */
    public List<PolicyRepresentation> getPolicies() {
        return policies;
    }

    /** @param scopes 作用域列表 */
    public void setScopes(List<ScopeRepresentation> scopes) {
        this.scopes = scopes;
    }

    /** @return 作用域列表 */
    public List<ScopeRepresentation> getScopes() {
        return scopes;
    }

    /** @param decisionStrategy 决策策略 */
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        this.decisionStrategy = decisionStrategy;
    }

    /** @return 决策策略 */
    public DecisionStrategy getDecisionStrategy() {
        return decisionStrategy;
    }

    /** @param authorizationSchema 授权模式定义 */
    public void setAuthorizationSchema(AuthorizationSchema authorizationSchema) {
        this.authorizationSchema = authorizationSchema;
    }

    /** @return 授权模式定义 */
    public AuthorizationSchema getAuthorizationSchema() {
        return authorizationSchema;
    }
}
