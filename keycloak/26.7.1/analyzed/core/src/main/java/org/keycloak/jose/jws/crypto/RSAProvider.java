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

package org.keycloak.jose.jws.crypto;


import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.keycloak.common.util.PemUtils;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.jose.jws.JWSInput;

/**
 * RSA/RSASSA-PSS 算法 JWS 签名与验签提供者。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RSAProvider implements SignatureProvider {
    /**
     * 将 JWS RSA 算法枚举映射为 JCA 签名算法名。
     *
     * @param alg JWS 算法
     * @return JCA 算法名
     */
    public static String getJavaAlgorithm(Algorithm alg) {
        switch (alg) {
            case RS256:
                return "SHA256withRSA";
            case RS384:
                return "SHA384withRSA";
            case RS512:
                return "SHA512withRSA";
            case PS256:
                return "SHA256withRSAandMGF1";
            case PS384:
                return "SHA384withRSAandMGF1";
            case PS512:
                return "SHA512withRSAandMGF1";
            default:
                throw new IllegalArgumentException("Not a supported RSA Algorithm: " + alg);
        }
    }

    /** 获取指定算法的 {@link Signature} 实例。 */
    public static Signature getSignature(Algorithm alg) {
        try {
            return Signature.getInstance(getJavaAlgorithm(alg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用 RSA 私钥对数据签名。
     *
     * @param data 待签名数据
     * @param algorithm JWS 算法
     * @param privateKey RSA 私钥
     * @return 签名字节
     */
    public static byte[] sign(byte[] data, Algorithm algorithm, PrivateKey privateKey) {
        try {
            Signature signature = getSignature(algorithm);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过 PEM 编码的 X.509 证书验签。
     *
     * @param input JWS 输入
     * @param cert PEM 证书字符串
     * @return 验签是否通过
     */
    public static boolean verifyViaCertificate(JWSInput input, String cert) {
        X509Certificate certificate;
        try {
            certificate = PemUtils.decodeCertificate(cert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return verify(input, certificate.getPublicKey());
    }

    /**
     * 使用 RSA 公钥验证 JWS 签名。
     *
     * @param input JWS 输入
     * @param publicKey RSA 公钥
     * @return 验签是否通过
     */
    public static boolean verify(JWSInput input, PublicKey publicKey) {
        try {
            Signature verifier = getSignature(input.getHeader().getAlgorithm());
            verifier.initVerify(publicKey);
            verifier.update(input.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8));
            return verifier.verify(input.getSignature());
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * {@link SignatureProvider} 接口实现：通过证书字符串验签。
     *
     * @param input JWS 输入
     * @param key PEM 证书字符串
     * @return 验签是否通过
     */
    @Override
    public boolean verify(JWSInput input, String key) {
        return verifyViaCertificate(input, key);
    }


}
