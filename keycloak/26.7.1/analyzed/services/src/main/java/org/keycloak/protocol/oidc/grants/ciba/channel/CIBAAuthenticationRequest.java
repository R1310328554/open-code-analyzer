/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */
package org.keycloak.protocol.oidc.grants.ciba.channel;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

import org.keycloak.OAuth2Constants;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jwe.JWEException;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.Urls;
import org.keycloak.util.TokenUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CIBA 认证请求：由消费设备（CD）发起，可序列化为 JWE 与认证设备（AD）交换。
 * <p>作为 {@code auth_req_id} 的载荷，绑定后台认证流程与用户会话。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class CIBAAuthenticationRequest extends JsonWebToken {

    /**
     * 将 JWE 格式的 {@code auth_req_id} 反序列化为 {@link CIBAAuthenticationRequest}。
     *
     * @param session Keycloak 会话
     * @param jwe JWE 编码的认证请求
     * @return 解析后的认证请求实例
     * @throws Exception 解密或解码失败时
     */
    public static CIBAAuthenticationRequest deserialize(KeycloakSession session, String jwe) {
        SecretKey aesKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.ENC, Algorithm.AES).getSecretKey();
        SecretKey hmacKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, Constants.INTERNAL_SIGNATURE_ALGORITHM).getSecretKey();

        try {
            byte[] contentBytes = TokenUtil.jweDirectVerifyAndDecode(aesKey, hmacKey, jwe);
            jwe = new String(contentBytes, StandardCharsets.UTF_8);
        } catch (JWEException e) {
            throw new RuntimeException("Error decoding auth_req_id.", e);
        }

        return session.tokens().decode(jwe, CIBAAuthenticationRequest.class);
    }

    /** 会话状态声明名（与 ID Token 一致） */
    public static final String SESSION_STATE = IDToken.SESSION_STATE;
    /** 认证结果标识声明名 */
    public static final String AUTH_RESULT_ID = "auth_result_id";

    /** 请求的作用域 */
    @JsonProperty(OAuth2Constants.SCOPE)
    protected String scope;

    /** 认证结果 ID，用于关联通道回调 */
    @JsonProperty(AUTH_RESULT_ID)
    protected String authResultId;

    /** 绑定消息 */
    @JsonProperty(CibaGrantType.BINDING_MESSAGE)
    protected String bindingMessage;

    /** ACR 值 */
    @JsonProperty(OAuth2Constants.ACR_VALUES)
    protected String acrValues;

    /** 发起请求的客户端（不序列化到 JWT） */
    @JsonIgnore
    protected ClientModel client;

    /** 客户端通知令牌（不序列化） */
    @JsonIgnore
    protected String clientNotificationToken;

    /** 目标用户（不序列化） */
    @JsonIgnore
    protected UserModel user;

    /** 无参构造，供反射/Jackson 使用 */
    public CIBAAuthenticationRequest() {
        // for reflection
    }

    /**
     * 为指定用户与客户端构造新的 CIBA 认证请求并填充标准 JWT 声明。
     * @param session Keycloak 会话
     * @param user 目标用户
     * @param client OAuth 客户端
     */
        id(KeycloakModelUtils.generateId());
        issuedNow();
        RealmModel realm = session.getContext().getRealm();
        issuer(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        audience(getIssuer());
        subject(user.getId());
        issuedFor(client.getClientId());
        setAuthResultId(KeycloakModelUtils.generateId());
        setClient(client);
        setUser(user);
    }

    /** @return 作用域 */
    public String getScope() {
        return scope;
    }

    /** 设置作用域 */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** @return 认证结果 ID */
    public String getAuthResultId() {
        return authResultId;
    }

    /** 设置认证结果 ID */
    public void setAuthResultId(String authResultId) {
        this.authResultId = authResultId;
    }

    /** @return 绑定消息 */
    public String getBindingMessage() {
        return bindingMessage;
    }

    /** 设置绑定消息 */
    public void setBindingMessage(String binding_message) {
        this.bindingMessage = binding_message;
    }

    /** @return ACR 值 */
    public String getAcrValues() {
        return acrValues;
    }

    /** 设置 ACR 值 */
    public void setAcrValues(String acrValues) {
        this.acrValues = acrValues;
    }

    /**
     * 将本实例序列化为 JWE（JWS 签名后再 JWE 加密）。
     *
     * @param session Keycloak 会话
     * @return JWE 字符串，用作 auth_req_id
     */
    public String serialize(KeycloakSession session) {
        try {
            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, Constants.INTERNAL_SIGNATURE_ALGORITHM);
            SignatureSignerContext signer = signatureProvider.signer();
            String encodedJwt = new JWSBuilder().type("JWT").jsonContent(this).sign(signer);
            SecretKey aesKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.ENC, Algorithm.AES).getSecretKey();
            SecretKey hmacKey = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, Constants.INTERNAL_SIGNATURE_ALGORITHM).getSecretKey();

            return TokenUtil.jweDirectEncode(aesKey, hmacKey, encodedJwt.getBytes(StandardCharsets.UTF_8));
        } catch (JWEException e) {
            throw new RuntimeException("Error encoding auth_req_id.", e);
        }
    }

    /** 设置关联客户端 */
    public void setClient(ClientModel client) {
        this.client = client;
    }

    /** @return 关联客户端 */
    public ClientModel getClient() {
        return client;
    }

    /** @return 客户端通知令牌 */
    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    /** 设置客户端通知令牌 */
    public void setClientNotificationToken(String clientNotificationToken) {
        this.clientNotificationToken = clientNotificationToken;
    }

    /** 设置目标用户 */
    public void setUser(UserModel user) {
        this.user = user;
    }

    /** @return 目标用户 */
    public UserModel getUser() {
        return user;
    }
}
