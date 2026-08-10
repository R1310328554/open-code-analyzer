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

package org.keycloak.common.util;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * 从 OpenSSL 风格 PEM 提取 {@link PublicKey}、{@link PrivateKey} 与 {@link X509Certificate} 的工具类。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PemUtils {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";

    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    public static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
    public static final String END_RSA_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";

    /**
     * 从 PEM 字符串解码 X509 证书。
     *
     * @param cert PEM 编码证书
     * @return 解码后的证书
     */
    public static X509Certificate decodeCertificate(String cert) {
        return CryptoIntegration.getProvider().getPemUtils().decodeCertificate(cert);
    }

    /**
     * 从 PEM 字符串解码一条或多条 X509 证书（证书链/捆绑包）。
     *
     * @param certs PEM 文本
     * @return 证书数组
     */
    public static X509Certificate[] decodeCertificates(String certs) {
        return Arrays.stream(certs.split(END_CERT))
                .map(String::trim)
                .filter(pemBlock -> !pemBlock.isEmpty())
                .map(pemBlock -> PemUtils.decodeCertificate(pemBlock + END_CERT))
                .toArray(X509Certificate[]::new);
    }

    /**
     * 从 PEM 字符串解码公钥。
     *
     * @param pem PEM 编码公钥
     * @return 公钥
     */
    public static PublicKey decodePublicKey(String pem) {
        return CryptoIntegration.getProvider().getPemUtils().decodePublicKey(pem);
    }

    /**
     * 从 PEM 字符串解码指定类型的公钥。
     *
     * @param pem PEM 编码公钥
     * @param type 密钥类型（RSA、EC 等）
     * @return 公钥，失败时可能为 null
     */
    public static PublicKey decodePublicKey(String pem, String type){
        return CryptoIntegration.getProvider().getPemUtils().decodePublicKey(pem, type);
    }


    /**
     * 从 PEM 字符串解码私钥。
     *
     * @param pem PEM 编码私钥
     * @return 私钥
     */
    public static PrivateKey decodePrivateKey(String pem){
        return CryptoIntegration.getProvider().getPemUtils().decodePrivateKey(pem);
    }


    /**
     * 将 {@link Key} 编码为 PEM 字符串。
     *
     * @param key 密钥
     * @return PEM 文本
     */
    public static String encodeKey(Key key){
        return CryptoIntegration.getProvider().getPemUtils().encodeKey(key);
    }

    /**
     * 将 X509 证书编码为 PEM 字符串。
     *
     * @param certificate 证书
     * @return PEM 文本
     */
    public static String encodeCertificate(Certificate certificate){
        return CryptoIntegration.getProvider().getPemUtils().encodeCertificate(certificate);
    }

    public static byte[] pemToDer(String pem){
        return CryptoIntegration.getProvider().getPemUtils().pemToDer(pem);
    }

    public static String removeBeginEnd(String pem){
        return CryptoIntegration.getProvider().getPemUtils().removeBeginEnd(pem);
    }

    public static String addPrivateKeyBeginEnd(String privateKeyPem) {
        return new StringBuilder(PemUtils.BEGIN_PRIVATE_KEY + "\n")
                .append(privateKeyPem)
                .append("\n" + PemUtils.END_PRIVATE_KEY)
                .toString();
    }

    public static String addCertificateBeginEnd(String certificate) {
        return new StringBuilder(BEGIN_CERT + "\n")
            .append(certificate)
            .append("\n" + END_CERT)
            .toString();
    }

    public static String addRsaPrivateKeyBeginEnd(String privateKeyPem) {
        return new StringBuilder(PemUtils.BEGIN_RSA_PRIVATE_KEY + "\n")
                .append(privateKeyPem)
                .append("\n" + PemUtils.END_RSA_PRIVATE_KEY)
                .toString();
    }

    public static String generateThumbprint(String[] certChain, String encoding) throws NoSuchAlgorithmException{
        return CryptoIntegration.getProvider().getPemUtils().generateThumbprint(certChain, encoding);
    }

}
