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
package org.keycloak.adapters.saml.descriptor.parsers;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.core.parsers.saml.xmldsig.XmlDSigQNames;
import org.keycloak.saml.processing.core.util.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 从 SAML IdP 元数据描述符 XML 中提取 {@link KeyInfo} 密钥材料。
 *
 * <p>遍历 EntityDescriptor/IDPSSODescriptor/KeyDescriptor 节点，
 * 按 use 属性（signing/encryption）分组返回证书与密钥信息。</p>
 *
 * @author hmlnarik
 */
public class SamlDescriptorIDPKeysExtractor {

    /** XPath 命名空间前缀：m=元数据，dsig=XML 数字签名。 */
    private static final NamespaceContext NS_CONTEXT = new NamespaceContext();
    static {
        NS_CONTEXT.addNsUriPair("m", JBossSAMLURIConstants.METADATA_NSURI.get());
        NS_CONTEXT.addNsUriPair("dsig", JBossSAMLURIConstants.XMLDSIG_NSURI.get());
    }

    /** 用于反序列化 KeyInfo DOM 节点的工厂。 */
    private final KeyInfoFactory kif = KeyInfoFactory.getInstance();

    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private final XPath xpath = xPathfactory.newXPath();
    {
        xpath.setNamespaceContext(NS_CONTEXT);
    }

    /**
     * 解析 IdP 元数据流，提取所有 KeyDescriptor 中的 {@link KeyInfo}。
     *
     * @param stream SAML 元数据 XML 输入流
     * @return 按 use 属性分组的 KeyInfo 多值映射
     * @throws ParsingException 解析或 XPath 评估失败时抛出
     */
    public MultivaluedHashMap<String, KeyInfo> parse(InputStream stream) throws ParsingException {
        MultivaluedHashMap<String, KeyInfo> res = new MultivaluedHashMap<>();

        try {
            DocumentBuilder builder = DocumentUtil.getDocumentBuilder();
            Document doc = builder.parse(stream);

            XPathExpression expr = xpath.compile("//m:EntityDescriptor/m:IDPSSODescriptor/m:KeyDescriptor");
            NodeList keyDescriptors = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < keyDescriptors.getLength(); i ++) {
                Node keyDescriptor = keyDescriptors.item(i);
                Element keyDescriptorEl = (Element) keyDescriptor;
                KeyInfo ki = processKeyDescriptor(keyDescriptorEl);
                if (ki != null) {
                    String use = keyDescriptorEl.getAttribute(JBossSAMLConstants.USE.get());
                    res.add(use, ki);
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException | MarshalException | XPathExpressionException e) {
            throw new ParsingException("Error parsing SAML descriptor", e);
        }

        return res;
    }

    /** 从单个 KeyDescriptor 元素 unmarshals 首个 ds:KeyInfo 子节点。 */
    private KeyInfo processKeyDescriptor(Element keyDescriptor) throws MarshalException {
        NodeList childNodes = keyDescriptor.getElementsByTagNameNS(JBossSAMLURIConstants.XMLDSIG_NSURI.get(), XmlDSigQNames.KEY_INFO.getQName().getLocalPart());

        if (childNodes.getLength() == 0) {
            return null;
        }
        Node keyInfoNode = childNodes.item(0);
        return (keyInfoNode == null) ? null : kif.unmarshalKeyInfo(new DOMStructure(keyInfoNode));
    }

}
