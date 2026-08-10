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

import java.util.List;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.protocol.oid4vc.issuance.VCIssuanceContext;
import org.keycloak.protocol.oid4vc.issuance.VCIssuerException;
import org.keycloak.provider.Provider;

/**
 * OID4VCI 密钥绑定证明（Proof）校验器 Provider 接口。
 * <p>各实现按 {@link #getProofType()} 声明支持的 proof 类型，在凭证签发前校验钱包提交的密钥绑定材料，并返回待绑定到凭证的 {@link JWK} 列表。</p>
 */
public interface ProofValidator extends Provider {

    /** {@inheritDoc} 默认无资源需释放。 */
    @Override
    default void close() {
    }

    /** @return 本校验器支持的 proof 类型标识（如 {@code jwt}、{@code attestation}） */
    String getProofType();

    /**
     * 校验客户端提交的密钥绑定证明。
     *
     * @param vcIssuanceContext 包含凭证请求与配置的签发上下文
     * @return 待绑定到凭证的 JWK 列表（每项凭证对应一个 JWK）
     */
    List<JWK> validateProof(VCIssuanceContext vcIssuanceContext) throws VCIssuerException;
}
