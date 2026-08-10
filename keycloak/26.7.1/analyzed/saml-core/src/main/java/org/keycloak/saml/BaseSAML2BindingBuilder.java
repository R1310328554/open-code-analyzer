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

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;

import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.sig.SAML2Signature;
import org.keycloak.saml.processing.core.saml.v2.util.DocumentUtil;
import org.keycloak.saml.processing.core.util.XMLEncryptionUtil;
import org.keycloak.saml.processing.web.util.PostBindingUtil;
import org.keycloak.saml.processing.web.util.RedirectBindingUtil;

import org.apache.xml.security.encryption.XMLCipher;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.keycloak.common.util.HtmlUtils.escapeAttribute;
import static org.keycloak.saml.common.util.StringUtil.isNotNull;

/**
 * SAML 2.0 绑定（POST / Redirect / SOAP）构建基类，提供签名、加密与 HTML 表单生成等通用能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BaseSAML2BindingBuilder<T extends BaseSAML2BindingBuilder> {
    protected static final Logger logger = Logger.getLogger(BaseSAML2BindingBuilder.class);

    /** 签名密钥在 KeyInfo 中的名称。 */
    protected String signingKeyName;
    /** 用于签名的密钥对。 */
    protected KeyPair signingKeyPair;
    /** 签名对应的 X509 证书（可选）。 */
    protected X509Certificate signingCertificate;
    /** 是否对整个 SAML 文档签名。 */
    protected boolean sign;
    /** 是否对 Assertion 元素单独签名。 */
    protected boolean signAssertions;
    /** XML 数字签名算法。 */
    protected SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RSA_SHA1;
    /** RelayState 参数值。 */
    protected String relayState;
    /** AES 加密密钥长度（位），已弃用路径使用。 */
    protected int encryptionKeySize = 256;
    /** 加密用的公钥。 */
    protected PublicKey encryptionPublicKey;
    /** XML 加密算法 URI（如 {@code XMLCipher.AES_256_GCM}）。 */
    protected String encryptionAlgorithm = XMLCipher.AES_256_GCM;
    /** 是否加密 Assertion。 */
    protected boolean encrypt;
    /** XML 规范化方法类型。 */
    protected String canonicalizationMethodType = CanonicalizationMethod.EXCLUSIVE;
    /** 密钥加密算法。 */
    protected String keyEncryptionAlgorithm;
    /** 密钥加密摘要方法。 */
    protected String keyEncryptionDigestMethod;
    /** 密钥加密 MGF 算法。 */
    protected String keyEncryptionMgfAlgorithm;

    /**
     * 设置 XML 签名规范化方法。
     *
     * @param method 规范化方法 URI
     * @return 当前构建器
     */
    public T canonicalizationMethod(String method) {
        this.canonicalizationMethodType = method;
        return (T)this;
    }

    /** 启用对整个 SAML 文档的签名。 */
    public T signDocument() {
        this.sign = true;
        return (T)this;
    }

    /** 启用对 Assertion 元素的单独签名。 */
    public T signAssertions() {
        this.signAssertions = true;
        return (T)this;
    }

    /**
     * 使用密钥对配置签名。
     *
     * @param signingKeyName KeyInfo 中的密钥名称
     * @param keyPair 签名密钥对
     * @return 当前构建器
     */
    public T signWith(String signingKeyName, KeyPair keyPair) {
        this.signingKeyName = signingKeyName;
        this.signingKeyPair = keyPair;
        return (T)this;
    }

    /**
     * 使用私钥与公钥配置签名。
     *
     * @param signingKeyName KeyInfo 中的密钥名称
     * @param privateKey 签名私钥
     * @param publicKey 对应公钥
     * @return 当前构建器
     */
    public T signWith(String signingKeyName, PrivateKey privateKey, PublicKey publicKey) {
        this.signingKeyName = signingKeyName;
        this.signingKeyPair = new KeyPair(publicKey, privateKey);
        return (T)this;
    }

    /**
     * 使用密钥对及 X509 证书配置签名。
     *
     * @param signingKeyName KeyInfo 中的密钥名称
     * @param keyPair 签名密钥对
     * @param cert 签名证书
     * @return 当前构建器
     */
    public T signWith(String signingKeyName, KeyPair keyPair, X509Certificate cert) {
        this.signingKeyName = signingKeyName;
        this.signingKeyPair = keyPair;
        this.signingCertificate = cert;
        return (T)this;
    }

    /**
     * 使用私钥、公钥及 X509 证书配置签名。
     *
     * @param signingKeyName KeyInfo 中的密钥名称
     * @param privateKey 签名私钥
     * @param publicKey 对应公钥
     * @param cert 签名证书
     * @return 当前构建器
     */
    public T signWith(String signingKeyName, PrivateKey privateKey, PublicKey publicKey, X509Certificate cert) {
        this.signingKeyName = signingKeyName;
        this.signingKeyPair = new KeyPair(publicKey, privateKey);
        this.signingCertificate = cert;
        return (T)this;
    }

    /**
     * 设置 XML 数字签名算法。
     *
     * @param alg 签名算法枚举
     * @return 当前构建器
     */
    public T signatureAlgorithm(SignatureAlgorithm alg) {
        this.signatureAlgorithm = alg;
        return (T)this;
    }

    /**
     * 启用 Assertion 加密并指定加密公钥。
     *
     * @param publicKey 加密用公钥
     * @return 当前构建器
     */
    public T encrypt(PublicKey publicKey) {
        encrypt = true;
        encryptionPublicKey = publicKey;
        return (T)this;
    }

    private String updateAesWithSize(int size) {
        switch (size) {
            case 128: return XMLCipher.AES_128;
            case 192: return XMLCipher.AES_192;
            case 256: return XMLCipher.AES_256;
            default:
                throw new RuntimeException("Invalid size for AES: " + size);
        }
    }

    /**
     * 设置 XMLCipher 格式的加密算法（例如 {@code XMLCipher.AES_256_GCM}）。
     *
     * @param alg XMLCipher 算法 URI
     * @return 当前构建器
     */
    public T encryptionAlgorithm(String alg) {
        if ("AES".equals(alg)) {
            // deprecated way, remove later
            this.encryptionAlgorithm = updateAesWithSize(this.encryptionKeySize);
        } else {
            this.encryptionAlgorithm = alg;
        }
        return (T)this;
    }

    /**
     * 设置 AES 加密密钥长度（位）。
     *
     * @param size 密钥长度
     * @return 当前构建器
     * @deprecated 请直接使用 {@link #encryptionAlgorithm(java.lang.String)} 指定 XMLCipher 算法
     * （例如 {@code XMLCipher.AES_256_GCM}）。
     */
    @Deprecated(since = "26.4.0", forRemoval = true)
    public T encryptionKeySize(int size) {
        this.encryptionAlgorithm = updateAesWithSize(size);
        this.encryptionKeySize = size;
        return (T)this;
    }

    /**
     * 设置密钥加密算法。
     *
     * @param keyEncryptionAlgorithm 密钥加密算法 URI
     * @return 当前构建器
     */
    public T keyEncryptionAlgorithm(String keyEncryptionAlgorithm) {
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        return (T)this;
    }

    /**
     * 设置密钥加密摘要方法。
     *
     * @param keyEncryptionDigestMethod 摘要方法 URI
     * @return 当前构建器
     */
    public T  keyEncryptionDigestMethod(String keyEncryptionDigestMethod) {
        this.keyEncryptionDigestMethod = keyEncryptionDigestMethod;
        return (T)this;
    }

    /**
     * 设置密钥加密 MGF 算法。
     *
     * @param keyEncryptionMgfAlgorithm MGF 算法 URI
     * @return 当前构建器
     */
    public T  keyEncryptionMgfAlgorithm(String keyEncryptionMgfAlgorithm) {
        this.keyEncryptionMgfAlgorithm = keyEncryptionMgfAlgorithm;
        return (T)this;
    }

    /**
     * 设置 RelayState 参数值。
     *
     * @param relayState RelayState 字符串
     * @return 当前构建器
     */
    public T relayState(String relayState) {
        this.relayState = relayState;
        return (T)this;
    }

    /** HTTP POST 绑定构建器，处理 Base64 编码与自动提交 HTML 表单。 */
    public class BasePostBindingBuilder {
        protected Document document;
        protected BaseSAML2BindingBuilder builder;

        /**
         * 创建 POST 绑定构建器，并按需签名/加密文档。
         *
         * @param builder 父级 SAML 绑定构建器
         * @param document SAML DOM 文档
         * @throws ProcessingException 签名或加密失败时抛出
         */
        public BasePostBindingBuilder(BaseSAML2BindingBuilder builder, Document document) throws ProcessingException {
            this.builder = builder;
            this.document = document;
            if (builder.signAssertions) {
                builder.signAssertion(document);
            }
            if (builder.encrypt) builder.encryptDocument(document);
            if (builder.sign) {
                builder.signDocument(document);
            }
        }

        /** 返回 Base64 编码的 SAML 消息字符串。 */
        public String encoded() throws ProcessingException, ConfigurationException, IOException {
            byte[] responseBytes = DocumentUtil.getDocumentAsString(document).getBytes(GeneralConstants.SAML_CHARSET);
            return PostBindingUtil.base64Encode(new String(responseBytes, GeneralConstants.SAML_CHARSET));
        }
        /** 返回当前 SAML DOM 文档。 */
        public Document getDocument() {
            return document;
        }
        /** 生成包含 SAMLResponse 的自动提交 HTML 表单。 */
        public String getHtmlResponse(String actionUrl) throws ProcessingException, ConfigurationException, IOException {
            String str = builder.buildHtmlPostResponse(document, actionUrl, false);
            return str;
        }
        /** 生成包含 SAMLRequest 的自动提交 HTML 表单。 */
        public String getHtmlRequest(String actionUrl) throws ProcessingException, ConfigurationException, IOException {
            String str = builder.buildHtmlPostResponse(document, actionUrl, true);
            return str;
        }
        /** 返回 RelayState 值。 */
        public String getRelayState() {
            return relayState;
        }
    }


    /** HTTP Redirect 绑定构建器，生成带 deflate+Base64 参数的 URI。 */
    public static class BaseRedirectBindingBuilder {
        protected Document document;
        protected BaseSAML2BindingBuilder builder;

        /**
         * 创建 Redirect 绑定构建器，并按需加密/签名 Assertion。
         *
         * @param builder 父级 SAML 绑定构建器
         * @param document SAML DOM 文档
         * @throws ProcessingException 处理失败时抛出
         */
        public BaseRedirectBindingBuilder(BaseSAML2BindingBuilder builder, Document document) throws ProcessingException {
            this.builder = builder;
            this.document = document;
            if (builder.encrypt) builder.encryptDocument(document);
            if (builder.signAssertions) {
                builder.signAssertion(document);
            }
        }

        /** 返回当前 SAML DOM 文档。 */
        public Document getDocument() {
            return document;
        }
        /**
         * 生成 Redirect 绑定 URI。
         *
         * @param redirectUri 重定向目标 URI
         * @param asRequest 为 true 时使用 SAMLRequest 参数，否则使用 SAMLResponse
         * @return 含 SAML 参数的完整 URI
         */
        public URI generateURI(String redirectUri, boolean asRequest) throws ConfigurationException, ProcessingException, IOException {
            String samlParameterName = GeneralConstants.SAML_RESPONSE_KEY;

            if (asRequest) {
                samlParameterName = GeneralConstants.SAML_REQUEST_KEY;
            }

            return builder.generateRedirectUri(samlParameterName, redirectUri, document);
        }

        /** 生成 SAMLRequest 的 Redirect URI。 */
        public URI requestURI(String actionUrl)  throws ConfigurationException, ProcessingException, IOException {
            return builder.generateRedirectUri(GeneralConstants.SAML_REQUEST_KEY, actionUrl, document);
        }
        /** 生成 SAMLResponse 的 Redirect URI。 */
        public URI responseURI(String actionUrl)  throws ConfigurationException, ProcessingException, IOException {
            return builder.generateRedirectUri(GeneralConstants.SAML_RESPONSE_KEY, actionUrl, document);
        }
    }

    /** SOAP 绑定构建器，在 SOAP 信封中承载 SAML 消息。 */
    public static class BaseSoapBindingBuilder {
        protected Document document;
        protected BaseSAML2BindingBuilder builder;

        /**
         * 创建 SOAP 绑定构建器，并按需签名/加密文档。
         *
         * @param builder 父级 SAML 绑定构建器
         * @param document SAML DOM 文档
         * @throws ProcessingException 处理失败时抛出
         */
        public BaseSoapBindingBuilder(BaseSAML2BindingBuilder builder, Document document) throws ProcessingException {
            this.builder = builder;
            this.document = document;
            if (builder.signAssertions) {
                builder.signAssertion(document);
            }
            if (builder.encrypt) builder.encryptDocument(document);
            if (builder.sign) {
                builder.signDocument(document);
            }
        }

        /** 返回当前 SAML DOM 文档。 */
        public Document getDocument() {
            return document;
        }

    }

    /**
     * 创建 Redirect 绑定构建器。
     *
     * @param document SAML DOM 文档
     * @return Redirect 绑定构建器
     * @throws ProcessingException 处理失败时抛出
     */
    public BaseRedirectBindingBuilder redirectBinding(Document document) throws ProcessingException {
        return new BaseRedirectBindingBuilder(this, document);

    }

    /**
     * 创建 POST 绑定构建器。
     *
     * @param document SAML DOM 文档
     * @return POST 绑定构建器
     * @throws ProcessingException 处理失败时抛出
     */
    public BasePostBindingBuilder postBinding(Document document) throws ProcessingException {
        return new BasePostBindingBuilder(this, document);

    }

    /**
     * 创建 SOAP 绑定构建器。
     *
     * @param document SAML DOM 文档
     * @return SOAP 绑定构建器
     * @throws ProcessingException 处理失败时抛出
     */
    public BaseSoapBindingBuilder soapBinding(Document document) throws ProcessingException {
        return new BaseSoapBindingBuilder(this, document);
    }

    /**
     * 从 SAML 响应文档中提取 Assertion 元素的命名空间前缀。
     *
     * @param samlResponseDocument SAML 响应 DOM
     * @return Assertion 前缀，未找到则抛出异常
     */
    public String getSAMLNSPrefix(Document samlResponseDocument) {
        Node assertionElement = samlResponseDocument.getDocumentElement()
                .getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ASSERTION.get()).item(0);

        if (assertionElement == null) {
            throw new IllegalStateException("Unable to find assertion in saml response document");
        }

        return assertionElement.getPrefix();
    }

    /**
     * 使用配置的公钥加密 SAML 文档中的 Assertion，并以 EncryptedAssertion 替换。
     *
     * @param samlDocument 待加密的 SAML DOM 文档
     * @throws ProcessingException 加密失败时抛出
     */
    public void encryptDocument(Document samlDocument) throws ProcessingException {
        String samlNSPrefix = getSAMLNSPrefix(samlDocument);

        try {
            QName encryptedAssertionElementQName = new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
                    JBossSAMLConstants.ENCRYPTED_ASSERTION.get(), samlNSPrefix);

            final String keyAlgorithm = XMLEncryptionUtil.getJCEKeyAlgorithmFromURI(encryptionAlgorithm);
            final int keySize = XMLEncryptionUtil.getKeyLengthFromURI(encryptionAlgorithm);
            byte[] secret = RandomSecret.createRandomSecret(keySize / 8);
            SecretKey secretKey = new SecretKeySpec(secret, keyAlgorithm);

            // 加密 Assertion 元素并以 EncryptedAssertion 替换
            XMLEncryptionUtil.encryptElement(new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
                            JBossSAMLConstants.ASSERTION.get(), samlNSPrefix), samlDocument, encryptionPublicKey, secretKey, keySize,
                            encryptedAssertionElementQName, true, encryptionAlgorithm, keyEncryptionAlgorithm, keyEncryptionDigestMethod, keyEncryptionMgfAlgorithm);
        } catch (Exception e) {
            throw new ProcessingException("failed to encrypt", e);
        }

    }

    /**
     * 对 SAML DOM 文档执行 XML 数字签名。
     *
     * @param samlDocument 待签名的 SAML DOM 文档
     * @throws ProcessingException 签名失败时抛出
     */
    public void signDocument(Document samlDocument) throws ProcessingException {
        String signatureMethod = signatureAlgorithm.getXmlSignatureMethod();
        String signatureDigestMethod = signatureAlgorithm.getXmlSignatureDigestMethod();
        SAML2Signature samlSignature = new SAML2Signature();

        if (signatureMethod != null) {
            samlSignature.setSignatureMethod(signatureMethod);
        }

        if (signatureDigestMethod != null) {
            samlSignature.setDigestMethod(signatureDigestMethod);
        }

        Node nextSibling = samlSignature.getNextSiblingOfIssuer(samlDocument);

        samlSignature.setNextSibling(nextSibling);

        if (signingCertificate != null) {
            samlSignature.setX509Certificate(signingCertificate);
        }

        samlSignature.signSAMLDocument(samlDocument, signingKeyName, signingKeyPair, canonicalizationMethodType);
    }

    /**
     * 单独对文档中的 Assertion 元素签名并替换原文档中的 Assertion。
     *
     * @param samlDocument 含 Assertion 的 SAML DOM 文档
     * @throws ProcessingException 签名失败时抛出
     */
    public void signAssertion(Document samlDocument) throws ProcessingException {
        Element originalAssertionElement = org.keycloak.saml.common.util.DocumentUtil.getChildElement(samlDocument.getDocumentElement(), new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ASSERTION.get()));
        if (originalAssertionElement == null) return;
        Node clonedAssertionElement = originalAssertionElement.cloneNode(true);
        Document temporaryDocument;

        try {
            temporaryDocument = org.keycloak.saml.common.util.DocumentUtil.createDocument();
        } catch (ConfigurationException e) {
            throw new ProcessingException(e);
        }

        temporaryDocument.adoptNode(clonedAssertionElement);
        temporaryDocument.appendChild(clonedAssertionElement);

        signDocument(temporaryDocument);

        samlDocument.adoptNode(clonedAssertionElement);

        Element parentNode = (Element) originalAssertionElement.getParentNode();

        parentNode.replaceChild(clonedAssertionElement, originalAssertionElement);
    }

    /**
     * 构建 POST 绑定的 HTML 自动提交表单。
     *
     * @param responseDoc SAML DOM 文档
     * @param actionUrl 表单提交目标 URL
     * @param asRequest 为 true 时使用 SAMLRequest，否则使用 SAMLResponse
     * @return HTML 表单字符串
     */
    public String buildHtmlPostResponse(Document responseDoc, String actionUrl, boolean asRequest) throws ProcessingException, ConfigurationException, IOException {
        return buildHtml(getSAMLResponse(responseDoc), actionUrl, asRequest);
    }

    /**
     * 将 SAML DOM 文档序列化并 Base64 编码。
     *
     * @param responseDoc SAML DOM 文档
     * @return Base64 编码的 SAML 消息
     */
    public static String getSAMLResponse(Document responseDoc) throws ProcessingException, ConfigurationException, IOException {
        byte[] responseBytes = org.keycloak.saml.common.util.DocumentUtil.getDocumentAsString(responseDoc).getBytes(GeneralConstants.SAML_CHARSET);
        return PostBindingUtil.base64Encode(new String(responseBytes, GeneralConstants.SAML_CHARSET));
    }

    /**
     * 根据已编码的 SAML 消息构建自动提交 HTML 表单。
     *
     * @param samlResponse Base64 编码的 SAML 消息
     * @param actionUrl 表单提交目标 URL
     * @param asRequest 为 true 时使用 SAMLRequest 参数
     * @return HTML 表单字符串
     */
    public String buildHtml(String samlResponse, String actionUrl, boolean asRequest) {

        String key = GeneralConstants.SAML_RESPONSE_KEY;

        if (asRequest) {
            key = GeneralConstants.SAML_REQUEST_KEY;
        }

        Map<String, String> inputTypes = new HashMap<>();
        inputTypes.put(key, samlResponse);
        if (isNotNull(relayState)) {
            inputTypes.put(GeneralConstants.RELAY_STATE, relayState);
        }

        return buildHtmlForm(actionUrl, inputTypes);
    }

    /**
     * 构建含隐藏字段的自动提交 HTML 表单。
     *
     * @param actionUrl 表单 POST 目标 URL
     * @param inputTypes 隐藏字段名到值的映射
     * @return HTML 表单字符串
     */
    public String buildHtmlForm(String actionUrl, Map<String, String> inputTypes) {
        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>")
                .append("<HEAD>")

                .append("<TITLE>Authentication Redirect</TITLE>")
                .append("</HEAD>")
                .append("<BODY Onload=\"document.forms[0].submit()\">")

                .append("<FORM METHOD=\"POST\" ACTION=\"").append(escapeAttribute(actionUrl)).append("\">");

        builder.append("<p>Redirecting, please wait.</p>");

        for (var entry : inputTypes.entrySet()) {
            builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"").append(entry.getKey()).append("\"").append(" VALUE=\"").append(escapeAttribute(entry.getValue())).append("\"/>");
        }

        builder.append("<NOSCRIPT>")
                .append("<P>JavaScript is disabled. We strongly recommend to enable it. Click the button below to continue.</P>")
                .append("<INPUT TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />")
                .append("</NOSCRIPT>")

                .append("</FORM></BODY></HTML>");

        return builder.toString();
    }


    /**
     * 将 SAML 文档 deflate 压缩后 Base64 编码（Redirect 绑定使用）。
     *
     * @param document SAML DOM 文档
     * @return deflate+Base64 编码字符串
     */
    public String base64Encoded(Document document) throws ConfigurationException, ProcessingException, IOException  {
        String documentAsString = DocumentUtil.getDocumentAsString(document);
        logger.debugv("saml document: {0}", documentAsString);
        byte[] responseBytes = documentAsString.getBytes(GeneralConstants.SAML_CHARSET);

        return RedirectBindingUtil.deflateBase64Encode(responseBytes);
    }


    /**
     * 生成 HTTP Redirect 绑定 URI，可选附加签名参数。
     *
     * @param samlParameterName 查询参数名（SAMLRequest 或 SAMLResponse）
     * @param redirectUri 基础重定向 URI
     * @param document SAML DOM 文档
     * @return 含 SAML 及可选签名参数的完整 URI
     */
    public URI generateRedirectUri(String samlParameterName, String redirectUri, Document document) throws ConfigurationException, ProcessingException, IOException {
        KeycloakUriBuilder builder = KeycloakUriBuilder.fromUri(redirectUri);
        int pos = builder.getQuery() == null? 0 : builder.getQuery().length();
        builder.queryParam(samlParameterName, base64Encoded(document));
        if (relayState != null) {
            builder.queryParam(GeneralConstants.RELAY_STATE, relayState);
        }

        if (sign) {
            builder.queryParam(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, signatureAlgorithm.getXmlSignatureMethod());
            URI uri = builder.build();
            String rawQuery = uri.getRawQuery();
            if (pos > 0) {
                // 签名范围仅包含新增的 SAML 查询参数
                rawQuery = rawQuery.substring(pos + 1);
            }
            Signature signature = signatureAlgorithm.createSignature();
            byte[] sig = null;
            try {
                signature.initSign(signingKeyPair.getPrivate());
                signature.update(rawQuery.getBytes(GeneralConstants.SAML_CHARSET));
                sig = signature.sign();
            } catch (InvalidKeyException | SignatureException e) {
                throw new ProcessingException(e);
            }
            String encodedSig = RedirectBindingUtil.base64Encode(sig);
            builder.queryParam(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY, encodedSig);
        }
        return builder.build();
    }

 }
