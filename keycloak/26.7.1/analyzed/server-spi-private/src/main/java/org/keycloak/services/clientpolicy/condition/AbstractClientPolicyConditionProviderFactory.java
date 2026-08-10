/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.condition;

import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端策略条件工厂抽象基类：提供通用配置属性与生命周期空实现。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractClientPolicyConditionProviderFactory implements ClientPolicyConditionProviderFactory {

    /** 负向逻辑配置项键名。 */
    public static final String IS_NEGATIVE_LOGIC = "is-negative-logic";

    /** 向工厂配置列表添加负向逻辑等通用属性。 */
    static protected void addCommonConfigProperties(List<ProviderConfigProperty> configProperties) {
        ProviderConfigProperty property = new ProviderConfigProperty(IS_NEGATIVE_LOGIC, "Negative Logic",
                "If On, the result of condition's evaluation is reverted from true to false and vice versa.",
                ProviderConfigProperty.BOOLEAN_TYPE, false);
        configProperties.add(property);
    }

    /** 默认无初始化逻辑。 */
    @Override
    public void init(Scope config) {
    }

    /** 默认无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 默认无关闭资源逻辑。 */
    @Override
    public void close() {
    }
}
