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

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.model.ProofType;

/**
 * 创建 {@link JwtProofValidator} 的工厂，proof 类型为 {@link ProofType#JWT}。
 * <p>默认注入 {@link TrustedAttestationKeyResolver} 解析 attestation/kid 密钥。</p>
 */
public class JwtProofValidatorFactory implements ProofValidatorFactory {

    /** @return proof 类型 {@link ProofType#JWT} */
    @Override
    public String getId() {
        return ProofType.JWT;
    }

    /** 创建绑定可信密钥解析器的 JWT proof 校验器。 */
    @Override
    public JwtProofValidator create(KeycloakSession session) {
        AttestationKeyResolver keyResolver = new TrustedAttestationKeyResolver(session);
        return new JwtProofValidator(session, keyResolver);
    }
}
