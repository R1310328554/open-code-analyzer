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

package org.keycloak.jose.jws;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.JOSE;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * JWS Compact 序列化输入的解析器：将 {@code header.payload[.signature]} 拆分为结构化字段。
 * 实现 {@link JOSE}，供验签与载荷反序列化使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JWSInput implements JOSE {
    /** 原始 Compact JWS 字符串。 */
    String wireString;
    /** Base64URL 编码的头部段。 */
    String encodedHeader;
    /** Base64URL 编码的载荷段。 */
    String encodedContent;
    /** Base64URL 编码的签名段（未签名 JWS 可能为空）。 */
    String encodedSignature;
    /** 参与签名的输入：{@code encodedHeader + '.' + encodedContent}。 */
    String encodedSignatureInput;
    /** 解析后的 JWS 头部。 */
    JWSHeader header;
    /** 解码后的载荷字节。 */
    byte[] content;
    /** 解码后的签名字节。 */
    byte[] signature;


    /**
     * 从 Compact JWS 字符串构造解析结果。
     *
     * @param wire Compact 序列化的 JWS
     * @throws JWSInputException 段数非法、Base64 解码或头部 JSON 解析失败时抛出
     */
    public JWSInput(String wire) throws JWSInputException {
        try {
            this.wireString = wire;
            String[] parts = wire.split("\\.");
            if (parts.length < 2 || parts.length > 3) throw new IllegalArgumentException("Parsing error");
            encodedHeader = parts[0];
            encodedContent = parts[1];
            encodedSignatureInput = encodedHeader + '.' + encodedContent;
            content = Base64Url.decode(encodedContent);
            if (parts.length > 2) {
                encodedSignature = parts[2];
                signature = Base64Url.decode(encodedSignature);

            }
            byte[] headerBytes = Base64Url.decode(encodedHeader);
            header = JsonSerialization.readValue(headerBytes, JWSHeader.class);
        } catch (Throwable t) {
            throw new JWSInputException(t);
        }
    }

    /** @return 原始 Compact JWS 字符串 */
    public String getWireString() {
        return wireString;
    }

    /** @return Base64URL 编码的头部段 */
    public String getEncodedHeader() {
        return encodedHeader;
    }

    /** @return Base64URL 编码的载荷段 */
    public String getEncodedContent() {
        return encodedContent;
    }

    /** @return Base64URL 编码的签名段 */
    public String getEncodedSignature() {
        return encodedSignature;
    }

    /** @return 签名输入字符串（header.payload） */
    public String getEncodedSignatureInput() {
        return encodedSignatureInput;
    }

    /** @return 解析后的 {@link JWSHeader} */
    public JWSHeader getHeader() {
        return header;
    }

    /** @return 解码后的载荷字节 */
    public byte[] getContent() {
        return content;
    }

    /** @return 解码后的签名字节 */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * 将载荷反序列化为指定 Java 类型。
     *
     * @param type 目标类型
     * @param <T> 反序列化结果类型
     * @return 载荷对象
     * @throws JWSInputException JSON 解析失败时抛出
     */
    public <T> T readJsonContent(Class<T> type) throws JWSInputException {
        try {
            return JsonSerialization.readValue(content, type);
        } catch (IOException e) {
            throw new JWSInputException(e);
        }
    }

    /**
     * 将载荷反序列化为指定泛型类型（如 {@link TypeReference}）。
     *
     * @param type 目标类型引用
     * @param <T> 反序列化结果类型
     * @return 载荷对象
     * @throws JWSInputException JSON 解析失败时抛出
     */
    public <T> T readJsonContent(TypeReference<T> type) throws JWSInputException {
        try {
            return JsonSerialization.readValue(content, type);
        } catch (IOException e) {
            throw new JWSInputException(e);
        }
    }

    /** @return 载荷的 UTF-8 字符串形式 */
    public String readContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
