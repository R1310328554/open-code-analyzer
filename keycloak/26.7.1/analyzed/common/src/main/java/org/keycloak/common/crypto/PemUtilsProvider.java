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

import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.DerUtils;
import org.keycloak.common.util.PemException;

/**
 * 从 OpenSSL 生成的 PEM 文件中解析公钥、私钥与 X.509 证书的 SPI 抽象基类。
 *
 * <p>提供 PEM 与 DER 互转、证书指纹（thumbprint）计算等通用逻辑，
 * 私钥解码与编码由具体 {@link org.keycloak.common.crypto.CryptoProvider} 实现子类完成。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class PemUtilsProvider {


    /**
     * 从 PEM 字符串解码 X.509 证书。
     *
     * @param cert PEM 编码的证书字符串
     * @return 解码后的证书；输入为 {@code null} 时返回 {@code null}
     * @throws PemException PEM 格式无效或 DER 解析失败
     */
    public X509Certificate decodeCertificate(String cert) {
        if (cert == null) {
            return null;
        }

        try {
            byte[] der = pemToDer(cert);
            ByteArrayInputStream bis = new ByteArrayInputStream(der);
            return DerUtils.decodeCertificate(bis);
        } catch (Exception e) {
            throw new PemException(e);
        }
    }


    /**
     * 从 PEM 字符串解码 RSA 公钥（默认算法类型为 RSA）。
     *
     * @param pem PEM 编码的公钥
     * @return 解码后的公钥；输入为 {@code null} 时返回 {@code null}
     * @throws PemException PEM 格式无效或 DER 解析失败
     */
    public PublicKey decodePublicKey(String pem) {
        return decodePublicKey(pem, "RSA");
    }

    /**
     * 从 PEM 字符串解码指定算法类型的公钥。
     *
     * @param pem PEM 编码的公钥
     * @param type 密钥算法类型（如 RSA、EC）
     * @return 解码后的公钥；输入为 {@code null} 时返回 {@code null}
     * @throws PemException PEM 格式无效或 DER 解析失败
     */
    public PublicKey decodePublicKey(String pem, String type) {
        if (pem == null) {
            return null;
        }

        try {
            byte[] der = pemToDer(pem);
            return DerUtils.decodePublicKey(der, type);
        } catch (Exception e) {
            throw new PemException(e);
        }
    }


    /**
     * 从 PEM 字符串解码私钥。
     *
     * @param pem PEM 编码的私钥
     * @return 解码后的私钥
     * @throws PemException PEM 格式无效或 DER 解析失败
     */
    public abstract PrivateKey decodePrivateKey(String pem);


    /**
     * 将密钥编码为 PEM 字符串。
     *
     * @param key 待编码的密钥
     * @return PEM 编码结果
     */
    public String encodeKey(Key key) {
        return encode(key);
    }
    

    /**
     * 将 X.509 证书编码为 PEM 字符串。
     *
     * @param certificate 待编码的证书
     * @return PEM 编码结果
     */
    public String encodeCertificate(Certificate certificate) {
        return encode(certificate);
    }

    /** 将 PEM 文本（去除头尾标记后）解码为 DER 字节数组。 */
    public byte[] pemToDer(String pem) {
        try {
            pem = removeBeginEnd(pem);
            return Base64.getMimeDecoder().decode(pem);
        } catch (IllegalArgumentException e) {
            throw new PemException(e);
        }
    }

    /** 移除 PEM 头尾标记（BEGIN/END）及换行符，返回纯 Base64 载荷。 */
    public String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }

    /**
     * 计算证书链首证书的指纹并以 Base64Url 编码返回。
     *
     * @param certChain PEM 证书链数组
     * @param encoding 摘要算法名（如 SHA-256）
     * @return Base64Url 编码的指纹字符串
     */
    public String generateThumbprint(String[] certChain, String encoding) throws NoSuchAlgorithmException{
        return Base64Url.encode(generateThumbprintBytes(certChain, encoding));
    }

    private byte[] generateThumbprintBytes(String[] certChain, String encoding) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(encoding).digest(pemToDer(certChain[0]));
    }

    /** 将密钥或证书对象编码为 PEM 字符串（由子类实现）。 */
    protected abstract String encode(Object obj);

}
