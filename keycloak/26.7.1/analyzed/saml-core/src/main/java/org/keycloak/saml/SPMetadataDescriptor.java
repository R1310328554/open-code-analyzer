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

package org.keycloak.saml;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.parsers.DocumentBuilder;

import org.keycloak.dom.saml.v2.metadata.EndpointType;
import org.keycloak.dom.saml.v2.metadata.EntityDescriptorType;
import org.keycloak.dom.saml.v2.metadata.IndexedEndpointType;
import org.keycloak.dom.saml.v2.metadata.KeyDescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyTypes;
import org.keycloak.dom.saml.v2.metadata.SPSSODescriptorType;
import org.keycloak.dom.xmlsec.w3.xmlenc.EncryptionMethodType;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.keycloak.saml.common.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;
import static org.keycloak.saml.common.constants.JBossSAMLURIConstants.XMLDSIG_NSURI;

/**
 * SP（服务提供者）SAML 元数据（EntityDescriptor）构建工具类。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SPMetadataDescriptor {

    /**
     * 构建单 ACS / 单 SLO 端点的 SP 元数据（简化重载）。
     *
     * @param loginBinding 登录绑定 URI
     * @param logoutBinding 登出绑定 URI
     * @param assertionEndpoint ACS 端点 URL
     * @param logoutEndpoint SLO 端点 URL
     * @param wantAuthnRequestsSigned 是否要求 AuthnRequest 签名
     * @param wantAssertionsSigned 是否要求 Assertion 签名
     * @param wantAssertionsEncrypted 是否要求 Assertion 加密
     * @param entityId SP 实体 ID
     * @param nameIDPolicyFormat 支持的 NameID 格式
     * @param signingCerts 签名密钥描述符列表
     * @param encryptionCerts 加密密钥描述符列表
     * @return EntityDescriptor 对象
     */
    public static EntityDescriptorType buildSPDescriptor(URI loginBinding, URI logoutBinding, URI assertionEndpoint, URI logoutEndpoint,
            boolean wantAuthnRequestsSigned, boolean wantAssertionsSigned, boolean wantAssertionsEncrypted,
            String entityId, String nameIDPolicyFormat, List<KeyDescriptorType> signingCerts, List<KeyDescriptorType> encryptionCerts) {
        return buildSPDescriptor(Collections.singletonList(new EndpointType(loginBinding, assertionEndpoint)),
                Collections.singletonList(new EndpointType(logoutBinding, logoutEndpoint)),
                wantAuthnRequestsSigned, wantAssertionsSigned, wantAssertionsEncrypted, entityId, nameIDPolicyFormat, signingCerts, encryptionCerts, null);
    }

    /**
     * 构建 SP 元数据（多 ACS / SLO 端点，无缓存过期）。
     *
     * @return EntityDescriptor 对象
     */
    public static EntityDescriptorType buildSPDescriptor(List<EndpointType> assertionConsumerServices, List<EndpointType> singleLogoutServices,
            boolean wantAuthnRequestsSigned, boolean wantAssertionsSigned, boolean wantAssertionsEncrypted,
            String entityId, String nameIDPolicyFormat, List<KeyDescriptorType> signingCerts,
            List<KeyDescriptorType> encryptionCerts) {
        return buildSPDescriptor(assertionConsumerServices, singleLogoutServices, wantAuthnRequestsSigned, wantAssertionsSigned, wantAssertionsEncrypted,
                entityId, nameIDPolicyFormat, signingCerts, encryptionCerts, null);
    }

    /**
     * 构建完整 SP 元数据，可指定缓存过期时间。
     *
     * @param expiration 元数据缓存时长（秒），{@code null} 表示不设置
     * @return EntityDescriptor 对象
     */
    public static EntityDescriptorType buildSPDescriptor(List<EndpointType> assertionConsumerServices, List<EndpointType> singleLogoutServices,
            boolean wantAuthnRequestsSigned, boolean wantAssertionsSigned, boolean wantAssertionsEncrypted,
            String entityId, String nameIDPolicyFormat, List<KeyDescriptorType> signingCerts,
            List<KeyDescriptorType> encryptionCerts, Long expiration) {
        EntityDescriptorType entityDescriptor = new EntityDescriptorType(entityId);
        entityDescriptor.setID(IDGenerator.create("ID_"));
        if (expiration != null && expiration > 0) {
            try {
                Duration cacheDuration = DatatypeFactory.newInstance().newDuration(TimeUnit.SECONDS.toMillis(expiration));
                entityDescriptor.setCacheDuration(cacheDuration);
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException("Cannot create datatype factory to create duration", e);
            }
        }

        SPSSODescriptorType spSSODescriptor = new SPSSODescriptorType(Arrays.asList(PROTOCOL_NSURI.get()));
        spSSODescriptor.setAuthnRequestsSigned(wantAuthnRequestsSigned);
        spSSODescriptor.setWantAssertionsSigned(wantAssertionsSigned);
        spSSODescriptor.addNameIDFormat(nameIDPolicyFormat);
        singleLogoutServices.forEach(spSSODescriptor::addSingleLogoutService);

        if (wantAuthnRequestsSigned && signingCerts != null) {
            for (KeyDescriptorType key: signingCerts) {
                spSSODescriptor.addKeyDescriptor(key);
            }
        }

        if (wantAssertionsEncrypted && encryptionCerts != null) {
            for (KeyDescriptorType key: encryptionCerts) {
                spSSODescriptor.addKeyDescriptor(key);
            }
        }

        for (ListIterator<EndpointType> iter = assertionConsumerServices.listIterator(); iter.hasNext(); ) {
            EndpointType endpoint = iter.next();
            IndexedEndpointType assertionConsumerService = new IndexedEndpointType(endpoint.getBinding(), endpoint.getLocation());
            assertionConsumerService.setIndex(iter.nextIndex());
            assertionConsumerService.setIsDefault(iter.nextIndex() == 1 ? Boolean.TRUE : null);
            spSSODescriptor.addAssertionConsumerService(assertionConsumerService);
        }

        entityDescriptor.addChoiceType(new EntityDescriptorType.EDTChoiceType(Arrays.asList(new EntityDescriptorType.EDTDescriptorChoiceType(spSSODescriptor))));

        return entityDescriptor;
    }

    /**
     * 构建 KeyDescriptor 元素（含可选加密算法列表）。
     *
     * @param keyInfo ds:KeyInfo DOM 元素
     * @param use 密钥用途（signing / encryption）
     * @param algorithm 支持的加密算法 URI（可变参数）
     * @return KeyDescriptorType 对象
     */
    public static KeyDescriptorType buildKeyDescriptorType(Element keyInfo, KeyTypes use, String... algorithm) {
        KeyDescriptorType keyDescriptor = new KeyDescriptorType();
        keyDescriptor.setUse(use);
        keyDescriptor.setKeyInfo(keyInfo);

        if (algorithm != null) {
            for (String alg : algorithm) {
                EncryptionMethodType encMethod = new EncryptionMethodType(alg);
                keyDescriptor.addEncryptionMethod(encMethod);
            }
        }

        return keyDescriptor;
    }

    /**
     * 构建包含 X509 证书的 ds:KeyInfo DOM 元素。
     *
     * @param keyName 可选密钥名称
     * @param pemEncodedCertificate PEM 编码的 X509 证书（不含头尾）
     * @return KeyInfo DOM 元素
     */
    public static Element buildKeyInfoElement(String keyName, String pemEncodedCertificate)
        throws javax.xml.parsers.ParserConfigurationException
    {
        DocumentBuilder db = DocumentUtil.getDocumentBuilder();
        Document doc = db.newDocument();

        Element keyInfo = doc.createElementNS(XMLDSIG_NSURI.get(), "ds:KeyInfo");

        if (keyName != null) {
            Element keyNameElement = doc.createElementNS(XMLDSIG_NSURI.get(), "ds:KeyName");
            keyNameElement.setTextContent(keyName);
            keyInfo.appendChild(keyNameElement);
        }

        Element x509Data = doc.createElementNS(XMLDSIG_NSURI.get(), "ds:X509Data");

        Element x509Certificate = doc.createElementNS(XMLDSIG_NSURI.get(), "ds:X509Certificate");
        x509Certificate.setTextContent(pemEncodedCertificate);
      
        x509Data.appendChild(x509Certificate);

        keyInfo.appendChild(x509Data);

        return keyInfo;
    }
}
