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
package org.keycloak.sdjwt.vp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.IssuerSignedJwtVerificationOpts;
import org.keycloak.sdjwt.SdJwtUtils;
import org.keycloak.sdjwt.SdJwtVerificationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM;
import static org.keycloak.OID4VCConstants.SDJWT_DELIMITER;
import static org.keycloak.OID4VCConstants.SD_HASH;

/**
 * SD-JWT 可验证展示（Verifiable Presentation）的解析、构建与验证入口。
 *
 * <p>
 * 从紧凑序列化字符串解析签发者 JWT、披露项与可选的密钥绑定 JWT，
 * 并支持按需选择披露声明生成新的展示。
 * </p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class SdJwtVP {
    /** 不含密钥绑定部分的 SD-JWT VP 紧凑字符串。 */
    private final String sdJwtVpString;
    /** 签发者签名的 JWT。 */
    private final IssuerSignedJWT issuerSignedJWT;

    /** 披露摘要到披露数据的映射。 */
    private final Map<String, ArrayNode> claims;
    /** 披露摘要到原始披露字符串的映射。 */
    private final Map<String, String> disclosures;
    /** 嵌套在其他披露中的递归摘要映射。 */
    private final Map<String, String> recursiveDigests;
    /** 在签发者载荷中找不到对应节点的幽灵摘要列表。 */
    private final List<String> ghostDigests;
    /** 哈希算法名称。 */
    private final String hashAlgorithm;

    /** 可选的密钥绑定 JWT。 */
    private final Optional<KeyBindingJWT> keyBindingJWT;
    /** 用于验证的上下文对象。 */
    private final SdJwtVerificationContext sdJwtVerificationContext;

    public Map<String, ArrayNode> getClaims() {
        return claims;
    }

    public IssuerSignedJWT getIssuerSignedJWT() {
        return issuerSignedJWT;
    }

    public Map<String, String> getDisclosures() {
        return disclosures;
    }

    public Collection<String> getDisclosuresString() {
        return disclosures.values();
    }

    public Map<String, String> getRecursiveDigests() {
        return recursiveDigests;
    }

    public Collection<String> getGhostDigests() {
        return ghostDigests;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public Optional<KeyBindingJWT> getKeyBindingJWT() {
        return keyBindingJWT;
    }

    private SdJwtVP(String sdJwtVpString, String hashAlgorithm, IssuerSignedJWT issuerSignedJWT,
            Map<String, ArrayNode> claims, Map<String, String> disclosures, Map<String, String> recursiveDigests,
            List<String> ghostDigests, Optional<KeyBindingJWT> keyBindingJWT) {
        this.sdJwtVpString = sdJwtVpString;
        this.hashAlgorithm = hashAlgorithm;
        this.issuerSignedJWT = issuerSignedJWT;
        this.claims = Collections.unmodifiableMap(claims);
        this.disclosures = Collections.unmodifiableMap(disclosures);
        this.recursiveDigests = Collections.unmodifiableMap(recursiveDigests);
        this.ghostDigests = Collections.unmodifiableList(ghostDigests);
        this.keyBindingJWT = keyBindingJWT;

        // 实例化验证上下文
        this.sdJwtVerificationContext = new SdJwtVerificationContext(
                this.sdJwtVpString,
                this.issuerSignedJWT,
                this.disclosures,
                this.keyBindingJWT.orElse(null)
        );
    }

    /**
     * 从 SD-JWT 紧凑序列化字符串解析 {@link SdJwtVP}。
     *
     * @param sdJwtString SD-JWT 紧凑序列化字符串
     * @return 解析后的 VP 对象
     * @throws IllegalArgumentException 若格式不合法或披露数据无效
     */
    public static SdJwtVP of(String sdJwtString) {
        int disclosureStart = sdJwtString.indexOf(SDJWT_DELIMITER);
        int disclosureEnd = sdJwtString.lastIndexOf(SDJWT_DELIMITER);

        if (disclosureStart == -1) {
            throw new IllegalArgumentException("SD-JWT is malformed, expected to contain a '" + SDJWT_DELIMITER + "'");
        }

        String issuerSignedJWTString = sdJwtString.substring(0, disclosureStart);
        String disclosuresString = "";

        if (disclosureEnd > disclosureStart) {
            disclosuresString = sdJwtString.substring(disclosureStart + 1, disclosureEnd);
        }

        IssuerSignedJWT issuerSignedJWT = new IssuerSignedJWT(issuerSignedJWTString);

        ObjectNode issuerPayload = issuerSignedJWT.getPayload();
        String hashAlgorithm = Optional.ofNullable(issuerPayload.get(CLAIM_NAME_SD_HASH_ALGORITHM))
                                       .map(JsonNode::asText)
                                       .orElse(JavaAlgorithm.SHA256.toLowerCase());

        Map<String, ArrayNode> claims = new HashMap<>();
        Map<String, String> disclosures = new HashMap<>();

        List<String> split = Arrays.stream(disclosuresString.split(SDJWT_DELIMITER))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        for (String disclosure : split) {
            String disclosureDigest = SdJwtUtils.hashAndBase64EncodeNoPad(disclosure.getBytes(), hashAlgorithm);
            if (disclosures.containsKey(disclosureDigest)) {
                throw new IllegalArgumentException("Duplicate disclosure digest");
            }
            disclosures.put(disclosureDigest, disclosure);
            ArrayNode disclosureData;
            try {
                disclosureData = (ArrayNode) SdJwtUtils.mapper.readTree(Base64Url.decode(disclosure));
                claims.put(disclosureDigest, disclosureData);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid disclosure data");
            }
        }
        Set<String> allDigests = claims.keySet();

        Map<String, String> recursiveDigests = new HashMap<>();
        List<String> ghostDigests = new ArrayList<>();
        allDigests.forEach(disclosureDigest -> {
            JsonNode node = findNode(issuerPayload, disclosureDigest);
            processDisclosureDigest(node, disclosureDigest, claims, recursiveDigests, ghostDigests);
        });

        Optional<KeyBindingJWT> keyBindingJWT = Optional.empty();
        if (sdJwtString.length() > disclosureEnd + 1) {
            String keyBindingJWTString = sdJwtString.substring(disclosureEnd + 1);
            keyBindingJWT = Optional.of(new KeyBindingJWT(keyBindingJWTString));
        }

        // 截断密钥绑定部分；该部分已由 keyBindingJWT 对象持有
        String sdJWtVPString = sdJwtString.substring(0, disclosureEnd + 1);

        return new SdJwtVP(sdJWtVPString, hashAlgorithm, issuerSignedJWT, claims, disclosures, recursiveDigests,
                ghostDigests, keyBindingJWT);

    }

    private static JsonNode processDisclosureDigest(JsonNode node, String disclosureDigest,
            Map<String, ArrayNode> claims,
            Map<String, String> recursiveDigests,
            List<String> ghostDigests) {
        if (node == null) { // 摘要嵌套在另一项披露中
            Set<Entry<String, ArrayNode>> entrySet = claims.entrySet();
            for (Entry<String, ArrayNode> entry : entrySet) {
                if (entry.getKey().equals(disclosureDigest)) {
                    continue;
                }
                node = findNode(entry.getValue(), disclosureDigest);
                if (node != null) {
                    recursiveDigests.put(disclosureDigest, entry.getKey());
                    break;
                }
            }
        }
        if (node == null) { // 未找到与披露对应的摘要节点
            ghostDigests.add(disclosureDigest);
        }
        return node;
    }

    /** 返回签发者 JWT 中的 cnf（确认密钥）声明。 */
    public JsonNode getCnfClaim() {
        return issuerSignedJWT.getCnfClaim().orElse(null);
    }

    /**
     * 基于当前 SD-JWT 生成新的可验证展示。
     *
     * @param disclosureDigests 要披露的声明摘要（哈希）列表。
     * @param discloseAllClaims 为 true 时忽略 disclosureDigests 并披露全部声明；
     *                         为 false 时仅披露指定摘要对应的声明
     * @param keyBindingClaims 密钥绑定声明；省略则生成的展示可能不含密钥绑定
     * @param holdSignatureSignerContext 用于签名密钥绑定 JWT 的签名上下文
     * @return 含所选披露声明与可选密钥绑定的新 SD-JWT 展示字符串
     */
    public String present(List<String> disclosureDigests,
                          boolean discloseAllClaims,
                          ObjectNode keyBindingClaims,
                          SignatureSignerContext holdSignatureSignerContext) {
        StringBuilder sb = new StringBuilder();
        if (discloseAllClaims) {
            // 披露全部声明
            sb.append(sdJwtVpString);
        } else {
            sb.append(issuerSignedJWT.getJws());
            sb.append(SDJWT_DELIMITER);
            if (disclosureDigests != null) {
                for (String disclosureDigest : disclosureDigests) {
                    sb.append(disclosures.get(disclosureDigest));
                    sb.append(SDJWT_DELIMITER);
                }
            }
        }
        String unboundPresentation = sb.toString();
        if (keyBindingClaims == null || holdSignatureSignerContext == null) {
            return unboundPresentation;
        }
        String sd_hash = SdJwtUtils.hashAndBase64EncodeNoPad(unboundPresentation.getBytes(), getHashAlgorithm());
        keyBindingClaims.put(SD_HASH, sd_hash);
        KeyBindingJWT keyBindingJWT = KeyBindingJWT.builder()
                .withPayload(keyBindingClaims)
                .withSignerContext(holdSignatureSignerContext)
                .build();
        sb.append(keyBindingJWT.getJws());
        return sb.toString();
    }


    /**
     * 基于当前 SD-JWT 生成新的可验证展示，与 {@link #present(List, boolean, ObjectNode, SignatureSignerContext)} 功能相同，
     * 但允许按声明名称（如 given_name、family_name）而非披露摘要指定要展示的声明。
     *
     * @param claimsToDisclose 要披露的声明名称列表（如 given_name、family_name）
     * @param discloseAllClaims 当 claimsToDisclose 为空或 null 时使用：为 true 则披露全部声明，
     *                          为 false 则仅披露 claimsToDisclose 指定的声明
     * @param keyBindingClaims 密钥绑定声明；省略则生成的展示可能不含密钥绑定
     * @param holdSignatureSignerContext 用于签名密钥绑定 JWT 的签名上下文
     * @return 含所选披露声明与可选密钥绑定的新 SD-JWT 展示字符串
     */
    public String presentWithSpecifiedClaims(List<String> claimsToDisclose,
                                             boolean discloseAllClaims,
                                             ObjectNode keyBindingClaims,
                                             SignatureSignerContext holdSignatureSignerContext) {
        if (discloseAllClaims) {
            return present(null, true, keyBindingClaims, holdSignatureSignerContext);
        } else {
            List<String> digests = getClaims().entrySet().stream()
                    .filter(entry -> {
                        ArrayNode node = entry.getValue();
                        if (node.size() >= 2) {
                            String claimName = node.get(1).asText();
                            return (claimsToDisclose.contains(claimName));
                        }
                        return false;
                    })
                    .map(Map.Entry::getKey)
                    .sorted()
                    .collect(Collectors.toList());

            return present(digests, false, keyBindingClaims, holdSignatureSignerContext);
        }
    }

    /**
     * 验证 SD-JWT 展示。
     *
     * @param issuerVerifyingKeys             用于验证签发者签名 JWT 的公钥；调用方须自行确认密钥属于预期签发者
     * @param issuerSignedJwtVerificationOpts 签发者签名 JWT 验证参数
     * @param keyBindingJwtVerificationOpts   密钥绑定 JWT 验证参数，须包含是否校验密钥绑定的策略
     * @throws VerificationException 验证失败时抛出
     */
    public void verify(List<SignatureVerifierContext> issuerVerifyingKeys,
                       IssuerSignedJwtVerificationOpts issuerSignedJwtVerificationOpts,
                       KeyBindingJwtVerificationOpts keyBindingJwtVerificationOpts)
            throws VerificationException
    {
        sdJwtVerificationContext.verifyPresentation(
                issuerVerifyingKeys,
                issuerSignedJwtVerificationOpts,
                keyBindingJwtVerificationOpts,
                null
        );
    }

    /**
     * 获取验证上下文，供高级验证场景使用。
     */
    public SdJwtVerificationContext getSdJwtVerificationContext() {
        return sdJwtVerificationContext;
    }

    // 递归搜索包含给定值的节点；找到则返回该节点，否则返回 null
    private static JsonNode findNode(JsonNode node, String value) {
        if (node == null) {
            return null;
        }
        if (node.isValueNode()) {
            if (node.asText().equals(value)) {
                return node;
            } else {
                return null;
            }
        }
        if (node.isArray() || node.isObject()) {
            for (JsonNode child : node) {
                JsonNode found = findNode(child, value);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return sdJwtVpString;
    }

    /** 返回包含签发者 JWT、披露项与密钥绑定详情的可读调试字符串。 */
    public String verbose() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issuer Signed JWT: ");
        sb.append(issuerSignedJWT.getPayload());
        sb.append("\n");
        disclosures.forEach((digest, disclosure) -> {
            sb.append("\n");
            sb.append("Digest: ");
            sb.append(digest);
            sb.append("\n");
            sb.append("Disclosure: ");
            sb.append(disclosure);
            sb.append("\n");
            sb.append("Content: ");
            sb.append(claims.get(digest));
            sb.append("\n");
        });
        sb.append("\n");
        sb.append("Recursive Digests: ");
        sb.append(recursiveDigests);
        sb.append("\n");
        sb.append("\n");
        sb.append("Ghost Digests: ");
        sb.append(ghostDigests);
        sb.append("\n");
        sb.append("\n");
        if (keyBindingJWT.isPresent()) {
            sb.append("Key Binding JWT: ");
            sb.append("\n");
            sb.append(keyBindingJWT.get().getPayload().toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
