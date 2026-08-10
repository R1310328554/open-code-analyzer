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
package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.AttributeAuthorityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.AuthnAuthorityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.EntityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.IDPSSODescriptorType;
import org.keycloak.dom.saml.v2.metadata.PDPDescriptorType;
import org.keycloak.dom.saml.v2.metadata.SPSSODescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ENTITY_DESCRIPTOR;

/**
 * 解析 SAML 元数据中的 {@code EntityDescriptor} 元素。
 * <p>描述单个 SAML 实体的标识、角色描述符（IdP/SP/属性权威/认证权威/PDP）、组织及联系人信息。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 14, 2010
 */
public class SAMLEntityDescriptorParser extends AbstractStaxSamlMetadataParser<EntityDescriptorType> {

    /** 单例实例。 */
    private static final SAMLEntityDescriptorParser INSTANCE = new SAMLEntityDescriptorParser();

    /** 构造并绑定 ENTITY_DESCRIPTOR 根元素。 */
    private SAMLEntityDescriptorParser() {
        super(ENTITY_DESCRIPTOR);
    }

    /** 返回解析器单例。 */
    public static SAMLEntityDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建实体描述符并读取 entityID、validUntil、cacheDuration 及 ID 属性。 */
    @Override
    protected EntityDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        String entityID = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_ENTITY_ID);
        EntityDescriptorType descriptor = new EntityDescriptorType(entityID);

        descriptor.setValidUntil(StaxParserUtil.getXmlTimeAttributeValue(element, SAMLMetadataQNames.ATTR_VALID_UNTIL));
        descriptor.setCacheDuration(StaxParserUtil.getXmlDurationAttributeValue(element, SAMLMetadataQNames.ATTR_CACHE_DURATION));
        descriptor.setID(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_ID));

        return descriptor;
    }

    /** 解析签名、扩展、各类角色描述符、组织及联系人等子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EntityDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case SIGNATURE:
                target.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
                break;

            case EXTENSIONS:
                target.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;

            case IDP_SSO_DESCRIPTOR:
                {
                    IDPSSODescriptorType idpSSO = SAMLIDPSSODescriptorParser.getInstance().parse(xmlEventReader);

                    EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(idpSSO);
                    EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);
                    target.addChoiceType(edtChoice);
                }
                break;

            case SP_SSO_DESCRIPTOR:
                {
                    SPSSODescriptorType spSSO = SAMLSPSSODescriptorParser.getInstance().parse(xmlEventReader);

                    EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(spSSO);
                    EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);
                    target.addChoiceType(edtChoice);
                }
                break;

            case ATTRIBUTE_AUTHORITY_DESCRIPTOR:
                {
                    AttributeAuthorityDescriptorType attrAuthority = SAMLAttributeAuthorityDescriptorParser.getInstance().parse(xmlEventReader);

                    EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(attrAuthority);
                    EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);
                    target.addChoiceType(edtChoice);
                }
                break;

            case AUTHN_AUTHORITY_DESCRIPTOR:
                {
                    AuthnAuthorityDescriptorType authAuthority = SAMLAuthnAuthorityDescriptorParser.getInstance().parse(xmlEventReader);

                    EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(authAuthority);
                    EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);
                    target.addChoiceType(edtChoice);
                }
                break;

            case PDP_DESCRIPTOR:
                {
                    PDPDescriptorType pdpDescriptor = SAMLPDPDescriptorParser.getInstance().parse(xmlEventReader);

                    EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(pdpDescriptor);
                    EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);
                    target.addChoiceType(edtChoice);
                }
                break;
            case ROLE_DESCRIPTOR:
            case AFFILIATION_DESCRIPTOR:
            case ADDITIONAL_METADATA_LOCATION:
                // 暂不支持的描述符类型，跳过整个元素块
                StaxParserUtil.bypassElementBlock(xmlEventReader);
                break;

            case ORGANIZATION:
                target.setOrganization(SAMLOrganizationParser.getInstance().parse(xmlEventReader));
                break;

            case CONTACT_PERSON:
                target.addContactPerson(SAMLContactPersonParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());

        }
    }
}
