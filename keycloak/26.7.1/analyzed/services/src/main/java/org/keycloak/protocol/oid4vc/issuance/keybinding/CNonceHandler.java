/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.keycloak.common.VerificationException;
import org.keycloak.provider.Provider;

/**
 * OID4VCI c_nonce（密码学 nonce）的生成与校验提供方。
 * <p>JWT 实现将受众等信息编码进 nonce，供 proof/attestation 防重放。</p>
 *
 * @author Pascal Knüppel
 */
public interface CNonceHandler extends Provider {

    /**
     * 构建 c_nonce 字符串。JWT 实现会将 {@code audiences} 写入 aud 声明。
     *
     * @param audiences JWT c_nonce 的目标受众列表
     * @param additionalDetails 实现特定的附加属性（如 source endpoint）
     * @return c_nonce 字符串
     */
    public String buildCNonce(List<String> audiences, @Nullable Map<String, Object> additionalDetails);

    /**
     * 校验由 {@link #buildCNonce(List, Map)} 签发的 c_nonce 是否仍有效。
     *
     * @param cNonce 待校验的 c_nonce
     * @param audiences 期望的 JWT aud 值
     * @param additionalDetails 实现特定的附加校验属性
     * @throws VerificationException nonce 无效、过期或签名失败
     */
    public void verifyCNonce(String cNonce, List<String> audiences, @Nullable Map<String, Object> additionalDetails) throws VerificationException;

    @Override
    default void close() {
        // 无资源需释放
    }
}
