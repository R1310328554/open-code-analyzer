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

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * 从 DER 编码字节流或文件中解析私钥、公钥与 X509 证书（通常由 OpenSSL 生成）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public final class DerUtils {

    private DerUtils() {
    }

    /** 从输入流读取 DER 私钥。 */
    public static PrivateKey decodePrivateKey(InputStream is)
            throws Exception {

        DataInputStream dis = new DataInputStream(is);
        byte[] keyBytes = new byte[dis.available()];
        dis.readFully(keyBytes);
        dis.close();

        return decodePrivateKey(keyBytes);
    }

    /** 从 Base64 编码字符串解码 RSA 公钥。 */
    public static PublicKey decodePublicKey(String encoded) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        return decodePublicKey(encoded, "RSA");
    }

    /** 从 Base64 字符串按指定算法类型解码公钥。 */
    public static PublicKey decodePublicKey(String encoded, String type) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        byte[] der = Base64.getDecoder().decode(encoded);
        return decodePublicKey(der, type);
    }

    /** 从 DER 字节数组解码 RSA 公钥。 */
    public static PublicKey decodePublicKey(byte[] der) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        return decodePublicKey(der, "RSA");
    }

    /** 从 DER 字节数组按指定算法类型解码公钥。 */
    public static PublicKey decodePublicKey(byte[] der, String type) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(der);
        KeyFactory kf = CryptoIntegration.getProvider().getKeyFactory(type);
        return kf.generatePublic(spec);
    }

    /** 从输入流解码 X509 证书。 */
    public static X509Certificate decodeCertificate(InputStream is) throws Exception {
        CertificateFactory cf = CryptoIntegration.getProvider().getX509CertFactory();
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        is.close();
        return cert;
    }

    /** 从 DER 字节数组解码私钥，依次尝试 RSA 与 EC 算法。 */
    public static PrivateKey decodePrivateKey(byte[] der) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(der);
        String[] algorithms = { "RSA", "EC" };
        for (String algorithm : algorithms) {
            try {
                return CryptoIntegration.getProvider().getKeyFactory(algorithm).generatePrivate(spec);
            } catch (InvalidKeySpecException e) {
                // 忽略并尝试下一算法
            }
        }
        throw new InvalidKeySpecException("Unable to decode the private key with supported algorithms: " + String.join(", ", algorithms));
   }
}
