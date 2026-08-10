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
 * {@code dir}（Direct）密钥管理算法：CEK 直接来自共享密钥，无需额外的 CEK 封装步骤。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DirectAlgorithmProvider implements JWEAlgorithmProvider {

    /** Direct 模式下无独立 CEK 密文，返回空数组。 */
    @Override
    public byte[] decodeCek(byte[] encodedCek, Key encryptionKey, JWEHeader header, JWEEncryptionProvider encryptionProvider) {
        return new byte[0];
    }

    /** Direct 模式下无需编码 CEK，返回空数组。 */
    @Override
    public byte[] encodeCek(JWEEncryptionProvider encryptionProvider, JWEKeyStorage keyStorage, Key encryptionKey, JWEHeaderBuilder headerBuilder) {
        return new byte[0];
    }
}
