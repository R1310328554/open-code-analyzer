/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.saml.config.parsers;

import java.util.Properties;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.adapters.saml.config.SP;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 {@code <RoleMappingsProvider>} 元素（schema 中 role-mappings-provider-type）。
 *
 * <p>读取提供者 id 及嵌套 Property 键值对，供 SPI 实现初始化使用。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RoleMappingsProviderParser extends AbstractKeycloakSamlAdapterV1Parser<SP.RoleMappingsProviderConfig> {

    /** 单例解析器实例。 */
    private static final RoleMappingsProviderParser INSTANCE = new RoleMappingsProviderParser();

    private RoleMappingsProviderParser() {
        super(KeycloakSamlAdapterV1QNames.ROLE_MAPPINGS_PROVIDER);
    }

    /** @return 共享的 {@link RoleMappingsProviderParser} 实例 */
    public static RoleMappingsProviderParser getInstance() {
        return INSTANCE;
    }

    /** 读取 id 属性并初始化空的 Properties 配置容器。 */
    @Override
    protected SP.RoleMappingsProviderConfig instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        SP.RoleMappingsProviderConfig providerConfig = new SP.RoleMappingsProviderConfig();
        providerConfig.setId(StaxParserUtil.getRequiredAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_ID));
        providerConfig.setConfiguration(new Properties());
        return providerConfig;
    }

    /** 解析 Property 子元素，将 name/value 写入提供者配置。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, SP.RoleMappingsProviderConfig target, KeycloakSamlAdapterV1QNames element, StartElement elementDetail) throws ParsingException {
        switch(element) {
            case PROPERTY:
                final String name = StaxParserUtil.getRequiredAttributeValueRP(elementDetail, KeycloakSamlAdapterV1QNames.ATTR_NAME);
                final String value = StaxParserUtil.getRequiredAttributeValueRP(elementDetail, KeycloakSamlAdapterV1QNames.ATTR_VALUE);
                target.addConfigurationProperty(name, value);
                break;
        }
    }
}
