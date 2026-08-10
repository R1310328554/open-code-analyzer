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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.keycloak.OID4VCConstants;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.sdjwt.vp.KeyBindingJWT;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM;

/**
 * 选择性披露 JWT（SD-JWT）的主入口类，封装签发者 JWT、披露字符串与密钥绑定 JWT。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class SdJwt {

    /** 未显式配置诱饵声明时的默认诱饵数量。 */
    public static final int DEFAULT_NUMBER_OF_DECOYS = 5;

    /** 签发者签名的 JWT。 */
    private final IssuerSignedJWT issuerSignedJWT;
    /** 声明列表。 */
    private final List<SdJwtClaim> claims;
    /** Base64Url 编码的披露字符串列表。 */
    private final List<String> disclosures;

    private SdJwtVerificationContext sdJwtVerificationContext;
    private KeyBindingJWT keyBindingJWT;
    private Optional<String> sdJwtString = Optional.empty();

    public SdJwt(IssuerSignedJWT issuerSignedJWT,
                 KeyBindingJWT keyBindingJWT) {
        this(issuerSignedJWT, keyBindingJWT, null);
    }

    public SdJwt(IssuerSignedJWT issuerSignedJWT,
                 KeyBindingJWT keyBindingJWT,
                 List<SdJwt> nesteSdJwts) {
        this.issuerSignedJWT = issuerSignedJWT;
        this.claims = issuerSignedJWT.getDisclosureClaims();
        this.keyBindingJWT = keyBindingJWT;

        this.disclosures = new ArrayList<>();
        Optional.ofNullable(nesteSdJwts).ifPresent(nestedSdJwtList -> {
            nestedSdJwtList.forEach(nestedJwt -> this.disclosures.addAll(nestedJwt.getDisclosures()));
        });
        this.disclosures.addAll(getDisclosureStrings(claims));

        this.sdJwtVerificationContext = new SdJwtVerificationContext(this.issuerSignedJWT, this.disclosures);
    }

    public SdJwt(ObjectNode claimSet,
                 KeyBindingJWT keyBindingJWT) {
        this(new IssuerSignedJWT(new JWSHeader(), claimSet), keyBindingJWT, null);
    }

    public SdJwt(ObjectNode claimSet,
                 KeyBindingJWT keyBindingJWT,
                 List<SdJwt> nesteSdJwts) {
        this(new IssuerSignedJWT(new JWSHeader(), claimSet), keyBindingJWT, nesteSdJwts);
    }

    public SdJwt(IssuerSignedJWT issuerSignedJWT,
                 KeyBindingJWT keyBindingJWT,
                 List<SdJwtClaim> claims,
                 List<String> disclosures) {
        this.issuerSignedJWT = issuerSignedJWT;
        this.keyBindingJWT = keyBindingJWT;
        this.claims = claims;
        this.disclosures = disclosures;
    }

    private List<DecoyClaim> createdDecoyClaims(DisclosureSpec disclosureSpec) {
        return disclosureSpec.getDecoyClaims().stream()
                             .map(disclosureData -> DecoyClaim.builder().withSalt(disclosureData.getSalt()).build())
                             .collect(Collectors.toList());
    }

    /**
     * 将此 SD-JWT 准备为嵌套载荷（移除算法声明）。
     *
     * @return 嵌套载荷 JSON 节点
     */
    public JsonNode asNestedPayload() {
        JsonNode nestedPayload = JsonSerialization.mapper.convertValue(issuerSignedJWT.getPayload(), JsonNode.class);
        ((ObjectNode) nestedPayload).remove(CLAIM_NAME_SD_HASH_ALGORITHM);
        return nestedPayload;
    }

    public String toSdJwtString() {
        List<String> parts = new ArrayList<>();

        parts.add(issuerSignedJWT.getJws());
        parts.addAll(disclosures);
        parts.add(Optional.ofNullable(keyBindingJWT).map(KeyBindingJWT::getJws).orElse(""));

        return String.join(OID4VCConstants.SDJWT_DELIMITER, parts);
    }

    private static List<String> getDisclosureStrings(List<SdJwtClaim> claims) {
        List<String> disclosureStrings = new ArrayList<>();
        claims.stream()
              .map(SdJwtClaim::getDisclosureStrings)
              .forEach(disclosureStrings::addAll);
        return Collections.unmodifiableList(disclosureStrings);
    }

    public KeyBindingJWT getKeybindingJwt() {
        return keyBindingJWT;
    }

    public void setKeybindingJwt(KeyBindingJWT keybindingJwt) {
        this.keyBindingJWT = keybindingJwt;
    }

    public List<SdJwtClaim> getClaims() {
        return claims;
    }

    public SdJwtVerificationContext getSdJwtVerificationContext() {
        return sdJwtVerificationContext;
    }

    public void setSdJwtVerificationContext(SdJwtVerificationContext sdJwtVerificationContext) {
        this.sdJwtVerificationContext = sdJwtVerificationContext;
    }

    public Optional<String> getSdJwtString() {
        return sdJwtString;
    }

    public void setSdJwtString(Optional<String> sdJwtString) {
        this.sdJwtString = sdJwtString;
    }

    @Override
    public String toString() {
        return sdJwtString.orElseGet(() -> {
            String sdString = toSdJwtString();
            sdJwtString = Optional.of(sdString);
            return sdString;
        });
    }

    public IssuerSignedJWT getIssuerSignedJWT() {
        return issuerSignedJWT;
    }

    public List<String> getDisclosures() {
        return disclosures;
    }

    /**
     * 验证 SD-JWT 的签发者签名与披露字符串是否有效。
     *
     * @param issuerVerifyingKeys 用于验证签发者 JWT 的验证密钥；调用方需自行确保证书/密钥属于预期签发者
     * @param verificationOpts 签发者签名 JWT 的验证选项
     * @throws VerificationException 验证失败时抛出
     */
    public void verify(List<SignatureVerifierContext> issuerVerifyingKeys,
                       IssuerSignedJwtVerificationOpts verificationOpts)
        throws VerificationException {
        sdJwtVerificationContext.verifyIssuance(
            issuerVerifyingKeys,
            verificationOpts,
            null
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    // SdJwt 建造者
    public static class Builder {

        /** 嵌套的 SD-JWT 列表。 */
        private final List<SdJwt> nestedSdJwts = new ArrayList<>();

        private IssuerSignedJWT issuerSignedJwt;

        private KeyBindingJWT keyBindingJWT;

        private SignatureSignerContext issuerSigningContext;

        private SignatureSignerContext keyBindingSigningContext;

        private String sdHashAlgorithm;

        private boolean useDefaultDecoys = true;

        public Builder withIssuerSignedJwt(IssuerSignedJWT issuerSignedJwt) {
            this.issuerSignedJwt = issuerSignedJwt;
            return this;
        }

        public Builder withKeybindingJwt(KeyBindingJWT keybindingJwt) {
            this.keyBindingJWT = keybindingJwt;
            return this;
        }

        public Builder withNestedSdJwt(SdJwt nestedSdJwt) {
            nestedSdJwts.add(nestedSdJwt);
            return this;
        }

        public Builder withIssuerSigningContext(SignatureSignerContext issuerSigningContext) {
            this.issuerSigningContext = issuerSigningContext;
            return this;
        }

        public Builder withKeyBindingSigningContext(SignatureSignerContext keyBindingSigningContext) {
            this.keyBindingSigningContext = keyBindingSigningContext;
            return this;
        }

        public Builder withSdHashAlgorithm(String sdHashAlgorithm) {
            this.sdHashAlgorithm = sdHashAlgorithm;
            return this;
        }

        public Builder withUseDefaultDecoys(boolean useDefaultDecoys) {
            this.useDefaultDecoys = useDefaultDecoys;
            return this;
        }

        public SdJwt build() {
            int numberOfDecoys = Optional.ofNullable(issuerSignedJwt.getDecoyClaims()).map(List::size).orElse(0);
            if (useDefaultDecoys && numberOfDecoys == 0) {
                List<DecoyClaim> decoyClaims = new ArrayList<>();
                for (int i = 0; i < DEFAULT_NUMBER_OF_DECOYS; i++) {
                    decoyClaims.add(DecoyClaim.builder().build());
                }
                issuerSignedJwt.setDisclosureClaims(issuerSignedJwt.getDisclosureSpec(),
                                                    issuerSignedJwt.getDisclosureClaims(),
                                                    decoyClaims);
            }

            SdJwt sdJwt = new SdJwt(issuerSignedJwt, keyBindingJWT, nestedSdJwts);
            AtomicInteger signCounter = new AtomicInteger(0);
            // 向密钥绑定 JWT 添加 sd-hash
            Optional.ofNullable(keyBindingJWT).ifPresent(keyBindJwt -> {
                // 获取密钥绑定使用的哈希算法，若未设置则写入载荷
                String hashAlgorithm = getEffectiveHashAlgorithm(sdHashAlgorithm);
                // 规范化为小写以符合 IANA 注册的哈希算法名称
                issuerSignedJwt.getPayload().put(OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM,
                                                 hashAlgorithm.toLowerCase());
                if (issuerSigningContext != null) {
                    issuerSignedJwt.sign(issuerSigningContext);
                }
                signCounter.incrementAndGet();
                // 密钥绑定 JWT 尚未设置，toSdJwtString 返回的正是所需字符串
                String sdHashString;
                {
                    List<String> parts = new ArrayList<>();
                    parts.add(sdJwt.getIssuerSignedJWT().getJws());
                    parts.addAll(sdJwt.getDisclosures());
                    parts.add("");
                    sdHashString = String.join(OID4VCConstants.SDJWT_DELIMITER, parts);
                }
                String sdHash = SdJwtUtils.hashAndBase64EncodeNoPad(sdHashString.getBytes(), hashAlgorithm);
                keyBindJwt.getPayload().put(OID4VCConstants.SD_HASH, sdHash);
                Optional.ofNullable(keyBindingSigningContext).ifPresent(keyBindJwt::sign);
            });
            // 若签发者 JWT 尚未签名则在此签名
            if (issuerSigningContext != null && signCounter.get() == 0) {
                issuerSignedJwt.sign(issuerSigningContext);
            }
            sdJwt.setKeybindingJwt(keyBindingJWT);
            return sdJwt;
        }

        /** 解析有效哈希算法：优先使用参数，其次从签发者 JWT 载荷读取，最后使用默认值。 */
        private String getEffectiveHashAlgorithm(String sdHashAlgorithm) {
            return Optional.ofNullable(sdHashAlgorithm).orElseGet(() -> {
                // 未作为参数传入时，尝试从 issuerSignedJwt 载荷中读取
                return issuerSignedJwt.getSdHashAlgorithm().orElse(OID4VCConstants.SD_HASH_DEFAULT_ALGORITHM);
            });
        }
    }
}
