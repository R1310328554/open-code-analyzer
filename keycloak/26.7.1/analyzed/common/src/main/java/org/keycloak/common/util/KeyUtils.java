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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * 密钥生成、加载与标识（kid）等密码学工具方法。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeyUtils {

    /** 生成密钥 ID 时使用的默认摘要算法。 */
    private static final String DEFAULT_MESSAGE_DIGEST = "SHA-256";

    private KeyUtils() {
    }

    /** 从原始字节与算法名构造 {@link SecretKey}。 */
    public static SecretKey loadSecretKey(byte[] secret, String javaAlgorithmName) {
        return new SecretKeySpec(secret, javaAlgorithmName);
    }

    /** 生成指定位长的 RSA 密钥对。 */
    public static KeyPair generateRsaKeyPair(int keysize) {
        try {
            KeyPairGenerator generator = CryptoIntegration.getProvider().getKeyPairGen("RSA"); 
            generator.initialize(keysize);
            KeyPair keyPair = generator.generateKeyPair();
            return keyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 从 RSA 私钥（{@link RSAPrivateCrtKey}）提取对应公钥。 */
    public static PublicKey extractPublicKey(PrivateKey key) {
        if (key == null) {
            return null;
        }

        try {
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) key;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 按曲线/算法名生成 EdDSA 密钥对。 */
    public static KeyPair generateEddsaKeyPair(String curveName) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(curveName);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 按命名曲线（如 {@code secp256r1}）生成 EC 密钥对。 */
    public static KeyPair generateEcKeyPair(String keySpecName) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom randomGen = new SecureRandom();
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(keySpecName);
            keyGen.initialize(ecSpec, randomGen);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 对密钥编码做 SHA-256 摘要并 Base64Url 编码，用作 JWK kid。 */
    public static String createKeyId(Key key) {
        try {
            return Base64Url.encode(MessageDigest.getInstance(DEFAULT_MESSAGE_DIGEST).digest(key.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
