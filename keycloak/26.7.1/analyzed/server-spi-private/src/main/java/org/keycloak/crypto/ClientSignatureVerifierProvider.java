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
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.ClientModel;
import org.keycloak.provider.Provider;

/**
 * 客户端 JWS 签名验证提供者 SPI。
 * <p>根据客户端配置与 JWS 输入创建 {@link SignatureVerifierContext}，用于校验请求签名。</p>
 */
public interface ClientSignatureVerifierProvider extends Provider {
    /**
     * 为指定客户端与 JWS 输入构建签名验证上下文。
     * @param client 客户端模型
     * @param input 待验证的 JWS
     * @return 可用于执行验签的上下文
     * @throws VerificationException 无法构建验证器时抛出
     */
    SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException;

    /** 默认空实现，无资源需释放。 */
    @Override
    default void close() {
    }

    /** @return 该提供者支持的 JWS 算法标识（如 {@code RS256}） */
    String getAlgorithm();

    /** @return 是否为非对称签名算法（RSA/EC 等） */
    boolean isAsymmetricAlgorithm();
}
