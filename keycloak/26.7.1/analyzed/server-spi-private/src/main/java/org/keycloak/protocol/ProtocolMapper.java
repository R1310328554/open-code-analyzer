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

package org.keycloak.protocol;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * 协议映射器 SPI：在令牌/断言签发时将用户/客户端属性映射到声明或属性。
 * <p>同时实现 {@link Provider}、{@link ProviderFactory} 与 {@link ConfiguredProvider}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ProtocolMapper extends Provider, ProviderFactory<ProtocolMapper>,ConfiguredProvider {
    /** @return 关联协议 ID（如 {@code openid-connect}） */
    String getProtocol();
    /** @return 管理控制台分组类别 */
    String getDisplayCategory();
    /** @return 管理控制台显示类型名称 */
    String getDisplayType();

    /**
     * 映射器执行优先级，数值越小越先执行。
     * Priority of this protocolMapper implementation. Lower goes first.
     * @return
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 通过管理端点创建/更新映射器配置时校验参数。
     * @param session
     * @param realm
     * @param client client or clientTemplate
     * @param mapperModel
     * @throws ProtocolMapperConfigException if configuration provided in mapperModel is not valid
     */
    default void validateConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel client, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {
    };

    /**
     * 返回映射器的有效配置（合并选项默认值）。
     * <p>签发令牌/断言时实际使用的配置；未显式设置的项将填充默认值。</p>
     * <p>Get effective configuration of protocol mapper.</p>
     * @param session
     * @param realm
     * @param protocolMapperModel
     */
    default ProtocolMapperModel getEffectiveModel(KeycloakSession session, RealmModel realm, ProtocolMapperModel protocolMapperModel) {
        return protocolMapperModel;
    }

}
