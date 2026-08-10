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

package org.keycloak.jose.jwe.enc;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEKeyStorage;

/**
 * JWE 内容加密算法（{@code enc}）提供者：负责明文加解密与 CEK 编解码。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface JWEEncryptionProvider {

    /**
     * 加密 JWE 明文，通常产生三项输出：
     * <ul>
     *   <li>初始化向量（IV）</li>
     *   <li>密文</li>
     *   <li>用于 MAC/完整性校验的 authenticationTag</li>
     * </ul>
     * 完成后应调用 {@link JWE#setEncryptedContentInfo(byte[], byte[], byte[])}。
     *
     * @param jwe 待加密的 JWE 对象
     * @throws IOException IO 异常
     * @throws GeneralSecurityException 密码学异常
     */
    void encodeJwe(JWE jwe) throws Exception;


    /**
     * 校验完整性并解密内容，完成后应调用 {@link JWE#content(byte[])}。
     *
     * @param jwe 待解密的 JWE 对象
     * @throws IOException IO 异常
     * @throws GeneralSecurityException 密码学异常
     */
    void verifyAndDecodeJwe(JWE jwe) throws Exception;


    /**
     * 将 {@link JWEKeyStorage} 中已解码的 CEK 子密钥序列化为字节。
     * 调用前需在 keyStorage 中准备好各用途的 CEK 密钥。
     *
     * @param keyStorage 密钥存储
     * @return CEK 字节
     */
    byte[] serializeCEK(JWEKeyStorage keyStorage);

    /**
     * 从 {@link JWEKeyStorage#getCekBytes()} 反序列化 CEK，并按算法所需用途
     * 调用 {@link JWEKeyStorage#setCEKKey(Key, JWEKeyStorage.KeyUse)}。
     *
     * @param keyStorage 密钥存储
     */
    void deserializeCEK(JWEKeyStorage keyStorage);

    /** 返回该 {@code enc} 算法期望的 CEK 总字节长度。 */
    int getExpectedCEKLength();

}
