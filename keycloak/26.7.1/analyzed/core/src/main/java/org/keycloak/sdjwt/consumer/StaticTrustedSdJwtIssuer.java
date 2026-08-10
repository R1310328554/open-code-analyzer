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

import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.sdjwt.IssuerSignedJWT;

/**
 * 静态配置的受信任 SD-JWT 签发者，用于 SD-JWT VP 验证场景。
 *
 * <p>
 * 无论传入的签发者签名 JWT 为何，均返回构造时预先配置的验签密钥列表。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class StaticTrustedSdJwtIssuer implements TrustedSdJwtIssuer {

    /** 预配置的签名验证上下文列表。 */
    private final List<SignatureVerifierContext> signatureVerifierContexts;

    public StaticTrustedSdJwtIssuer(List<SignatureVerifierContext> signatureVerifierContexts) {
        this.signatureVerifierContexts = signatureVerifierContexts;
    }

    @Override
    public List<SignatureVerifierContext> resolveIssuerVerifyingKeys(IssuerSignedJWT issuerSignedJWT) {
        return signatureVerifierContexts;
    }
}
