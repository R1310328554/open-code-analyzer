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

package org.keycloak.sdjwt;

import java.util.Objects;

import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.util.JWKSUtils;
import org.keycloak.util.KeyWrapperUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * JWK 解析工具类，将 JSON Web Key 转换为 Keycloak 签名验证上下文。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class JwkParsingUtils {

    /**
     * 将 JSON 节点形式的 JWK 转换为 {@link SignatureVerifierContext}。
     *
     * @param jwkNode JWK 的 JSON 表示
     * @return 可用于验证 JWS 签名的验证上下文
     * @throws IllegalArgumentException 若 JWK 格式错误或不受支持
     */
    public static SignatureVerifierContext convertJwkNodeToVerifierContext(JsonNode jwkNode) {
        JWK jwk;

        try {
            jwk = SdJwtUtils.mapper.convertValue(jwkNode, JWK.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JWK");
        }

        return convertJwkToVerifierContext(jwk);
    }

    /**
     * 将 {@link JWK} 对象转换为 {@link SignatureVerifierContext}。
     *
     * @param jwk JSON Web Key
     * @return 签名验证上下文
     * @throws IllegalArgumentException 若 JWK 无效或算法不受支持
     */
    public static SignatureVerifierContext convertJwkToVerifierContext(JWK jwk) {
        // 将 JWK 包装为 KeyWrapper
        KeyWrapper keyWrapper;

        try {
            keyWrapper = JWKSUtils.getKeyWrapper(jwk);
            Objects.requireNonNull(keyWrapper);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported or invalid JWK");
        }

        // 构建验证器
        return KeyWrapperUtil.createSignatureVerifierContext(keyWrapper);
    }
}
