/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.exceptions.TokenNotActiveException;
import org.keycloak.exceptions.TokenSignatureInvalidException;
import org.keycloak.jose.jws.AlgorithmType;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.jose.jws.crypto.ECDSAProvider;
import org.keycloak.jose.jws.crypto.HMACProvider;
import org.keycloak.jose.jws.crypto.RSAProvider;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.util.TokenUtil;

/**
 * JWT 令牌验证器：解析 JWS、验签并按可组合谓词链执行声明检查。
 * 支持 RSA、ECDSA、HMAC 及自定义 {@link SignatureVerifierContext}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class TokenVerifier<T extends JsonWebToken> {

    private static final Logger LOG = Logger.getLogger(TokenVerifier.class.getName());

    // 本接口存在是因为项目要求 JDK 7；若强制 JDK 8 可改用 java.util.function.Predicate。

    /**
     * 对 JWT 某一部分执行单项检查的函数式接口。
     * @param <T> 谓词处理的令牌类型
     */
    // @FunctionalInterface
    public static interface Predicate<T extends JsonWebToken> {
        /**
         * 对给定令牌执行一次检查。
         * @param t 令牌，保证非 null
         * @return 检查通过时返回 {@code true}
         * @throws VerificationException 检查失败时抛出
         */
        boolean test(T t) throws VerificationException;
    }

    /** 检查 {@code sub}（subject）声明必须存在。 */
    public static final Predicate<JsonWebToken> SUBJECT_EXISTS_CHECK = new Predicate<JsonWebToken>() {
        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            String subject = t.getSubject();
            if (subject == null) {
                throw new VerificationException("Subject missing in token");
            }

            return true;
        }
    };

    /**
     * 检查令牌既未过期也未早于生效时间（nbf）。
     * @see JsonWebToken#isActive()
     */
    public static final Predicate<JsonWebToken> IS_ACTIVE = new Predicate<JsonWebToken>() {
        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            if (! t.isActive()) {
                throw new TokenNotActiveException(t, "Token is not active");
            }

            return true;
        }
    };

    /** 校验 JWT {@code iss}（issuer）与期望的领域 URL 一致。 */
    public static class RealmUrlCheck implements Predicate<JsonWebToken> {

        private static final RealmUrlCheck NULL_INSTANCE = new RealmUrlCheck(null);

        private final String realmUrl;

        public RealmUrlCheck(String realmUrl) {
            this.realmUrl = realmUrl;
        }

        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            if (this.realmUrl == null) {
                throw new VerificationException("Realm URL not set");
            }

            if (! this.realmUrl.equals(t.getIssuer())) {
                throw new VerificationException("Invalid token issuer. Expected '" + this.realmUrl + "'");
            }

            return true;
        }
    }

    /** 校验 JWT {@code typ} 是否为允许的令牌类型之一。 */
    public static class TokenTypeCheck implements Predicate<JsonWebToken> {

        private static final TokenTypeCheck INSTANCE_DEFAULT_TOKEN_TYPE = new TokenTypeCheck(Arrays.asList(TokenUtil.TOKEN_TYPE_BEARER));

        private final List<String> tokenTypes;

        public TokenTypeCheck(List<String> tokenTypes) {
            this.tokenTypes = tokenTypes;
        }

        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            for (String tokenType : tokenTypes) {
                if (tokenType.equalsIgnoreCase(t.getType())) return true;
            }
            throw new VerificationException("Token type is incorrect. Expected '" + tokenTypes.toString() + "' but was '" + t.getType() + "'");
        }
    }


    /** 校验 JWT {@code aud}（audience）包含期望受众。 */
    public static class AudienceCheck implements Predicate<JsonWebToken> {

        private final String expectedAudience;

        public AudienceCheck(String expectedAudience) {
            this.expectedAudience = expectedAudience;
        }

        @Override
        public boolean test(JsonWebToken t) throws VerificationException {
            if (expectedAudience == null) {
                throw new VerificationException("Missing expectedAudience");
            }

            String[] audience = t.getAudience();
            if (audience == null) {
                throw new VerificationException("No audience in the token");
            }

            if (t.hasAudience(expectedAudience)) {
                return true;
            }

            throw new VerificationException("Expected audience not available in the token");
        }
    }


    /** 校验 JWT {@code azp}（issuedFor）与期望值一致。 */
    public static class IssuedForCheck implements Predicate<JsonWebToken> {

        private final String expectedIssuedFor;

        public IssuedForCheck(String expectedIssuedFor) {
            this.expectedIssuedFor = expectedIssuedFor;
        }

        @Override
        public boolean test(JsonWebToken jsonWebToken) throws VerificationException {
            if (expectedIssuedFor == null) {
                throw new VerificationException("Missing expectedIssuedFor");
            }

            if (expectedIssuedFor.equals(jsonWebToken.getIssuedFor())) {
                return true;
            }

            throw new VerificationException("Expected issuedFor doesn't match");
        }
    }


    private String tokenString;
    private Class<? extends T> clazz;
    private PublicKey publicKey;
    private SecretKey secretKey;
    private String realmUrl;
    private List<String> expectedTokenType = Arrays.asList(TokenUtil.TOKEN_TYPE_BEARER, TokenUtil.TOKEN_TYPE_DPOP);
    private boolean checkTokenType = true;
    private boolean checkRealmUrl = true;
    private final LinkedList<Predicate<? super T>> checks = new LinkedList<>();

    private JWSInput jws;
    private T token;

    private SignatureVerifierContext verifier = null;

    /**
     * 设置自定义验签上下文（替代内置 RSA/ECDSA/HMAC 验签）。
     *
     * @param verifier 验签上下文
     * @return 当前验证器实例（链式调用）
     */
    public TokenVerifier<T> verifierContext(SignatureVerifierContext verifier) {
        this.verifier = verifier;
        return this;
    }

    protected TokenVerifier(String tokenString, Class<T> clazz) {
        this.tokenString = tokenString;
        this.clazz = clazz;
    }

    protected TokenVerifier(T token) {
        this.token = token;
    }

    /**
     * 由 JWT 字符串及目标类型创建 {@code TokenVerifier}，初始无任何检查项。
     * 检查仅在调用 {@link #verify()} 时执行。
     * @param <T> 令牌类型
     * @param tokenString JWT 字符串
     * @param clazz 令牌 POJO 类
     * @return 新的验证器实例
     */
    public static <T extends JsonWebToken> TokenVerifier<T> create(String tokenString, Class<T> clazz) {
        return new TokenVerifier<>(tokenString, clazz);
    }

    /**
     * 由已解析的令牌对象创建 {@code TokenVerifier}，初始无任何检查项。
     * 检查仅在调用 {@link #verify()} 时执行。
     * <p>
     * <b>注意：</b> 此方式无法验签，因为 {@link JsonWebToken} 不含原始 JWS 签名数据。
     * @param token 已解析的令牌
     * @return 新的验证器实例
     */
    public static <T extends JsonWebToken> TokenVerifier<T> createWithoutSignature(T token) {
        return new TokenVerifier<>(token);
    }

    /**
     * 添加默认检查项：
     * <ul>
     * <li>领域 URL（JWT {@code iss}）须已设置且与 {@link #realmUrl(java.lang.String)} 一致</li>
     * <li>Subject（JWT {@code sub}）须已定义</li>
     * <li>令牌类型（JWT {@code typ}）须为 {@code Bearer}，可通过 {@link #tokenType(List)} 修改</li>
     * <li>令牌须处于有效期内（{@code exp} 与 {@code nbf}）</li>
     * </ul>
     * @return 当前验证器实例
     */
    public TokenVerifier<T> withDefaultChecks()  {
        return withChecks(
          RealmUrlCheck.NULL_INSTANCE,
          TokenTypeCheck.INSTANCE_DEFAULT_TOKEN_TYPE,
          IS_ACTIVE
        );
    }

    private void removeCheck(Class<? extends Predicate<?>> checkClass) {
        for (Iterator<Predicate<? super T>> it = checks.iterator(); it.hasNext();) {
            if (it.next().getClass() == checkClass) {
                it.remove();
            }
        }
    }

    private void removeCheck(Predicate<? super T> check) {
        checks.remove(check);
    }

    @SuppressWarnings("unchecked")
    private <P extends Predicate<? super T>> TokenVerifier<T> replaceCheck(Class<? extends Predicate<?>> checkClass, boolean active, P... predicate) {
        removeCheck(checkClass);
        if (active) {
            checks.addAll(Arrays.asList(predicate));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private <P extends Predicate<? super T>> TokenVerifier<T> replaceCheck(Predicate<? super T> check, boolean active, P... predicate) {
        removeCheck(check);
        if (active) {
            checks.addAll(Arrays.asList(predicate));
        }
        return this;
    }

    /**
     * 在 {@link #verify()} 中追加执行给定检查项（保留已有检查）。
     * @param checks 追加的检查谓词
     * @return 当前验证器实例
     */
    @SafeVarargs
    public final TokenVerifier<T> withChecks(Predicate<? super T>... checks) {
        if (checks != null) {
            this.checks.addAll(Arrays.asList(checks));
        }
        return this;
    }

    /**
     * 设置 RSA/ECDSA 验签公钥。
     * @param publicKey 公钥
     * @return 当前验证器实例
     */
    public TokenVerifier<T> publicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     * 设置 HMAC 验签对称密钥。
     * @param secretKey 对称密钥
     * @return 当前验证器实例
     */
    public TokenVerifier<T> secretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    /**
     * 设置期望的领域 URL 并启用/更新 {@link RealmUrlCheck}。
     * @deprecated 仅为兼容旧版 {@code TokenVerifier} 保留。
     * @param realmUrl 领域 issuer URL
     * @return 当前验证器实例
     */
    public TokenVerifier<T> realmUrl(String realmUrl) {
        this.realmUrl = realmUrl;
        return replaceCheck(RealmUrlCheck.class, checkRealmUrl, new RealmUrlCheck(realmUrl));
    }

    /**
     * 启用或禁用令牌类型检查。
     * @deprecated 仅为兼容旧版 {@code TokenVerifier} 保留。
     * @param checkTokenType 是否检查 typ
     * @return 当前验证器实例
     */
    public TokenVerifier<T> checkTokenType(boolean checkTokenType) {
        this.checkTokenType = checkTokenType;
        return replaceCheck(TokenTypeCheck.class, this.checkTokenType, new TokenTypeCheck(expectedTokenType));
    }

    /**
     * 设置允许的令牌类型列表。
     * @param tokenTypes 允许的 typ 值列表
     * @return 当前验证器实例
     */
    public TokenVerifier<T> tokenType(List<String> tokenTypes) {
        this.expectedTokenType = tokenTypes;
        return replaceCheck(TokenTypeCheck.class, this.checkTokenType, new TokenTypeCheck(expectedTokenType));
    }

    /**
     * 启用或禁用令牌有效期检查。
     * @deprecated 仅为兼容旧版 {@code TokenVerifier} 保留。
     * @param checkActive 是否检查 exp/nbf
     * @return 当前验证器实例
     */
    public TokenVerifier<T> checkActive(boolean checkActive) {
        return replaceCheck(IS_ACTIVE, checkActive, IS_ACTIVE);
    }

    /**
     * 启用或禁用领域 URL 检查。
     * @deprecated 仅为兼容旧版 {@code TokenVerifier} 保留。
     * @param checkRealmUrl 是否检查 iss
     * @return 当前验证器实例
     */
    public TokenVerifier<T> checkRealmUrl(boolean checkRealmUrl) {
        this.checkRealmUrl = checkRealmUrl;
        return replaceCheck(RealmUrlCheck.class, this.checkRealmUrl, new RealmUrlCheck(realmUrl));
    }

    /**
     * 添加受众（aud）检查：令牌须包含给定受众之一。
     *
     * @param expectedAudiences 期望受众，可为 {@code null}
     * @return 当前验证器实例
     */
    public TokenVerifier<T> audience(String... expectedAudiences) {
        if (expectedAudiences == null || expectedAudiences.length == 0) {
            return this.replaceCheck(AudienceCheck.class, true, new AudienceCheck(null));
        }
        AudienceCheck[] audienceChecks = new AudienceCheck[expectedAudiences.length];
        for (int i = 0; i < expectedAudiences.length; ++i) {
            audienceChecks[i] = new AudienceCheck(expectedAudiences[i]);
        }
        return this.replaceCheck(AudienceCheck.class, true, audienceChecks);
    }

    /**
     * 添加 issuedFor（azp）检查：令牌授权方须与期望值一致。
     *
     * @param expectedIssuedFor 期望的 azp 值，不可为 null
     * @return 当前验证器实例
     */
    public TokenVerifier<T> issuedFor(String expectedIssuedFor) {
        return this.replaceCheck(IssuedForCheck.class, true, new IssuedForCheck(expectedIssuedFor));
    }

    /**
     * 解析 JWT 字符串为 {@link JWSInput} 及目标类型 POJO（若尚未解析）。
     *
     * @return 当前验证器实例
     * @throws VerificationException 解析失败时抛出
     */
    public TokenVerifier<T> parse() throws VerificationException {
        if (jws == null) {
            if (tokenString == null) {
                throw new VerificationException("Token not set");
            }

            try {
                jws = new JWSInput(tokenString);
            } catch (JWSInputException e) {
                throw new VerificationException("Failed to parse JWT", e);
            }


            try {
                token = jws.readJsonContent(clazz);
            } catch (JWSInputException e) {
                throw new VerificationException("Failed to read access token from JWT", e);
            }
        }
        return this;
    }

    /**
     * 返回已解析的令牌 POJO，必要时先执行 {@link #parse()}。
     *
     * @return 令牌对象
     * @throws VerificationException 解析失败时抛出
     */
    public T getToken() throws VerificationException {
        if (token == null) {
            parse();
        }
        return token;
    }

    /**
     * 返回 JWS 头部，必要时先解析。
     *
     * @return JWS 头
     * @throws VerificationException 解析失败时抛出
     */
    public JWSHeader getHeader() throws VerificationException {
        parse();
        return jws.getHeader();
    }

    /**
     * 验证 JWS 签名：优先使用 {@link SignatureVerifierContext}，否则按 alg 选择 RSA/ECDSA/HMAC。
     *
     * @throws VerificationException 验签失败或密钥/算法未配置时抛出
     */
    public void verifySignature() throws VerificationException {
        if (this.verifier != null) {
            try {
                if (!verifier.verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature())) {
                    throw new TokenSignatureInvalidException(token, "Invalid token signature");
                }
            } catch (Exception e) {
                throw new VerificationException(e);
            }
        } else {
            AlgorithmType algorithmType = getHeader().getAlgorithm().getType();
            if (null == algorithmType) {
                throw new VerificationException("Unknown or unsupported token algorithm");
            }
            switch (algorithmType) {
                case RSA:
                    if (publicKey == null) {
                        throw new VerificationException("Public key not set");
                    }
                    if (!RSAProvider.verify(jws, publicKey)) {
                        throw new TokenSignatureInvalidException(token, "Invalid token signature");
                    }
                    break;
                case ECDSA:
                    if (publicKey == null) {
                        throw new VerificationException("Public key not set");
                    }
                    if (!ECDSAProvider.verify(jws, publicKey)) {
                        throw new TokenSignatureInvalidException(token, "Invalid token signature");
                    }
                    break;
                case HMAC:
                    if (secretKey == null) {
                        throw new VerificationException("Secret key not set");
                    }
                    if (!HMACProvider.verify(jws, secretKey)) {
                        throw new TokenSignatureInvalidException(token, "Invalid token signature");
                    }
                    break;
                default:
                    throw new VerificationException("Unknown or unsupported token algorithm");
            }
        }
    }

    /**
     * 完整验证：解析（若需要）、验签并依次执行所有已注册检查谓词。
     *
     * @return 当前验证器实例
     * @throws VerificationException 任一步骤失败时抛出
     */
    public TokenVerifier<T> verify() throws VerificationException {
        if (getToken() == null) {
            parse();
        }
        if (jws != null) {
            verifySignature();
        }

        for (Predicate<? super T> check : checks) {
            if (! check.test(getToken())) {
                throw new VerificationException("JWT check failed for check " + check);
            }
        }

        return this;
    }

    /**
     * 将必选谓词包装为可选谓词：仍执行检查，但失败时仅记录日志并始终返回通过。
     * @param <T> 令牌类型
     * @param mandatoryPredicate 原始必选谓词
     * @return 可选谓词
     */
    public static <T extends JsonWebToken> Predicate<T> optional(final Predicate<T> mandatoryPredicate) {
        return new Predicate<T>() {
            @Override
            public boolean test(T t) throws VerificationException {
                try {
                    if (! mandatoryPredicate.test(t)) {
                        LOG.finer("[optional] predicate failed: " + mandatoryPredicate);
                    }

                    return true;
                } catch (VerificationException ex) {
                    LOG.log(Level.FINER, "[optional] predicate " + mandatoryPredicate + " failed.", ex);
                    return true;
                }
            }
        };
    }

    /**
     * 组合多个谓词为“或”逻辑：依次尝试，任一通过即整体通过。
     * @param <T> 令牌类型
     * @param predicates 候选谓词数组
     * @return 组合后的谓词
     */
    @SafeVarargs
    public static <T extends JsonWebToken> Predicate<T> alternative(final Predicate<? super T>... predicates) {
        return new Predicate<T>() {
            @Override
            public boolean test(T t) {
                for (Predicate<? super T> predicate : predicates) {
                    try {
                        if (predicate.test(t)) {
                            return true;
                        }

                        LOG.finer("[alternative] predicate failed: " + predicate);
                    } catch (VerificationException ex) {
                        LOG.log(Level.FINER, "[alternative] predicate " + predicate + " failed.", ex);
                    }
                }

                return false;
            }
        };
    }
}
