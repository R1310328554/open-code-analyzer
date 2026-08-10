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

package org.keycloak.jose.jwe.alg;

import java.security.Key;

import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEHeader.JWEHeaderBuilder;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;

/**
 * JWE 密钥管理算法（{@code alg}）提供者：负责 CEK 的编码与解码。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface JWEAlgorithmProvider {

    /**
     * 从 JWE 中的加密 CEK 恢复明文 CEK 字节。
     *
     * @param encodedCek 加密后的 CEK
     * @param encryptionKey 密钥管理用密钥
     * @param header JWE 头部
     * @param encryptionProvider 内容加密提供者（用于 CEK 长度等）
     */
    byte[] decodeCek(byte[] encodedCek, Key encryptionKey, JWEHeader header, JWEEncryptionProvider encryptionProvider) throws Exception;

    /**
     * 将 CEK 编码为 JWE 中的加密密钥段，并可更新头部（如 ECDH 临时公钥）。
     *
     * @param encryptionProvider 内容加密提供者
     * @param keyStorage CEK 与密钥存储
     * @param encryptionKey 密钥管理用密钥
     * @param headerBuilder 可修改的头部构建器
     */
    byte[] encodeCek(JWEEncryptionProvider encryptionProvider, JWEKeyStorage keyStorage, Key encryptionKey, JWEHeaderBuilder headerBuilder) throws Exception;

}
