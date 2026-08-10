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

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * X509 证书生成工具类，提供 V1 与 V3 版本 {@link java.security.cert.X509Certificate} 的创建方法。
 *
 * <p>具体加解密与证书构建逻辑委托给 {@link org.keycloak.common.crypto.CryptoIntegration} 所选 Provider。</p>
 */
public class CertificateUtils {


    /**
     * 生成 V3 版本 {@link java.security.cert.X509Certificate}。
     *
     * @param keyPair the key pair
     * @param caPrivateKey the CA private key
     * @param caCert the CA certificate
     * @param subject the subject name
     *
     * @return the x509 certificate
     *
     * @throws Exception the exception
     */
    public static X509Certificate generateV3Certificate(KeyPair keyPair, PrivateKey caPrivateKey,
            X509Certificate caCert, String subject) throws Exception {
        return CryptoIntegration.getProvider().getCertificateUtils().generateV3Certificate(keyPair, caPrivateKey,
                caCert, subject);
    }

    /**
     * 生成 V1 自签名 {@link java.security.cert.X509Certificate}。
     *
     * @param caKeyPair the CA key pair
     * @param subject the subject name
     *
     * @return the x509 certificate
     *
     * @throws Exception the exception
     */
    public static X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject) {
        return CryptoIntegration.getProvider().getCertificateUtils().generateV1SelfSignedCertificate(caKeyPair, subject);
    } 

    /** 生成带指定序列号的 V1 自签名证书。 */
    public static X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject, BigInteger serialNumber) {
        return CryptoIntegration.getProvider().getCertificateUtils().generateV1SelfSignedCertificate(caKeyPair, subject, serialNumber);
    }

    /** 生成带序列号与有效期截止时间的 V1 自签名证书。 */
    public static X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject, BigInteger serialNumber, Date validityEndDate) {
        return CryptoIntegration.getProvider().getCertificateUtils().generateV1SelfSignedCertificate(caKeyPair, subject, serialNumber, validityEndDate);
    }
        
}
