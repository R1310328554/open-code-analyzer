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
package org.keycloak.client.cli.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 单个 endpoint/realm 下的认证与会话配置。
 * <p>
 * 存储 clientId、access/refresh 令牌、过期时间、client secret 及多 client 令牌映射等字段，
 * 支持合并与深拷贝。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class RealmConfigData {

    /** 所属服务器 URL（运行时填充，非 JSON 持久化字段）。 */
    private String serverUrl;

    /** 所属 realm 名称（运行时填充）。 */
    private String realm;

    /** OAuth 客户端 ID。 */
    private String clientId;

    /** 当前 access token。 */
    private String token;

    /** 当前 refresh token。 */
    private String refreshToken;

    /** JWT client assertion 签名令牌。 */
    private String signingToken;

    /** 客户端密钥。 */
    private String secret;

    /** 认证时使用的 grant type。 */
    private String grantTypeForAuthentication;

    /** access token 过期时间戳（毫秒）。 */
    private Long expiresAt;

    /** refresh token 过期时间戳（毫秒）。 */
    private Long refreshExpiresAt;

    /** 签名令牌过期时间戳（毫秒）。 */
    private Long sigExpiresAt;

    /** 初始令牌（仅序列化非 null 值）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String initialToken;

    /** 多 client 令牌映射（clientId → token）。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> clients = new LinkedHashMap<String, String>();

    public String serverUrl() {
        return serverUrl;
    }

    public void serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String realm() {
        return realm;
    }

    public void realm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSigningToken() {
        return signingToken;
    }

    public void setSigningToken(String signingToken) {
        this.signingToken = signingToken;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getGrantTypeForAuthentication() {
        return grantTypeForAuthentication;
    }

    public void setGrantTypeForAuthentication(String grantTypeForAuthentication) {
        this.grantTypeForAuthentication = grantTypeForAuthentication;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public void setRefreshExpiresAt(Long refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public Long getSigExpiresAt() {
        return sigExpiresAt;
    }

    public void setSigExpiresAt(Long sigExpiresAt) {
        this.sigExpiresAt = sigExpiresAt;
    }

    public String getInitialToken() {
        return initialToken;
    }

    public void setInitialToken(String initialToken) {
        this.initialToken = initialToken;
    }

    public Map<String, String> getClients() {
        return clients;
    }

    /** 将源配置的非空字段合并到本实例（含 clients 映射）。 */
    public void merge(RealmConfigData source) {
        serverUrl = source.serverUrl;
        realm = source.realm;
        clientId = source.clientId;
        token = source.token;
        refreshToken = source.refreshToken;
        signingToken = source.signingToken;
        secret = source.secret;
        grantTypeForAuthentication = source.grantTypeForAuthentication;
        expiresAt = source.expiresAt;
        refreshExpiresAt = source.refreshExpiresAt;
        sigExpiresAt = source.sigExpiresAt;
        initialToken = source.initialToken;

        mergeClients(source);
    }

    /** 合并 clients 映射：空字符串值表示删除对应条目。 */
    private void mergeClients(RealmConfigData source) {
        if (source.clients != null) {
            if (clients == null) {
                clients = source.clients;
            } else {
                for (var entry : source.clients.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    if (!"".equals(val)) {
                        clients.put(key, val);
                    } else {
                        clients.remove(key);
                    }
                }
            }
        }
    }

    /** 仅合并令牌相关字段（用于 refresh token 轮换）。 */
    public void mergeRefreshTokens(RealmConfigData source) {
        token = source.token;
        refreshToken = source.refreshToken;
        expiresAt = source.expiresAt;
        refreshExpiresAt = source.refreshExpiresAt;
    }

    @Override
    public String toString() {
        try {
            return JsonSerialization.writeValueAsPrettyString(this);
        } catch (IOException e) {
            return super.toString() + " - Error: " + e.toString();
        }
    }

    /** 深拷贝本实例的所有字段。 */
    public RealmConfigData deepcopy() {
        RealmConfigData data = new RealmConfigData();
        data.serverUrl = serverUrl;
        data.realm = realm;
        data.clientId = clientId;
        data.token = token;
        data.refreshToken = refreshToken;
        data.signingToken = signingToken;
        data.secret = secret;
        data.grantTypeForAuthentication = grantTypeForAuthentication;
        data.expiresAt = expiresAt;
        data.refreshExpiresAt = refreshExpiresAt;
        data.sigExpiresAt = sigExpiresAt;
        data.initialToken = initialToken;
        data.clients = new LinkedHashMap<>(clients);
        return data;
    }
}
