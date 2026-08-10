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

package org.keycloak.testsuite.broker.provider;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用于测试多值属性的身份提供方映射器。
 *
 * @author Martin Bartos <mabartos@redhat.com>
 */
public class MultiValuedTestIdPMapper extends AbstractIdentityProviderMapper {
    /** 兼容任意身份提供方。 */
    public static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};

    /** 映射器提供方标识符。 */
    public static final String PROVIDER_ID = "multi-valued-test-idp-mapper";
    /** 多值测试属性名称。 */
    public static final String VALUES_ATTRIBUTE = "values";

    /** 映射器配置属性列表。 */
    protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(VALUES_ATTRIBUTE);
        property.setLabel("Test values");
        property.setHelpText("Define test values");
        property.setType(ProviderConfigProperty.MULTIVALUED_STRING_TYPE);
        configProperties.add(property);
    }

    /** {@inheritDoc} 返回兼容的身份提供方列表。 */
    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    /** {@inheritDoc} 返回管理控制台中的显示分类。 */
    @Override
    public String getDisplayCategory() {
        return "Test IdP Mapper";
    }

    /** {@inheritDoc} 返回映射器显示类型名称。 */
    @Override
    public String getDisplayType() {
        return "Test MultiValued Mapper";
    }

    /** {@inheritDoc} 返回映射器帮助说明文本。 */
    @Override
    public String getHelpText() {
        return "This is testing IdP mapper with multivalued property";
    }

    /** {@inheritDoc} 返回可配置的属性定义列表。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 返回映射器唯一标识。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
