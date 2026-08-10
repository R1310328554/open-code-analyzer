/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jose.jwe;

import java.io.IOException;

import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.JOSE;
import org.keycloak.jose.JOSEHeader;
import org.keycloak.jose.jwe.JWEHeader.JWEHeaderBuilder;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;
import org.keycloak.util.JsonSerialization;

/**
 * JSON Web Encryption（JWE）实现：支持 Compact 序列化的编码、解码与验密。
 * 五段式格式：Base64URL(Header) . EncryptedKey . IV . Ciphertext . AuthTag。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWE implements JOSE {

    /** JWE 头部对象。 */
    private JWEHeader header;
    /** Base64URL 编码的头部字符串。 */
    private String base64Header;

    /** 密钥存储与 CEK 管理。 */
    private JWEKeyStorage keyStorage = new JWEKeyStorage();
    /** Base64URL 编码的加密内容加密密钥（CEK）。 */
    private String base64Cek;

    /** 初始化向量（IV）。 */
    private byte[] initializationVector;

    /** 明文载荷。 */
    private byte[] content;
    /** 加密后的载荷。 */
    private byte[] encryptedContent;

    /** 认证标签（Auth Tag）。 */
    private byte[] authenticationTag;

    /** 构造空 JWE 实例，用于编码流程。 */
    public JWE() {
    }

    /**
     * 从 Compact JWE 字符串解析并填充各段。
     *
     * @param jwt Compact 序列化的 JWE 字符串
     */
    public JWE(String jwt) {
        setupJWEHeader(jwt);
    }

    /**
     * 设置 JWE 头部。
     *
     * @param header JWE 头部
     * @return 当前实例（链式调用）
     */
    public JWE header(JWEHeader header) {
        this.header = header;
        this.base64Header = null;
        return this;
    }

    /** @return 解析后的 JWE 头部 */
    public JOSEHeader getHeader() {
        if (header == null && base64Header != null) {
            try {
                byte[] decodedHeader = Base64Url.decode(base64Header);
                header = JsonSerialization.readValue(decodedHeader, JWEHeader.class);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        return header;
    }

    /**
     * 返回 Base64URL 编码的头部；若尚未编码则从 header 对象生成。
     *
     * @return Base64URL 编码的头部
     * @throws IOException 序列化头部失败时抛出
     */
    public String getBase64Header() throws IOException {
        if (base64Header == null && header != null) {
            byte[] contentBytes = JsonSerialization.writeValueAsBytes(header);
            base64Header = Base64Url.encode(contentBytes);
        }
        return base64Header;
    }


    /** @return 密钥存储对象 */
    public JWEKeyStorage getKeyStorage() {
        return keyStorage;
    }


    /** @return 初始化向量 */
    public byte[] getInitializationVector() {
        return initializationVector;
    }


    /**
     * 设置明文载荷。
     *
     * @param content 待加密的明文字节
     * @return 当前实例（链式调用）
     */
    public JWE content(byte[] content) {
        this.content = content;
        return this;
    }

    /** @return 明文载荷 */
    public byte[] getContent() {
        return content;
    }

    /** @return 加密后的载荷 */
    public byte[] getEncryptedContent() {
        return encryptedContent;
    }


    /** @return 认证标签 */
    public byte[] getAuthenticationTag() {
        return authenticationTag;
    }


    /**
     * 设置加密段信息（IV、密文、认证标签）。
     *
     * @param initializationVector 初始化向量
     * @param encryptedContent 加密后的载荷
     * @param authenticationTag 认证标签
     */
    public void setEncryptedContentInfo(byte[] initializationVector, byte[] encryptedContent, byte[] authenticationTag) {
        this.initializationVector = initializationVector;
        this.encryptedContent = encryptedContent;
        this.authenticationTag = authenticationTag;
    }


    /**
     * 使用注册表中的算法与加密提供器编码 JWE。
     *
     * @return Compact 序列化的 JWE 字符串
     * @throws JWEException 编码过程失败时抛出
     */
    public String encodeJwe() throws JWEException {
        try {
            if (header == null) throw new IllegalStateException("Header must be set");
            return encodeJwe(JWERegistry.getAlgProvider(header.getAlgorithm()), JWERegistry.getEncProvider(header.getEncryptionAlgorithm()));
        } catch (Exception e) {
            throw new JWEException(e);
        }
    }

    /**
     * 使用指定算法与加密提供器编码 JWE。
     *
     * @param algorithmProvider 密钥加密算法提供器
     * @param encryptionProvider 内容加密算法提供器
     * @return Compact 序列化的 JWE 字符串
     * @throws JWEException 编码过程失败时抛出
     */
    public String encodeJwe(JWEAlgorithmProvider algorithmProvider, JWEEncryptionProvider encryptionProvider) throws JWEException {
        try {
            if (header == null) {
                throw new IllegalStateException("Header must be set");
            }
            if (content == null) {
                throw new IllegalStateException("Content must be set");
            }

            if (algorithmProvider == null) {
                throw new IllegalArgumentException("No provider for alg '" + header.getAlgorithm() + "'");
            }

            if (encryptionProvider == null) {
                throw new IllegalArgumentException("No provider for enc '" + header.getEncryptionAlgorithm() + "'");
            }

            keyStorage.setEncryptionProvider(encryptionProvider);
            // 若 CEK 尚未生成则在此创建
            keyStorage.getCEKKey(JWEKeyStorage.KeyUse.ENCRYPTION, true);

            JWEHeaderBuilder headerBuilder = header.toBuilder();
            byte[] encodedCEK = algorithmProvider.encodeCek(encryptionProvider, keyStorage, keyStorage.getEncryptionKey(), headerBuilder);
            base64Cek = Base64Url.encode(encodedCEK);
            header = headerBuilder.build();

            encryptionProvider.encodeJwe(this);

            return getEncodedJweString();
        } catch (Exception e) {
            throw new JWEException(e);
        }
    }

    /** 拼接 Compact JWE 五段字符串。 */
    private String getEncodedJweString() {
        StringBuilder builder = new StringBuilder();
        builder.append(base64Header).append(".")
                .append(base64Cek).append(".")
                .append(Base64Url.encode(initializationVector)).append(".")
                .append(Base64Url.encode(encryptedContent)).append(".")
                .append(Base64Url.encode(authenticationTag));

        return builder.toString();
    }

    /** 从 Compact JWE 字符串解析各段并填充内部状态。 */
    private void setupJWEHeader(String jweStr) throws IllegalStateException {
        String[] parts = jweStr.split("\\.");
        if (parts.length != 5) {
            throw new IllegalStateException("Not a JWE String");
        }

        this.base64Header = parts[0];
        this.base64Cek = parts[1];
        this.initializationVector = Base64Url.decode(parts[2]);
        this.encryptedContent = Base64Url.decode(parts[3]);
        this.authenticationTag = Base64Url.decode(parts[4]);

        this.header = (JWEHeader) getHeader();
    }

    /** 使用指定提供器解密 CEK 并验证/解码 JWE 载荷。 */
    private JWE getProcessedJWE(JWEAlgorithmProvider algorithmProvider, JWEEncryptionProvider encryptionProvider) throws Exception {
        if (algorithmProvider == null) {
            throw new IllegalArgumentException("No provider for alg ");
        }

        if (encryptionProvider == null) {
            throw new IllegalArgumentException("No provider for enc ");
        }

        keyStorage.setEncryptionProvider(encryptionProvider);

        byte[] decodedCek = algorithmProvider.decodeCek(Base64Url.decode(base64Cek), keyStorage.getDecryptionKey(), this.header, encryptionProvider);
        keyStorage.setCEKBytes(decodedCek);

        encryptionProvider.verifyAndDecodeJwe(this);

        return this;
    }

    /**
     * 解析并验证/解码 Compact JWE 字符串（使用注册表提供器）。
     *
     * @param jweStr Compact 序列化的 JWE 字符串
     * @return 解密后的 JWE 实例
     * @throws JWEException 解码或验密失败时抛出
     */
    public JWE verifyAndDecodeJwe(String jweStr) throws JWEException {
        try {
            setupJWEHeader(jweStr);
            return verifyAndDecodeJwe();
        } catch (Exception e) {
            throw new JWEException(e);
        }
    }

    /**
     * 解析并验证/解码 Compact JWE 字符串（使用指定提供器）。
     *
     * @param jweStr Compact 序列化的 JWE 字符串
     * @param algorithmProvider 密钥加密算法提供器
     * @param encryptionProvider 内容加密算法提供器
     * @return 解密后的 JWE 实例
     * @throws JWEException 解码或验密失败时抛出
     */
    public JWE verifyAndDecodeJwe(String jweStr, JWEAlgorithmProvider algorithmProvider, JWEEncryptionProvider encryptionProvider) throws JWEException {
        try {
            setupJWEHeader(jweStr);
            return getProcessedJWE(algorithmProvider, encryptionProvider);
        } catch (Exception e) {
            throw new JWEException(e);
        }
    }

    /**
     * 验证并解码当前 JWE 实例（使用注册表提供器）。
     *
     * @return 解密后的 JWE 实例
     * @throws JWEException 解码或验密失败时抛出
     */
    public JWE verifyAndDecodeJwe() throws JWEException {
        try {
            return getProcessedJWE(JWERegistry.getAlgProvider(header.getAlgorithm()), JWERegistry.getEncProvider(header.getEncryptionAlgorithm()));
        } catch (Exception e) {
            throw new JWEException(e);
        }
    }

}
