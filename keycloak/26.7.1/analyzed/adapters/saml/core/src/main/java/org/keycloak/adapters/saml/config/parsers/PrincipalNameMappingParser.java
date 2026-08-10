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

package org.keycloak.adapters.saml.config.parsers;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.adapters.saml.config.SP;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 {@code <PrincipalNameMapping>}，配置 SAML 主体到应用用户名的映射策略。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PrincipalNameMappingParser extends AbstractKeycloakSamlAdapterV1Parser<SP.PrincipalNameMapping> {

    /** 单例解析器实例。 */
    private static final PrincipalNameMappingParser INSTANCE = new PrincipalNameMappingParser();

    private PrincipalNameMappingParser() {
        super(KeycloakSamlAdapterV1QNames.PRINCIPAL_NAME_MAPPING);
    }

    /** @return 共享的 {@link PrincipalNameMappingParser} 实例 */
    public static PrincipalNameMappingParser getInstance() {
        return INSTANCE;
    }

    /** 读取 policy 与 attribute 属性，构建主体名称映射配置。 */
    @Override
    protected SP.PrincipalNameMapping instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        final SP.PrincipalNameMapping mapping = new SP.PrincipalNameMapping();

        mapping.setPolicy(StaxParserUtil.getRequiredAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_POLICY));
        mapping.setAttributeName(StaxParserUtil.getAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_ATTRIBUTE));

        return mapping;
    }

    /** 本元素无子节点，无需额外处理。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, SP.PrincipalNameMapping target, KeycloakSamlAdapterV1QNames element, StartElement elementDetail) throws ParsingException {
    }
}
