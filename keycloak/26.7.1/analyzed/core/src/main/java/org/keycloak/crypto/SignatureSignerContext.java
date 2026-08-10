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
package org.keycloak.crypto;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * JWS 签名上下文接口：提供 kid、算法标识及签名能力。
 */
public interface SignatureSignerContext {

    /** @return 签名所用密钥的 kid */
    String getKid();

    /** @return JWS 算法标识（如 RS256、HS256） */
    String getAlgorithm();

    /** @return 底层哈希算法名称 */
    String getHashAlgorithm();

    /**
     * 对给定字节序列执行数字签名。
     *
     * @param data 待签名原始字节
     * @return 签名结果
     * @throws SignatureException 签名过程失败时抛出
     */
    byte[] sign(byte[] data) throws SignatureException;

    /**
     * 返回与该签名者关联的 X.509 证书链（若可用）。
     * 对基于 MAC 的签名者通常不可用，返回 {@code null}。
     * 允许在不额外传入 {@link KeyWrapper} 的情况下访问证书信息。
     *
     * @return X.509 证书列表，不可用时返回 {@code null}
     */
    default List<X509Certificate> getCertificateChain() {
        return null;
    }

}
