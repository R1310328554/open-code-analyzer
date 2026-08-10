/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants.ciba.clientpolicy.executor;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;

/**
 * {@link SecureCibaSessionEnforceExecutor} 的 Provider 工厂。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureCibaSessionEnforceExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** Executor Provider 标识符 */
    public static final String PROVIDER_ID = "secure-ciba-session";

    /** 创建 Executor 实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SecureCibaSessionEnforceExecutor(session);
    }

    /** 工厂初始化 */
    @Override
    public void init(Scope config) {
    }

    /** 会话工厂就绪回调 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {
    }

    /** @return Provider ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台帮助文本 */
    @Override
    public String getHelpText() {
        return "To distinguish which authentication belongs to which CIBA flow, it refuses backchannel authentication request which lacks 'binding_message' parameter.";
    }

    /** @return 无额外配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

}
