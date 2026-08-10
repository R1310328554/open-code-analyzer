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

import org.keycloak.common.VerificationException;

/**
 * JWS 签名验证上下文接口：提供 kid、算法标识及验签能力。
 */
public interface SignatureVerifierContext {

    /** @return 验签所用密钥的 kid */
    String getKid();

    /** @return JWS 算法标识（如 RS256、HS256） */
    String getAlgorithm();

    /**
     * 验证给定数据与签名的匹配性。
     *
     * @param data 原始待验签字节
     * @param signature 待验证的签名
     * @return 签名有效返回 {@code true}
     * @throws VerificationException 验签过程失败时抛出
     */
    boolean verify(byte[] data, byte[] signature) throws VerificationException;

}
