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

package org.keycloak.services.clientregistration.policy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

import org.jboss.logging.Logger;

/**
 * 客户端范围白名单注册策略。
 * <p>在注册与更新时校验请求中的默认/可选客户端范围是否在允许列表内；更新时保留客户端已有的范围不受限。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientScopesClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(ClientScopesClientRegistrationPolicy.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 策略所属领域 */
    private final RealmModel realm;
    /** 策略组件配置模型 */
    private final ComponentModel componentModel;

    /** 构造策略实例并解析所属领域。
     * @param session Keycloak 会话
     * @param componentModel 策略组件模型
     */
    public ClientScopesClientRegistrationPolicy(KeycloakSession session, ComponentModel componentModel) {
        this.session = session;
        this.componentModel = componentModel;
        this.realm = session.realms().getRealm(componentModel.getParentId());
    }

    /** {@inheritDoc} 校验注册请求中的默认与可选客户端范围 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        List<String> requestedDefaultScopeNames = context.getClient().getDefaultClientScopes();
        List<String> requestedOptionalScopeNames = context.getClient().getOptionalClientScopes();

        List<String> allowedScopeNames = new ArrayList<>();
        allowedScopeNames.addAll(getAllowedScopeNames(realm, true));
        allowedScopeNames.addAll(getAllowedScopeNames(realm, false));


        checkClientScopesAllowed(requestedDefaultScopeNames, allowedScopeNames);
        checkClientScopesAllowed(requestedOptionalScopeNames, allowedScopeNames);
    }

    /** {@inheritDoc} 注册后无额外处理 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {

    }

    /** {@inheritDoc} 校验新增的范围名称，已存在的范围不受限 */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        List<String> requestedDefaultScopeNames = new LinkedList<>();
        List<String> requestedOptionalScopeNames = new LinkedList<>();

        if(context.getClient().getDefaultClientScopes() != null) {
            requestedDefaultScopeNames.addAll(context.getClient().getDefaultClientScopes());
        }
        if(context.getClient().getOptionalClientScopes() != null) {
            requestedOptionalScopeNames.addAll(context.getClient().getOptionalClientScopes());
        }

        // 移除客户端更新前已绑定的范围，仅校验新增部分
        requestedDefaultScopeNames.removeAll(clientModel.getClientScopes(true).keySet());
        requestedOptionalScopeNames.removeAll(clientModel.getClientScopes(false).keySet());

        List<String> allowedScopeNames = new ArrayList<>();
        allowedScopeNames.addAll(getAllowedScopeNames(realm, true));
        allowedScopeNames.addAll(getAllowedScopeNames(realm, false));

        checkClientScopesAllowed(requestedDefaultScopeNames, allowedScopeNames);
        checkClientScopesAllowed(requestedOptionalScopeNames, allowedScopeNames);
    }

    /** {@inheritDoc} 更新后无额外处理 */
    @Override
    public void afterUpdate(ClientRegistrationContext context, ClientModel clientModel) {

    }

    /** {@inheritDoc} 查看前无额外校验 */
    @Override
    public void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {

    }

    /** {@inheritDoc} 删除前无额外校验 */
    @Override
    public void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {

    }

    /** 校验请求的范围列表是否全部在白名单内。
     * @param requestedScopes 请求中的范围名称
     * @param allowedScopes 允许的范围名称
     * @throws ClientRegistrationPolicyException 存在未授权范围时抛出
     */
    private void checkClientScopesAllowed(List<String> requestedScopes, List<String> allowedScopes) throws ClientRegistrationPolicyException {
        if (requestedScopes != null) {
            for (String requested : requestedScopes) {
                if (!allowedScopes.contains(requested)) {
                    logger.warnf("Requested scope '%s' not trusted in the list: %s", requested, allowedScopes.toString());
                    throw new ClientRegistrationPolicyException("Not permitted to use specified clientScope");
                }
            }
        }
    }

    /** 合并配置白名单与（可选）领域默认范围，得到允许的范围名称列表。
     * @param realm 目标领域
     * @param defaultScopes 是否查询默认范围（否则为可选范围）
     * @return 允许的范围名称列表
     */
    private List<String> getAllowedScopeNames(RealmModel realm, boolean defaultScopes) {
        // 合并组件配置中显式允许的范围
        List<String> allowedScopesConfig = componentModel.getConfig().getOrDefault(ClientScopesClientRegistrationPolicyFactory.ALLOWED_CLIENT_SCOPES, Collections.emptyList());
        List<String> allAllowed = new LinkedList<>(allowedScopesConfig);

        // 若启用 allowDefaultScopes，则领域默认/可选范围也视为允许
        boolean allowDefaultScopes = componentModel.get(ClientScopesClientRegistrationPolicyFactory.ALLOW_DEFAULT_SCOPES, true);
        if (allowDefaultScopes) {
            allAllowed.addAll(realm.getDefaultClientScopesStream(defaultScopes).map(ClientScopeModel::getName).toList());
        }

        return allAllowed;
    }
}
