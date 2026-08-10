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

package org.keycloak.protocol.oid4vc.issuance.signing;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import javax.security.auth.x500.X500Principal;

import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBody;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.SdJwtCredentialBody;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;

import org.jboss.logging.Logger;

/**
 * 实现 SD-JWT VC（{@code sd_jwt_vc}）格式的 {@link CredentialSigner}。
 * <p>返回已签名的 SD-JWT 字符串；按 HAIP-6.1.1 在头中附加 x5c 证书链（若可用）。</p>
 * {@see https://drafts.oauth.net/oauth-sd-jwt-vc/draft-ietf-oauth-sd-jwt-vc.html}
 * {@see https://www.ietf.org/archive/id/draft-fett-oauth-selective-disclosure-jwt-02.html}
 */
public class SdJwtCredentialSigner extends AbstractCredentialSigner<String> {

    private static final Logger LOGGER = Logger.getLogger(SdJwtCredentialSigner.class);

    /** @param keycloakSession 当前 Keycloak 会话 */
    public SdJwtCredentialSigner(KeycloakSession keycloakSession) {
        super(keycloakSession);
    }

    @Override
    public String signCredential(CredentialBody credentialBody, CredentialBuildConfig credentialBuildConfig)
            throws CredentialSignerException {
        if (!(credentialBody instanceof SdJwtCredentialBody sdJwtCredentialBody)) {
            throw new CredentialSignerException("Credential body unexpectedly not of type SdJwtCredentialBody");
        }

        LOGGER.debugf("Sign credentials to sd-jwt format.");

        // 先解析签名器，确保 x5c 与最终签名使用同一密钥
        SignatureSignerContext signer = getSigner(credentialBuildConfig);

        // 若存在证书链则写入 x5c 头（HAIP-6.1.1 要求）
        addX5cHeader(sdJwtCredentialBody, signer);

        return sdJwtCredentialBody.sign(signer);
    }

    /**
     * 若签名器携带 X.509 证书链，将其写入 IssuerSignedJWT 头的 x5c 字段。
     * <p>遵循 Keycloak 惯例：x5c 与签名密钥一致，满足 HAIP-6.1.1 发行方标识与密钥解析要求。</p>
     * <p>
     * 参见 <a href="https://openid.github.io/OpenID4VC-HAIP/openid4vc-high-assurance-interoperability-profile-wg-draft.html#section-6.1.1">HAIP 6.1.1</a>
     *
     * @param sdJwtCredentialBody 待附加 x5c 的 SD-JWT 凭证体
     * @param signer              含签名密钥证书链的签名上下文
     */
    private void addX5cHeader(SdJwtCredentialBody sdJwtCredentialBody, SignatureSignerContext signer) {
        List<X509Certificate> certificateChain = signer.getCertificateChain();
        if (certificateChain != null && !certificateChain.isEmpty()) {
            List<String> x5cList = certificateChain.stream()
                    .filter(Objects::nonNull)
                    .filter(cert -> !isTrustAnchor(cert))
                    .map(cert -> {
                        try {
                            return Base64.getEncoder().encodeToString(cert.getEncoded());
                        } catch (CertificateEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            if (!x5cList.isEmpty()) {
                sdJwtCredentialBody.getIssuerSignedJWT().getJwsHeader().setX5c(x5cList);
            } else {
                LOGGER.debugf("No valid certificates found in certificate chain for x5c header in SD-JWT credential.");
            }
        } else {
            LOGGER.debugf("No certificate or certificate chain available for x5c header in SD-JWT credential.");
        }
    }

    private boolean isTrustAnchor(X509Certificate cert) {
        boolean isTrustAnchor = false;
        try {
            int basicConstraints = cert.getBasicConstraints();
            X500Principal issuerPrincipal = cert.getIssuerX500Principal();
            X500Principal subjectPrincipal = cert.getSubjectX500Principal();
            isTrustAnchor = subjectPrincipal.equals(issuerPrincipal) && basicConstraints >= 0;
        } catch (Exception e) {
            // 解析基本约束失败时视为非信任锚
        }
        return isTrustAnchor;
    }
}
