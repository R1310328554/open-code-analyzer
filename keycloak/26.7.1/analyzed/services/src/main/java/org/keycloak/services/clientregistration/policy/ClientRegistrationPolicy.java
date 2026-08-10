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

import java.util.Collection;
import java.util.Collections;

import org.keycloak.models.ClientModel;
import org.keycloak.provider.Provider;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;

/**
 * 动态客户端注册策略 SPI 接口。
 * <p>在注册、更新、查看与删除客户端的生命周期各阶段执行校验与后处理逻辑。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientRegistrationPolicy extends Provider {

    /** 客户端创建前校验 */
    void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException;

    /** 客户端创建后处理 */
    void afterRegister(ClientRegistrationContext context, ClientModel clientModel);

    /** 客户端更新前校验 */
    void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException;

    /** 客户端更新后处理 */
    void afterUpdate(ClientRegistrationContext context, ClientModel clientModel);

    /** 客户端查询前校验 */
    void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException;

    /** 客户端删除前校验 */
    void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException;

    /**
     * 该策略为客户端注册端点响应贡献的额外 CORS 允许来源。
     * <p>来源在处理器执行前统一收集，以便单次 {@code checkAllowedOrigins} 完成校验。</p>
     */
    default Collection<String> getAllowedOrigins() {
        return Collections.emptyList();
    }

    /** Provider 关闭钩子（默认空实现） */
    @Override
    default void close() {
    }

}
