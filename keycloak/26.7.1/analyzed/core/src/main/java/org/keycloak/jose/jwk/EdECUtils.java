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

import java.security.Key;
import java.security.PublicKey;

import org.keycloak.crypto.KeyUse;

/**
 * EdDSA（Ed25519/Ed448）JWK 工具接口，仅在 JDK 15+ 环境下由 {@link EdECUtilsImpl} 实现。
 *
 * @author rmartinc
 */
interface EdECUtils {

    /** 当前运行时是否支持 EdEC（EdDSA）算法。 */
    boolean isEdECSupported();

    /**
     * 将 OKP 公钥转换为 JWK。
     *
     * @param kid 密钥 ID，可为 {@code null}（将自动生成）
     * @param algorithm 算法名
     * @param key 公钥
     * @param keyUse 密钥用途
     * @return OKP 类型的 {@link JWK}
     */
    JWK okp(String kid, String algorithm, Key key, KeyUse keyUse);

    /**
     * 从 OKP 类型 JWK 重建 {@link PublicKey}。
     *
     * @param jwk OKP JWK
     * @return 对应的 EdEC 公钥
     */
    PublicKey createOKPPublicKey(JWK jwk);
}
