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
package org.keycloak.sdjwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.jws.crypto.HashUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * SD-JWT 通用工具类，提供编解码、哈希、盐值生成与 JSON 处理等功能。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class SdJwtUtils {

    /** 共享 Jackson 对象映射器。 */
    public static final ObjectMapper mapper = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Base64Url 编码（无填充）。 */
    public static String encodeNoPad(byte[] bytes) {
        return Base64Url.encode(bytes);
    }

    /** 将字符串以 UTF-8 编码后 Base64Url 编码（无填充）。 */
    public static String encodeNoPad(String input) {
        return encodeNoPad(utf8Bytes(input));
    }

    /** Base64Url 解码（无填充）。 */
    public static byte[] decodeNoPad(String encoded) {
        return Base64Url.decode(encoded);
    }

    /** 对披露字节计算哈希并 Base64Url 编码。 */
    public static String hashAndBase64EncodeNoPad(byte[] disclosureBytes, String hashAlg) {
        return encodeNoPad(HashUtils.hash(hashAlg, disclosureBytes));
    }

    /** 对披露字符串计算哈希并 Base64Url 编码。 */
    public static String hashAndBase64EncodeNoPad(String disclosure, String hashAlg) {
        return hashAndBase64EncodeNoPad(utf8Bytes(disclosure), hashAlg);
    }

    /** 获取字符串的 UTF-8 字节。 */
    public static byte[] utf8Bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /** 要求字符串非空，否则抛出 {@link IllegalArgumentException}。 */
    public static String requireNonEmpty(String str, String message) {
        return Optional.ofNullable(str)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    /** 生成随机盐值（128 位熵，Base64Url 编码）。 */
    public static String randomSalt() {
        // 16 字节对应 128 位熵，Base64Url 编码
        return encodeNoPad(randomBytes(16));
    }

    /** 生成指定长度的随机字节。 */
    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /** 将数组序列化为 JSON 字符串。 */
    public static String printJsonArray(Object[] array) throws JsonProcessingException {
        if (arrayEltSpaced) {
            return arraySpacedPrettyPrinter.writer.writeValueAsString(array);
        } else {
            return mapper.writeValueAsString(array);
        }
    }

    /** 解码 Base64Url 编码的披露字符串并解析为 JSON 数组。 */
    public static ArrayNode decodeDisclosureString(String disclosure) throws VerificationException {
        JsonNode jsonNode;

    // 使用 UTF-8 解码 Base64URL 编码的披露字符串
    String decoded = new String(decodeNoPad(disclosure), StandardCharsets.UTF_8);

        // 将披露字符串解析为 JSON 数组
        try {
            jsonNode = mapper.readTree(decoded);
        } catch (JsonProcessingException e) {
            throw new VerificationException("Disclosure is not a valid JSON", e);
        }

        // 校验解析结果为数组
        if (!jsonNode.isArray()) {
            throw new VerificationException("Disclosure is not a JSON array");
        }

        return (ArrayNode) jsonNode;
    }

    /** 深拷贝 JSON 节点。 */
    public static JsonNode deepClone(JsonNode node) {
        try {
            byte[] serializedNode = mapper.writeValueAsBytes(node);
            return mapper.readTree(serializedNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static ArraySpacedPrettyPrinter arraySpacedPrettyPrinter = new ArraySpacedPrettyPrinter();

    /** 数组元素间带空格的 JSON 格式化器。 */
    static class ArraySpacedPrettyPrinter extends MinimalPrettyPrinter {
        final ObjectMapper prettyPrinObjectMapper;
        final ObjectWriter writer;

        public ArraySpacedPrettyPrinter() {
            prettyPrinObjectMapper = new ObjectMapper();
            prettyPrinObjectMapper.setDefaultPrettyPrinter(this);
            writer = prettyPrinObjectMapper.writer(this);
        }

        @Override
        public void writeArrayValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(',');
            jg.writeRaw(' ');
        }

        @Override
        public void writeObjectEntrySeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(',');
            jg.writeRaw(' '); // 逗号后添加空格
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(':');
            jg.writeRaw(' '); // 冒号后添加空格
        }
    }

    /** 数组元素序列化时是否在逗号后插入空格。 */
    public static boolean arrayEltSpaced = true;
}
