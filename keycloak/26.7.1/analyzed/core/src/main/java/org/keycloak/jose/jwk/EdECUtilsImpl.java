/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import java.util.Optional;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;

/**
 * 基于 JDK 15+ EdEC API 的 {@link EdECUtils} 实现，负责 OKP JWK 与 {@link EdECPublicKey} 互转。
 *
 * @author rmartinc
 */
class EdECUtilsImpl implements EdECUtils {

    public EdECUtilsImpl() {
    }

    @Override
    public boolean isEdECSupported() {
        return true;
    }

    @Override
    public JWK okp(String kid, String algorithm, Key key, KeyUse keyUse) {
        EdECPublicKey eddsaPublicKey = (EdECPublicKey) key;

        OKPPublicJWK k = new OKPPublicJWK();

        kid = kid != null ? kid : KeyUtils.createKeyId(key);

        k.setKeyId(kid);
        k.setKeyType(KeyType.OKP);
        k.setAlgorithm(algorithm);
        k.setPublicKeyUse(keyUse == null ? JWKBuilder.DEFAULT_PUBLIC_KEY_USE.getSpecName() : keyUse.getSpecName());
        k.setCrv(eddsaPublicKey.getParams().getName());

        Optional<String> x = edPublicKeyInJwkRepresentation(eddsaPublicKey);
        k.setX(x.orElse(""));

        return k;
    }

    @Override
    public PublicKey createOKPPublicKey(JWK jwk) {
        String x = jwk.getOtherClaim(OKPPublicJWK.X, String.class);
        String crv = jwk.getOtherClaim(OKPPublicJWK.CRV, String.class);
        // JWK 中公钥的 "x" 表示（RFC 8037）
        int bytesLength = 0;
        if (Algorithm.Ed25519.equals(crv)) {
            bytesLength = 32;
        } else if (Algorithm.Ed448.equals(crv)) {
            bytesLength = 57;
        } else {
            throw new RuntimeException("Invalid JWK representation of OKP type algorithm");
        }

        byte[] decodedX = Base64Url.decode(x);
        if (decodedX.length != bytesLength) {
            throw new RuntimeException("Invalid JWK representation of OKP type public key");
        }

        // x 坐标奇偶性由 decoded "x" 最高字节的最高位表示：1 为奇，0 为偶
        boolean isOddX = false;
        if ((decodedX[decodedX.length - 1] & -128) != 0) { // 0b10000000
            isOddX = true;
        }

        // 将表示 x 坐标奇偶性的最高位清零
        decodedX[decodedX.length - 1] &= 127; // 0b01111111

        // 扭曲 Edwards 曲线上的 x、y 坐标均为非负整数
        BigInteger y = new BigInteger(1, reverseBytes(decodedX));
        NamedParameterSpec spec = new NamedParameterSpec(crv);
        EdECPoint ep = new EdECPoint(isOddX, y);
        EdECPublicKeySpec keySpec = new EdECPublicKeySpec(spec, ep);

        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance(crv).generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return publicKey;
    }

    /** 将 {@link EdECPublicKey} 编码为 JWK 的 {@code x} 字段（RFC 8032/8037）。 */
    private static Optional<String> edPublicKeyInJwkRepresentation(EdECPublicKey eddsaPublicKey) {
        EdECPoint edEcPoint = eddsaPublicKey.getPoint();
        BigInteger yCoordinate = edEcPoint.getY();

        // JWK 中公钥的 "x" 表示
        int bytesLength = 0;
        if (Algorithm.Ed25519.equals(eddsaPublicKey.getParams().getName())) {
            bytesLength = 32;
        } else if (Algorithm.Ed448.equals(eddsaPublicKey.getParams().getName())) {
            bytesLength = 57;
        } else {
            return Optional.ofNullable(null);
        }

        // y 坐标较小时 toByteArray() 可能短于 bytesLength，需左侧补零
        byte[] yCoordinateLittleEndianBytes = new byte[bytesLength];

        // 将 BigInteger 大端字节序转为 JWK 要求的小端表示（RFC 8032/8037）
        byte[] yCoordinateLittleEndian = reverseBytes(yCoordinate.toByteArray());
        System.arraycopy(yCoordinateLittleEndian, 0, yCoordinateLittleEndianBytes, 0, yCoordinateLittleEndian.length);

        // 将 x 坐标奇偶性写入最后一字节最高位（RFC 8032/8037）
        if (edEcPoint.isXOdd()) {
            yCoordinateLittleEndianBytes[yCoordinateLittleEndianBytes.length - 1] |= -128; // 0b10000000
        }

        return Optional.ofNullable(Base64Url.encode(yCoordinateLittleEndianBytes));
    }

    /** 反转字节数组（大端与小端互转）。 */
    private static byte[] reverseBytes(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[length - 1 - i] = array[i];
        }

        return reversedArray;
    }
}
