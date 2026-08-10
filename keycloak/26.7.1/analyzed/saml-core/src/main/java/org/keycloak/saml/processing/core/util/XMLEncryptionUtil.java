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
package org.keycloak.saml.processing.core.util;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import javax.crypto.SecretKey;
import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.namespace.QName;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.common.util.StringUtil;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML 加密工具类。
 * <p><b>说明：</b>当前基于 Apache XML Security 库 API 实现；JSR-106 标准尚未定稿，故使用非标准 API。</p>
 * <p>支持元素加密/解密、对称密钥包装传输及多种 RSA/AES 算法配置。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 4, 2009
 */
public class XMLEncryptionUtil {

    /**
     * 解密密钥定位器接口。
     * <p>根据 {@link EncryptedData} 提供可用于解密的私钥列表。</p>
     */
    public interface DecryptionKeyLocator {

        /**
         * 返回适合解密给定 {@code encryptedData} 的私钥列表。
         *
         * @param encryptedData 待解密的数据
         * @return 私钥列表
         */
        List<PrivateKey> getKeys(EncryptedData encryptedData);
    }

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    static {
        // Initialize the Apache XML Security Library
        org.apache.xml.security.Init.init();
    }

    /** ds:KeyInfo 元素本地名称常量。 */
    public static final String DS_KEY_INFO = "ds:KeyInfo";

    /** 默认 RSA 密钥包装算法（RSA-OAEP-MGF1P）。 */
    private static final String RSA_ENCRYPTION_SCHEME = XMLCipher.RSA_OAEP_11;

    /**
     * <p>
     * Encrypt the Key to be transported
     * </p>
     * <p>
     * Data is encrypted with a SecretKey. Then the key needs to be transported to the other end where it is needed for
     * decryption. For the Key transport, the SecretKey is encrypted with the recipient's public key. At the receiving
     * end, the
     * receiver can decrypt the Secret Key using his private key.s
     * </p>
     *
     * @param document
     * @param keyToBeEncrypted Symmetric Key (SecretKey)
     * @param keyUsedToEncryptSecretKey Asymmetric Key (Public Key)
     *
     * @return
     *
     * @throws org.keycloak.saml.common.exceptions.ProcessingException
     */
    private static EncryptedKey encryptKey(Document document, SecretKey keyToBeEncrypted, PublicKey keyUsedToEncryptSecretKey,
                                          String keyEncryptionAlgorithm, String keyEncryptionDigestMethod,
                                          String keyEncryptionMgfAlgorithm) throws ProcessingException {
        XMLCipher keyCipher;

        try {
            keyCipher = XMLCipher.getInstance(keyEncryptionAlgorithm, null, keyEncryptionDigestMethod);

            keyCipher.init(XMLCipher.WRAP_MODE, keyUsedToEncryptSecretKey);
            return keyCipher.encryptKey(document, keyToBeEncrypted, keyEncryptionMgfAlgorithm, null);
        } catch (XMLEncryptionException e) {
            throw logger.processingError(e);
        }
    }

    /** 将 XML 加密算法 URI 映射为 JCE 密钥算法名称。 */
    public static String getJCEKeyAlgorithmFromURI(String algorithm) {
        return JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
    }

    /** 从算法 URI 解析密钥长度（位）。 */
    public static int getKeyLengthFromURI(String algorithm) {
        return JCEMapper.getKeyLengthFromURI(algorithm);
    }

    public static void encryptElement(QName elementQName, Document document, PublicKey publicKey, SecretKey secretKey,
                                      int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo) throws ProcessingException {
        encryptElement(elementQName, document, publicKey, secretKey, keySize, wrappingElementQName, addEncryptedKeyInKeyInfo,
                null, null, null, null);
    }

    public static void encryptElement(QName elementQName, Document document, PublicKey publicKey, SecretKey secretKey,
                                      int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo,
                                      String keyEncryptionAlgorithm) throws ProcessingException {
        encryptElement(elementQName, document, publicKey, secretKey, keySize, wrappingElementQName,
                addEncryptedKeyInKeyInfo, null, keyEncryptionAlgorithm, null, null);
    }

    public static void encryptElement(QName elementQName, Document document, PublicKey publicKey, SecretKey secretKey,
                                      int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo, String keyEncryptionAlgorithm,
                                      String keyEncryptionDigestMethod, String keyEncryptionMgfAlgorithm) throws ProcessingException {
        encryptElement(elementQName, document, publicKey, secretKey, keySize, wrappingElementQName, addEncryptedKeyInKeyInfo,
                null, keyEncryptionAlgorithm, keyEncryptionDigestMethod, keyEncryptionMgfAlgorithm);
    }

    /**
     * 加密文档中指定元素，并用加密数据替换原元素。
     *
     * @param elementQName 待加密元素的 QName
     * @param document 包含待加密元素的文档
     * @param publicKey 用于包装对称密钥的公钥
     * @param secretKey 用于数据加密的对称密钥
     * @param keySize 公钥/密钥尺寸（位）
     * @param wrappingElementQName 包裹加密结果的外层元素 QName
     * @param addEncryptedKeyInKeyInfo 是否将 EncryptedKey 放入 ds:KeyInfo
     * @param encryptionAlgorithm 数据加密算法（可为 null，按 secretKey 推断）
     * @param keyEncryptionAlgorithm 对称密钥包装算法（可为 null）
     * @param keyEncryptionDigestMethod 可选摘要算法（可为 null）
     * @param keyEncryptionMgfAlgorithm xenc11 MGF 算法（可为 null）
     *
     * @throws ProcessingException 加密失败时抛出
     */
    public static void encryptElement(QName elementQName, Document document, PublicKey publicKey, SecretKey secretKey,
                                      int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo, String encryptionAlgorithm,
                                      String keyEncryptionAlgorithm, String keyEncryptionDigestMethod, String keyEncryptionMgfAlgorithm) throws ProcessingException {
        if (elementQName == null)
            throw logger.nullArgumentError("elementQName");
        if (document == null)
            throw logger.nullArgumentError("document");
        String wrappingElementPrefix = wrappingElementQName.getPrefix();
        if (wrappingElementPrefix == null || "".equals(wrappingElementPrefix))
            throw logger.wrongTypeError("Wrapping element prefix invalid");

        Element documentElement = DocumentUtil.getElement(document, elementQName);

        if (documentElement == null)
            throw logger.domMissingDocElementError(elementQName.toString());

        // set default algorithms
        if (encryptionAlgorithm == null) {
            // set default encryption based on the secret key passed
            encryptionAlgorithm = getXMLEncryptionURL(secretKey.getAlgorithm(), keySize);
        }

        if (keyEncryptionAlgorithm == null) {
            // get default one for the public key
            keyEncryptionAlgorithm = getXMLEncryptionURLForKeyUnwrap(publicKey.getAlgorithm(), keySize);
        }

        if ((XMLCipher.RSA_OAEP.equals(keyEncryptionAlgorithm) || XMLCipher.RSA_OAEP_11.equals(keyEncryptionAlgorithm))) {
            if (keyEncryptionDigestMethod == null) {
                keyEncryptionDigestMethod = XMLCipher.SHA256; // default digest method to SHA256
            } else if (XMLCipher.SHA1.equals(keyEncryptionDigestMethod)){
                keyEncryptionDigestMethod = null; // default by spec
            }
        } else {
            keyEncryptionDigestMethod = null; // not used for RSA_v1dot5
        }

        if (XMLCipher.RSA_OAEP_11.equals(keyEncryptionAlgorithm)) {
            if (keyEncryptionMgfAlgorithm == null) {
                keyEncryptionMgfAlgorithm = EncryptionConstants.MGF1_SHA256; // default mgf to mgf1sha256
            } else if (EncryptionConstants.MGF1_SHA1.equals(keyEncryptionMgfAlgorithm)) {
                keyEncryptionMgfAlgorithm = null; // default by spec
            }
        } else {
            keyEncryptionMgfAlgorithm = null; // only available for RSA_OAEP_11
        }

        EncryptedKey encryptedKey = encryptKey(document, secretKey, publicKey, keyEncryptionAlgorithm, keyEncryptionDigestMethod, keyEncryptionMgfAlgorithm);

        XMLCipher cipher = null;
        // Encrypt the Document
        try {
            cipher = XMLCipher.getInstance(encryptionAlgorithm);
            cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
        } catch (XMLEncryptionException e1) {
            throw logger.processingError(e1);
        }

        Document encryptedDoc;
        try {
            encryptedDoc = cipher.doFinal(document, documentElement);
        } catch (Exception e) {
            throw logger.processingError(e);
        }

        // The EncryptedKey element is added
        Element encryptedKeyElement = cipher.martial(document, encryptedKey);

        final String wrappingElementName;

        if (StringUtil.isNullOrEmpty(wrappingElementPrefix)) {
            wrappingElementName = wrappingElementQName.getLocalPart();
        } else {
            wrappingElementName = wrappingElementPrefix + ":" + wrappingElementQName.getLocalPart();
        }
        // Create the wrapping element and set its attribute NS
        Element wrappingElement = encryptedDoc.createElementNS(wrappingElementQName.getNamespaceURI(), wrappingElementName);

        if (! StringUtil.isNullOrEmpty(wrappingElementPrefix)) {
            wrappingElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + wrappingElementPrefix, wrappingElementQName.getNamespaceURI());
        }

        // Get Hold of the Cipher Data
        NodeList cipherElements = encryptedDoc.getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDDATA);
        if (cipherElements == null || cipherElements.getLength() == 0)
            throw logger.domMissingElementError("xenc:EncryptedData");
        Element encryptedDataElement = (Element) cipherElements.item(0);

        Node parentOfEncNode = encryptedDataElement.getParentNode();
        parentOfEncNode.replaceChild(wrappingElement, encryptedDataElement);

        wrappingElement.appendChild(encryptedDataElement);

        if (addEncryptedKeyInKeyInfo) {
            // Outer ds:KeyInfo Element to hold the EncryptionKey
            Element sigElement = encryptedDoc.createElementNS(XMLSignature.XMLNS, DS_KEY_INFO);
            sigElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ds", XMLSignature.XMLNS);
            sigElement.appendChild(encryptedKeyElement);

            // Insert the Encrypted key before the CipherData element
            NodeList nodeList = encryptedDoc.getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_CIPHERDATA);
            if (nodeList == null || nodeList.getLength() == 0)
                throw logger.domMissingElementError("xenc:CipherData");
            Element cipherDataElement = (Element) nodeList.item(0);
            Node cipherParent = cipherDataElement.getParentNode();
            cipherParent.insertBefore(sigElement, cipherDataElement);
        } else {
            // Add the encrypted key as a child of the wrapping element
            wrappingElement.appendChild(encryptedKeyElement);
        }
    }

    /**
     * 解密文档中的加密元素。
     * <p>依次尝试 {@code decryptionKeyLocator} 提供的全部私钥；均失败则抛出 {@link ProcessingException}。</p>
     *
     * @param documentWithEncryptedElement 含加密元素的文档
     * @param decryptionKeyLocator 解密密钥定位器
     *
     * @return 解密后替换为明文数据元素的根元素
     *
     * @throws ProcessingException 解密失败时抛出
     */
    public static Element decryptElementInDocument(Document documentWithEncryptedElement, DecryptionKeyLocator decryptionKeyLocator)
            throws ProcessingException {
        if (documentWithEncryptedElement == null)
            throw logger.nullArgumentError("Input document is null");

        // Look for encrypted data element
        Element documentRoot = documentWithEncryptedElement.getDocumentElement();
        Element encDataElement = getNextElementNode(documentRoot.getFirstChild());
        if (encDataElement == null)
            throw logger.domMissingElementError("No element representing the encrypted data found");

        XMLCipher cipher;
        EncryptedData encryptedData;
        EncryptedKey encryptedKey;
        try {
            cipher = XMLCipher.getInstance();
            cipher.init(XMLCipher.DECRYPT_MODE, null);
            encryptedData = cipher.loadEncryptedData(documentWithEncryptedElement, encDataElement);
            if (encryptedData.getKeyInfo() == null) {
                throw logger.domMissingElementError("No element representing KeyInfo found in the EncryptedData");
            }

            encryptedKey = encryptedData.getKeyInfo().itemEncryptedKey(0);
            if (encryptedKey == null) {
                // the encrypted key is not inside the encrypted data, locate it
                Element encKeyElement = locateEncryptedKeyElement(encDataElement);
                encryptedKey = cipher.loadEncryptedKey(documentWithEncryptedElement, encKeyElement);
                encryptedData.getKeyInfo().add(encryptedKey);
            }
        } catch (XMLSecurityException e1) {
            throw logger.processingError(e1);
        }

        Document decryptedDoc = null;

        if (encryptedData != null && encryptedKey != null) {
            boolean success = false;
            final Exception enclosingThrowable = new RuntimeException("Cannot decrypt element in document");
            List<PrivateKey> encryptionKeys;
            encryptionKeys = decryptionKeyLocator.getKeys(encryptedData);

            if (encryptionKeys == null || encryptionKeys.isEmpty()) {
                throw logger.nullValueError("Key for EncryptedData not found.");
            }

            for (PrivateKey privateKey : encryptionKeys) {
                try {
                    String encAlgoURL = encryptedData.getEncryptionMethod().getAlgorithm();
                    XMLCipher keyCipher = XMLCipher.getInstance();
                    keyCipher.init(XMLCipher.UNWRAP_MODE, privateKey);
                    Key encryptionKey = keyCipher.decryptKey(encryptedKey, encAlgoURL);
                    cipher = XMLCipher.getInstance();
                    cipher.init(XMLCipher.DECRYPT_MODE, encryptionKey);

                    decryptedDoc = cipher.doFinal(documentWithEncryptedElement, encDataElement);
                    success = true;
                    break;
                } catch (Exception e) {
                    enclosingThrowable.addSuppressed(e);
                }
            }

            if (!success) {
                throw logger.processingError(enclosingThrowable);
            }
        }

        if (decryptedDoc == null) {
            throw logger.nullValueError("decryptedDoc");
        }

        Element decryptedRoot = decryptedDoc.getDocumentElement();
        Element dataElement = getNextElementNode(decryptedRoot.getFirstChild());
        if (dataElement == null)
            throw logger.nullValueError("Data Element after encryption is null");

        decryptedRoot.removeChild(dataElement);
        decryptedDoc.replaceChild(dataElement, decryptedRoot);

        return decryptedDoc.getDocumentElement();
    }

    /**
     * Locates the EncryptedKey element once the EncryptedData element is found.
     * A exception is thrown if not found.
     *
     * @param encDataElement The EncryptedData element found
     * @return The EncryptedKey element
     */
    private static Element locateEncryptedKeyElement(Element encDataElement) {
        // Look at siblings for the key
        Element encKeyElement = getNextElementNode(encDataElement.getNextSibling());
        if (encKeyElement == null) {
            // Search the enc data element for enc key
            NodeList nodeList = encDataElement.getElementsByTagNameNS(EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDKEY);

            if (nodeList == null || nodeList.getLength() == 0)
                throw logger.nullValueError("Encrypted Key not found in the enc data");

            encKeyElement = (Element) nodeList.item(0);
        }
        return encKeyElement;
    }

    /**
     * From the secret key, get the W3C XML Encryption URL
     *
     * @param publicKeyAlgo
     * @param keySize
     *
     * @return
     */
    private static String getXMLEncryptionURLForKeyUnwrap(String publicKeyAlgo, int keySize) {
        if ("AES".equals(publicKeyAlgo)) {
            switch (keySize) {
                case 192:
                    return XMLCipher.AES_192_KeyWrap;
                case 256:
                    return XMLCipher.AES_256_KeyWrap;
                default:
                    return XMLCipher.AES_128_KeyWrap;
            }
        }
        if (publicKeyAlgo.contains("RSA"))
            return RSA_ENCRYPTION_SCHEME;
        throw logger.unsupportedType("unsupported publicKey Algo:" + publicKeyAlgo);
    }

    /**
     * From the secret key, get the W3C XML Encryption URL
     *
     * @param secretKey
     * @param keySize
     *
     * @return
     */
    private static String getXMLEncryptionURL(String algo, int keySize) {
        if ("AES".equals(algo)) {
            switch (keySize) {
                case 192:
                    return XMLCipher.AES_192;
                case 256:
                    return XMLCipher.AES_256;
                default:
                    return XMLCipher.AES_128;
            }
        }
        if (algo.contains("RSA"))
            return XMLCipher.RSA_v1dot5;
        throw logger.unsupportedType("Secret Key with unsupported algo:" + algo);
    }

    /**
     * Returns the next Element node.
     */
    private static Element getNextElementNode(Node node) {
        while (node != null) {
            if (Node.ELEMENT_NODE == node.getNodeType())
                return (Element) node;
            node = node.getNextSibling();
        }
        return null;
    }
}