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

package org.keycloak.common.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * X.509 证书生成与解析工具 SPI。
 *
 * <p>提供 V1/V3 {@link java.security.cert.X509Certificate} 的签发、策略 OID 与 CRL 分发点读取等能力，
 * 具体实现随 FIPS/非 FIPS {@link CryptoProvider} 切换。</p>
 */
public interface CertificateUtilsProvider {

    /** CRL Distribution Points X.509 扩展 OID（2.5.29.31）。 */
    public static final String CRL_DISTRIBUTION_POINTS_OID = "2.5.29.31";

    /**
     * 签发 X.509 v3 证书（由 CA 私钥签名）。
     *
     * @param keyPair 待证书主体的密钥对
     * @param caPrivateKey CA 私钥
     * @param caCert CA 证书
     * @param subject 证书主体 DN
     * 
     * @return 签发的 X.509 证书
     * 
     * @throws Exception 签名或编码失败
     */
    public X509Certificate generateV3Certificate(KeyPair keyPair, PrivateKey caPrivateKey, X509Certificate caCert,
            String subject) throws Exception;

    /**
     * 签发 X.509 v1 自签名证书。
     *
     * @param caKeyPair 自签名密钥对
     * @param subject 证书主体 DN
     * 
     * @return 自签名 X.509 证书
     * 
     * @throws Exception 签名或编码失败
     */
    public X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject); 

    /** 指定序列号签发 v1 自签名证书。 */
    public X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject, BigInteger serialNumber);

    /** 指定序列号与有效期签发 v1 自签名证书。 */
    public X509Certificate generateV1SelfSignedCertificate(KeyPair caKeyPair, String subject, BigInteger serialNumber, Date validityEndDate);

    /** 读取证书策略（Certificate Policies）扩展中的 OID 列表。 */
    public List<String> getCertificatePolicyList(X509Certificate cert) throws GeneralSecurityException;

    /** 读取 CRL Distribution Points 扩展中的 URI 列表。 */
    public List<String> getCRLDistributionPoints(X509Certificate cert) throws IOException;

    /** 生成用于测试的服务器/客户端证书。 */
    public X509Certificate createServicesTestCertificate(String dn,
                                             Date startDate,
                                             Date expiryDate,
                                             KeyPair keyPair,
                                             String... certificatePolicyOid);
        
}
