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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.OID4VCConstants;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.sdjwt.consumer.PresentationRequirements;
import org.keycloak.sdjwt.vp.KeyBindingJWT;
import org.keycloak.sdjwt.vp.KeyBindingJwtVerificationOpts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.logging.Logger;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_JWK;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_UNDISCLOSED_ARRAY;
import static org.keycloak.OID4VCConstants.SDJWT_DELIMITER;
import static org.keycloak.OID4VCConstants.SD_HASH;


/**
 * 在隔离上下文中执行 SD-JWT 验证，仅保留验证所需的核心属性。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class SdJwtVerificationContext {

    private static final Logger logger = Logger.getLogger(SdJwtVerificationContext.class.getName());

    /** 完整 SD-JWT VP 字符串。 */
    private String sdJwtVpString;

    /** 签发者签名的 JWT。 */
    private final IssuerSignedJWT issuerSignedJwt;

    /** 摘要到披露字符串的映射。 */
    private final Map<String, String> disclosures;

    /** 密钥绑定 JWT（可选）。 */
    private KeyBindingJWT keyBindingJwt;

    public SdJwtVerificationContext(String sdJwtVpString,
                                    IssuerSignedJWT issuerSignedJwt,
                                    Map<String, String> disclosures,
                                    KeyBindingJWT keyBindingJwt) {
        this(issuerSignedJwt, disclosures);
        this.keyBindingJwt = keyBindingJwt;
        this.sdJwtVpString = sdJwtVpString;
    }

    public SdJwtVerificationContext(IssuerSignedJWT issuerSignedJwt, Map<String, String> disclosures) {
        this.issuerSignedJwt = issuerSignedJwt;
        this.disclosures = disclosures;
    }

    public SdJwtVerificationContext(IssuerSignedJWT issuerSignedJwt, List<String> disclosureStrings) {
        this.issuerSignedJwt = issuerSignedJwt;
        this.disclosures = computeDigestDisclosureMap(disclosureStrings);
    }

    /** 将披露字符串列表转换为摘要到披露字符串的映射。 */
    private Map<String, String> computeDigestDisclosureMap(List<String> disclosureStrings) {
        return disclosureStrings.stream()
                .map(disclosureString -> {
                    String digest = SdJwtUtils.hashAndBase64EncodeNoPad(
                            disclosureString.getBytes(), issuerSignedJwt.getSdHashAlg());
                    return new AbstractMap.SimpleEntry<>(digest, disclosureString);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 验证 SD-JWT 签发：校验签发者签名 JWT 的签名及披露的有效性。
     *
     * <p>收到 SD-JWT 后，持有者或验证者须确保：</p>
     * - 签发者签名 JWT 有效，即由签发者签名且签名验证通过；
     * - 所有披露均有效，且与签发者签名 JWT 载荷中（直接或递归嵌套于其他披露中）的摘要值对应。
     *
     * @param issuerVerifyingKeys             用于验证签发者签名 JWT 的公钥。调用方须自行建立对这些密钥属于预期签发者的信任。
     * @param issuerSignedJwtVerificationOpts 签发者签名 JWT 验证参数。
     * @param presentationRequirements        若设置，在验证过程中完全披露签发者签名 JWT 后将强制满足演示要求。
     * @throws VerificationException 验证失败时
     */
    public void verifyIssuance(List<SignatureVerifierContext> issuerVerifyingKeys,
                               IssuerSignedJwtVerificationOpts issuerSignedJwtVerificationOpts,
                               PresentationRequirements presentationRequirements)
            throws VerificationException
    {
        // 验证签发者签名 JWT
        validateIssuerSignedJwt(issuerVerifyingKeys);

        // 验证披露
        JsonNode disclosedPayload = validateDisclosuresDigests();

        // 验证时间声明与算法头声明
        // 签发者通常会在 SD-JWT 载荷中以明文包含控制有效期的声明，但无法保证一定如此。
        // 因此验证者不能依赖这一点，须假定安全关键声明可能是选择性披露的。
        issuerSignedJwtVerificationOpts.verify(issuerSignedJwt);

        // 强制满足演示要求
        if (presentationRequirements != null) {
            presentationRequirements.checkIfSatisfiedBy(disclosedPayload);
        }
    }

    /**
     * 验证 SD-JWT 演示。
     *
     * <p>
     * 收到演示时，除 {@link #verifyIssuance} 中的检查外，验证者还须确保：
     * 若要求密钥绑定，则密钥绑定 JWT 须由持有者签名且有效。
     * </p>
     *
     * @param issuerVerifyingKeys             用于验证签发者签名 JWT 的公钥。调用方须自行建立对这些密钥属于预期签发者的信任。
     * @param issuerSignedJwtVerificationOpts 签发者签名 JWT 验证参数。
     * @param keyBindingJwtVerificationOpts   密钥绑定 JWT 验证参数，其中须指定验证者是否检查密钥绑定的策略。
     * @param presentationRequirements        若设置，在验证过程中完全披露签发者签名 JWT 后将强制满足演示要求。
     * @throws VerificationException 验证失败时
     */
    public void verifyPresentation(List<SignatureVerifierContext> issuerVerifyingKeys,
                                   IssuerSignedJwtVerificationOpts issuerSignedJwtVerificationOpts,
                                   KeyBindingJwtVerificationOpts keyBindingJwtVerificationOpts,
                                   PresentationRequirements presentationRequirements)
            throws VerificationException
    {
        // 若要求密钥绑定但未提供密钥绑定 JWT，验证者 MUST 拒绝该演示
        if (keyBindingJwtVerificationOpts.isKeyBindingRequired() && keyBindingJwt == null) {
            throw new VerificationException("Missing Key Binding JWT");
        }

        // 收到演示时，除 {@link #verifyIssuance} 中的检查外...
        verifyIssuance(issuerVerifyingKeys, issuerSignedJwtVerificationOpts, presentationRequirements);

        // 若需要则验证密钥绑定 JWT
        if (keyBindingJwtVerificationOpts.isKeyBindingRequired()) {
            validateKeyBindingJwt(keyBindingJwtVerificationOpts);
        }
    }

    /**
     * 验证签发者签名 JWT。
     *
     * <p>
     * 收到 SD-JWT 后，持有者或验证者须确保：
     * 签发者签名 JWT 有效，即由签发者签名且签名验证通过。
     * </p>
     *
     * @param verifiers 用于验证签发者签名 JWT 的公钥列表
     * @throws VerificationException 验证失败时
     */
    private void validateIssuerSignedJwt(
            List<SignatureVerifierContext> verifiers
    ) throws VerificationException {
        // 检查 _sd_alg 声明值是否可识别且哈希算法被认为安全
        issuerSignedJwt.verifySdHashAlgorithm();

        // 验证签发者签名 JWT 的签名
        Iterator<SignatureVerifierContext> iterator = verifiers.iterator();
        while (iterator.hasNext()) {
            try {
                SignatureVerifierContext verifier = iterator.next();
                issuerSignedJwt.verifySignature(verifier);
                return;
            } catch (VerificationException e) {
                logger.debugf(e, "Issuer-signed JWT's signature verification failed against one potential verifying key");
                if (iterator.hasNext()) {
                    logger.debugf("Retrying Issuer-signed JWT's signature verification with next potential verifying key");
                }
            }
        }

        // 所有候选验证者均无法验证 JWT 签名
        throw new VerificationException("Invalid Issuer-Signed JWT: Signature could not be verified");
    }

    /**
     * 验证密钥绑定 JWT。
     *
     * @throws VerificationException 验证失败时
     */
    private void validateKeyBindingJwt(KeyBindingJwtVerificationOpts keyBindingJwtVerificationOpts)
            throws VerificationException
    {
        // 检查密钥绑定 JWT 的 typ 是否为 kb+jwt
        validateKeyBindingJwtTyp();

        // 从 SD-JWT 中获取持有者的公钥
        JsonNode cnf = issuerSignedJwt.getCnfClaim().orElseThrow(
                () -> new VerificationException("No cnf claim in Issuer-signed JWT for key binding")
        );

        // 确保使用了被认为安全的签名算法，MUST NOT 接受 none 算法
        SignatureVerifierContext holderVerifier = buildHolderVerifier(cnf);

        // 验证密钥绑定 JWT 的签名
        try {
            keyBindingJwt.verifySignature(holderVerifier);
        } catch (VerificationException e) {
            throw new VerificationException("Key binding JWT invalid", e);
        }

        // 检查密钥绑定 JWT 的时间戳与头算法
        keyBindingJwtVerificationOpts.verify(keyBindingJwt);

        // 披露与密钥绑定 JWT 必须使用相同的哈希算法（由签发者签名 JWT 中的 _sd_alg 或第 5.1.1 节默认值定义）
        validateKeyBindingJwtSdHashIntegrity();

        // 检查密钥绑定 JWT 在其他方面是否为有效 JWT
        // -> 部分由 keyBindingJwt 为 SdJws 实例覆盖
        // -> 时间声明已在上方检查
    }

    /**
     * 验证密钥绑定 JWT 的 typ 头属性。
     *
     * @throws VerificationException 验证失败时
     */
    private void validateKeyBindingJwtTyp() throws VerificationException {
        String typ = keyBindingJwt.getJwsHeader().getType();
        if (!OID4VCConstants.KEYBINDING_JWT_TYP.equals(typ)) {
            throw new VerificationException("Key Binding JWT is not of declared typ " + OID4VCConstants.KEYBINDING_JWT_TYP);
        }
    }

    /**
     * 从 JWK 节点构建持有者验证器。
     *
     * @throws VerificationException 无法构建时
     */
    private SignatureVerifierContext buildHolderVerifier(JsonNode cnf) throws VerificationException {
        Objects.requireNonNull(cnf);

        // 读取 JWK
        JsonNode cnfJwk = cnf.get(CLAIM_NAME_JWK);
        if (cnfJwk == null) {
            throw new UnsupportedOperationException("Only cnf/jwk claim supported");
        }

        // 转换 JWK
        try {
            return JwkParsingUtils.convertJwkNodeToVerifierContext(cnfJwk);
        } catch (Exception e) {
            throw new VerificationException("Could not process cnf/jwk", e);
        }
    }

    /**
     * 验证披露的摘要。
     *
     * <p>
     * 收到 SD-JWT 后，持有者或验证者须确保：
     * 所有披露均有效，且与签发者签名 JWT 载荷中（直接或递归嵌套于其他披露中）的摘要值对应。
     * </p>
     *
     * <p>
     * 此外检查盐值未被重复使用：
     * 每个待选择性披露的声明 MUST 使用唯一的盐值。
     * </p>
     *
     * @return 完全披露后的 SD-JWT 载荷
     * @throws VerificationException 验证失败时
     */
    private JsonNode validateDisclosuresDigests() throws VerificationException {
        // 通过尝试完整递归披露来验证 SD-JWT 摘要
        Set<String> visitedSalts = new HashSet<>();
        Set<String> visitedDigests = new HashSet<>();
        Set<String> visitedDisclosureStrings = new HashSet<>();
        JsonNode disclosedPayload = validateViaRecursiveDisclosing(
                SdJwtUtils.deepClone(issuerSignedJwt.getPayload()),
                visitedSalts, visitedDigests, visitedDisclosureStrings);

        // 验证所有披露均已被访问
        validateDisclosuresVisits(visitedDisclosureStrings);

        return disclosedPayload;
    }

    /**
     * 通过完整递归披露验证 SD-JWT 摘要。
     *
     * <p>
     * 递归披露载荷中所有可披露字段以强制执行关联披露的合规性规则。
     * 调用此方法后，应基于已访问数据参数执行额外规则检查。
     * </p>
     *
     * @return 完全披露后的 SD-JWT 载荷
     */
    private JsonNode validateViaRecursiveDisclosing(
            JsonNode currentNode,
            Set<String> visitedSalts,
            Set<String> visitedDigests,
            Set<String> visitedDisclosureStrings
    ) throws VerificationException {
        if (!currentNode.isObject() && !currentNode.isArray()) {
            return currentNode;
        }

        // 查找所有包含 _sd 键且其值为字符串数组的对象
        if (currentNode.isObject()) {
            ObjectNode currentObjectNode = ((ObjectNode) currentNode);

            JsonNode sdArray = currentObjectNode.get(CLAIM_NAME_SD);
            if (sdArray != null && sdArray.isArray()) {
                for (JsonNode el : sdArray) {
                    if (!el.isTextual()) {
                        throw new VerificationException(
                                "Unexpected non-string element inside _sd array: " + el
                        );
                    }

                    // 将值与先前计算的摘要比较，查找匹配的披露；若找不到则 MUST 忽略该摘要

                    String digest = el.asText();
                    markDigestAsVisited(digest, visitedDigests);
                    String disclosure = disclosures.get(digest);

                    if (disclosure != null) {
                        // 标记披露为已访问
                        visitedDisclosureStrings.add(disclosure);

                        // 验证披露格式
                        DisclosureFields decodedDisclosure = validateSdArrayDigestDisclosureFormat(disclosure);

                        // 标记盐值为已访问
                        markSaltAsVisited(decodedDisclosure.getSaltValue(), visitedSalts);

                        // 若声明名在 _sd 键层级已存在，则 MUST 拒绝 SD-JWT（选择性披露 JWT 签发者签名 JWT 验证），
                        // 否则披露可能静默覆盖明文存在或由其他披露添加的声明
                        String claimName = decodedDisclosure.getClaimName();
                        if (currentObjectNode.has(claimName)) {
                            throw new VerificationException(
                                    "Disclosure claim name already present in the payload: " + claimName);
                        }

                        // 在 _sd 键层级插入新声明，使用披露中的声明名与声明值
                        currentObjectNode.set(claimName, decodedDisclosure.getClaimValue());
                    }
                }
            }

            // 从签发者签名 JWT 载荷中移除所有 _sd 键及其内容；
            // 若结果为空对象，应表示为 {}
            currentObjectNode.remove(CLAIM_NAME_SD);

            // 从 SD-JWT 载荷中移除 _sd_alg 声明
            currentObjectNode.remove(CLAIM_NAME_SD_HASH_ALGORITHM);
        }

        // 查找所有数组元素：单键对象且键为 ... 且值为字符串
        if (currentNode.isArray()) {
            ArrayNode currentArrayNode = ((ArrayNode) currentNode);
            ArrayList<Integer> indexesToRemove = new ArrayList<>();

            for (int i = 0; i < currentArrayNode.size(); ++i) {
                JsonNode itemNode = currentArrayNode.get(i);
                if (itemNode.isObject() && itemNode.size() == 1) {
                    // 检查单字段 "..."
                    Map.Entry<String, JsonNode> field = itemNode.fields().next();
                    if (field.getKey().equals(CLAIM_NAME_SD_UNDISCLOSED_ARRAY)
                            && field.getValue().isTextual()) {
                        // 将值与先前计算的摘要比较，查找匹配的披露；若找不到则 MUST 忽略该摘要

                        String digest = field.getValue().asText();
                        markDigestAsVisited(digest, visitedDigests);
                        String disclosure = disclosures.get(digest);

                        if (disclosure != null) {
                            // 标记披露为已访问
                            visitedDisclosureStrings.add(disclosure);

                            // 验证披露格式
                            DisclosureFields decodedDisclosure = validateArrayElementDigestDisclosureFormat(disclosure);

                            // 标记盐值为已访问
                            markSaltAsVisited(decodedDisclosure.getSaltValue(), visitedSalts);

                            // 用披露中的值替换数组元素，移除操作见下方
                            currentArrayNode.set(i, decodedDisclosure.getClaimValue());
                        } else {
                            // 移除上一步未找到摘要的所有数组元素
                            indexesToRemove.add(i);
                        }
                    }
                }
            }

            // 逆序移除，避免每次移除影响更早元素的索引
            for (int i = indexesToRemove.size() - 1; i >= 0; i--) {
                currentArrayNode.remove((int) indexesToRemove.get(i));
            }
        }

        for (JsonNode childNode : currentNode) {
            validateViaRecursiveDisclosing(childNode, visitedSalts, visitedDigests, visitedDisclosureStrings);
        }

        return currentNode;
    }

    /**
     * 标记摘要为已访问。
     *
     * <p>
     * 若在签发者签名 JWT 载荷中（直接或通过其他披露递归）多次遇到同一摘要值，则 MUST 拒绝 SD-JWT。
     * </p>
     *
     * @throws VerificationException 非首次访问时
     */
    private void markDigestAsVisited(String digest, Set<String> visitedDigests)
            throws VerificationException {
        if (!visitedDigests.add(digest)) {
            // add 返回 false 表示重复
            throw new VerificationException("A digest was encountered more than once: " + digest);
        }
    }

    /**
     * 标记盐值为已访问。
     *
     * <p>
     * 每个待选择性披露的声明 MUST 使用唯一的盐值。
     * </p>
     *
     * @throws VerificationException 非首次访问时
     */
    private void markSaltAsVisited(String salt, Set<String> visitedSalts)
            throws VerificationException {
        if (!visitedSalts.add(salt)) {
            // add 返回 false 表示重复
            throw new VerificationException("A salt value was reused: " + salt);
        }
    }

    /**
     * 验证在对象 _sd 键中找到的摘要所对应的披露。
     *
     * <p>
     * 若相应披露内容不是包含三个元素（盐值、声明名、声明值）的 JSON 编码数组，则 MUST 拒绝 SD-JWT。
     * </p>
     *
     * <p>
     * 若声明名为 _sd 或 ...，则 MUST 拒绝 SD-JWT。
     * </p>
     *
     * @return 解码后的披露（盐值、声明名、声明值）
     */
    private DisclosureFields validateSdArrayDigestDisclosureFormat(String disclosure)
            throws VerificationException {
        ArrayNode arrayNode = SdJwtUtils.decodeDisclosureString(disclosure);

        // 检查数组是否恰好包含三个元素
        if (arrayNode.size() != 3) {
            throw new VerificationException("A field disclosure must contain exactly three elements");
        }

        // 若声明名为 _sd 或 ...，则 MUST 拒绝 SD-JWT

        List<String> denylist = Arrays.asList(
                CLAIM_NAME_SD,
                CLAIM_NAME_SD_UNDISCLOSED_ARRAY
        );

        String claimName = arrayNode.get(1).asText();
        if (denylist.contains(claimName)) {
            throw new VerificationException("Disclosure claim name must not be '_sd' or '...'");
        }

        // 返回解码后的披露
        return new DisclosureFields(
                arrayNode.get(0).asText(),
                claimName,
                arrayNode.get(2)
        );
    }

    /**
     * 验证作为未披露数组元素找到的摘要所对应的披露。
     *
     * <p>
     * 若相应披露内容不是包含两个元素（盐值、值）的 JSON 编码数组，则 MUST 拒绝 SD-JWT。
     * </p>
     *
     * @return 解码后的披露（盐值、值）
     */
    private DisclosureFields validateArrayElementDigestDisclosureFormat(String disclosure)
            throws VerificationException {
        ArrayNode arrayNode = SdJwtUtils.decodeDisclosureString(disclosure);

        // 检查数组是否恰好包含两个元素
        if (arrayNode.size() != 2) {
            throw new VerificationException("An array element disclosure must contain exactly two elements");
        }

        // 返回解码后的披露
        return new DisclosureFields(
                arrayNode.get(0).asText(),
                null,
                arrayNode.get(1)
        );
    }

    /**
     * 验证所有披露均已被访问。
     *
     * <p>
     * 若有任何披露未在签发者签名 JWT 中（直接或通过其他披露递归）被摘要值引用，则 MUST 拒绝 SD-JWT。
     * </p>
     *
     * @throws VerificationException 不满足时
     */
    private void validateDisclosuresVisits(Set<String> visitedDisclosureStrings)
            throws VerificationException {
        if (visitedDisclosureStrings.size() < disclosures.size()) {
            throw new VerificationException("At least one disclosure is not protected by digest");
        }
    }

    /**
     * 验证密钥绑定 JWT 的 sd_hash 完整性。
     *
     * <p>
     * 计算签发者签名 JWT 与披露的摘要，并验证其与密钥绑定 JWT 中 sd_hash 声明的值一致。
     * </p>
     *
     * @throws VerificationException 验证失败时
     */
    private void validateKeyBindingJwtSdHashIntegrity() throws VerificationException {
        Objects.requireNonNull(sdJwtVpString);

        JsonNode sdHash = keyBindingJwt.getPayload().get(SD_HASH);
        if (sdHash == null || !sdHash.isTextual()) {
            throw new VerificationException("Key binding JWT: Claim `sd_hash` missing or not a string");
        }

        int lastDelimiterIndex = sdJwtVpString.lastIndexOf(SDJWT_DELIMITER);
        String toHash = sdJwtVpString.substring(0, lastDelimiterIndex + 1);

        String digest = SdJwtUtils.hashAndBase64EncodeNoPad(
                toHash.getBytes(), issuerSignedJwt.getSdHashAlg());

        if (!digest.equals(sdHash.asText())) {
            throw new VerificationException("Key binding JWT: Invalid `sd_hash` digest");
        }
    }

    /** 披露字段的简单记录类。 */
    private static class DisclosureFields {
        String saltValue;
        String claimName;
        JsonNode claimValue;

        public DisclosureFields(String saltValue, String claimName, JsonNode claimValue) {
            this.saltValue = saltValue;
            this.claimName = claimName;
            this.claimValue = claimValue;
        }

        public String getSaltValue() {
            return saltValue;
        }

        public String getClaimName() {
            return claimName;
        }

        public JsonNode getClaimValue() {
            return claimValue;
        }
    }
}
