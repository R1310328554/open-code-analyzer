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

import org.jboss.logging.Logger;

/**
 * 基于内存可信密钥映射表的 {@link AttestationKeyResolver} 静态实现。
 * <p>适用于测试或预配置场景：按 {@code kid} 从构造时注入的映射中查找 JWK。</p>
 *
 * @author <a href="mailto:Rodrick.Awambeng@adorsys.com">Rodrick Awambeng</a>
 */
public class StaticAttestationKeyResolver implements AttestationKeyResolver {
    private static final Logger logger = Logger.getLogger(StaticAttestationKeyResolver.class);
    /** kid → JWK 的可信密钥注册表。 */
    private final Map<String, JWK> trustedKeys;

    /**
     * @param trustedKeys 预置的可信 attestation 密钥映射
     */
    public StaticAttestationKeyResolver(Map<String, JWK> trustedKeys) {
        this.trustedKeys = trustedKeys;
    }

    /**
     * {@inheritDoc}
     * <p>在静态映射中按 {@code kid} 查找；未命中时记录警告并返回 {@code null}。</p>
     */
    @Override
    public JWK resolveKey(String kid, Map<String, Object> header, Map<String, Object> payload) {
        JWK key = trustedKeys.get(kid);
        if (key == null) {
            logger.warnf("Key with kid '%s' not found in trusted static key registry", kid);
        }
        return key;
    }
}
