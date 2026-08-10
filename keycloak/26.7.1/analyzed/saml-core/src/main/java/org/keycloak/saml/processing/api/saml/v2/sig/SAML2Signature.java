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
package org.keycloak.saml.processing.api.saml.v2.sig;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.core.util.SignatureUtilTransferObject;
import org.keycloak.saml.processing.core.util.XMLSignatureUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SAML 2.0 XML 数字签名的创建与校验工具类。
 * Class that deals with SAML2 Signature
 *
 * @author Anil.Saldhana@redhat.com
 * @author alessio.soldano@jboss.com
 * @since May 26, 2009
 */
public class SAML2Signature {

    /** 日志实例。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 签名算法 URI（默认 RSA-SHA1）。 */
    private String signatureMethod = SignatureMethod.RSA_SHA1;

    /** 摘要算法 URI（默认 SHA1）。 */
    private String digestMethod = DigestMethod.SHA1;

    /** 签名元素插入位置的兄弟节点。 */
    private Node sibling;

    /** 写入 SignedInfo 的 X509 证书（可选）。 */
    private X509Certificate x509Certificate;

    /** 获取签名算法 URI。 */
    public String getSignatureMethod() {
        return signatureMethod;
    }

    /** 设置签名算法 URI。 */
    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    /** 获取摘要算法 URI。 */
    public String getDigestMethod() {
        return digestMethod;
    }

    /** 设置摘要算法 URI。 */
    public void setDigestMethod(String digestMethod) {
        this.digestMethod = digestMethod;
    }

    /** 指定签名块插入位置的兄弟节点。 */
    public void setNextSibling(Node sibling) {
        this.sibling = sibling;
    }

    /**
     * 设为 false 时不在签名中包含 KeyInfo。
     *
     * Set to false, if you do not want to include keyinfo in the signature
     *
     * @param val 是否包含 KeyInfo
     *
     * @since v2.0.1
     */
    public void setSignatureIncludeKeyInfo(boolean val) {
        if (!val) {
            XMLSignatureUtil.setIncludeKeyInfoInSignature(false);
        }
    }

    /**
     * 设置 {@link X509Certificate}，使 SignedInfo 携带 X509Data。
     *
     * Set the {@link X509Certificate} if you desire
     * to have the SignedInfo have X509 Data
     *
     * This method needs to be called before any of the sign methods.
     *
     * @param x509Certificate
     *
     * @since v2.5.0
     */
    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    /**
     * 对文档根元素执行 XML 签名。
     *
     * Sign an Document at the root
     *
     * @param keyPair Key Pair 密钥对
     *
     * @return
     *
     * @throws ParserConfigurationException
     * @throws XMLSignatureException
     * @throws MarshalException
     * @throws GeneralSecurityException
     */
    public Document sign(Document doc, String referenceID, String keyName, KeyPair keyPair, String canonicalizationMethodType) throws ParserConfigurationException,
            GeneralSecurityException, MarshalException, XMLSignatureException {
        String referenceURI = "#" + referenceID;

        configureIdAttribute(doc);

        if (sibling != null) {
            SignatureUtilTransferObject dto = new SignatureUtilTransferObject();
            dto.setDocumentToBeSigned(doc);
            dto.setKeyName(keyName);
            dto.setKeyPair(keyPair);
            dto.setDigestMethod(digestMethod);
            dto.setSignatureMethod(signatureMethod);
            dto.setReferenceURI(referenceURI);
            dto.setNextSibling(sibling);
            dto.setX509Certificate(x509Certificate);

            return XMLSignatureUtil.sign(dto, canonicalizationMethodType);
        }
        return XMLSignatureUtil.sign(doc, keyName, keyPair, digestMethod, signatureMethod, referenceURI, x509Certificate, canonicalizationMethodType);
    }

    /**
     * 对 SAML 文档根元素 ID 引用处签名。
     *
     * Sign a SAML Document
     *
     * @param samlDocument SAML DOM 文档
     * @param keypair 签名密钥对
     *
     * @throws org.keycloak.saml.common.exceptions.ProcessingException
     */
    public void signSAMLDocument(Document samlDocument, String keyName, KeyPair keypair, String canonicalizationMethodType) throws ProcessingException {
        // 从根元素读取 ID 属性
        String id = samlDocument.getDocumentElement().getAttribute(JBossSAMLConstants.ID.get());
        try {
            sign(samlDocument, id, keyName, keypair, canonicalizationMethodType);
        } catch (ParserConfigurationException | GeneralSecurityException | MarshalException | XMLSignatureException e) {
            throw new ProcessingException(logger.signatureError(e));
        }
    }

    /**
     * 校验 SAML 2.0 文档的 XML 签名。
     *
     * Validate the SAML2 Document
     *
     * @param signedDocument 已签名文档
     * @param keyLocator 公钥定位器
     *
     * @return
     *
     * @throws ProcessingException
     */
    public boolean validate(Document signedDocument, KeyLocator keyLocator) throws ProcessingException {
        try {
            configureIdAttribute(signedDocument);
            return XMLSignatureUtil.validate(signedDocument, keyLocator);
        } catch (MarshalException | XMLSignatureException me) {
            throw new ProcessingException(logger.signatureError(me));
        }
    }

    /**
     * 查找 Issuer 元素之后的兄弟节点（常用作签名插入点）。
     *
     * Given a {@link Document}, find the {@link Node} which is the sibling of the Issuer element
     *
     * @param doc
     *
     * @return
     */
    public Node getNextSiblingOfIssuer(Document doc) {
        // 定位 Issuer 的下一个兄弟节点
        NodeList nl = doc.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get());
        if (nl.getLength() > 0) {
            Node issuer = nl.item(0);

            return issuer.getNextSibling();
        }
        return null;
    }

    /**
     * <p>将 ID 属性标记为 XML ID（Santuario 1.5.1+ 不再仅凭属性名推断）。</p>
     * <p>
     * Sets the IDness of the ID attribute. Santuario 1.5.1 does not assumes IDness based on attribute names anymore.
     * This
     * method should be called before signing/validating a saml document.
     * </p>
     *
     * @param document SAML document to have its ID attribute configured.
     */
    public static void configureIdAttribute(Document document) {
        // 将根元素 ID 属性设为 XML ID
        configureIdAttribute(document.getDocumentElement());

        NodeList nodes = document.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
                JBossSAMLConstants.ASSERTION.get());

        for (int i = 0; i < nodes.getLength(); i++) {
            configureIdAttribute((Element) nodes.item(i));
        }
    }
    
    /** 将单个元素的 ID 属性设为 XML ID。 */
    public static void configureIdAttribute(Element element) {
        element.setIdAttribute(JBossSAMLConstants.ID.get(), true);
    }

}
