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
import org.keycloak.provider.Provider;

/**
 * 数字签名 SPI，提供签名与验签上下文，支持对称与非对称算法。
 */
public interface SignatureProvider extends Provider {

    /** 校验密钥算法与类型是否匹配签名操作要求，不匹配时抛出 {@link SignatureException}。 */
    static void checkKeyForSignature(KeyWrapper key, String algorithm, String type) throws SignatureException {
        if (!type.equals(key.getType()) || !algorithm.equals(key.getAlgorithmOrDefault())) {
            throw new SignatureException(String.format("Key with algorithm %s and type %s is incorrect for provider algorithm %s",
                    key.getAlgorithm(), key.getType(), algorithm));
        }
    }

    /** 校验密钥算法与类型是否匹配验签操作要求，不匹配时抛出 {@link VerificationException}。 */
    static void checkKeyForVerification(KeyWrapper key, String algorithm, String type) throws VerificationException {
        if (!type.equals(key.getType()) || !algorithm.equals(key.getAlgorithmOrDefault())) {
            throw new VerificationException(String.format("Key with algorithm %s and type %s is incorrect for provider algorithm %s",
                    key.getAlgorithm(), key.getType(), algorithm));
        }
    }

    /** 使用默认密钥创建签名上下文。 */
    SignatureSignerContext signer() throws SignatureException;

    /** 使用指定 {@link KeyWrapper} 创建签名上下文。 */
    SignatureSignerContext signer(KeyWrapper key) throws SignatureException;

    /** 按密钥 ID（{@code kid}）查找密钥并创建验签上下文。 */
    SignatureVerifierContext verifier(String kid) throws VerificationException;

    /** 使用指定 {@link KeyWrapper} 创建验签上下文。 */
    SignatureVerifierContext verifier(KeyWrapper key) throws VerificationException;

    /** @return 当前算法是否为非对称（公钥/私钥）类型 */
    boolean isAsymmetricAlgorithm();

    /** 默认空实现，无状态资源需释放。 */
    @Override
    default void close() {
    }

}
