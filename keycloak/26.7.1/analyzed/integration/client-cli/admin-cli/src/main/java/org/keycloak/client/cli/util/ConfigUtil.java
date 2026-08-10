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
package org.keycloak.client.cli.util;

import org.keycloak.OAuth2Constants;
import org.keycloak.client.cli.config.ConfigData;
import org.keycloak.client.cli.config.ConfigHandler;
import org.keycloak.client.cli.config.ConfigUpdateOperation;
import org.keycloak.client.cli.config.InMemoryConfigHandler;
import org.keycloak.client.cli.config.RealmConfigData;
import org.keycloak.representations.AccessTokenResponse;

/**
 * Keycloak 客户端 CLI 的配置读写与凭证校验工具。
 * <p>
 * 封装 {@link ConfigHandler} 访问、令牌持久化、服务器/域校验及内存配置切换等逻辑。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ConfigUtil {

    /** 未显式指定客户端时使用的默认 OAuth 客户端 ID。 */
    public static final String DEFAULT_CLIENT = "admin-cli";

    /** 全局配置处理器实例，由启动流程注入。 */
    private static ConfigHandler handler;

    /** 返回当前绑定的 {@link ConfigHandler}。 */
    public static ConfigHandler getHandler() {
        return handler;
    }

    /** 设置全局配置处理器。 */
    public static void setHandler(ConfigHandler handler) {
        ConfigUtil.handler = handler;
    }

    /**
     * 读取指定客户端的注册令牌。
     *
     * @param data 域配置数据
     * @param clientId 客户端 ID
     * @return 非空令牌字符串，否则 {@code null}
     */
    public static String getRegistrationToken(RealmConfigData data, String clientId) {
        String token = data.getClients().get(clientId);
        return token == null || token.length() == 0 ? null : token;
    }

    /**
     * 保存客户端注册令牌（{@code null} 写入空字符串）。
     *
     * @param data 域配置数据
     * @param clientId 客户端 ID
     * @param token 注册令牌
     */
    public static void setRegistrationToken(RealmConfigData data, String clientId, String token) {
        data.getClients().put(clientId, token == null ? "" : token);
    }

    /**
     * 将 OAuth 令牌响应合并写入本地配置。
     * <p>
     * 更新服务器 URL、域、访问/刷新令牌、签名密钥、过期时间及授权类型等字段。
     *
     * @param tokens 令牌响应
     * @param endpoint 服务器端点
     * @param realm 域名称
     * @param clientId 客户端 ID
     * @param signKey 客户端断言签名密钥
     * @param sigExpiresAt 签名密钥过期时间戳
     * @param secret 客户端密钥
     * @param grantTypeForAuthentication 认证所用 grant type
     */
    public static void saveTokens(AccessTokenResponse tokens, String endpoint, String realm, String clientId, String signKey, Long sigExpiresAt, String secret,
                                  String grantTypeForAuthentication) {
        handler.saveMergeConfig(config -> {
            config.setServerUrl(endpoint);
            config.setRealm(realm);

            RealmConfigData realmConfig = config.ensureRealmConfigData(endpoint, realm);
            realmConfig.setToken(tokens.getToken());
            realmConfig.setRefreshToken(tokens.getRefreshToken());
            realmConfig.setSigningToken(signKey);
            realmConfig.setSecret(secret);
            realmConfig.setExpiresAt(System.currentTimeMillis() + tokens.getExpiresIn() * 1000);
            if (realmConfig.getRefreshToken() != null) {
                realmConfig.setRefreshExpiresAt(tokens.getRefreshExpiresIn() == 0 ?
                        Long.MAX_VALUE : System.currentTimeMillis() + tokens.getRefreshExpiresIn() * 1000);
            }
            realmConfig.setSigExpiresAt(sigExpiresAt);
            realmConfig.setClientId(clientId);
            realmConfig.setGrantTypeForAuthentication(grantTypeForAuthentication);
        });
    }

    /**
     * 校验配置中是否已指定服务器与域（或外部令牌）。
     *
     * @param config 当前配置
     * @param cmd 命令名称，用于错误提示中的凭证配置指引
     */
    public static void checkServerInfo(ConfigData config, String cmd) {
        if (config.getServerUrl() == null) {
            throw new RuntimeException("No server specified. Use --server, or '" + cmd + " config credentials'.");
        }
        if (config.getRealm() == null && config.getExternalToken() == null) {
            throw new RuntimeException("No realm or token specified. Use --realm, --token, or '" + cmd + " config credentials'.");
        }
    }

    /**
     * 判断当前配置是否具备发起 API 请求的凭证。
     * <p>
     * 支持外部令牌、刷新令牌，或在缺少刷新令牌时对 {@code client_credentials} grant 的访问令牌回退。
     *
     * @param config 当前配置
     * @return 凭证可用时返回 {@code true}
     */
    public static boolean credentialsAvailable(ConfigData config) {
        // 缺少 refresh token 时仅支持 client_credentials grant
        boolean credsAvailable = config.getServerUrl() != null && (config.getExternalToken() != null || (config.getRealm() != null
                && config.sessionRealmConfigData() != null &&
                (config.sessionRealmConfigData().getRefreshToken() != null || (config.sessionRealmConfigData().getToken() != null && OAuth2Constants.CLIENT_CREDENTIALS.equals(config.sessionRealmConfigData().getGrantTypeForAuthentication())))
        ));
        return credsAvailable;
    }

    /** 通过已注册的 {@link ConfigHandler} 加载配置。 */
    public static ConfigData loadConfig() {
        if (handler == null) {
            throw new RuntimeException("No ConfigHandler set");
        }

        return handler.loadConfig();
    }

    /** 执行配置合并更新操作。 */
    public static void saveMergeConfig(ConfigUpdateOperation op) {
        if (handler == null) {
            throw new RuntimeException("No ConfigHandler set");
        }

        handler.saveMergeConfig(op);
    }

    /**
     * 将配置切换为内存处理器，便于测试或临时会话。
     *
     * @param config 要载入的内存配置
     */
    public static void setupInMemoryHandler(ConfigData config) {
        InMemoryConfigHandler memhandler = null;
        if (handler instanceof InMemoryConfigHandler) {
            memhandler = (InMemoryConfigHandler) handler;
        } else {
            memhandler = new InMemoryConfigHandler();
            handler = memhandler;
        }
        memhandler.setConfigData(config);
    }

    /**
     * 解析实际使用的客户端 ID（域配置优先，否则 {@link #DEFAULT_CLIENT}）。
     *
     * @param config 当前配置
     * @return 有效客户端 ID
     */
    public static String getEffectiveClientId(ConfigData config) {
        String clientId = DEFAULT_CLIENT;

        RealmConfigData realmData = config.sessionRealmConfigData();
        if (realmData != null && realmData.getClientId() != null) {
            clientId = realmData.getClientId();
        }
        return clientId;
    }
}
