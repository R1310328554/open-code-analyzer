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
import org.keycloak.models.RealmModel;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

/**
 * 领域客户端数量上限注册策略。
 * <p>在注册前检查当前领域客户端总数，达到配置上限时拒绝新注册。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MaxClientsClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 策略组件配置模型 */
    private final ComponentModel componentModel;

    /** 构造策略实例。
     * @param session Keycloak 会话
     * @param componentModel 策略组件模型
     */
    public MaxClientsClientRegistrationPolicy(KeycloakSession session, ComponentModel componentModel) {
        this.session = session;
        this.componentModel = componentModel;
    }

    /** {@inheritDoc} 注册前校验领域客户端数量未达上限 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        RealmModel realm = session.getContext().getRealm();
        long currentCount = realm.getClientsCount();
        int maxCount = componentModel.get(MaxClientsClientRegistrationPolicyFactory.MAX_CLIENTS, MaxClientsClientRegistrationPolicyFactory.DEFAULT_MAX_CLIENTS);

        if (currentCount >= maxCount) {
            throw new ClientRegistrationPolicyException("It's allowed to have max " + maxCount + " clients per realm");
        }
    }

    /** {@inheritDoc} 注册后无额外处理 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {

    }

    /** {@inheritDoc} 更新前无额外校验 */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
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
