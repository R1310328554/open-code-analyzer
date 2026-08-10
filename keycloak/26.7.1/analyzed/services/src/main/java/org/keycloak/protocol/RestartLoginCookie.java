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

package org.keycloak.protocol;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

import org.keycloak.Token;
import org.keycloak.TokenCategory;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * 登录重启 Cookie 令牌：在客户端超时时保存认证会话上下文，以便用户重新发起登录时恢复流程。
 * <p>以 JWE 加密后写入 {@link CookieType#AUTH_RESTART}（{@code KC_RESTART}）Cookie。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RestartLoginCookie implements Token {
    private static final Logger logger = Logger.getLogger(RestartLoginCookie.class);
    /** Cookie 名称：{@code KC_RESTART}。 */
    public static final String KC_RESTART = "KC_RESTART";

    /** JSON 字段：客户端 ID（{@code cid}）。 */
    @JsonProperty("cid")
    protected String clientId;

    /** JSON 字段：认证协议/方法（{@code pty}）。 */
    @JsonProperty("pty")
    protected String authMethod;

    /** JSON 字段：重定向 URI（{@code ruri}）。 */
    @JsonProperty("ruri")
    protected String redirectUri;

    /** JSON 字段：认证动作（{@code act}）。 */
    @JsonProperty("act")
    protected String action;

    /** JSON 字段：客户端备注键值对（{@code notes}）。 */
    @JsonProperty("notes")
    protected Map<String, String> notes = new HashMap<>();

    /** @deprecated 向后兼容字段，已弃用 */
    @Deprecated // Backwards compatibility
    @JsonProperty("cs")
    protected String cs;

    public Map<String, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, String> notes) {
        this.notes = notes;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public RestartLoginCookie() {
    }

    /** 从认证会话快照构造重启 Cookie 内容。 */
    public RestartLoginCookie(AuthenticationSessionModel authSession) {
        this.action = authSession.getAction();
        this.clientId = authSession.getClient().getClientId();
        this.authMethod = authSession.getProtocol();
        this.redirectUri = authSession.getRedirectUri();
        for (Map.Entry<String, String> entry : authSession.getClientNotes().entrySet()) {
            notes.put(entry.getKey(), entry.getValue());
        }
    }

    /** 编码加密当前认证会话并写入 KC_RESTART Cookie。 */
    public static void setRestartCookie(KeycloakSession session, AuthenticationSessionModel authSession) {
        RestartLoginCookie restart = new RestartLoginCookie(authSession);
        String encoded = encodeAndEncrypt(session, restart);
        session.getProvider(CookieProvider.class).set(CookieType.AUTH_RESTART, encoded);
    }

    /** 使 KC_RESTART Cookie 过期。 */
    public static void expireRestartCookie(KeycloakSession session) {
        session.getProvider(CookieProvider.class).expire(CookieType.AUTH_RESTART);
    }

    /** @return KC_RESTART Cookie 原始值，不存在时返回 {@code null} */
    public static String getRestartCookie(KeycloakSession session){
        String cook = session.getProvider(CookieProvider.class).get(CookieType.AUTH_RESTART);
        if (cook ==  null) {
            logger.debug("KC_RESTART cookie doesn't exist");
            return null;
        }
        return cook;
    }

    /**
     * 解密 Cookie 并重建认证会话；Cookie 中客户端须与 {@code expectedClientId} 一致。
     * @param session Keycloak 会话
     * @param realm Realm 模型
     * @param rootSession 根认证会话，为 {@code null} 时新建
     * @param expectedClientId URL 请求中的客户端 ID
     * @param encodedCookie 加密的 Cookie 值
     * @return 新建的 {@link AuthenticationSessionModel}，校验失败时返回 {@code null}
     */
    public static AuthenticationSessionModel restartSession(KeycloakSession session, RealmModel realm,
                                                            RootAuthenticationSessionModel rootSession, String expectedClientId,
                                                            String encodedCookie) throws Exception {
        RestartLoginCookie cookie = decryptAndDecode(session, encodedCookie);
        if (cookie == null) {
            logger.debug("Failed to verify encoded RestartLoginCookie");
            return null;
        }

        ClientModel client = realm.getClientByClientId(cookie.getClientId());
        if (client == null) return null;

        // 仅当 Cookie 中的客户端与 URL 请求客户端一致时才重启会话
        if (!client.getClientId().equals(expectedClientId)) {
            logger.debugf("Skip restarting from the KC_RESTART. Clients doesn't match: Cookie client: %s, Requested client: %s", client.getClientId(), expectedClientId);
            return null;
        }

        // 无根会话时创建全新认证会话
        if (rootSession == null) {
            rootSession = new AuthenticationSessionManager(session).createAuthenticationSession(realm, true);
        }

        AuthenticationSessionModel authSession = rootSession.createAuthenticationSession(client);
        authSession.setProtocol(cookie.getAuthMethod());
        authSession.setRedirectUri(cookie.getRedirectUri());
        authSession.setAction(cookie.getAction());
        for (Map.Entry<String, String> entry : cookie.getNotes().entrySet()) {
            authSession.setClientNote(entry.getKey(), entry.getValue());
        }

        return authSession;
    }

    /** 解密并解码 KC_RESTART 令牌；兼容旧版未带 kid 的格式。 */
    public static RestartLoginCookie decryptAndDecode(KeycloakSession session, String encodedToken) {
        try {
            String kid = new JWE(encodedToken).getHeader().getKeyId();
            if (kid != null) {
                // 新方式：通过 JWE 头 kid 查找加密密钥
                String algAlgorithm = session.tokens().cekManagementAlgorithm(TokenCategory.INTERNAL);
                RealmModel realm = session.getContext().getRealm();
                KeyWrapper encKey = session.keys().getKey(realm, kid, KeyUse.ENC, algAlgorithm);
                if (encKey == null) {
                    return null;
                }
                byte[] contentBytes = TokenUtil.jweDirectVerifyAndDecode(encKey.getSecretKey(), null, encodedToken);
                String jwt = new String(contentBytes, StandardCharsets.UTF_8);
                return session.tokens().decode(jwt, RestartLoginCookie.class);
            } else {
                // 旧方式：使用当前活跃 ENC/SIG 密钥解密
                String sigAlgorithm = session.tokens().signatureAlgorithm(TokenCategory.INTERNAL);
                String algAlgorithm = session.tokens().cekManagementAlgorithm(TokenCategory.INTERNAL);
                SecretKey encKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.ENC, algAlgorithm).getSecretKey();
                SecretKey signKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, sigAlgorithm).getSecretKey();
                byte[] contentBytes = TokenUtil.jweDirectVerifyAndDecode(encKey, signKey, encodedToken);
                String jwt = new String(contentBytes, StandardCharsets.UTF_8);
                return session.tokens().decode(jwt, RestartLoginCookie.class);
            }
        } catch (Exception e) {
            // 可能是更旧版本的明文/不同格式 Cookie
            return session.tokens().decode(encodedToken, RestartLoginCookie.class);
        }
    }

    /** 将重启 Cookie 编码为 JWT 并用 JWE 直接加密。 */
    public static String encodeAndEncrypt(KeycloakSession session, RestartLoginCookie cookie) {
        try {
            String algAlgorithm = session.tokens().cekManagementAlgorithm(cookie.getCategory());
            KeyWrapper encKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.ENC, algAlgorithm);

            String encodedJwt = session.tokens().encode(cookie);
            return TokenUtil.jweDirectEncode(encKey.getKid(), encKey.getSecretKey(), null, encodedJwt.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error encoding cookie.", e);
        }
    }

    /** @return 内部令牌类别 {@link TokenCategory#INTERNAL} */
    @Override
    public TokenCategory getCategory() {
        return TokenCategory.INTERNAL;
    }
}
