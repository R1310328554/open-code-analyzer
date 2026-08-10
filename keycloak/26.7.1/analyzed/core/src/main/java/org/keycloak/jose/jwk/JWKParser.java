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

package org.keycloak.jose.jwk;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.Base64Url;
import org.keycloak.crypto.KeyType;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * JWK JSON 解析器：将 JWK 字符串或对象转为 {@link PublicKey}，并检测密钥类型是否受支持。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JWKParser {

    protected JWK jwk;

    private JWKParser() {
    }

    /** 创建空解析器。 */
    public static JWKParser create() {
        return new JWKParser();
    }

    /** 以已有 JWK 构造解析器。 */
    public JWKParser(JWK jwk) {
        this.jwk = jwk;
    }

    /** 以已有 JWK 创建解析器。 */
    public static JWKParser create(JWK jwk) {
        return new JWKParser(jwk);
    }

    /**
     * 解析 JWK JSON 字符串。
     *
     * @param jwk JWK JSON
     * @return 当前解析器
     */
    public JWKParser parse(String jwk) {
        try {
            this.jwk = JsonSerialization.mapper.readValue(jwk, JWK.class);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 返回已解析的 JWK 对象。 */
    public JWK getJwk() {
        return jwk;
    }

    /**
     * 将当前 JWK 转为 {@link PublicKey}。
     *
     * @return 对应公钥
     * @throws IllegalStateException 尚未设置 JWK 时
     * @throws RuntimeException 不支持的 {@code kty} 或字段缺失
     */
    public PublicKey toPublicKey() {
        if (jwk == null) {
            throw new IllegalStateException("Not possible to convert to the publicKey. The jwk is not set");
        }
        String keyType = jwk.getKeyType();

        // 子类字段存储方式可能不同；序列化为 JsonNode 可统一访问路径
        JsonNode normalizedJwkNode = JsonSerialization.writeValueAsNode(jwk);
        if (KeyType.RSA.equals(keyType)) {
            return createRSAPublicKey(normalizedJwkNode);
        } else if (KeyType.EC.equals(keyType)) {
            return createECPublicKey(normalizedJwkNode);
        } else if (KeyType.OKP.equals(keyType)) {
            return JWKBuilder.EdEC_UTILS.createOKPPublicKey(jwk);
        } else if (KeyType.AKP.equals(keyType)) {
            return createAPKPublicKey(normalizedJwkNode);
        } else {
            throw new RuntimeException("Unsupported keyType " + keyType);
        }
    }

    /** 从规范化 JsonNode 构建 EC 公钥。 */
    private static PublicKey createECPublicKey(JsonNode jwk) {


        /* 读取 EC 必需字段 */
        String crv = jwk.path(ECPublicJWK.CRV).asText(null);
        String xStr = jwk.get(ECPublicJWK.X).asText(null);
        String yStr = jwk.get(ECPublicJWK.Y).asText(null);

        /* 校验字段是否齐全 */
        if (crv == null || xStr == null || yStr == null) {
            throw new RuntimeException("Fail to retrieve ECPublicJWK.CRV, ECPublicJWK.X or ECPublicJWK.Y field.");
        }

        BigInteger x = new BigInteger(1, Base64Url.decode(xStr));
        BigInteger y = new BigInteger(1, Base64Url.decode(yStr));

        String name;
        switch (crv) {
            case "P-256" :
                name = "secp256r1";
                break;
            case "P-384" :
                name = "secp384r1";
                break;
            case "P-521" :
                name = "secp521r1";
                break;
            default :
                throw new RuntimeException("Unsupported curve");
        }

        try {

            ECPoint point = new ECPoint(x, y);
            ECParameterSpec params = CryptoIntegration.getProvider().createECParams(name);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);

            KeyFactory kf = CryptoIntegration.getProvider().getKeyFactory("ECDSA");
            return kf.generatePublic(pubKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 从 JsonNode 构建 RSA 公钥。 */
    private static PublicKey createRSAPublicKey(JsonNode jwk) {
        BigInteger modulus = new BigInteger(1, Base64Url.decode(jwk.path(RSAPublicJWK.MODULUS).asText(null)));
        BigInteger publicExponent = new BigInteger(1, Base64Url.decode(jwk.path(RSAPublicJWK.PUBLIC_EXPONENT).asText(null)));

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 从 JsonNode 构建 AKP 公钥。 */
    private static PublicKey createAPKPublicKey(JsonNode jwk) {
        String algorithm = jwk.path(JWK.ALGORITHM).asText();
        String publicKey = jwk.path(AKPPublicJWK.PUB).asText();
        return AKPUtils.fromEncodedPub(publicKey, algorithm);
    }

    /**
     * 判断给定 {@code kty} 是否可由本解析器转为 {@link PublicKey}。
     *
     * @param keyType JWK 密钥类型
     * @return 支持时返回 {@code true}
     */
    public boolean isKeyTypeSupported(String keyType) {
        return (RSAPublicJWK.RSA.equals(keyType) || ECPublicJWK.EC.equals(keyType)
                || (JWKBuilder.EdEC_UTILS.isEdECSupported() && OKPPublicJWK.OKP.equals(keyType)))
                || KeyType.AKP.equals(keyType);
    }
}
