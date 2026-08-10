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

package org.keycloak.services.clientpolicy.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link SecureRequestObjectExecutor} 的 Provider 工厂。
 * <p>暴露 nbf 校验、可用期、加密要求及时钟偏差等 FAPI 相关配置项。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureRequestObjectExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "secure-request-object";

    /** 配置键：是否校验 nbf 声明 */
    public static final String VERIFY_NBF = "verify-nbf";

    /** nbf 校验开关配置属性定义 */
    private static final ProviderConfigProperty VERIFY_NBF_PROPERTY = new ProviderConfigProperty(
            VERIFY_NBF, "Verify Not-Before", "If ON, then it will be verified if 'request' object used in OIDC authorization request contains not-before " +
            "claim and this claim will be validated", ProviderConfigProperty.BOOLEAN_TYPE, true);

    /** 配置键：request 对象最大可用期（秒） */
    public static final String AVAILABLE_PERIOD = "available-period";

    private static final ProviderConfigProperty AVAILABLE_PERIOD_PROPERTY = new ProviderConfigProperty(
            AVAILABLE_PERIOD, "Available Period", "The maximum period in seconds for which the 'request' object used in OIDC authorization request is considered valid. " +
            "It is used if 'Verify Not-Before' is ON.", ProviderConfigProperty.STRING_TYPE, "3600");

    /** 配置键：是否强制 request 对象加密 */
    public static final String ENCRYPTION_REQUIRED = "encryption-required";

    private static final ProviderConfigProperty ENCRYPTION_REQUIRED_PROPERTY = new ProviderConfigProperty(
            ENCRYPTION_REQUIRED, "Encryption Required", "Whether request object encryption is required. If enabled, request objects must be encrypted. Otherwise, encryption is optional.",
            ProviderConfigProperty.BOOLEAN_TYPE, Boolean.FALSE);

    /** 配置键：客户端与 Keycloak 间允许的时钟偏差（秒） */
    public static final String ALLOWED_CLOCK_SKEW = "allowed-clock-skew";

    private static final ProviderConfigProperty ALLOWED_CLOCK_SKEW_PROPERTY = new ProviderConfigProperty(
             ALLOWED_CLOCK_SKEW, "Allowed Clock Skew", "The allowed clock skew (sec) between a client and Keycloak. " +
            "For example, if nbf claim values of 'request' object is ahead from Keycloak's clock within the clock skew, Keycloak accept the object even if its nbf claim value indicates the future.",
            ProviderConfigProperty.STRING_TYPE, "15");
    
    /** @param session Keycloak 会话 @return 新的执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SecureRequestObjectExecutor(session);
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

    /** @return 执行器标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "The executor checks whether the client treats the request object in its authorization request by following Financial-grade API Security Profile : Read and Write API Security Profile.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>(Arrays.asList(VERIFY_NBF_PROPERTY, AVAILABLE_PERIOD_PROPERTY, ENCRYPTION_REQUIRED_PROPERTY, ALLOWED_CLOCK_SKEW_PROPERTY));
    }

}
