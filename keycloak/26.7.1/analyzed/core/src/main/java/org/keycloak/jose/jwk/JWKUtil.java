/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import java.util.Arrays;

/**
 * JWK 编码辅助工具：将 {@link BigInteger} 转为固定长度无符号字节数组（RFC 7518 对 EC 坐标等字段的要求）。
 */
public class JWKUtil {

    /**
     * 将 {@code BigInteger} 转为字节数组，必要时去掉符号字节。
     *
     * @param bigInt 待转换的大整数
     * @return 无符号大端字节表示
     */
    public static byte[] toIntegerBytes(final BigInteger bigInt) {
        return toIntegerBytes(bigInt, bigInt.bitLength());
    }

    /**
     * 将 {@code BigInteger} 转为指定比特长度的字节数组（RFC 7518 中 EC 密钥 X/Y 等字段）。
     *
     * @param bigInt 待转换的大整数
     * @param bitlen 目标比特长度（如 P-521 为 521）
     * @return 长度为 {@code (bitlen + 7) / 8} 的字节数组
     * @throws IllegalStateException 若大整数比特长度超过 {@code bitlen}
     */
    public static byte[] toIntegerBytes(final BigInteger bigInt, int bitlen) {
        assert bigInt.bitLength() <= bitlen : "Incorrect big integer with bit length " + bigInt.bitLength() + " for " + bitlen;
        final int bytelen = (bitlen + 7) / 8;
        final byte[] array = bigInt.toByteArray();
        if (array.length == bytelen) {
            // 长度符合预期，直接返回
            return array;
        } else if (bytelen < array.length) {
            // 多出的 1 字节为符号位，截去
            return Arrays.copyOfRange(array, array.length - bytelen, array.length);
        } else {
            // 长度不足时在高位补零
            final byte[] resizedBytes = new byte[bytelen];
            System.arraycopy(array, 0, resizedBytes, bytelen - array.length, array.length);
            return resizedBytes;
        }
    }
}
