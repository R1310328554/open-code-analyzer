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
package org.keycloak.jose.jws;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.keycloak.Token;
import org.keycloak.TokenCategory;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.CekManagementProvider;
import org.keycloak.crypto.ClientSignatureVerifierProvider;
import org.keycloak.crypto.ContentEncryptionProvider;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.JOSE;
import org.keycloak.jose.JOSEParser;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.jose.jwe.JWEException;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.loader.PublicKeyStorageManager;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.TokenManager;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.LogoutTokenMapper;
import org.keycloak.representations.LogoutToken;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * {@link TokenManager} 默认实现：JWT/JWE 编码解码、签名算法解析与 Logout Token 构建。
 * <p>签名/加密算法按 Token 类别、客户端属性与领域默认值逐级回退。</p>
 */
public class DefaultTokenManager implements TokenManager {

    private static final Logger logger = Logger.getLogger(DefaultTokenManager.class);

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public DefaultTokenManager(KeycloakSession session) {
        this.session = session;
    }

    @Override
    /** 按 Token 类别选择签名算法并编码为 JWS 字符串。 */
    public String encode(Token token) {
        String signatureAlgorithm = signatureAlgorithm(token.getCategory());

        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, signatureAlgorithm);
        SignatureSignerContext signer = signatureProvider.signer();

        String type = type(token.getCategory());
        String encodedToken = new JWSBuilder().type(type).jsonContent(token).sign(signer);
        return encodedToken;
    }

    @Override
    /** 验证 JWS 签名并反序列化为指定 Token 类型；失败返回 null。 */
    public <T extends Token> T decode(String token, Class<T> clazz) {
        if (token == null) {
            return null;
        }

        try {
            JWSInput jws = new JWSInput(token);

            String signatureAlgorithm = jws.getHeader().getAlgorithm().name();

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, signatureAlgorithm);
            if (signatureProvider == null) {
                return null;
            }

            String kid = jws.getHeader().getKeyId();
            // 向后兼容：旧离线令牌/Cookie 头中无 KID，回退到领域活跃签名密钥
            if (kid == null) {
                logger.debugf("KID is null in token. Using the realm active key to verify token signature.");
                kid = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, signatureAlgorithm).getKid();
            }

            boolean valid = signatureProvider.verifier(kid).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());
            return valid ? jws.readJsonContent(clazz) : null;
        } catch (Exception e) {
            logger.debug("Failed to decode token", e);
            return null;
        }
    }

    @Override
    /** 解码客户端 JWT（支持 JWE 嵌套 JWS）；{@code allowNoneAlgorithm} 为 true 时允许 alg=none。 */
    public <T> T decodeClientJWT(String jwt, ClientModel client, BiConsumer<JOSE, ClientModel> jwtValidator, Class<T> clazz, boolean allowNoneAlgorithm) {
        if (jwt == null) {
            return null;
        }

        JOSE joseToken = JOSEParser.parse(jwt);

        jwtValidator.accept(joseToken, client);

        if (joseToken instanceof JWE) {
            try {
                Optional<KeyWrapper> activeKey;
                String kid = joseToken.getHeader().getKeyId();
                Stream<KeyWrapper> keys = session.keys().getKeysStream(session.getContext().getRealm());

                if (kid == null) {
                    activeKey = keys.filter(k -> KeyUse.ENC.equals(k.getUse()) && k.getPublicKey() != null)
                            .sorted(Comparator.comparingLong(KeyWrapper::getProviderPriority).reversed())
                            .findFirst();
                } else {
                    activeKey = keys
                            .filter(k -> KeyUse.ENC.equals(k.getUse()) && k.getKid().equals(kid)).findAny();
                }

                JWE jwe = JWE.class.cast(joseToken);
                Key privateKey = activeKey.map(KeyWrapper::getPrivateKey)
                        .orElseThrow(() -> new RuntimeException("Could not find private key for decrypting token"));

                jwe.getKeyStorage().setDecryptionKey(privateKey);

                byte[] content = jwe.verifyAndDecodeJwe().getContent();

                try {
                    JOSE jws = JOSEParser.parse(new String(content));

                    if (jws instanceof JWSInput) {
                        jwtValidator.accept(jws, client);
                        return verifyJWS(client, clazz, (JWSInput) jws, allowNoneAlgorithm);
                    }
                } catch (Exception e) {
                    logger.debug("Decrypted JWE content is not a valid JWS", e);
                    rejectUnsignedContentIfSignatureRequired(client);
                }

                return JsonSerialization.readValue(content, clazz);
            } catch (IOException cause) {
                throw new RuntimeException("Failed to deserialize JWT", cause);
            } catch (JWEException cause) {
                throw new RuntimeException("Failed to decrypt JWT", cause);
            }
        }

        return verifyJWS(client, clazz, (JWSInput) joseToken, allowNoneAlgorithm);
    }

    /** 使用客户端签名验证器校验 JWS 并反序列化。 */
    private <T> T verifyJWS(ClientModel client, Class<T> clazz, JWSInput jws, boolean allowNoneAlgorithm) {
        try {
            String signatureAlgorithm = jws.getHeader().getAlgorithm().name();
            ClientSignatureVerifierProvider signatureProvider = session.getProvider(ClientSignatureVerifierProvider.class, signatureAlgorithm);

            if (signatureProvider == null) {
                if (allowNoneAlgorithm && jws.getHeader().getAlgorithm().equals(org.keycloak.jose.jws.Algorithm.none)) {
                    return jws.readJsonContent(clazz);
                }
                return null;
            }

            boolean valid = signatureProvider.verifier(client, jws).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());
            return valid ? jws.readJsonContent(clazz) : null;
        } catch (Exception e) {
            logger.debug("Failed to decode token", e);
            return null;
        }
    }

    @Override
    /** 按 {@link TokenCategory} 解析签名算法名称。 */
    public String signatureAlgorithm(TokenCategory category) {
        switch (category) {
            case INTERNAL:
                return Constants.INTERNAL_SIGNATURE_ALGORITHM;
            case ADMIN:
                return getSignatureAlgorithm(null);
            case ACCESS:
                return getSignatureAlgorithm(OIDCConfigAttributes.ACCESS_TOKEN_SIGNED_RESPONSE_ALG);
            case ID:
            case LOGOUT:
                return getSignatureAlgorithm(OIDCConfigAttributes.ID_TOKEN_SIGNED_RESPONSE_ALG);
            case USERINFO:
                return getSignatureAlgorithm(OIDCConfigAttributes.USER_INFO_RESPONSE_SIGNATURE_ALG);
            case AUTHORIZATION_RESPONSE:
                return getSignatureAlgorithm(OIDCConfigAttributes.AUTHORIZATION_SIGNED_RESPONSE_ALG);
            default:
                throw new RuntimeException("Unknown token type");
        }
    }

    /** 客户端属性 → 领域默认 → 全局默认 三级回退解析签名算法。 */
    private String getSignatureAlgorithm(String clientAttribute) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = session.getContext().getClient();

        String algorithm = client != null && clientAttribute != null ? client.getAttribute(clientAttribute) : null;
        if (algorithm != null && !algorithm.equals("")) {
            return algorithm;
        }

        algorithm = realm.getDefaultSignatureAlgorithm();
        if (algorithm != null && !algorithm.equals("")) {
            return algorithm;
        }

        return Constants.DEFAULT_SIGNATURE_ALGORITHM;
    }

    @Override
    /** 先 JWS 签名，再按类别配置 JWE 加密（若需要）。 */
    public String encodeAndEncrypt(Token token) {
        String encodedToken = encode(token);
        if (isTokenEncryptRequired(token.getCategory())) {
            encodedToken = getEncryptedToken(token.getCategory(), encodedToken);
        }
        return encodedToken;
    }

    /** 确定 JWS {@code typ} 头值（如 JWT、logout+jwt、at+jwt）。 */
    private String type(TokenCategory category) {
        switch (category) {
            case ACCESS:
                ClientModel client = session.getContext().getClient();
                return client != null && OIDCAdvancedConfigWrapper.fromClientModel(client).isUseRfc9068AccessTokenHeaderType()
                    ? TokenUtil.TOKEN_TYPE_JWT_ACCESS_TOKEN
                    : "JWT";
            case LOGOUT:
                return TokenUtil.TOKEN_TYPE_JWT_LOGOUT_TOKEN;
            default:
                return "JWT";
        }
    }

    /** 判断该 Token 类别是否同时配置了 CEK 管理与内容加密算法。 */
    private boolean isTokenEncryptRequired(TokenCategory category) {
        if (cekManagementAlgorithm(category) == null) return false;
        if (encryptAlgorithm(category) == null) return false;
        return true;
    }

    /** 使用客户端加密公钥对已签名 JWT 进行 JWE 封装。 */
    private String getEncryptedToken(TokenCategory category, String encodedToken) {
        String encryptedToken = null;

        String algAlgorithm = cekManagementAlgorithm(category);
        String encAlgorithm = encryptAlgorithm(category);

        CekManagementProvider cekManagementProvider = session.getProvider(CekManagementProvider.class, algAlgorithm);
        JWEAlgorithmProvider jweAlgorithmProvider = cekManagementProvider.jweAlgorithmProvider();

        ContentEncryptionProvider contentEncryptionProvider = session.getProvider(ContentEncryptionProvider.class, encAlgorithm);
        JWEEncryptionProvider jweEncryptionProvider = contentEncryptionProvider.jweEncryptionProvider();

        ClientModel client = session.getContext().getClient();

        KeyWrapper keyWrapper = PublicKeyStorageManager.getClientPublicKeyWrapper(session, client, JWK.Use.ENCRYPTION, algAlgorithm);
        if (keyWrapper == null) {
            throw new RuntimeException("can not get encryption KEK");
        }
        Key encryptionKek = keyWrapper.getPublicKey();
        String encryptionKekId = keyWrapper.getKid();
        try {
            encryptedToken = TokenUtil.jweKeyEncryptionEncode(encryptionKek, encodedToken.getBytes(StandardCharsets.UTF_8), algAlgorithm, encAlgorithm, encryptionKekId, jweAlgorithmProvider, jweEncryptionProvider);
        } catch (JWEException e) {
            throw new RuntimeException(e);
        }
        return encryptedToken;
    }

    @Override
    /** 按 Token 类别返回 JWE 密钥管理算法（alg）。 */
    public String cekManagementAlgorithm(TokenCategory category) {
        if (category == null) return null;
        switch (category) {
            case INTERNAL:
                return Algorithm.AES;
            case ID:
            case LOGOUT:
                return getCekManagementAlgorithm(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ALG);
            case AUTHORIZATION_RESPONSE:
                return getCekManagementAlgorithm(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ALG);
            case USERINFO:
                return getCekManagementAlgorithm(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ALG);
            default:
                return null;
        }
    }

    /** 从客户端属性读取 CEK 管理算法。 */
    private String getCekManagementAlgorithm(String clientAttribute) {
        ClientModel client = session.getContext().getClient();
        String algorithm = client != null && clientAttribute != null ? client.getAttribute(clientAttribute) : null;
        if (algorithm != null && !algorithm.equals("")) {
            return algorithm;
        }
        return null;
    }

    @Override
    /** 按 Token 类别返回 JWE 内容加密算法（enc）。 */
    public String encryptAlgorithm(TokenCategory category) {
        if (category == null) return null;
        switch (category) {
            case ID:
                return getEncryptAlgorithm(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ENC, JWEConstants.A128CBC_HS256);
            case INTERNAL:
                return JWEConstants.A128CBC_HS256;
            case LOGOUT:
                return getEncryptAlgorithm(OIDCConfigAttributes.ID_TOKEN_ENCRYPTED_RESPONSE_ENC);
            case AUTHORIZATION_RESPONSE:
                return getEncryptAlgorithm(OIDCConfigAttributes.AUTHORIZATION_ENCRYPTED_RESPONSE_ENC);
            case USERINFO:
                return getEncryptAlgorithm(OIDCConfigAttributes.USER_INFO_ENCRYPTED_RESPONSE_ENC, JWEConstants.A128CBC_HS256);
            default:
                return null;
        }
    }

    /** 从客户端属性读取内容加密算法。 */
    private String getEncryptAlgorithm(String clientAttribute) {
        return getEncryptAlgorithm(clientAttribute, null);
    }

    /** 从客户端属性读取 enc 算法，无配置时使用 defaultValue。 */
    private String getEncryptAlgorithm(String clientAttribute, String defaultValue) {
        ClientModel client = session.getContext().getClient();
        String algorithm = client != null && clientAttribute != null ? client.getAttribute(clientAttribute) : null;
        if (algorithm != null && !algorithm.equals("")) {
            return algorithm;
        }
        return defaultValue;
    }

    /** 构建 Back-Channel Logout Token，含 sid、撤销离线令牌事件及协议映射器变换。 */
    public LogoutToken initLogoutToken(ClientModel client, UserModel user,
                                       AuthenticatedClientSessionModel clientSession) {
        LogoutToken token = new LogoutToken();
        token.id(SecretGenerator.getInstance().generateSecureID());
        token.issuedNow();
        // 规范 OpenID Connect Back-Channel Logout 1.0 建议 Logout Token 过期时间不超过两分钟
        // "OP 应使用较短过期时间， preferably 最多两分钟后过期"
        token.exp(Time.currentTime() + Duration.ofMinutes(2).getSeconds());
        token.issuer(clientSession.getNote(OIDCLoginProtocol.ISSUER));
        token.putEvents(TokenUtil.TOKEN_BACKCHANNEL_LOGOUT_EVENT, JsonSerialization.createObjectNode());
        token.addAudience(client.getClientId());

        OIDCAdvancedConfigWrapper oidcAdvancedConfigWrapper = OIDCAdvancedConfigWrapper.fromClientModel(client);
        if (oidcAdvancedConfigWrapper.isBackchannelLogoutSessionRequired()){
            token.setSid(clientSession.getUserSession().getId());
        }
        if (oidcAdvancedConfigWrapper.getBackchannelLogoutRevokeOfflineTokens()){
            token.putEvents(TokenUtil.TOKEN_BACKCHANNEL_LOGOUT_EVENT_REVOKE_OFFLINE_TOKENS, true);
        }
        token.setSubject(user.getId());

        // 若配置了 PairwiseSubMapper，通过 LogoutTokenMapper 调整 sub 声明
        ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionScopeParameter(clientSession, session);
        ProtocolMapperUtils
            .getSortedProtocolMappers(session, clientSessionCtx)
            .filter(mapperEntry -> mapperEntry.getValue() instanceof LogoutTokenMapper)
            .forEach(mapperEntry -> {
                LogoutTokenMapper mapper = (LogoutTokenMapper) mapperEntry.getValue();
                mapper.transformLogoutToken(token, mapperEntry.getKey(), session, clientSession.getUserSession(), clientSessionCtx);
            });

        return token;
    }

    /** 客户端要求 request object 签名但 JWE 解密内容非 JWS 时抛出异常。 */
    private void rejectUnsignedContentIfSignatureRequired(ClientModel client) {
        String requestedSignatureAlgorithm = OIDCAdvancedConfigWrapper.fromClientModel(client).getRequestObjectSignatureAlg();
        if (requestedSignatureAlgorithm != null) {
            throw new RuntimeException("Request object signature algorithm is required but decrypted JWE content is not a signed JWS");
        }
    }
}
