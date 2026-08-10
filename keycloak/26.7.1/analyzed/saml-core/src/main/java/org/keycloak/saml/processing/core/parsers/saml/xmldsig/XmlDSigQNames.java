/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.saml.processing.core.parsers.saml.xmldsig;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.processing.core.parsers.util.HasQName;

/**
 * XML 数字签名（XMLDSig）解析器使用的元素 QName 枚举。
 * <p>对应 W3C XML Signature 规范中的核心元素与密钥相关子元素。</p>
 *
 * @author hmlnarik
 */
public enum XmlDSigQNames implements HasQName {

    /** 规范化方法元素。 */
    CANONICALIZATION_METHOD("CanonicalizationMethod"),
    /** 摘要算法元素。 */
    DIGEST_METHOD("DigestMethod"),
    /** 摘要值元素。 */
    DIGEST_VALUE("DigestValue"),
    /** DSA 公钥值元素。 */
    DSA_KEY_VALUE("DSAKeyValue"),
    /** RSA 公钥指数。 */
    EXPONENT("Exponent"),
    /** DSA 参数 G。 */
    G("G"),
    /** HMAC 输出长度。 */
    HMAC_OUTPUT_LENGTH("HMACOutputLength"),
    /** DSA 参数 J。 */
    J("J"),
    /** 密钥信息容器。 */
    KEY_INFO("KeyInfo"),
    /** 密钥名称。 */
    KEY_NAME("KeyName"),
    /** 密钥值容器（RSA/DSA）。 */
    KEY_VALUE("KeyValue"),
    /** 引用清单。 */
    MANIFEST("Manifest"),
    /** 管理数据。 */
    MGMT_DATA("MgmtData"),
    /** RSA 公钥模数。 */
    MODULUS("Modulus"),
    /** 签名对象。 */
    OBJECT("Object"),
    /** DSA PgenCounter。 */
    PGEN_COUNTER("PgenCounter"),
    /** PGP 密钥数据。 */
    PGP_DATA("PGPData"),
    /** PGP 密钥 ID。 */
    PGP_KEY_ID("PGPKeyID"),
    /** PGP 密钥包。 */
    PGP_KEY_PACKET("PGPKeyPacket"),
    /** DSA 参数 P。 */
    P("P"),
    /** DSA 参数 Q。 */
    Q("Q"),
    /** 签名引用。 */
    REFERENCE("Reference"),
    /** 检索方法。 */
    RETRIEVAL_METHOD("RetrievalMethod"),
    /** RSA 公钥值元素。 */
    RSA_KEY_VALUE("RSAKeyValue"),
    /** DSA Seed。 */
    SEED("Seed"),
    /** 签名算法。 */
    SIGNATURE_METHOD("SignatureMethod"),
    /** 签名属性集合。 */
    SIGNATURE_PROPERTIES("SignatureProperties"),
    /** 单个签名属性。 */
    SIGNATURE_PROPERTY("SignatureProperty"),
    /** 根签名元素。 */
    SIGNATURE("Signature"),
    /** 签名值。 */
    SIGNATURE_VALUE("SignatureValue"),
    /** 待签名信息。 */
    SIGNED_INFO("SignedInfo"),
    /** SPKI 密钥数据。 */
    SPKI_DATA("SPKIData"),
    /** SPKI 指数。 */
    SPKIS_EXP("SPKISexp"),
    /** 变换集合。 */
    TRANSFORMS("Transforms"),
    /** 单个变换。 */
    TRANSFORM("Transform"),
    /** XPath 表达式。 */
    XPATH("XPath"),
    /** X.509 证书。 */
    X509_CERTIFICATE("X509Certificate"),
    /** X.509 CRL。 */
    X509_CRL("X509CRL"),
    /** X.509 证书数据容器。 */
    X509_DATA("X509Data"),
    /** X.509 颁发者名称。 */
    X509_ISSUER_NAME("X509IssuerName"),
    /** X.509 颁发者序列号组合。 */
    X509_ISSUER_SERIAL("X509IssuerSerial"),
    /** X.509 序列号。 */
    X509_SERIAL_NUMBER("X509SerialNumber"),
    /** X.509 主体密钥标识符。 */
    X509_SKI("X509SKI"),
    /** X.509 主体名称。 */
    X509_SUBJECT_NAME("X509SubjectName"),
    /** DSA 公钥 Y 值。 */
    Y("Y"),

    /** 未知或未映射元素占位符。 */
    UNKNOWN_ELEMENT("")
    ;

    /** 元素对应的 QName。 */
    private final QName qName;

    /** 使用 XMLDSig 默认命名空间构造 QName。 */
    XmlDSigQNames(String localName) {
        this(JBossSAMLURIConstants.XMLDSIG_NSURI, localName);
    }

    /** 从已有 {@link HasQName} 复制 QName。 */
    XmlDSigQNames(HasQName source) {
        this.qName = source.getQName();
    }

    /** 指定命名空间 URI 与本地名构造 QName。 */
    XmlDSigQNames(JBossSAMLURIConstants nsUri, String localName) {
        this.qName = new QName(nsUri == null ? null : nsUri.get(), localName);
    }

    /** @return 此枚举常量绑定的 QName */
    @Override
    public QName getQName() {
        return qName;
    }
}
