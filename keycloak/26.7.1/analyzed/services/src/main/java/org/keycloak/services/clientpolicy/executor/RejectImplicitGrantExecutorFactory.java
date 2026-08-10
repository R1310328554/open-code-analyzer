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

import java.util.Collections;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link RejectImplicitGrantExecutor} 的 Provider 工厂。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class RejectImplicitGrantExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "reject-implicit-grant";

    /** 配置键：是否自动关闭 implicit flow */
    public static final String AUTO_CONFIGURE = "auto-configure";

    /** 自动配置开关的配置属性定义 */
    private static final ProviderConfigProperty AUTO_CONFIGURE_PROPERTY = new ProviderConfigProperty(
            AUTO_CONFIGURE, "Auto-configure", "If On, then the during client creation or update, the configuration of the client will be auto-configured to reject an implicit grant/hybrid flow.", ProviderConfigProperty.BOOLEAN_TYPE, false);

    /** @param session Keycloak 会话 @return 新的执行器实例 */
    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new RejectImplicitGrantExecutor(session);
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
        return "It makes keycloak to reject an implicit grant / hybrid flow.";
    }

    /** @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.singletonList(AUTO_CONFIGURE_PROPERTY);
    }

}
