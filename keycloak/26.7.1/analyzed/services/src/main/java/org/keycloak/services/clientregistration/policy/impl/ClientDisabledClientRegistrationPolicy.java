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

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

/**
 * 客户端禁用注册策略。
 * <p>新注册客户端在 {@code afterRegister} 阶段自动设为禁用，且禁止通过动态注册重新启用已禁用客户端。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientDisabledClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** {@inheritDoc} 注册前无额外校验 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {

    }

    /** {@inheritDoc} 注册后将客户端设为禁用，需管理员手动启用 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
        clientModel.setEnabled(false);
    }

    /** {@inheritDoc} 禁止将已禁用客户端通过注册 API 重新启用 */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        if (context.getClient().isEnabled() == null) {
            return;
        }
        if (clientModel == null) {
            return;
        }

        boolean isEnabled = clientModel.isEnabled();
        boolean newEnabled = context.getClient().isEnabled();

        if (!isEnabled && newEnabled) {
            throw new ClientRegistrationPolicyException("Not permitted to enable client");
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
