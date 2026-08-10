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
 */

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import java.util.Map;

import org.keycloak.jose.jwk.JWK;

/**
 * 按 kid 解析 attestation 公钥的接口，供 JWT attestation 校验使用。
 * <p>实现可从本地注册表、远程 JWKS 或其他可信源解析密钥。</p>
 *
 * @author <a href="mailto:Rodrick.Awambeng@adorsys.com">Rodrick Awambeng</a>
 */
public interface AttestationKeyResolver {
    /**
     * 根据 kid、JWS 头与载荷上下文解析 JWK。
     * <p>无法解析或密钥不可信时返回 {@code null}。</p>
     *
     * @param kid JWT 头中的 key id
     * @param header JWS 头声明映射
     * @param payload attestation JWT 载荷声明
     * @return 可信公钥 JWK，或 {@code null}
     */
    JWK resolveKey(String kid, Map<String, Object> header, Map<String, Object> payload);
}
