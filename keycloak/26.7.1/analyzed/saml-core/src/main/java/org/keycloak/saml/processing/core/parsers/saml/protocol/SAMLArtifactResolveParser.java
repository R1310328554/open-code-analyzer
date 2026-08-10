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
package org.keycloak.saml.processing.core.parsers.saml.protocol;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.protocol.ArtifactResolveType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.util.SAMLParserUtil;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import static org.keycloak.saml.processing.core.parsers.saml.protocol.SAMLRequestAbstractParser.VERSION_2_0;

/**
 * 解析 SAML 2.0 {@link ArtifactResolveType} 元素。
 * <p>Artifact 解析请求用于通过 HTTP Artifact 绑定获取原始 SAML 消息。</p>
 *
 * @since Jul 1, 2011
 */
public class SAMLArtifactResolveParser extends SAMLRequestAbstractParser<ArtifactResolveType> {

    /** 单例实例。 */
    private static final SAMLArtifactResolveParser INSTANCE = new SAMLArtifactResolveParser();

    private SAMLArtifactResolveParser() {
        super(SAMLProtocolQNames.ARTIFACT_RESOLVE);
    }

    /** 返回解析器单例。 */
    public static SAMLArtifactResolveParser getInstance() {
        return INSTANCE;
    }

    /**
     * 创建 ArtifactResolve 对象并解析根元素属性。
     *
     * @param xmlEventReader XML 事件读取器
     * @param startElement 起始元素
     * @return 解析后的 ArtifactResolveType
     * @throws ParsingException 解析失败时抛出
     */
    @Override
    protected ArtifactResolveType instantiateElement(XMLEventReader xmlEventReader, StartElement startElement) throws ParsingException {
        SAMLParserUtil.validateAttributeValue(startElement, SAMLProtocolQNames.ATTR_VERSION, VERSION_2_0);
        String id = StaxParserUtil.getRequiredAttributeValue(startElement, SAMLProtocolQNames.ATTR_ID);
        XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getRequiredAttributeValue(startElement, SAMLProtocolQNames.ATTR_ISSUE_INSTANT));

        ArtifactResolveType authnRequest = new ArtifactResolveType(id, issueInstant);
        super.parseBaseAttributes(startElement, authnRequest);

        return authnRequest;
    }

    /** 分发并解析 Issuer、Signature、Extensions 及 Artifact 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, ArtifactResolveType target,
      SAMLProtocolQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ISSUER:
            case SIGNATURE:
            case EXTENSIONS:
                parseCommonElements(element, elementDetail, xmlEventReader, target);
                break;

            case ARTIFACT:
                StaxParserUtil.advance(xmlEventReader);
                target.setArtifact(StaxParserUtil.getElementText(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}