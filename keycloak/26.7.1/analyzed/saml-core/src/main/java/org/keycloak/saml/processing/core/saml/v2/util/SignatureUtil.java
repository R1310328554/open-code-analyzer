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

import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import jakarta.xml.bind.JAXBException;

import org.keycloak.dom.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.keycloak.dom.xmlsec.w3.xmldsig.KeyValueType;
import org.keycloak.dom.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.keycloak.dom.xmlsec.w3.xmldsig.SignatureType;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.processing.core.constants.PicketLinkFederationConstants;

import org.xml.sax.SAXException;

/**
 * SAML 签名工具类，提供内容签名、验签及密钥值构造。
 * <p>支持 DSA/RSA 算法，将公钥封装为 {@code KeyValueType}。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 16, 2008
 */
public class SignatureUtil {

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 将 {@code SignatureType} 序列化到输出流。
     *
     * @param signature 待序列化的签名对象
     * @param os 目标输出流
     *
     * @throws SAXException SAX 处理异常
     * @throws JAXBException JAXB 序列化异常
     */
    public static void marshall(SignatureType signature, OutputStream os) throws JAXBException, SAXException {
        throw logger.notImplementedYet("NYI");
        /*
         * JAXBElement<SignatureType> jsig = objectFactory.createSignature(signature); Marshaller marshaller =
         * JAXBUtil.getValidatingMarshaller(pkgName, schemaLocation); marshaller.marshal(jsig, os);
         */
    }

    /**
     * 根据算法名称（RSA/DSA）获取对应的 XML 签名 URI。
     *
     * @param algo 算法名称
     *
     * @return XML 数字签名算法 URI，未知算法返回 {@code null}
     */
    public static String getXMLSignatureAlgorithmURI(String algo) {
        String xmlSignatureAlgo = null;

        if ("DSA".equalsIgnoreCase(algo)) {
            xmlSignatureAlgo = JBossSAMLConstants.SIGNATURE_SHA1_WITH_DSA.get();
        } else if ("RSA".equalsIgnoreCase(algo)) {
            xmlSignatureAlgo = JBossSAMLConstants.SIGNATURE_SHA1_WITH_RSA.get();
        }
        return xmlSignatureAlgo;
    }

    /**
     * 使用私钥对字符串进行签名。
     *
     * @param stringToBeSigned 待签名字符串
     * @param signingKey 签名私钥
     *
     * @return 签名字节数组
     *
     * @throws GeneralSecurityException 签名过程安全异常
     */
    public static byte[] sign(String stringToBeSigned, PrivateKey signingKey) throws GeneralSecurityException {
        if (stringToBeSigned == null)
            throw logger.nullArgumentError("stringToBeSigned");
        if (signingKey == null)
            throw logger.nullArgumentError("signingKey");

        String algo = signingKey.getAlgorithm();
        Signature sig = getSignature(algo);
        sig.initSign(signingKey);
        sig.update(stringToBeSigned.getBytes(GeneralConstants.SAML_CHARSET));
        return sig.sign();
    }

    /**
     * 使用公钥验证签名内容。
     *
     * @param signedContent 原始待验签内容
     * @param signatureValue 签名值
     * @param validatingKey 验签公钥
     *
     * @return 验签是否通过
     *
     * @throws GeneralSecurityException 验签过程安全异常
     */
    public static boolean validate(byte[] signedContent, byte[] signatureValue, PublicKey validatingKey)
            throws GeneralSecurityException {
        if (signedContent == null)
            throw logger.nullArgumentError("signedContent");
        if (signatureValue == null)
            throw logger.nullArgumentError("signatureValue");
        if (validatingKey == null)
            throw logger.nullArgumentError("validatingKey");

        // 假定 signatureValue 与公钥使用相同算法；否则将抛出异常
        String algo = validatingKey.getAlgorithm();
        Signature sig = getSignature(algo);

        sig.initVerify(validatingKey);
        sig.update(signedContent);
        return sig.verify(signatureValue);
    }

    /**
     * 使用 X509 证书验证签名。
     *
     * @param signedContent 原始待验签内容
     * @param signatureValue 签名值
     * @param signatureAlgorithm 签名算法标识
     * @param validatingCert 验签证书
     *
     * @return 验签是否通过
     *
     * @throws GeneralSecurityException 验签过程安全异常
     */
    public static boolean validate(byte[] signedContent, byte[] signatureValue, String signatureAlgorithm,
                                   X509Certificate validatingCert) throws GeneralSecurityException {
        if (signedContent == null)
            throw logger.nullArgumentError("signedContent");
        if (signatureValue == null)
            throw logger.nullArgumentError("signatureValue");
        if (signatureAlgorithm == null)
            throw logger.nullArgumentError("signatureAlgorithm");
        if (validatingCert == null)
            throw logger.nullArgumentError("validatingCert");

        Signature sig = getSignature(signatureAlgorithm);

        sig.initVerify(validatingCert);
        sig.update(signedContent);
        return sig.verify(signatureValue);
    }

    /**
     * <p>
     * 将指定公钥封装为 {@code KeyValueType}，支持 DSA 与 RSA 密钥。
     * </p>
     *
     * @param key 待表示为 {@code KeyValueType} 的公钥
     *
     * @return 构造的 {@code KeyValueType}；非 DSA/RSA 密钥时抛出异常
     */
    public static KeyValueType createKeyValue(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey pubKey = (RSAPublicKey) key;
            byte[] modulus = pubKey.getModulus().toByteArray();
            byte[] exponent = pubKey.getPublicExponent().toByteArray();

            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus(Base64.getEncoder().encodeToString(modulus).getBytes(GeneralConstants.SAML_CHARSET));
            rsaKeyValue.setExponent(Base64.getEncoder().encodeToString(exponent).getBytes(GeneralConstants.SAML_CHARSET));
            return rsaKeyValue;
        } else if (key instanceof DSAPublicKey) {
            DSAPublicKey pubKey = (DSAPublicKey) key;
            byte[] P = pubKey.getParams().getP().toByteArray();
            byte[] Q = pubKey.getParams().getQ().toByteArray();
            byte[] G = pubKey.getParams().getG().toByteArray();
            byte[] Y = pubKey.getY().toByteArray();

            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setP(Base64.getEncoder().encodeToString(P).getBytes(GeneralConstants.SAML_CHARSET));
            dsaKeyValue.setQ(Base64.getEncoder().encodeToString(Q).getBytes(GeneralConstants.SAML_CHARSET));
            dsaKeyValue.setG(Base64.getEncoder().encodeToString(G).getBytes(GeneralConstants.SAML_CHARSET));
            dsaKeyValue.setY(Base64.getEncoder().encodeToString(Y).getBytes(GeneralConstants.SAML_CHARSET));
            return dsaKeyValue;
        }
        throw logger.unsupportedType(key.toString());
    }

    /** 根据算法名称获取 {@code Signature} 实例。 */
    private static Signature getSignature(String algo) throws GeneralSecurityException {
        Signature sig = null;

        if ("DSA".equalsIgnoreCase(algo)) {
            sig = Signature.getInstance(PicketLinkFederationConstants.DSA_SIGNATURE_ALGORITHM);
        } else if ("RSA".equalsIgnoreCase(algo)) {
            sig = Signature.getInstance(PicketLinkFederationConstants.RSA_SIGNATURE_ALGORITHM);
        } else
            throw logger.signatureUnknownAlgo(algo);
        return sig;
    }
}
