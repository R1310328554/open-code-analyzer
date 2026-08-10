/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.common.util.Time;

/**
 * OAuth 2.0 设备授权流程的设备码（device code）缓存模型。
 * <p>序列化到单用途对象存储，跟踪授权状态、轮询间隔与 PKCE 参数。</p>
 *
 * @author <a href="mailto:h2-wada@nri.co.jp">Hiroyuki Wada</a>
 */
public class OAuth2DeviceCodeModel {

    private static final String REALM_ID = "rid";
    private static final String CLIENT_ID = "cid";
    private static final String EXPIRATION_NOTE = "exp";
    private static final String POLLING_INTERVAL_NOTE = "int";
    private static final String NONCE_NOTE = "nonce";
    private static final String SCOPE_NOTE = "scope";
    private static final String CLIENT_NOTIFICATION_TOKEN_NOTE = "cnt";
    private static final String AUTH_REQ_ID_NOTE = "ari";
    private static final String USER_SESSION_ID_NOTE = "uid";
    private static final String DENIED_NOTE = "denied";
    private static final String ADDITIONAL_PARAM_PREFIX = "additional_param_";
    private static final String CODE_CHALLENGE = "codeChallenge";
    private static final String CODE_CHALLENGE_METHOD = "codeChallengeMethod";

    private final RealmModel realm;
    private final String clientId;
    private final String deviceCode;
    private final int expiration;
    private final int pollingInterval;
    private final String clientNotificationToken;
    private final String authReqId;
    private final String scope;
    private final String nonce;
    private final String userSessionId;
    private final Boolean denied;
    private final Map<String, String> additionalParams;
    private final String codeChallenge;
    private final String codeChallengeMethod;

    /** 创建设备码模型并计算过期时间戳。 */
    public static OAuth2DeviceCodeModel create(RealmModel realm, ClientModel client,
                                               String deviceCode, String scope, String nonce, int expiresIn, int pollingInterval,
                                               String clientNotificationToken, String authReqId, Map<String, String> additionalParams, String codeChallenge, String codeChallengeMethod) {
        
        int expiration = Time.currentTime() + expiresIn;
        return new OAuth2DeviceCodeModel(realm, client.getClientId(), deviceCode, scope, nonce, expiration, pollingInterval,  clientNotificationToken, authReqId, null, null, additionalParams, codeChallenge, codeChallengeMethod);
    }

    /** 用户批准授权，关联用户会话 ID。 */
    public OAuth2DeviceCodeModel approve(String userSessionId) {
        return new OAuth2DeviceCodeModel(realm, clientId, deviceCode, scope, nonce, expiration,  pollingInterval, clientNotificationToken, authReqId, userSessionId, false, additionalParams, codeChallenge, codeChallengeMethod);
    }

    /** 用户批准授权并合并额外 OAuth 参数。 */
    public OAuth2DeviceCodeModel approve(String userSessionId, Map<String, String> additionalParams) {
        if (additionalParams != null) {
            this.additionalParams.putAll(additionalParams);
        }
        return new OAuth2DeviceCodeModel(realm, clientId, deviceCode, scope, nonce, expiration, pollingInterval, clientNotificationToken, authReqId, userSessionId, false, this.additionalParams, codeChallenge, codeChallengeMethod);
    }

    /** 用户拒绝设备授权请求。 */
    public OAuth2DeviceCodeModel deny() {
        return new OAuth2DeviceCodeModel(realm, clientId, deviceCode, scope, nonce, expiration, pollingInterval,  clientNotificationToken, authReqId, null, true, additionalParams, codeChallenge, codeChallengeMethod);
    }

    private OAuth2DeviceCodeModel(RealmModel realm, String clientId,
                                  String deviceCode, String scope, String nonce, int expiration, int pollingInterval, String clientNotificationToken,
                                  String authReqId, String userSessionId, Boolean denied, Map<String, String> additionalParams, String codeChallenge, String codeChallengeMethod) {
        this.realm = realm;
        this.clientId = clientId;
        this.deviceCode = deviceCode;
        this.scope = scope;
        this.nonce = nonce;
        this.expiration = expiration;
        this.pollingInterval = pollingInterval;
        this.clientNotificationToken = clientNotificationToken;
        this.authReqId = authReqId;
        this.userSessionId = userSessionId;
        this.denied = denied;
        this.additionalParams = additionalParams;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    /** 从缓存 Map 反序列化设备码；领域 ID 不匹配时返回 null。 */
    public static OAuth2DeviceCodeModel fromCache(RealmModel realm, String deviceCode, Map<String, String> data) {
        OAuth2DeviceCodeModel model = new OAuth2DeviceCodeModel(realm, deviceCode, data);

        if (!realm.getId().equals(data.get(REALM_ID))) {
            return null;
        }

        return model;
    }

    private OAuth2DeviceCodeModel(RealmModel realm, String deviceCode, Map<String, String> data) {
        this(realm, data.get(CLIENT_ID), deviceCode, data.get(SCOPE_NOTE), data.get(NONCE_NOTE),
            Integer.parseInt(data.get(EXPIRATION_NOTE)), Integer.parseInt(data.get(POLLING_INTERVAL_NOTE)), data.get(CLIENT_NOTIFICATION_TOKEN_NOTE),
                data.get(AUTH_REQ_ID_NOTE), data.get(USER_SESSION_ID_NOTE), Boolean.parseBoolean(data.get(DENIED_NOTE)), extractAdditionalParams(data), data.get(CODE_CHALLENGE), data.get(CODE_CHALLENGE_METHOD));
    }

    private static Map<String, String> extractAdditionalParams(Map<String, String> data) {
        Map<String, String> additionalParams = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().startsWith(ADDITIONAL_PARAM_PREFIX)) {
                additionalParams.put(entry.getKey().substring(ADDITIONAL_PARAM_PREFIX.length()), entry.getValue());
            }
        }
        return additionalParams;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getScope() {
        return scope;
    }

    public String getNonce() {
        return nonce;
    }

    public int getExpiration() {
        return expiration;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    public String getAuthReqId() {
        return authReqId;
    }

    public String getClientId() {
        return clientId;
    }

    /** 是否仍在等待用户授权（尚未关联用户会话）。 */
    public boolean isPending() {
        return userSessionId == null;
    }

    /** 用户是否已拒绝授权。 */
    public boolean isDenied() {
        return denied;
    }

    public String getUserSessionId() {
        return userSessionId;
    }

    /** 生成设备码在单用途存储中的缓存键。 */
    public static String createKey(String deviceCode) {
        return String.format("dc.%s", deviceCode);
    }

    public String serializeKey() {
        return createKey(deviceCode);
    }

    /** 轮询限流用的缓存键（设备码键加 {@code .polling} 后缀）。 */
    public String serializePollingKey() {
        return createKey(deviceCode) + ".polling";
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    /** 序列化为可写入单用途对象存储的键值 Map。 */
    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();

        result.put(REALM_ID, realm.getId());
        result.put(CLIENT_ID, clientId);

        if (clientNotificationToken != null) {
            result.put(CLIENT_NOTIFICATION_TOKEN_NOTE, clientNotificationToken);
        }
        if (authReqId != null) {
            result.put(AUTH_REQ_ID_NOTE, authReqId);
        }

        if (denied == null) {
            result.put(EXPIRATION_NOTE, String.valueOf(expiration));
            result.put(POLLING_INTERVAL_NOTE, String.valueOf(pollingInterval));
            result.put(SCOPE_NOTE, scope);
            result.put(NONCE_NOTE, nonce);
        } else if (denied) {
            result.put(EXPIRATION_NOTE, String.valueOf(expiration));
            result.put(POLLING_INTERVAL_NOTE, String.valueOf(pollingInterval));
            result.put(DENIED_NOTE, String.valueOf(denied));
        } else {
            result.put(EXPIRATION_NOTE, String.valueOf(expiration));
            result.put(POLLING_INTERVAL_NOTE, String.valueOf(pollingInterval));
            result.put(SCOPE_NOTE, scope);
            result.put(NONCE_NOTE, nonce);
            result.put(USER_SESSION_ID_NOTE, userSessionId);
        }
        if (codeChallenge != null)
            result.put(CODE_CHALLENGE, codeChallenge);
        if (codeChallengeMethod != null)
            result.put(CODE_CHALLENGE_METHOD, codeChallengeMethod);

        additionalParams.forEach((key, value) -> result.put(ADDITIONAL_PARAM_PREFIX + key, value));

        return result;
    }

    /** 获取授权请求 OAuth 参数（scope、nonce 与额外参数）。 */
    public MultivaluedMap<String, String> getParams() {
        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle(SCOPE_NOTE, scope);
        if (nonce != null) {
            params.putSingle(NONCE_NOTE, nonce);
        }
        this.additionalParams.forEach(params::putSingle);
        return params;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    /** 设备码是否已过期。 */
    public boolean isExpired() {
        return getExpiration() - Time.currentTime() < 0;
    }
}
