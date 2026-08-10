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
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;

/**
 * CIBA 安全签名认证请求执行器工厂。
 * <p>校验客户端在后台认证请求中是否按 FAPI CIBA 安全规范使用签名的 {@code request} 对象。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureCibaSignedAuthenticationRequestExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器提供方标识 */
    public static final String PROVIDER_ID = "secure-ciba-signed-authn-req";

    /** 配置项：签名 request 有效期的键名 */
    public static final String AVAILABLE_PERIOD = "available-period";

    private static final ProviderConfigProperty AVAILABLE_PERIOD_PROPERTY = new ProviderConfigProperty(
            AVAILABLE_PERIOD, "Available Period", "The maximum period in seconds for which the 'request' signed authentication request used in CIBA backchannel authentication request is considered valid.",
            ProviderConfigProperty.STRING_TYPE, "3600");

    /** @param session Keycloak 会话 @return 新的执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SecureCibaSignedAuthenticationRequestExecutor(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** @return 执行器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 执行器说明（FAPI CIBA 签名 request 校验） */
    @Override
    public String getHelpText() {
        return "The executor checks whether the client treats the signed authentication request in its CIBA backchannel authentication request by following Financial-grade API CIBA Security Profile.";
    }

    /** @return 可配置属性列表（含有效期限） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>(Arrays.asList(AVAILABLE_PERIOD_PROPERTY));
    }

}
