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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureProviderFactory;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;
import org.keycloak.protocol.oid4vc.issuance.VCIssuanceContext;
import org.keycloak.protocol.oid4vc.issuance.VCIssuerException;
import org.keycloak.protocol.oid4vc.model.CredentialRequest;
import org.keycloak.protocol.oid4vc.model.ErrorType;
import org.keycloak.protocol.oid4vc.model.ProofType;
import org.keycloak.protocol.oid4vc.model.ProofTypesSupported;
import org.keycloak.protocol.oid4vc.model.Proofs;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.SupportedProofTypeData;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

/**
 * 校验客户端提交的 JWT 类型 cryptographic binding proof 的合规性与真实性。
 * <p>支持 jwk/kid/x5c 密钥来源、可选 key_attestation 头及 c_nonce 校验。</p>
 *
 * @see "https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-jwt-proof-type"
 */
public class JwtProofValidator extends AbstractProofValidator {

    private static final Logger LOGGER = Logger.getLogger(JwtProofValidator.class);

    /** JWT proof 要求的 typ 值。 */
    public static final String PROOF_JWT_TYP = "openid4vci-proof+jwt";
    /** 当前实现支持的密码学绑定方法。 */
    private static final String CRYPTOGRAPHIC_BINDING_METHOD_JWK = "jwk";
    /** JWS 头中 key attestation JWT 的声明名。 */
    private static final String KEY_ATTESTATION_CLAIM = "key_attestation";
    
    /** proof iat 允许的最大年龄（秒）。 */
    private static final int PROOF_MAX_AGE_SECONDS = 30;
    /** 允许的未来时钟偏差（秒）。 */
    private static final int PROOF_FUTURE_SKEW_SECONDS = 10;
    /** 解析 kid 对应可信公钥的解析器。 */
    private final AttestationKeyResolver keyResolver;

    public JwtProofValidator(KeycloakSession keycloakSession, AttestationKeyResolver keyResolver) {
        super(keycloakSession);
        this.keyResolver = keyResolver;
    }

    @Override
    public String getProofType() {
        return ProofType.JWT;
    }

    @Override
    public List<JWK> validateProof(VCIssuanceContext vcIssuanceContext) throws VCIssuerException {
        try {
            return validateJwtProof(vcIssuanceContext);
        } catch (JWSInputException | VerificationException | IOException e) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Could not validate JWT proof", e);
        }
    }

    /*
     * 校验客户端提交的 JWT proof 数组。
     * <p>若凭证配置不要求 proof 则返回 {@code null}；否则返回校验通过的 JWK 列表用于密钥绑定。</p>
     *
     * @param vcIssuanceContext 凭证发放上下文
     * @return 绑定公钥 JWK 列表，或 {@code null}
     * @throws VCIssuerException proof 无效或配置不匹配
     * @throws JWSInputException JWT 解析失败
     * @throws VerificationException 签名或 nonce 校验失败
     * @throws IllegalStateException 凭证类型配置错误
     * @throws IOException 载荷反序列化失败
     */
    private List<JWK> validateJwtProof(VCIssuanceContext vcIssuanceContext) throws VCIssuerException, JWSInputException, VerificationException, IOException {

        Optional<List<String>> optionalProof = getProofFromContext(vcIssuanceContext);

        if (optionalProof.isEmpty() || optionalProof.get().isEmpty()) {
            return null; // No proof support
        }

        List<String> jwtProofs = optionalProof.get();

        // 检查 JWT 密码学绑定配置（当前仅支持 jwk 方法）
        checkCryptographicKeyBinding(vcIssuanceContext);

        // 逐条校验 proofs.jwt 数组中的 JWT
        List<JWK> validJwks = new ArrayList<>();

        for (int i = 0; i < jwtProofs.size(); i++) {
            String jwt = jwtProofs.get(i);
            JWK jwk = validateSingleJwtProof(vcIssuanceContext, jwt);
            validJwks.add(jwk);
            LOGGER.debugf("Successfully validated JWT proof at index %d", i);
        }

        if (validJwks.isEmpty()) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "No valid JWT proof found in the proofs array");
        }

        LOGGER.debugf("Successfully validated %d JWT proofs", validJwks.size());
        return validJwks;
    }

    private JWK validateSingleJwtProof(VCIssuanceContext vcIssuanceContext, String jwt) throws VCIssuerException, JWSInputException, VerificationException, IOException {
        JWSInput jwsInput = getJwsInput(jwt);
        JWSHeader jwsHeader = jwsInput.getHeader();
        validateJwsHeader(vcIssuanceContext, jwsHeader);

        // 解析原始 JOSE 头以便一致处理可选 key_attestation
        Map<String, Object> headerClaims = JsonSerialization.mapper.convertValue(jwsHeader,
                new TypeReference<>() {
                });
        String algorithm = jwsHeader.getAlgorithm().name();
        validateNoPrivateKeyInHeaderClaims(algorithm, headerClaims);
        KeyAttestationInfo attestationInfo = resolveHeaderAttestation(vcIssuanceContext, headerClaims);

        // 从 jwk/kid/x5c 解析 proof 签名公钥
        JWK jwk;
        if (jwsHeader.getKey() != null) {
            jwk = jwsHeader.getKey();
        } else if (jwsHeader.getKeyId() != null) {
            if (attestationInfo.isPresent()) {
                List<JWK> attestedKeys = attestationInfo.attestedKeys();

                // 从 attestation 的 attested_keys 中按 kid 匹配
                jwk = attestedKeys.stream()
                        .filter(k -> jwsHeader.getKeyId().equals(k.getKeyId()))
                        .findFirst()
                        .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF,
                                "No attested key found matching kid: " + jwsHeader.getKeyId()));
            } else {
                jwk = keyResolver.resolveKey(jwsHeader.getKeyId(), headerClaims, Map.of());
                if (jwk == null) {
                    throw new VCIssuerException(ErrorType.INVALID_PROOF,
                            "No trusted key found matching kid: " + jwsHeader.getKeyId());
                }
            }
        } else if (jwsHeader.getX5c() != null && !jwsHeader.getX5c().isEmpty()) {
            jwk = AttestationValidatorUtil.resolveJwkFromValidatedX5c(jwsHeader.getX5c(), jwsHeader.getAlgorithm().name());
        } else {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Missing binding key. JWT must contain either jwk, kid, or x5c in header.");
        }

        // 若存在 key_attestation，proof 公钥须属于 attested_keys
        if (attestationInfo.isPresent()) {
            boolean attested = attestationInfo.attestedKeys().stream()
                    .anyMatch(attestedKey -> jwkMaterialEquals(attestedKey, jwk));
            if (!attested) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF,
                        "JWT proof key is not included in attested_keys");
            }
        }

        // 校验载荷声明并验证 JWS 签名
        AccessToken proofPayload = JsonSerialization.readValue(jwsInput.getContent(), AccessToken.class);
        validateProofPayload(vcIssuanceContext, proofPayload);

        SignatureVerifierContext signatureVerifierContext = getVerifier(jwk, jwsHeader.getAlgorithm().name());
        if (signatureVerifierContext == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "No verifier configured for " + jwsHeader.getAlgorithm());
        }
        if (!signatureVerifierContext.verify(jwsInput.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8),
                jwsInput.getSignature())) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Could not verify signature of provided proof");
        }

        return jwk;
    }

    private void checkCryptographicKeyBinding(VCIssuanceContext vcIssuanceContext) {
        // 元数据未声明绑定方法时不强制 JWT proof；但若客户端仍提交 proof 则拒绝
        if (vcIssuanceContext.getCredentialConfig().getCryptographicBindingMethodsSupported() == null ||
                vcIssuanceContext.getCredentialConfig().getCryptographicBindingMethodsSupported().isEmpty()) {
            return;
        }

        // 需要绑定时当前实现仅支持 jwk 方法
        if (!vcIssuanceContext.getCredentialConfig().getCryptographicBindingMethodsSupported()
                .contains(CRYPTOGRAPHIC_BINDING_METHOD_JWK)) {
            throw new IllegalStateException("This SD-JWT implementation only supports jwk as cryptographic binding method");
        }
    }

    private Optional<List<String>> getProofFromContext(VCIssuanceContext vcIssuanceContext) throws VCIssuerException {
        SupportedCredentialConfiguration config = vcIssuanceContext.getCredentialConfig();
        if (config == null) {
            return Optional.empty();
        }
        ProofTypesSupported proofTypesSupported = config.getProofTypesSupported();
        CredentialRequest credentialRequest = vcIssuanceContext.getCredentialRequest();
        Proofs proofs = credentialRequest != null ? credentialRequest.getProofs() : null;

        // 未配置 proof_types 时不强制 proof；但若客户端仍提交 JWT proof 则显式拒绝
        if (proofTypesSupported == null
                || proofTypesSupported.getSupportedProofTypes() == null
                || proofTypesSupported.getSupportedProofTypes().isEmpty()) {
            if (proofs != null && proofs.getJwt() != null && !proofs.getJwt().isEmpty()) {
                throw new VCIssuerException(
                        ErrorType.INVALID_PROOF,
                        "Proof type " + ProofType.JWT + " is not supported for this credential configuration"
                );
            }
            return Optional.empty();
        }

        Map<String, SupportedProofTypeData> supportedProofTypes = proofTypesSupported.getSupportedProofTypes();
        Optional.ofNullable(supportedProofTypes.get(ProofType.JWT))
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "SD-JWT supports only jwt proof type."));

        // 已声明支持 JWT proof 时必须提供有效 proof
        if (proofs == null || proofs.getJwt() == null || proofs.getJwt().isEmpty()) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Credential configuration requires a proof of type: " + ProofType.JWT);
        }

        return Optional.of(proofs.getJwt());
    }

    private JWSInput getJwsInput(String jwt) throws JWSInputException {
        return new JWSInput(jwt);
    }

    /**
     * 校验 JWS 头：alg、typ、互斥的 jwk/kid/x5c 及 trust_chain 拒绝。
     * <p>算法须为发行方支持的非对称算法，且 typ 须为 {@link #PROOF_JWT_TYP}。</p>
     *
     * @param vcIssuanceContext 发放上下文（含 proof 签名算法白名单）
     * @param jwsHeader 待校验的 JWS 头
     * @throws VCIssuerException 头字段不合规
     */
    private void validateJwsHeader(VCIssuanceContext vcIssuanceContext, JWSHeader jwsHeader) throws VCIssuerException {
        String alg = Optional.ofNullable(jwsHeader.getAlgorithm())
                .map(Enum::name)
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "Missing jwsHeader claim alg"));
        if (!CryptoUtils.getSupportedAsymmetricSignatureAlgorithms(keycloakSession).contains(alg)) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Proof signature algorithm not supported: " + alg);
        }

        // 算法已在白名单约束，无需单独拒绝 alg=none
        Optional.ofNullable(vcIssuanceContext.getCredentialConfig())
                .map(SupportedCredentialConfiguration::getProofTypesSupported)
                .map(ProofTypesSupported::getSupportedProofTypes)
                .map(proofTypeData -> proofTypeData.get("jwt"))
                .map(SupportedProofTypeData::getSigningAlgorithmsSupported)
                .filter(supportedAlgs -> supportedAlgs.contains(alg))
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "Proof signature algorithm not supported: " + alg));

        Optional.ofNullable(jwsHeader.getType())
                .filter(type -> Objects.equals(PROOF_JWT_TYP, type))
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "JWT type must be: " + PROOF_JWT_TYP));

        boolean hasJwk = jwsHeader.getKey() != null;
        boolean hasKid = jwsHeader.getKeyId() != null;
        boolean hasX5c = jwsHeader.getX5c() != null && !jwsHeader.getX5c().isEmpty();

        int presentKeyHeaders = (hasJwk ? 1 : 0) + (hasKid ? 1 : 0) + (hasX5c ? 1 : 0);
        if (presentKeyHeaders > 1) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Header claims kid, jwk, and x5c are mutually exclusive");
        }

        // OID4VCI F.1：未实现 trust_chain（OpenID Federation），显式拒绝
        if (jwsHeader.getOtherClaims() != null && jwsHeader.getOtherClaims().get("trust_chain") != null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "trust_chain JOSE header is not supported");
        }

    }

    private KeyAttestationInfo resolveHeaderAttestation(VCIssuanceContext vcIssuanceContext, Map<String, Object> headerClaims)
            throws JWSInputException, VerificationException {
        if (!headerClaims.containsKey(KEY_ATTESTATION_CLAIM)) {
            return KeyAttestationInfo.absent();
        }

        Object keyAttestation = headerClaims.get(KEY_ATTESTATION_CLAIM);
        if (keyAttestation == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "The 'key_attestation' claim is present in JWT header but is null.");
        }

        List<JWK> attestedKeys = AttestationValidatorUtil.validateAttestationJwt(
                keyAttestation.toString(),
                keycloakSession,
                vcIssuanceContext,
                keyResolver,
                true,
                ProofType.JWT).getAttestedKeys();
        if (attestedKeys == null || attestedKeys.isEmpty()) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "key_attestation does not contain attested keys");
        }

        return new KeyAttestationInfo(attestedKeys);
    }

    private record KeyAttestationInfo(List<JWK> attestedKeys) {

        static KeyAttestationInfo absent() {
            return new KeyAttestationInfo(List.of());
        }

        boolean isPresent() {
            return !attestedKeys.isEmpty();
        }
    }

    /**
     * 比较公钥材料而非对象引用，以便在缺少 kid 时仍能正确匹配 attested_keys。
     */
    private boolean jwkMaterialEquals(JWK left, JWK right) {
        if (left == null || right == null) {
            return false;
        }
        if (!Objects.equals(left.getKeyType(), right.getKeyType())) {
            return false;
        }

        try {
            PublicKey leftPublicKey = JWKParser.create(left).toPublicKey();
            PublicKey rightPublicKey = JWKParser.create(right).toPublicKey();
            return Objects.equals(leftPublicKey.getAlgorithm(), rightPublicKey.getAlgorithm())
                    && Arrays.equals(leftPublicKey.getEncoded(), rightPublicKey.getEncoded());
        } catch (RuntimeException e) {
            // 无法解析为公钥时视为不匹配，由调用方返回 INVALID_PROOF
            return false;
        }
    }

    /**
     * 拒绝 JWK 头声明中包含私钥材料的 proof。
     */
    void validateNoPrivateKeyInHeaderClaims(String algorithm, Map<String, Object> headerClaims) {
        Object jwkClaim = headerClaims.get("jwk");
        if (!(jwkClaim instanceof Map<?, ?> jwkMap)) {
            return;
        }

        Object factory = keycloakSession.getKeycloakSessionFactory()
                .getProviderFactory(SignatureProvider.class, algorithm);

        Set<String> privateClaimNames = getPrivateClaimNames(algorithm, factory);

        for (String privateClaim : privateClaimNames) {
            if (jwkMap.containsKey(privateClaim)) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF,
                        "JWK header must not contain private key material claim: " + privateClaim);
            }
        }
    }

    private static Set<String> getPrivateClaimNames(String algorithm, Object factory) {
        if (!(factory instanceof SignatureProviderFactory signatureProviderFactory)) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "No SignatureProviderFactory found for algorithm: " + algorithm);
        }

        Set<String> privateClaimNames = signatureProviderFactory.getJwkPrivateKeyClaims();

        if (privateClaimNames.isEmpty()) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "SignatureProviderFactory for algorithm " + algorithm + " does not define private JWK claims");
        }
        return privateClaimNames;
    }

    private void validateProofPayload(VCIssuanceContext vcIssuanceContext, AccessToken proofPayload)
            throws VCIssuerException, VerificationException {
        AuthenticationManager.AuthResult authResult = vcIssuanceContext.getAuthResult();
        AccessToken requestToken = authResult != null ? authResult.token() : null;
        String expectedClientId = requestToken != null ? requestToken.getIssuedFor() : null;
        String proofIssuer = proofPayload.getIssuer();

        // OID4VCI F.1：客户端绑定流 iss 可选但须匹配 client_id；匿名流不得含 iss
        if (expectedClientId == null || expectedClientId.isBlank()) {
            if (proofIssuer != null) {
                throw new VCIssuerException(ErrorType.INVALID_PROOF, "Issuer claim must be omitted for anonymous flow");
            }
        } else if (proofIssuer != null && !Objects.equals(expectedClientId, proofIssuer)) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "Issuer claim must be the client_id of the request: " + expectedClientId);
        }

        // proof 的 aud 必须为凭证发行方标识符（单值）
        String credentialIssuer = OID4VCIssuerWellKnownProvider.getIssuer(keycloakSession.getContext());
        String[] audiences = Optional.ofNullable(proofPayload.getAudience())
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF,
                        "Proof not produced for this audience. Audience claim must be: " + credentialIssuer + " but is missing"));
        if (audiences.length != 1 || !Objects.equals(credentialIssuer, audiences[0])) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF,
                    "Proof not produced for this audience. Audience claim must be single value: " + credentialIssuer + " but are " + Arrays.asList(audiences));
        }

        // 校验必填 iat 及 exp/nbf 时间窗口
        Long iat = Optional.ofNullable(proofPayload.getIat())
                .orElseThrow(() -> new VCIssuerException(ErrorType.INVALID_PROOF, "Missing proof issuing time. iat claim must be provided."));
        long now = Time.currentTime();
        if (iat < now - PROOF_MAX_AGE_SECONDS) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Proof iat is too old");
        }
        if (iat > now + PROOF_FUTURE_SKEW_SECONDS) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Proof iat is in the future beyond allowed clock skew");
        }
        if (proofPayload.getExp() != null && proofPayload.getExp() < now) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Proof has expired");
        }
        if (proofPayload.getNbf() != null && proofPayload.getNbf() > now + PROOF_FUTURE_SKEW_SECONDS) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "Proof is not yet valid");
        }

        KeycloakContext keycloakContext = keycloakSession.getContext();
        CNonceHandler cNonceHandler = keycloakSession.getProvider(CNonceHandler.class);
        if (cNonceHandler == null) {
            throw new VCIssuerException(ErrorType.INVALID_PROOF, "CNonce handler not configured");
        }
        try {
            cNonceHandler.verifyCNonce(proofPayload.getNonce(),
                    List.of(OID4VCIssuerWellKnownProvider.getCredentialsEndpoint(keycloakContext)),
                    Map.of(JwtCNonceHandler.SOURCE_ENDPOINT,
                            OID4VCIssuerWellKnownProvider.getNonceEndpoint(keycloakContext)));
        } catch (VerificationException e) {
            throw new VCIssuerException(ErrorType.INVALID_NONCE, e.getMessage());
        }
    }
}
