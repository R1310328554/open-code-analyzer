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

package org.keycloak.testsuite.components.amphibian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

/**
 * 两栖组件测试提供方工厂实现，用于验证组件失效与配置校验逻辑。
 */
public class TestAmphibianProviderFactoryImpl implements TestAmphibianProviderFactory {

    /** 提供方标识符。 */
    public static final String PROVIDER_ID = "test";

    /** 管理控制台可配置属性列表。 */
    private static final List<ProviderConfigProperty> CONFIG = ProviderConfigurationBuilder.create()
            .property("secret", "Secret", "A secret value", STRING_TYPE, null, null, true)
            .property("number", "Number", "A number value", STRING_TYPE, null, null, false)
            .property("required", "Required", "A required value", STRING_TYPE, null, null, false)
            .property("val1", "Value 1", "Some more values", STRING_TYPE, null, null, false)
            .property("val2", "Value 2", "Some more values", STRING_TYPE, null, null, false)
            .property("val3", "Value 3", "Some more values", STRING_TYPE, null, null, false)
            .build();

    /** 密钥配置值。 */
    private String secret;
    /** 数值配置项。 */
    private Integer number;
    /** 必填字符串配置。 */
    private String required;
    /** 附加配置值 1。 */
    private String val1;
    /** 附加配置值 2。 */
    private String val2;
    /** 附加配置值 3。 */
    private String val3;

    /** {@inheritDoc} 创建测试提供方实例。 */
    @Override
    public TestImplProvider create(KeycloakSession session) {
        return new TestImplProvider();
    }

    /** {@inheritDoc} 校验组件配置是否满足约束。 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        ConfigurationValidationHelper.check(model)
                .checkRequired("required", "Required")
                .checkInt("number", "Number", false);
    }

    /** {@inheritDoc} 返回提供方帮助说明。 */
    @Override
    public String getHelpText() {
        return "Provider to test component invalidation";
    }

    /** {@inheritDoc} 返回可配置属性定义。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG;
    }

    /** {@inheritDoc} 从全局配置加载工厂级参数。 */
    @Override
    public void init(Config.Scope config) {
        this.secret = config.get("secret");
        this.number = config.getInt("number");
        this.required = config.get("required");
        this.val1 = config.get("val1");
        this.val2 = config.get("val2");
        this.val3 = config.get("val3");
    }

    /** {@inheritDoc} 返回提供方唯一标识。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 会话工厂初始化后的回调。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 测试提供方的具体实现，暴露当前工厂配置快照。 */
    public class TestImplProvider implements TestAmphibianProvider {

        /** {@inheritDoc} 汇总工厂注入的全部配置项。 */
        @Override
        public Map<String, Object> getDetails() {
            Map<String, Object> c = new HashMap<>();
            c.put("secret", secret);
            c.put("number", number);
            c.put("required", required);
            c.put("val1", val1);
            c.put("val2", val2);
            c.put("val3", val3);
            return c;
        }

        @Override
        public void close() {
        }

    }

}
