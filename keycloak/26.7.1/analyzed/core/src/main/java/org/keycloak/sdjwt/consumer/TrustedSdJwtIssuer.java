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

package org.keycloak.sdjwt.consumer;

import java.util.List;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.sdjwt.IssuerSignedJWT;

/**
 * 受信任的 SD-JWT 签发者接口，用于 SD-JWT VP 验证流程。
 *
 * <p>
 * 实现类负责解析并返回可用于验证签发者签名 JWT 的公钥，
 * 并确保这些密钥来自可信来源。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public interface TrustedSdJwtIssuer {

    /**
     * 解析用于验证签发者签名 JWT 的公钥。
     * 返回的公钥必须来自受信任的来源。
     *
     * @param issuerSignedJWT 待验证的签发者签名 JWT。
     * @return 受信任的验签密钥列表
     * @throws VerificationException 若无法解析到可信的验签密钥
     */
    List<SignatureVerifierContext> resolveIssuerVerifyingKeys(IssuerSignedJWT issuerSignedJWT)
            throws VerificationException;
}
