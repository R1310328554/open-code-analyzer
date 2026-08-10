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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;
import org.keycloak.protocol.oid4vc.issuance.VCIssuanceContext;
import org.keycloak.protocol.oid4vc.issuance.VCIssuerException;
import org.keycloak.protocol.oid4vc.model.ErrorType;
import org.keycloak.protocol.oid4vc.model.KeyAttestationJwtBody;
import org.keycloak.protocol.oid4vc.model.KeyAttestationsRequired;
import org.keycloak.protocol.oid4vc.model.ProofTypesSupported;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.SupportedProofTypeData;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

import static org.keycloak.protocol.oid4vc.model.ProofType.JWT;
import static org.keycloak.services.clientpolicy.executor.FapiConstant.ALLOWED_ALGORITHMS;

/**
 * 按 OID4VCI 规范校验 key attestation JWT 的工具类。
 * <p>负责 JWS 头/载荷校验、x5c 链验证、抗抵赖级别匹配及 c_nonce 校验。</p>
 *
 * @author <a href="mailto:Rodrick.Awambeng@adorsys.com">Rodrick Awambeng</a>
 */
public class AttestationValidatorUtil {

    private static final Logger LOGGER = Logger.getLogger(AttestationValidatorUtil.class);

    /** attestation JWT 标准 typ 值。 */
    public static final String ATTESTATION_JWT_TYP = "key-attestation+jwt";
    /** 已弃用的旧版 typ，为向后兼容仍接受。 */
    @Deprecated
    public static final String LEGACY_ATTESTATION_JWT_TYP = "keyattestation+jwt";
    private static final String CACERTS_PATH = System.getProperty("javax.net.ssl.trustStore",
            System.getProperty("java.home") + "/lib/security/cacerts");
    private static final char[] DEFAULT_TRUSTSTORE_PASSWORD = System.getProperty(
            "javax.net.ssl.trustStorePassword", "changeit").toCharArray();

    /**
     * @param requireExpForJwtProof OID4VCI D.1：与 {@code jwt} proof 联用时 attestation 必须含 {@code exp}
     * @param proofTypeKeyForSigningAlgPolicy 用于解析 {@code proof_signing_alg_values_supported} 的
     *                                        {@link org.keycloak.protocol.oid4vc.model.ProofType}（{@code jwt} 或 {@code attestation}）；
     *                                        为 {@code null} 时仅强制 FAPI {@code ALLOWED_ALGORITHMS}
     */
    public static KeyAttestationJwtBody validateAttestationJwt(
            String attestationJwt,
            KeycloakSession keycloakSession,
            VCIssuanceContext vcIssuanceContext,
            AttestationKeyResolver keyResolver,
            boolean requireExpForJwtProof,
            String proofTypeKeyForSigningAlgPolicy)
            throws JWSInputException, VerificationException {

        if (attestationJwt == null || attestationJwt.split("\\.").length != 3) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Invalid JWT format");
        }

        JWSInput jwsInput = new JWSInput(attestationJwt);

        String payloadString = new String(jwsInput.getContent(), StandardCharsets.UTF_8);

        // 确认载荷为合法 JSON
        try {
            JsonSerialization.mapper.readTree(payloadString);
        } catch (JsonProcessingException e) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Invalid JSON in attestation payload: " + payloadString, e);
        }

        KeyAttestationJwtBody attestationBody;
        try {
            attestationBody = JsonSerialization.readValue(
                    jwsInput.getContent(),
                    KeyAttestationJwtBody.class
            );
        } catch (IOException e) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Invalid attestation payload format", e);
        }

        JWSHeader header = jwsInput.getHeader();
        validateJwsHeader(keycloakSession, header, vcIssuanceContext, proofTypeKeyForSigningAlgPolicy);

        // 校验 attestation JWT 签名
        Map<String, Object> rawHeader = JsonSerialization.mapper.convertValue(
                jwsInput.getHeader(), new TypeReference<>() {
                });

        SignatureVerifierContext verifier;
        if (header.getX5c() != null && !header.getX5c().isEmpty()) {
            verifier = verifierFromX5CChain(header.getX5c(), header.getAlgorithm().name(), keycloakSession);
        } else if (header.getKeyId() != null) {
            JWK resolvedJwk = keyResolver.resolveKey(header.getKeyId(), rawHeader,
                    JsonSerialization.mapper.convertValue(attestationBody, Map.class));
            if (resolvedJwk == null) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF, "Key with kid '" + header.getKeyId() + "' not found in trusted key registry");
            }
            verifier = verifierFromResolvedJWK(resolvedJwk, header.getAlgorithm().name(), keycloakSession);
        } else {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Neither x5c nor kid present in attestation JWT header");
        }

        if (!verifier.verify(jwsInput.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8),
                jwsInput.getSignature())) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Could not verify signature of attestation JWT");
        }

        validateAttestationPayload(keycloakSession, vcIssuanceContext, attestationBody, requireExpForJwtProof);

        if (attestationBody.getAttestedKeys() == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Missing required attested_keys claim in attestation");
        }

        return attestationBody;
    }

    private static void validateAttestationPayload(
            KeycloakSession keycloakSession,
            VCIssuanceContext vcIssuanceContext,
            KeyAttestationJwtBody attestationBody,
            boolean requireExpForJwtProof) throws VCIssuerException, VerificationException {

        if (attestationBody.getIat() == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Missing 'iat' claim in attestation");
        }

        long now = Time.currentTime();
        if (requireExpForJwtProof && attestationBody.getExp() == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "Missing 'exp' claim in key attestation (required when used with jwt proof type per OID4VCI D.1)");
        }
        if (attestationBody.getExp() != null && attestationBody.getExp() < now) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Key attestation has expired");
        }

        // 从凭证元数据读取 key_attestations_required 抗抵赖要求
        KeyAttestationsRequired attestationRequirements = getAttestationRequirements(vcIssuanceContext);
        validateResistanceLevel(attestationBody, attestationRequirements);

        KeycloakContext keycloakContext = keycloakSession.getContext();
        CNonceHandler cNonceHandler = keycloakSession.getProvider(CNonceHandler.class);

        if (attestationBody.getNonce() == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Missing 'nonce' in attestation");
        }

        // 校验 nonce：须为 Keycloak 此前签发的 c_nonce
        if (attestationBody.getNonce() != null) {
            try {
                cNonceHandler.verifyCNonce(
                        attestationBody.getNonce(),
                        List.of(OID4VCIssuerWellKnownProvider.getCredentialsEndpoint(keycloakContext)),
                        Map.of(JwtCNonceHandler.SOURCE_ENDPOINT,
                                OID4VCIssuerWellKnownProvider.getNonceEndpoint(keycloakContext))
                );
            } catch (VerificationException e) {
                throw new VCIssuerException(ErrorType.INVALID_NONCE,
                        "The key attestation uses an invalid nonce", e);
            }
        }

        // 将 attested_keys 写入发放上下文供后续 proof 绑定
        if (attestationBody.getAttestedKeys() != null) {
            vcIssuanceContext.setAttestedKeys(attestationBody.getAttestedKeys());
        }
    }

    private static KeyAttestationsRequired getAttestationRequirements(VCIssuanceContext vcIssuanceContext) {
        if (vcIssuanceContext.getCredentialConfig() == null ||
                vcIssuanceContext.getCredentialConfig().getProofTypesSupported() == null ||
                vcIssuanceContext.getCredentialConfig().getProofTypesSupported().getSupportedProofTypes() == null) {
            return null;
        }

        SupportedProofTypeData proofTypeData = vcIssuanceContext.getCredentialConfig()
                .getProofTypesSupported()
                .getSupportedProofTypes()
                .get(JWT);

        return proofTypeData != null ? proofTypeData.getKeyAttestationsRequired() : null;
    }

    /**
     * 将 attestation 体与元数据中的 {@code key_attestations_required} 配置比对。
     *
     * @param attestationBody 待校验的 attestation JWT 载荷
     * @param attestationRequirements 元数据端点暴露的配置对象
     */
    private static void validateResistanceLevel(KeyAttestationJwtBody attestationBody,
                                                KeyAttestationsRequired attestationRequirements) {
        // 若配置为 null 表示发行方不要求 key attestation，无需校验抗抵赖级别（见 OID4VCI 元数据规范）
        if (attestationRequirements != null) {
            // 校验 key_storage 抗抵赖级别
            validateResistanceLevel(attestationBody.getKeyStorage(),
                    attestationRequirements.getKeyStorage(),
                    "key_storage");
            // 校验 user_authentication 抗抵赖级别
            validateResistanceLevel(attestationBody.getUserAuthentication(),
                    attestationRequirements.getUserAuthentication(),
                    "user_authentication");
        }
    }

    /**
     * 将 attestation 提供的级别与元数据接受的级别比对。
     *
     * @param providedLevels attestation JWT 中声明的级别列表
     * @param acceptedLevels 元数据端点接受的级别列表
     * @param levelType {@code key_storage} 或 {@code user_authentication}
     * @throws VCIssuerException 未满足要求的抗抵赖级别时抛出
     */
    private static void validateResistanceLevel(List<String> providedLevels,
                                                List<String> acceptedLevels,
                                                String levelType)
            throws VCIssuerException {

        if (acceptedLevels == null || acceptedLevels.isEmpty()) {
            // 未配置接受列表时接受任意提供级别
            return;
        }

        // 规范允许空约束：仅需 attestation 但不要求具体 key_storage/user_authentication 级别
        if (providedLevels == null || providedLevels.isEmpty()) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, levelType + " is required but was missing.");
        }

        // 检查提供级别是否与接受列表有交集
        boolean foundMatch = providedLevels.stream().anyMatch(acceptedLevels::contains);
        if (!foundMatch) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    levelType + " none of the provided levels from '" + providedLevels + "' did match any of the " +
                            "accepted levels: " + acceptedLevels);
        }
    }

    /**
     * 从凭证配置解析指定 proof 类型的 {@code proof_signing_alg_values_supported}。
     */
    private static Optional<List<String>> resolveProofSigningAlgorithms(
            VCIssuanceContext vcIssuanceContext, String proofTypeKey) {
        return Optional.ofNullable(vcIssuanceContext)
                .filter(context -> proofTypeKey != null)
                .map(VCIssuanceContext::getCredentialConfig)
                .map(SupportedCredentialConfiguration::getProofTypesSupported)
                .map(ProofTypesSupported::getSupportedProofTypes)
                .map(supportedProofTypes -> supportedProofTypes.get(proofTypeKey))
                .map(SupportedProofTypeData::getSigningAlgorithmsSupported)
                .filter(algs -> !algs.isEmpty());
    }

    private static void validateJwsHeader(KeycloakSession session, JWSHeader header, VCIssuanceContext vcIssuanceContext,
                                          String proofTypeKeyForSigningAlgPolicy) {
        String alg = Optional.ofNullable(header.getAlgorithm())
                .map(Algorithm::name)
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "Missing algorithm in JWS header"));

        List<String> supportedAsymmetricAlgs = CryptoUtils.getSupportedAsymmetricSignatureAlgorithms(session);
        if (!supportedAsymmetricAlgs.contains(alg)) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Unsupported algorithm for key attestation. Only asymmetric algorithms are allowed");
        }

        Optional<List<String>> metadataAlgs = resolveProofSigningAlgorithms(vcIssuanceContext, proofTypeKeyForSigningAlgPolicy);
        if (metadataAlgs.isPresent() && !metadataAlgs.get().isEmpty()) {
            if (!metadataAlgs.get().contains(alg)) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF,
                        "Attestation alg must match proof_signing_alg_values_supported for proof type "
                                + proofTypeKeyForSigningAlgPolicy + ": " + metadataAlgs.get());
            }
        } else if (!ALLOWED_ALGORITHMS.contains(alg)) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Unsupported algorithm: " + alg +
                    ". Allowed algorithms: " + ALLOWED_ALGORITHMS);
        }

        String typ = Optional.ofNullable(header.getType())
                .map(Object::toString)
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "Missing typ in JWS header"));

        if (!ATTESTATION_JWT_TYP.equals(typ)) {
            if (LEGACY_ATTESTATION_JWT_TYP.equals(typ)) {
                LOGGER.debugf("Accepting deprecated attestation JWT typ '%s' for backward compatibility", typ);
            } else {
                throw new VCIssuerException(ErrorType.INVALID_PROOF, "Invalid JWT typ: expected " + ATTESTATION_JWT_TYP);
            }
        }
    }

    private static SignatureVerifierContext verifierFromX5CChain(
            List<String> x5cList,
            String alg,
            KeycloakSession keycloakSession) throws VCIssuerException, VerificationException {
        JWK certJwk = resolveJwkFromValidatedX5c(x5cList, alg);
        return verifierFromResolvedJWK(certJwk, alg, keycloakSession);
    }

    /**
     * 校验 x5c 证书链并将叶子证书公钥转为 JWK。
     * <p>可被接受 x5c 作为证明密钥来源的 proof 校验器复用。</p>
     */
    static JWK resolveJwkFromValidatedX5c(List<String> x5cList, String alg) throws VCIssuerException {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certChain = new ArrayList<>();

            for (String certBase64 : x5cList) {
                // 使用 Base64 解码 x5c 中的 DER 证书
                byte[] certBytes = Base64.getMimeDecoder().decode(certBase64);
                try (InputStream in = new ByteArrayInputStream(certBytes)) {
                    certChain.add((X509Certificate) cf.generateCertificate(in));
                }
            }

            // 构建 CertPath 供 PKIX 校验
            CertPath certPath = cf.generateCertPath(certChain);

            // 拒绝自签名证书用于 key attestation
            X509Certificate firstCert = certChain.get(0);
            boolean isSelfSigned = firstCert.getSubjectX500Principal().equals(firstCert.getIssuerX500Principal());

            if (isSelfSigned) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF,
                        "Self-signed certificates are not accepted for key attestation");
            }

            // 使用系统信任库做 PKIX 链验证
            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(getTrustAnchors());
            params.setRevocationEnabled(false);

            validator.validate(certPath, params);

            // 取链首（叶子）证书公钥
            PublicKey publicKey = certChain.get(0).getPublicKey();
            return convertPublicKeyToJWK(publicKey, alg, certChain);

        } catch (Exception e) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Failed to validate x5c certificate chain", e);
        }
    }

    private static Set<TrustAnchor> getTrustAnchors() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = new FileInputStream(CACERTS_PATH)) {
            trustStore.load(in, DEFAULT_TRUSTSTORE_PASSWORD);
        }

        Set<TrustAnchor> anchors = new HashSet<>();
        Enumeration<String> aliases = trustStore.aliases();
        while (aliases.hasMoreElements()) {
            Certificate cert = trustStore.getCertificate(aliases.nextElement());
            if (cert instanceof X509Certificate) {
                anchors.add(new TrustAnchor((X509Certificate) cert, null));
            }
        }
        return anchors;
    }

    private static SignatureVerifierContext verifierFromResolvedJWK(
            JWK jwk,
            String alg,
            KeycloakSession session
    ) throws VerificationException {

        SignatureProvider provider = session.getProvider(SignatureProvider.class, alg);
        KeyWrapper wrapper = new KeyWrapper();
        wrapper.setType(jwk.getKeyType());
        wrapper.setAlgorithm(alg);
        wrapper.setUse(KeyUse.SIG);

        if (jwk.getOtherClaims().get("crv") != null) {
            wrapper.setCurve((String) jwk.getOtherClaims().get("crv"));
        }

        wrapper.setPublicKey(JWKParser.create(jwk).toPublicKey());
        return provider.verifier(wrapper);
    }

    private static JWK convertPublicKeyToJWK(
            PublicKey key,
            String alg,
            List<X509Certificate> certChain
    ) {
        if (key instanceof RSAPublicKey rsa) {
            return JWKBuilder.create().algorithm(alg).rsa(rsa, certChain);
        } else if (key instanceof ECPublicKey ec) {
            return JWKBuilder.create().algorithm(alg).ec(ec, certChain, null);
        } else {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Unsupported public key type in certificate: " + key.getClass().getName());
        }
    }
}
