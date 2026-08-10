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

package org.keycloak.services.clientregistration.policy;

import java.util.List;

import org.keycloak.component.ComponentFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端注册策略 Provider 工厂接口。
 * <p>扩展 {@link ComponentFactory}，为领域级可配置的注册策略提供实例化能力。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientRegistrationPolicyFactory extends ComponentFactory<ClientRegistrationPolicy, ClientRegistrationPolicy> {

    /**
     * 获取当前会话上下文下的可配置属性列表。
     * <p>假定 {@code session.getContext()} 已设置领域。</p>
     *
     * @param session Keycloak 会话
     * @return 可配置属性列表
     */
    List<ProviderConfigProperty> getConfigProperties(KeycloakSession session);
}
