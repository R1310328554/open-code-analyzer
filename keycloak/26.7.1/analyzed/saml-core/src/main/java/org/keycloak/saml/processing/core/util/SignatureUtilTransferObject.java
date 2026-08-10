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

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@link XMLSignatureUtil} 签名操作使用的传输对象（DTO）。
 * <p>封装待签文档、密钥对、摘要/签名算法及 X509 证书等参数。</p>
 *
 * @author anil saldhana
 */
public class SignatureUtilTransferObject {

    /** 用于签名的 X509 证书。 */
    private X509Certificate x509Certificate;

    /** 待签名的 DOM 文档。 */
    private Document documentToBeSigned;

    /** KeyInfo 中的密钥名称提示。 */
    private String keyName;

    /** 签名用密钥对。 */
    private KeyPair keyPair;

    /** 签名元素插入位置的参考兄弟节点。 */
    private Node nextSibling;

    /** 摘要算法 URI（如 SHA-256）。 */
    private String digestMethod;

    /** Reference 元素的 URI 属性值。 */
    private String referenceURI;

    /** 签名算法 URI。 */
    private String signatureMethod;

    /** 返回待签名的 DOM 文档。 */
    public Document getDocumentToBeSigned() {
        return documentToBeSigned;
    }

    /** 设置待签名的 DOM 文档。 */
    public void setDocumentToBeSigned(Document documentToBeSigned) {
        this.documentToBeSigned = documentToBeSigned;
    }

    /** 返回签名密钥对。 */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /** 设置签名密钥对。 */
    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public Node getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(Node nextSibling) {
        this.nextSibling = nextSibling;
    }

    public String getDigestMethod() {
        return digestMethod;
    }

    public void setDigestMethod(String digestMethod) {
        this.digestMethod = digestMethod;
    }

    public String getReferenceURI() {
        return referenceURI;
    }

    public void setReferenceURI(String referenceURI) {
        this.referenceURI = referenceURI;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    /**
     * 获取用于签名的 {@link X509Certificate}。
     *
     * @return X509 证书
     *
     * @since 2.5.0
     */
    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    /**
     * 设置用于签名的 {@link X509Certificate}。
     *
     * @param x509Certificate X509 证书
     *
     * @since 2.5.0
     */
    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    /** 返回 KeyInfo 密钥名称。 */
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}