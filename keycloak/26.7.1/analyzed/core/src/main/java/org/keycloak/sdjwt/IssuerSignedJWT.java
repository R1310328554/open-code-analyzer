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
package org.keycloak.sdjwt;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.OID4VCConstants;
import org.keycloak.VCFormat;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_CNF;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_JWK;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM;

/**
 * 可验证凭证（SD-JWT VC）处理器，支持解析已有 VC 以及创建与签发新 VC。
 * 集成 Keycloak 的 {@link SignatureSignerContext} 以生成签发者签名。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class IssuerSignedJWT extends JwsToken {

    /** 选择性披露规格。 */
    private DisclosureSpec disclosureSpec;

    /** 披露声明列表。 */
    private List<SdJwtClaim> disclosureClaims;

    /** 诱饵声明列表。 */
    private List<DecoyClaim> decoyClaims;


    public IssuerSignedJWT(JWSHeader jwsHeader,
                           ObjectNode payload) {
        super(jwsHeader, payload);
        this.disclosureSpec = null;
        this.disclosureClaims = new ArrayList<>();
        this.decoyClaims = new ArrayList<>();
    }

    public IssuerSignedJWT(String jwsString) {
        super(jwsString);
        this.disclosureSpec = null;
        this.disclosureClaims = new ArrayList<>();
        this.decoyClaims = new ArrayList<>();
    }

    protected IssuerSignedJWT(DisclosureSpec disclosureSpec,
                           JWSHeader jwsHeader,
                           List<SdJwtClaim> disclosureClaims,
                           List<DecoyClaim> decoyClaims,
                           String hashAlg,
                           boolean nestedDisclosures) {
        super(jwsHeader, generatePayloadString(disclosureClaims, decoyClaims, hashAlg, nestedDisclosures));
        this.disclosureSpec = disclosureSpec;
        this.disclosureClaims = disclosureClaims;
        this.decoyClaims = decoyClaims;
    }

    protected IssuerSignedJWT(DisclosureSpec disclosureSpec,
                           JWSHeader jwsHeader,
                           List<SdJwtClaim> disclosureClaims,
                           List<DecoyClaim> decoyClaims,
                           String hashAlg,
                           boolean nestedDisclosures,
                           SignatureSignerContext signer) {
        super(jwsHeader,
              generatePayloadString(disclosureClaims, decoyClaims, hashAlg, nestedDisclosures),
              signer);
        this.disclosureSpec = disclosureSpec;
        this.disclosureClaims = disclosureClaims;
        this.decoyClaims = decoyClaims;
    }

    /*
     * 根据声明列表生成签发者签名 JWT 的载荷。
     */
    private static ObjectNode generatePayloadString(List<SdJwtClaim> claims,
                                                    List<DecoyClaim> decoyClaims,
                                                    String hashAlg,
                                                    boolean nestedDisclosures) {

        SdJwtUtils.requireNonEmpty(hashAlg, "hashAlg must not be null or empty");
        final List<SdJwtClaim> claimsInternal = claims == null ? Collections.emptyList()
            : Collections.unmodifiableList(claims);
        final List<DecoyClaim> decoyClaimsInternal = decoyClaims == null ? Collections.emptyList()
            : Collections.unmodifiableList(decoyClaims);

        try {
            // 检查声明名称无重复
            claimsInternal.stream()
                          .filter(Objects::nonNull)
                          // 若有重复，toMap 将抛出 IllegalStateException
                          .collect(Collectors.toMap(SdJwtClaim::getClaimName, claim -> claim));
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("claims must not contain duplicate claim names", e);
        }

        ArrayNode sdArray = SdJwtUtils.mapper.createArrayNode();
        // 先筛选所有 UndisclosedClaim，按盐值排序后推入 sdArray
        Map<SdJwtSalt, UndisclosedClaim> undisclosedClaimMap = new HashMap<>();
        claimsInternal.stream()
                      .filter(claim -> claim instanceof UndisclosedClaim)
                      .map(claim -> (UndisclosedClaim) claim)
                      .forEach(undisclosedClaim -> {
                          if (undisclosedClaimMap.containsKey(undisclosedClaim.getSalt())) {
                              String errorMessage = String.format("Salt value '%s' was reused for claims "
                                                                      + "'%s' and '%s'",
                                                                  undisclosedClaim.getSalt(),
                                                                  undisclosedClaim.getClaimName(),
                                                                  undisclosedClaimMap.get(undisclosedClaim.getSalt())
                                                                                     .getClaimName());
                              throw new IllegalArgumentException(errorMessage);
                          }
                          undisclosedClaimMap.put(undisclosedClaim.getSalt(), undisclosedClaim);
                      });

        List<String> digests = undisclosedClaimMap.entrySet().stream()
                                                  .sorted(Map.Entry.comparingByKey())
                                                  .map(Map.Entry::getValue)
                                                  .filter(Objects::nonNull)
                                                  .map(od -> od.getDisclosureDigest(hashAlg))
                                                  .collect(Collectors.toList());

        // 添加诱饵声明摘要
        decoyClaimsInternal.stream().map(claim -> claim.getDisclosureDigest(hashAlg)).forEach(digests::add);

        digests.stream().sorted().forEach(sdArray::add);

        ObjectNode payload = SdJwtUtils.mapper.createObjectNode();

        if (sdArray.size() > 0) {
            // 非空时才写入 _sd 声明
            payload.set(CLAIM_NAME_SD, sdArray);
        }
        if (sdArray.size() > 0 || nestedDisclosures) {
            // 存在披露或嵌套披露时写入哈希算法
            // 规范化为小写以符合 IANA 注册的哈希算法名称
            payload.put(CLAIM_NAME_SD_HASH_ALGORITHM, hashAlg.toLowerCase());
        }

        // 将其余声明写入载荷（数组元素披露由对应声明对象处理）
        claimsInternal.stream()
                      .filter(Objects::nonNull)
                      .filter(claim -> !(claim instanceof UndisclosedClaim))
                      .forEach(nullableClaim -> {
                          SdJwtClaim claim = Objects.requireNonNull(nullableClaim);
                          payload.set(claim.getClaimNameAsString(), claim.getVisibleClaimValue(hashAlg));
                      });


        return payload;
    }

    /**
     * 返回 {@code cnf} 声明（用于建立密钥绑定）。
     */
    public Optional<JsonNode> getCnfClaim() {
        JsonNode cnf = getPayload().get(CLAIM_NAME_CNF);
        return Optional.ofNullable(cnf);
    }

    /**
     * 返回 SD 哈希声明中声明的哈希算法。
     */
    public String getSdHashAlg() {
        ObjectNode payload = getPayload();
        return Optional.ofNullable(payload.get(CLAIM_NAME_SD_HASH_ALGORITHM))
                       .map(JsonNode::textValue)
                       .orElse(OID4VCConstants.SD_HASH_DEFAULT_ALGORITHM);
    }

    /**
     * 验证 SD 哈希算法是否为已知且安全的算法。
     *
     * @throws VerificationException 若算法未知或不安全
     */
    public void verifySdHashAlgorithm() throws VerificationException {
        // 已知安全算法集合
        final Set<String> secureAlgorithms = new HashSet<>(Arrays.asList(
            OID4VCConstants.SD_HASH_DEFAULT_ALGORITHM, "sha-384", "sha-512",
            "sha3-256", "sha3-384", "sha3-512"
        ));

        // 读取 SD 哈希算法声明
        String hashAlg = getSdHashAlg();

        // 安全算法校验
        if (!secureAlgorithms.contains(hashAlg)) {
            throw new VerificationException("Unexpected or insecure hash algorithm: " + hashAlg);
        }
    }

    public DisclosureSpec getDisclosureSpec() {
        return disclosureSpec;
    }

    public List<SdJwtClaim> getDisclosureClaims() {
        return disclosureClaims;
    }

    public List<DecoyClaim> getDecoyClaims() {
        return decoyClaims;
    }

    public void setDisclosureClaims(DisclosureSpec disclosureSpec,
                                    List<SdJwtClaim> disclosureClaims,
                                    List<DecoyClaim> decoyClaims) {
        setDisclosureClaims(disclosureSpec, disclosureClaims, decoyClaims, null);
    }

    public void setDisclosureClaims(DisclosureSpec disclosureSpec,
                                    List<SdJwtClaim> disclosureClaims,
                                    List<DecoyClaim> decoyClaims,
                                    SignatureSignerContext signatureSignerContext) {
        this.disclosureSpec = disclosureSpec;
        this.disclosureClaims = disclosureClaims;
        this.decoyClaims = decoyClaims;
        super.setPayload(generatePayloadString(disclosureClaims,
                                               decoyClaims,
                                               getSdHashAlg(),
                                               false/* TODO do we need this flag? */));
        setJws(null);
        setJwsInput(null);
        Optional.ofNullable(signatureSignerContext).ifPresent(super::sign);
    }

    // 建造者
    public static Builder builder() {
        return new Builder();
    }

    /** 构建 {@link IssuerSignedJWT} 的流式建造者。 */
    public static class Builder {
        private DisclosureSpec disclosureSpec;
        private List<SdJwtClaim> claims;
        private String hashAlg;
        private SignatureSignerContext signer;
        private List<DecoyClaim> decoyClaims;
        private boolean nestedDisclosures;
        private JWSHeader jwsHeader = new JWSHeader();

        private JWSHeader getJwsHeader() {
            if (this.jwsHeader == null) {
                this.jwsHeader = new JWSHeader();
            }
            return jwsHeader;
        }

        private List<SdJwtClaim> getClaims() {
            if (this.claims == null) {
                this.claims = new ArrayList<>();
            }
            return claims;
        }

        public Builder withClaims(List<SdJwtClaim> claims) {
            this.claims = claims;
            return this;
        }

        public Builder withClaims(ObjectNode claimsNode) {
            this.disclosureSpec = DisclosureSpec.builder().build();
            this.claims = SdJwtClaimFactory.parsePayload(claimsNode, disclosureSpec);
            return this;
        }

        public Builder withClaims(ObjectNode claimsNode, DisclosureSpec disclosureSpec) {
            this.disclosureSpec = disclosureSpec;
            this.claims = SdJwtClaimFactory.parsePayload(claimsNode, disclosureSpec);
            return this;
        }

        public Builder withDecoyClaims(List<DecoyClaim> decoyClaims) {
            this.decoyClaims = decoyClaims;
            return this;
        }

        public Builder withHashAlg(String hashAlg) {
            this.hashAlg = hashAlg;
            return this;
        }

        public Builder withSigner(SignatureSignerContext signer) {
            this.signer = signer;
            return this;
        }

        public Builder withNestedDisclosures(boolean nestedDisclosures) {
            this.nestedDisclosures = nestedDisclosures;
            return this;
        }

        public Builder withJwsType(String jwsType) {
            if (this.jwsHeader == null) {
                this.jwsHeader = new JWSHeader();
            }
            this.jwsHeader.setType(jwsType);
            return this;
        }

        public Builder withJwsHeader(JWSHeader jwsHeader) {
            // preserve the type in case that the method 'withJwsType' was called before this method.
            String jwsType = Optional.ofNullable(this.jwsHeader).map(JWSHeader::getType).orElse(null);
            this.jwsHeader = jwsHeader;
            if (this.jwsHeader != null) {
                this.jwsHeader.setType(jwsType);
            }
            return this;
        }

        public Builder withKid(String kid) {
            getJwsHeader().setKeyId(kid);
            return this;
        }

        public Builder withX5c(List<String> x5c) {
            getJwsHeader().setX5c(x5c);
            return this;
        }

        public Builder withX5c(String x5c) {
            getJwsHeader().addX5c(x5c);
            return this;
        }

        public Builder withX5c(Certificate x5c) {
            getJwsHeader().addX5c(x5c);
            return this;
        }

        public Builder withIat(long iat) {
            getClaims().add(new VisibleSdJwtClaim(SdJwtClaimName.of(OID4VCConstants.CLAIM_NAME_IAT), new LongNode(iat)));
            return this;
        }

        public Builder withNbf(long nbf) {
            getClaims().add(new VisibleSdJwtClaim(SdJwtClaimName.of(OID4VCConstants.CLAIM_NAME_NBF), new LongNode(nbf)));
            return this;
        }

        public Builder withExp(long exp) {
            getClaims().add(new VisibleSdJwtClaim(SdJwtClaimName.of(OID4VCConstants.CLAIM_NAME_EXP), new LongNode(exp)));
            return this;
        }

        public Builder withKeyBindingKey(JWK keyBinding) {
            ObjectNode jwkNode = JsonSerialization.mapper.convertValue(keyBinding, ObjectNode.class);
            ObjectNode cnf = JsonNodeFactory.instance.objectNode();
            cnf.set(CLAIM_NAME_JWK, jwkNode);
            getClaims().add(new VisibleSdJwtClaim(SdJwtClaimName.of(CLAIM_NAME_CNF), cnf));
            return this;
        }

        public Builder withClaim(SdJwtClaim sdJwtClaim) {
            getClaims().add(sdJwtClaim);
            return this;
        }

        public IssuerSignedJWT build() {
            // 未提供 hashAlg 时默认 sha-256
            hashAlg = hashAlg == null ? OID4VCConstants.SD_HASH_DEFAULT_ALGORITHM : hashAlg;
            jwsHeader.setType(jwsHeader.getType() == null ? VCFormat.SD_JWT_VC : jwsHeader.getType());
            disclosureSpec = Optional.ofNullable(disclosureSpec).orElseGet(() -> DisclosureSpec.builder().build());
            // 未设置声明时使用空列表
            decoyClaims = decoyClaims == null ? disclosureSpec.createDecoyClaims() : decoyClaims;

            if (signer != null) {
                return new IssuerSignedJWT(disclosureSpec,
                                           jwsHeader,
                                           claims,
                                           decoyClaims,
                                           hashAlg,
                                           nestedDisclosures,
                                           signer);
            }
            else {
                return new IssuerSignedJWT(disclosureSpec,
                                           jwsHeader,
                                           claims,
                                           decoyClaims,
                                           hashAlg,
                                           nestedDisclosures);
            }
        }
    }

}
