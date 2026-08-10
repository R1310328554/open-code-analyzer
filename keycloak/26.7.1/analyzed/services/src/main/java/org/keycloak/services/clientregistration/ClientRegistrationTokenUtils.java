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

package org.keycloak.services.clientregistration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.TokenCategory;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.ClientInitialAccessModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.TokenManager.TokenRevocationCheck;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.Urls;
import org.keycloak.services.clientregistration.policy.RegistrationAuth;
import org.keycloak.util.TokenUtil;

/**
 * 客户端注册访问令牌工具类。
 * <p>负责创建、更新与校验初始访问令牌（Initial Access Token）及注册访问令牌（Registration Access Token），并封装 JWT 签名与验证逻辑。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistrationTokenUtils {

    /** JWT {@code typ} 声明：初始访问令牌类型 */
    public static final String TYPE_INITIAL_ACCESS_TOKEN = "InitialAccessToken";
    /** JWT {@code typ} 声明：注册访问令牌类型 */
    public static final String TYPE_REGISTRATION_ACCESS_TOKEN = "RegistrationAccessToken";

    /**
     * 在签名密钥轮换后重新签发注册访问令牌。
     * @param session Keycloak 会话
     * @param auth 当前注册认证上下文
     * @return 更新后的令牌字符串
     */
    public static String updateTokenSignature(KeycloakSession session, ClientRegistrationAuth auth) {
        String algorithm = session.tokens().signatureAlgorithm(TokenCategory.INTERNAL);
        SignatureSignerContext signer = session.getProvider(SignatureProvider.class, algorithm).signer();

        if (signer.getKid().equals(auth.getKid())) {
            return auth.getToken();
        } else {
            RegistrationAccessToken regToken = new RegistrationAccessToken();
            regToken.setRegistrationAuth(auth.getRegistrationAuth().toString().toLowerCase());

            regToken.type(auth.getJwt().getType());
            regToken.id(auth.getJwt().getId());
            regToken.issuedNow();
            regToken.issuer(auth.getJwt().getIssuer());
            regToken.audience(auth.getJwt().getIssuer());

            String token = new JWSBuilder().jsonContent(regToken).sign(signer);
            return token;
        }
    }

    /**
     * 为客户端生成新的注册访问令牌（使用当前领域）。
     * @param session Keycloak 会话
     * @param client 目标客户端
     * @param registrationAuth 注册授权级别
     * @param webOrigins 允许的 Web 来源列表
     * @return 编码后的注册访问令牌
     */
    public static String updateRegistrationAccessToken(KeycloakSession session, ClientModel client, RegistrationAuth registrationAuth, List<String> webOrigins) {
        return updateRegistrationAccessToken(session, session.getContext().getRealm(), client, registrationAuth, webOrigins);
    }

    /**
     * 为客户端生成新的注册访问令牌。
     * @param session Keycloak 会话
     * @param realm 目标领域
     * @param client 目标客户端
     * @param registrationAuth 注册授权级别
     * @param webOrigins 允许的 Web 来源列表
     * @return 编码后的注册访问令牌
     */
    public static String updateRegistrationAccessToken(KeycloakSession session, RealmModel realm, ClientModel client, RegistrationAuth registrationAuth, List<String> webOrigins) {
        String id = SecretGenerator.getInstance().generateSecureID();
        client.setRegistrationToken(id);

        RegistrationAccessToken regToken = new RegistrationAccessToken();
        regToken.setRegistrationAuth(registrationAuth.toString().toLowerCase());

        return setupToken(regToken, session, realm, id, TYPE_REGISTRATION_ACCESS_TOKEN, 0, webOrigins);
    }

    /**
     * 根据初始访问模型创建初始访问令牌。
     * @param session Keycloak 会话
     * @param realm 目标领域
     * @param model 初始访问配置模型
     * @param webOrigins 允许的 Web 来源列表
     * @return 编码后的初始访问令牌
     */
    public static String createInitialAccessToken(KeycloakSession session, RealmModel realm, ClientInitialAccessModel model, List<String> webOrigins) {
        InitialAccessToken initialToken = new InitialAccessToken();
        return setupToken(initialToken, session, realm, model.getId(), TYPE_INITIAL_ACCESS_TOKEN, model.getExpiration() > 0 ? model.getTimestamp() + model.getExpiration() : 0, webOrigins);
    }

    /**
     * 校验注册相关访问令牌的有效性。
     * @param session Keycloak 会话
     * @param realm 目标领域
     * @param token 待校验的 JWT 字符串
     * @return 校验结果（成功时含 kid 与 JWT，失败时含异常）
     */
    public static TokenVerification verifyToken(KeycloakSession session, RealmModel realm, String token) {
        if (token == null) {
            return TokenVerification.error(new RuntimeException("Missing token"));
        }

        String kid;
        AccessToken jwt;
        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(token, AccessToken.class)
                    .withChecks(new TokenVerifier.RealmUrlCheck(getIssuer(session, realm)), TokenVerifier.IS_ACTIVE, new TokenRevocationCheck(session));

            SignatureVerifierContext verifierContext = CryptoUtils.getSignatureProvider(session, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
            verifier.verifierContext(verifierContext);

            kid = verifierContext.getKid();

            verifier.verify();

            jwt = verifier.getToken();
        } catch (VerificationException e) {
            return TokenVerification.error(new RuntimeException("Failed decode token", e));
        }

        if (!(TokenUtil.TOKEN_TYPE_BEARER.equals(jwt.getType()) ||
                TYPE_INITIAL_ACCESS_TOKEN.equals(jwt.getType()) ||
                TYPE_REGISTRATION_ACCESS_TOKEN.equals(jwt.getType()))) {
            return TokenVerification.error(new RuntimeException("Invalid type of token"));
        }

        return TokenVerification.success(kid, jwt);
    }

    /** 填充 JWT 标准声明并编码为令牌字符串 */
    private static String setupToken(JsonWebToken jwt, KeycloakSession session, RealmModel realm, String id, String type, long expiration, List<String> webOrigins) {
        String issuer = getIssuer(session, realm);

        jwt.type(type);
        jwt.id(id);
        jwt.issuedNow();
        jwt.exp(expiration);
        jwt.issuer(issuer);
        jwt.audience(issuer);

        Set<String> webOriginsSet = webOrigins != null ? new HashSet<>(webOrigins) : null;
        if (jwt instanceof InitialAccessToken) {
            ((InitialAccessToken) jwt).setAllowedOrigins(webOriginsSet);
        } else if (jwt instanceof RegistrationAccessToken) {
            ((RegistrationAccessToken) jwt).setAllowedOrigins(webOriginsSet);
        }

        return session.tokens().encode(jwt);
    }

    /** @return 领域令牌签发者 URI */
    private static String getIssuer(KeycloakSession session, RealmModel realm) {
        return Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName());
    }

    /** 令牌校验结果封装 */
    protected static class TokenVerification {

        private final String kid;
        private final AccessToken jwt;
        private final RuntimeException error;

        /** @return 校验成功的结果 */
        public static TokenVerification success(String kid, AccessToken jwt) {
            return new TokenVerification(kid, jwt, null);
        }

        /** @return 校验失败的结果 */
        public static TokenVerification error(RuntimeException error) {
            return new TokenVerification(null,null, error);
        }

        private TokenVerification(String kid, AccessToken jwt, RuntimeException error) {
            this.kid = kid;
            this.jwt = jwt;
            this.error = error;
        }

        /** @return 签名密钥 kid */
        public String getKid() {
            return kid;
        }

        /** @return 解析后的 JWT */
        public AccessToken getJwt() {
            return jwt;
        }

        /** @return 校验失败时的异常 */
        public RuntimeException getError() {
            return error;
        }
    }

}
