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

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

import org.jboss.logging.Logger;

/**
 * 范围（Scope）客户端注册策略。
 * <p>注册后禁用 {@code fullScopeAllowed}，并阻止通过动态注册将已禁用的全范围访问重新启用。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ScopeClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(ScopeClientRegistrationPolicy.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 策略组件模型 */
    private final ComponentModel componentModel;

    /** 构造范围注册策略。
     * @param session Keycloak 会话
     * @param componentModel 策略组件配置
     */
    public ScopeClientRegistrationPolicy(KeycloakSession session, ComponentModel componentModel) {
        this.session = session;
        this.componentModel = componentModel;
    }


    /** {@inheritDoc} 注册前无额外校验 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {

    }

    /** {@inheritDoc} 注册后将客户端 {@code fullScopeAllowed} 设为 false */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
        clientModel.setFullScopeAllowed(false);
    }

    /** {@inheritDoc} 禁止将 {@code fullScopeAllowed} 从 false 改为 true */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        if (context.getClient().isFullScopeAllowed() == null) {
            return;
        }
        if (clientModel == null) {
            return;
        }

        boolean isAllowed = clientModel.isFullScopeAllowed();
        boolean newAllowed = context.getClient().isFullScopeAllowed();

        if (!isAllowed && newAllowed) {
            throw new ClientRegistrationPolicyException("Not permitted to enable fullScopeAllowed");
        }
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
}
