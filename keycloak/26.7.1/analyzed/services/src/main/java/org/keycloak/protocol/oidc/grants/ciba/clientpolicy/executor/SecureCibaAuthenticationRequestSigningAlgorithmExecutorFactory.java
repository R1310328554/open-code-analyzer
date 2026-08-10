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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.crypto.Algorithm;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;
import org.keycloak.services.clientpolicy.executor.FapiConstant;

/**
 * {@link SecureCibaAuthenticationRequestSigningAlgorithmExecutor} 的 Provider 工厂。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureCibaAuthenticationRequestSigningAlgorithmExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** Executor Provider 标识符 */
    public static final String PROVIDER_ID = "secure-ciba-req-sig-algorithm";

    /** 配置项键：默认签名算法 */
    public static final String DEFAULT_ALGORITHM = "default-algorithm";

    /** 管理控制台中的默认算法下拉配置 */
    private static final ProviderConfigProperty DEFAULT_ALGORITHM_PROPERTY = new ProviderConfigProperty(
            DEFAULT_ALGORITHM, "Default Algorithm", "Default signature algorithm, which will be set to clients during client registration/update in case that client does not specify any algorithm",
            ProviderConfigProperty.LIST_TYPE, Algorithm.PS256, new LinkedList<>(FapiConstant.ALLOWED_ALGORITHMS).toArray(new String[] {}));

    /** 创建 Executor 实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SecureCibaAuthenticationRequestSigningAlgorithmExecutor(session);
    }

    /** 工厂初始化（无全局配置） */
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

    /** @return 管理控制台帮助文本（英文原文保留） */
    @Override
    public String getHelpText() {
        return "It refuses the client whose signature algorithms are considered not to be secure. This is applied by server for CIBA backchannel signed authentication request. It accepts ES256, ES384, ES512, PS256, PS384 and PS512.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>(Arrays.asList(DEFAULT_ALGORITHM_PROPERTY));
    }

}
