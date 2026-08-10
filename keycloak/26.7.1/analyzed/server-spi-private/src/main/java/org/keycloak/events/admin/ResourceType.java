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
package org.keycloak.events.admin;

/**
 * 可触发 {@link AdminEvent} 的 Keycloak 管理资源类型。
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public enum ResourceType {

    /** Realm 本身。 */
    REALM

    /** Realm 级角色。 */
    , REALM_ROLE

    /** Realm 角色映射。 */
    , REALM_ROLE_MAPPING

    /** Realm 作用域映射。 */
    , REALM_SCOPE_MAPPING

    /** 认证流程。 */
    , AUTH_FLOW

    /** 认证执行子流程。 */
    , AUTH_EXECUTION_FLOW

    /** 认证执行步骤。 */
    , AUTH_EXECUTION

    /** 认证器配置。 */
    , AUTHENTICATOR_CONFIG

    /** Required Action 配置。 */
    , REQUIRED_ACTION_CONFIG

    /** Required Action。 */
    , REQUIRED_ACTION

    /** 身份提供者。 */
    , IDENTITY_PROVIDER

    /** 身份提供者映射器。 */
    , IDENTITY_PROVIDER_MAPPER

    /** 协议映射器。 */
    , PROTOCOL_MAPPER

    /** 用户。 */
    , USER

    /** 用户登录失败记录。 */
    , USER_LOGIN_FAILURE

    /** 用户会话。 */
    , USER_SESSION

    /** 用户联邦提供者。 */
    , USER_FEDERATION_PROVIDER

    /** 用户联邦映射器。 */
    , USER_FEDERATION_MAPPER

    /** 用户组。 */
    , GROUP

    /** 组成员关系。 */
    , GROUP_MEMBERSHIP

    /** OAuth/OIDC 客户端。 */
    , CLIENT

    /** 客户端初始访问令牌模型。 */
    , CLIENT_INITIAL_ACCESS_MODEL

    /** 客户端角色。 */
    , CLIENT_ROLE

    /** 客户端角色映射。 */
    , CLIENT_ROLE_MAPPING

    /** 客户端作用域。 */
    , CLIENT_SCOPE

    /** 客户端作用域映射。 */
    , CLIENT_SCOPE_MAPPING

    /** 客户端与作用域的关联映射。 */
    , CLIENT_SCOPE_CLIENT_MAPPING

    /** 集群节点。 */
    , CLUSTER_NODE

    /** 可插拔组件（如密钥提供者）。 */
    , COMPONENT

    /** 授权资源服务器。 */
    , AUTHORIZATION_RESOURCE_SERVER

    /** 授权资源。 */
    , AUTHORIZATION_RESOURCE

    /** 授权作用域。 */
    , AUTHORIZATION_SCOPE

    /** 授权策略。 */
    , AUTHORIZATION_POLICY

    /** 自定义或未识别的资源类型。 */
    , CUSTOM

    /** 用户 Profile 配置。 */
    , USER_PROFILE

    /** 组织。 */
    , ORGANIZATION
    
    /** 组织成员关系。 */
    , ORGANIZATION_MEMBERSHIP

    /** 组织内用户组。 */
    , ORGANIZATION_GROUP
    /** 组织组成员关系。 */
    , ORGANIZATION_GROUP_MEMBERSHIP
}
