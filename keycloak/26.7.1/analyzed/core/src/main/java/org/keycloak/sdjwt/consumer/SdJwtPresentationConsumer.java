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

import java.util.ArrayList;
import java.util.List;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.IssuerSignedJwtVerificationOpts;
import org.keycloak.sdjwt.vp.KeyBindingJwtVerificationOpts;
import org.keycloak.sdjwt.vp.SdJwtVP;

/**
 * SD-JWT 演示消费（验证）组件。
 *
 * <p>
 * 在 {@link org.keycloak.sdjwt.SdJwtVerificationContext} 的签名与披露检查之上，
 * 简化 SD-JWT VP 验证流程。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class SdJwtPresentationConsumer {

    /**
     * 根据特定要求验证 SD-JWT 演示。
     *
     * @param sdJwtVP                         待验证的演示
     * @param presentationRequirements        对演示声明的要求
     * @param trustedSdJwtIssuers             验证所信任的签发者
     * @param issuerSignedJwtVerificationOpts 签发者签名 JWT 验证策略
     * @param keyBindingJwtVerificationOpts   密钥绑定 JWT 验证策略
     * @throws VerificationException 验证因某种原因失败时
     */
    public void verifySdJwtPresentation(
            SdJwtVP sdJwtVP,
            PresentationRequirements presentationRequirements,
            List<TrustedSdJwtIssuer> trustedSdJwtIssuers,
            IssuerSignedJwtVerificationOpts issuerSignedJwtVerificationOpts,
            KeyBindingJwtVerificationOpts keyBindingJwtVerificationOpts
    ) throws VerificationException {
        // 获取签发者签名 JWT 的验证公钥
        IssuerSignedJWT issuerSignedJWT = sdJwtVP.getIssuerSignedJWT();
        List<SignatureVerifierContext> issuerVerifyingKeys = new ArrayList<>();
        for (TrustedSdJwtIssuer trustedSdJwtIssuer : trustedSdJwtIssuers) {
            List<SignatureVerifierContext> keys = trustedSdJwtIssuer
                    .resolveIssuerVerifyingKeys(issuerSignedJWT);
            issuerVerifyingKeys.addAll(keys);
        }

        // 对 SD-JWT 令牌进行密码学验证
        // 传入演示要求以强制演示令牌满足这些要求
        sdJwtVP.getSdJwtVerificationContext()
                .verifyPresentation(
                        issuerVerifyingKeys,
                        issuerSignedJwtVerificationOpts,
                        keyBindingJwtVerificationOpts,
                        presentationRequirements
                );
    }
}
