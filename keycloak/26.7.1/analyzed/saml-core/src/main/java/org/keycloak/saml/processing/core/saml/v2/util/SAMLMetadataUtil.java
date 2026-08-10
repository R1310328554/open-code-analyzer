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
package org.keycloak.saml.processing.core.saml.v2.util;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Function;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.keycloak.dom.saml.v2.metadata.EntitiesDescriptorType;
import org.keycloak.dom.saml.v2.metadata.EntityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.IDPSSODescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyDescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyTypes;
import org.keycloak.dom.saml.v2.metadata.SPSSODescriptorType;
import org.keycloak.dom.saml.v2.metadata.SSODescriptorType;
import org.keycloak.saml.SignatureAlgorithm;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.common.util.StaxUtil;
import org.keycloak.saml.processing.api.saml.v2.sig.SAML2Signature;
import org.keycloak.saml.processing.core.parsers.saml.SAMLParser;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.writers.SAMLMetadataWriter;
import org.keycloak.saml.processing.core.util.XMLSignatureUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SAML 2.0 元数据（Metadata）读写与签名工具类。
 * <p>支持 EntityDescriptor 解析、证书提取、IDP/SP 描述符定位及元数据签名。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLMetadataUtil {

    /** UTF-8 字节顺序标记（BOM），解析前需移除。 */
    public static final String UTF8_BOM = "\uFEFF";

    /**
     * 从 KeyDescriptor 的 KeyInfo 元素中提取 {@link X509Certificate}。
     *
     * @param keyDescriptor 密钥描述符
     *
     * @return X509 证书，未找到时返回 null
     *
     * @throws org.keycloak.saml.common.exceptions.ProcessingException
     * @throws org.keycloak.saml.common.exceptions.ConfigurationException
     */
    public static X509Certificate getCertificate(KeyDescriptorType keyDescriptor) throws ConfigurationException,
            ProcessingException {
        X509Certificate cert = null;
        Element keyInfo = keyDescriptor.getKeyInfo();
        if (keyInfo != null) {
            NodeList x509DataNodes = keyInfo.getElementsByTagName("X509Data");
            if (x509DataNodes == null || x509DataNodes.getLength() == 0) {
                x509DataNodes = keyInfo.getElementsByTagNameNS(JBossSAMLURIConstants.XMLDSIG_NSURI.get(), "X509Data");
            }

            if (x509DataNodes == null || x509DataNodes.getLength() == 0) {
                x509DataNodes = keyInfo.getElementsByTagName("ds:X509Data");
            }

            if (x509DataNodes != null && x509DataNodes.getLength() > 0) {
                // Choose the first one
                Node x509DataNode = x509DataNodes.item(0);
                NodeList children = x509DataNode.getChildNodes();
                int len = children != null ? children.getLength() : 0;
                for (int i = 0; i < len; i++) {
                    Node nl = children.item(i);
                    if (nl.getNodeName().contains("X509Certificate")) {
                        Node certNode = nl.getFirstChild();
                        String certNodeValue = certNode.getNodeValue();
                        cert = XMLSignatureUtil.getX509CertificateFromKeyInfoString(certNodeValue.replaceAll("\\s", ""));
                        break;
                    }
                }
            }
        }
        return cert;
    }

    /**
     * 从 SSO 描述符中按密钥用途（signing/encryption）查找 X509 证书。
     *
     * @param use 密钥用途，null 表示任意
     * @param ssoDescriptorType IDP 或 SP SSO 描述符
     * @return 匹配的证书，未找到时返回 null
     */
    public static X509Certificate getCertificate(KeyTypes use, SSODescriptorType ssoDescriptorType) {
        if (ssoDescriptorType != null) {
            for (KeyDescriptorType keyDescriptorType : ssoDescriptorType.getKeyDescriptor()) {
                KeyTypes keyUse = keyDescriptorType.getUse();

                if (keyUse == null || (use != null && keyUse.value().equals(use.value()))) {
                    try {
                        return getCertificate(keyDescriptorType);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not parse KeyDescriptor X509 certificate from metadata [" + ssoDescriptorType.getID() + "].");
                    }
                }
            }
        }

        return null;
    }

    /**
     * 解析 SAML 元数据 XML 字符串为 {@link EntityDescriptorType}。
     * <p>自动处理 UTF-8 BOM 及 EntitiesDescriptor 包装。</p>
     */
    public static EntityDescriptorType parseEntityDescriptorType(String descriptor) throws ParsingException {
        descriptor = removeUTF8BOM(descriptor);
        Object parsedObject = SAMLParser.getInstance().parse(StaxParserUtil.getXMLEventReader(descriptor));
        EntityDescriptorType entityType;

        if (EntitiesDescriptorType.class.isInstance(parsedObject)) {
            entityType = (EntityDescriptorType) ((EntitiesDescriptorType) parsedObject).getEntityDescriptor().get(0);
        } else {
            entityType = (EntityDescriptorType) parsedObject;
        }

        return entityType;
    }

    /** 在 EntityDescriptor 中定位 IDP SSO 描述符。 */
    public static IDPSSODescriptorType locateIDPSSODescriptorType(EntityDescriptorType entityType) {
        return locateSSODescriptorType(entityType, SAMLMetadataUtil::getIDPSSODescriptorType);
    }

    /** 在 EntityDescriptor 中定位 SP SSO 描述符。 */
    public static SPSSODescriptorType locateSPSSODescriptorType(EntityDescriptorType entityType) {
        return locateSSODescriptorType(entityType, SAMLMetadataUtil::getSPSSODescriptorType);
    }

    private static IDPSSODescriptorType getIDPSSODescriptorType(EntityDescriptorType.EDTDescriptorChoiceType type) {
        return type.getIdpDescriptor();
    }

    private static SPSSODescriptorType getSPSSODescriptorType(EntityDescriptorType.EDTDescriptorChoiceType type) {
        return type.getSpDescriptor();
    }

    private static <T> T locateSSODescriptorType(EntityDescriptorType entityType,
            Function<EntityDescriptorType.EDTDescriptorChoiceType, T> getter) {
        List<EntityDescriptorType.EDTChoiceType> choiceType = entityType.getChoiceType();
        T descriptor = null;
        if (!choiceType.isEmpty()) {

            // 元数据可能含多种描述符（如 ADFS），需遍历查找目标类型
            for (EntityDescriptorType.EDTChoiceType edtChoiceType : entityType.getChoiceType()) {
                List<EntityDescriptorType.EDTDescriptorChoiceType> descriptors = edtChoiceType.getDescriptors();

                if (!descriptors.isEmpty() && getter.apply(descriptors.get(0)) != null) {
                    descriptor = getter.apply(descriptors.get(0));
                }
            }
        }
        return descriptor;
    }

    /** 移除字符串开头的 UTF-8 BOM 字符。 */
    public static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /** 将 EntityDescriptor 序列化为 XML 字符串。 */
    public static String writeEntityDescriptorType(EntityDescriptorType type) throws ProcessingException {
        final StringWriter sw = new StringWriter();
        final SAMLMetadataWriter writer = new SAMLMetadataWriter(StaxUtil.getXMLStreamWriter(sw));
        writer.writeEntityDescriptor(type);
        return sw.toString();
    }

    /**
     * 对 EntityDescriptor 元数据进行 XML 数字签名。
     * <p>若 ID 为空则自动生成，签名后返回完整 XML 字符串。</p>
     */
    public static String signEntityDescriptorType(EntityDescriptorType type, SignatureAlgorithm sigAlg,
            String kid, X509Certificate certificate, KeyPair keyPair) throws ProcessingException, ConfigurationException, ParsingException {
        if (type.getID() == null) {
            type.setID(IDGenerator.create("ID_"));
        }

        // 将描述符序列化为 XML
        final String descriptor = writeEntityDescriptorType(type);

        // 从 XML 创建 DOM 文档
        final Document metadataDocument = DocumentUtil.getDocument(descriptor);
        final SAML2Signature signatureHelper = new SAML2Signature();
        signatureHelper.setSignatureMethod(sigAlg.getXmlSignatureMethod());
        signatureHelper.setDigestMethod(sigAlg.getXmlSignatureDigestMethod());
        signatureHelper.setX509Certificate(certificate);

        final Node nextSibling = metadataDocument.getDocumentElement().getFirstChild();
        signatureHelper.setNextSibling(nextSibling);

        // 对元数据文档进行 XML 签名
        signatureHelper.signSAMLDocument(metadataDocument, kid, keyPair, CanonicalizationMethod.EXCLUSIVE);

        return DocumentUtil.getDocumentAsString(metadataDocument);
    }
}
