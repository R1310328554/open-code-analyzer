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

import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

/**
 * SAML XML 数字签名算法枚举，映射 XML Signature Method / Digest Method 与 Java {@link Signature} 算法名。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum SignatureAlgorithm {
    /** RSA-SHA1 签名算法。 */
    RSA_SHA1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "http://www.w3.org/2000/09/xmldsig#sha1", "SHA1withRSA"),
    /** RSA-SHA256 签名算法。 */
    RSA_SHA256("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "http://www.w3.org/2001/04/xmlenc#sha256", "SHA256withRSA"),
    /** RSA-SHA256-MGF1 签名算法。 */
    RSA_SHA256_MGF1("http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1", "http://www.w3.org/2001/04/xmlenc#sha256", "SHA256withRSAandMGF1"),
    /** RSA-SHA512 签名算法。 */
    RSA_SHA512("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "http://www.w3.org/2001/04/xmlenc#sha512", "SHA512withRSA"),
    /** RSA-SHA512-MGF1 签名算法。 */
    RSA_SHA512_MGF1("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1", "http://www.w3.org/2001/04/xmlenc#sha512", "SHA512withRSAandMGF1"),
    /** DSA-SHA1 签名算法。 */
    DSA_SHA1("http://www.w3.org/2000/09/xmldsig#dsa-sha1", "http://www.w3.org/2000/09/xmldsig#sha1", "SHA1withDSA")
    ;
    /** XML SignatureMethod URI。 */
    private final String xmlSignatureMethod;
    /** XML DigestMethod URI。 */
    private final String xmlSignatureDigestMethod;
    /** Java {@link Signature#getInstance(String)} 算法名。 */
    private final String javaSignatureAlgorithm;

    /** 按 SignatureMethod URI 索引。 */
    private static final Map<String, SignatureAlgorithm> signatureMethodMap = new HashMap<>();
    /** 按 DigestMethod URI 索引。 */
    private static final Map<String, SignatureAlgorithm> signatureDigestMethodMap = new HashMap<>();

    static {
        signatureMethodMap.put(RSA_SHA1.getXmlSignatureMethod(), RSA_SHA1);
        signatureMethodMap.put(RSA_SHA256.getXmlSignatureMethod(), RSA_SHA256);
        signatureMethodMap.put(RSA_SHA256_MGF1.getXmlSignatureMethod(), RSA_SHA256_MGF1);
        signatureMethodMap.put(RSA_SHA512.getXmlSignatureMethod(), RSA_SHA512);
        signatureMethodMap.put(RSA_SHA512_MGF1.getXmlSignatureMethod(), RSA_SHA512_MGF1);
        signatureMethodMap.put(DSA_SHA1.getXmlSignatureMethod(), DSA_SHA1);

        signatureDigestMethodMap.put(RSA_SHA1.getXmlSignatureDigestMethod(), RSA_SHA1);
        signatureDigestMethodMap.put(RSA_SHA256.getXmlSignatureDigestMethod(), RSA_SHA256);
        signatureDigestMethodMap.put(RSA_SHA256_MGF1.getXmlSignatureDigestMethod(), RSA_SHA256_MGF1);
        signatureDigestMethodMap.put(RSA_SHA512.getXmlSignatureDigestMethod(), RSA_SHA512);
        signatureDigestMethodMap.put(RSA_SHA512_MGF1.getXmlSignatureDigestMethod(), RSA_SHA512_MGF1);
        signatureDigestMethodMap.put(DSA_SHA1.getXmlSignatureDigestMethod(), DSA_SHA1);
    }

    /**
     * 根据 XML SignatureMethod URI 查找对应枚举值。
     *
     * @param xml SignatureMethod URI
     * @return 匹配的算法，未找到时返回 {@code null}
     */
    public static SignatureAlgorithm getFromXmlMethod(String xml) {
        return signatureMethodMap.get(xml);
    }

    /**
     * 根据 XML DigestMethod URI 查找对应枚举值。
     *
     * @param xml DigestMethod URI
     * @return 匹配的算法，未找到时返回 {@code null}
     */
    public static SignatureAlgorithm getFromXmlDigest(String xml) {
        return signatureDigestMethodMap.get(xml);
    }

    SignatureAlgorithm(String xmlSignatureMethod, String xmlSignatureDigestMethod, String javaSignatureAlgorithm) {
        this.xmlSignatureMethod = xmlSignatureMethod;
        this.xmlSignatureDigestMethod = xmlSignatureDigestMethod;
        this.javaSignatureAlgorithm = javaSignatureAlgorithm;
    }

    /** @return XML SignatureMethod URI */
    public String getXmlSignatureMethod() {
        return xmlSignatureMethod;
    }

    /** @return XML DigestMethod URI */
    public String getXmlSignatureDigestMethod() {
        return xmlSignatureDigestMethod;
    }

    /** @return Java 签名算法名称 */
    public String getJavaSignatureAlgorithm() {
        return javaSignatureAlgorithm;
    }

    /**
     * 创建对应算法的 {@link Signature} 实例。
     *
     * @return 已初始化的 Signature 对象
     */
    public Signature createSignature() {
        try {
            return Signature.getInstance(javaSignatureAlgorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
