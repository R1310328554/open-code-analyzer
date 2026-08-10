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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;

import org.keycloak.client.cli.config.ConfigData;
import org.keycloak.client.cli.config.RealmConfigData;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.util.BasicAuthHelper;
import org.keycloak.util.JsonSerialization;

import static java.lang.System.currentTimeMillis;

import static org.keycloak.client.cli.util.ConfigUtil.checkServerInfo;
import static org.keycloak.client.cli.util.ConfigUtil.saveMergeConfig;
import static org.keycloak.client.cli.util.HttpUtil.APPLICATION_FORM_URL_ENCODED;
import static org.keycloak.client.cli.util.HttpUtil.APPLICATION_JSON;
import static org.keycloak.client.cli.util.HttpUtil.doPost;
import static org.keycloak.client.cli.util.HttpUtil.urlencode;

/**
 * Keycloak 客户端 CLI 的 OAuth/OIDC 认证工具类。
 * <p>
 * 负责 access token 自动刷新、密码/密钥/JWT 等多种 grant type 的令牌获取，
 * 以及 keystore 私钥签名的 client assertion 生成。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AuthUtil {

    /** 令牌过期缓冲时间（毫秒）：距过期不足此值时触发刷新。 */
    public static final int AUTH_BUFFER_TIME = 5000;

    /**
     * 确保返回有效 access token：优先使用外部令牌，否则检查过期并自动刷新。
     *
     * @param config 当前 CLI 配置
     * @param cmd 命令名（用于过期时的重新登录提示）
     * @return 可用的 access token
     */
    public static String ensureToken(ConfigData config, String cmd) {
        if (config.getExternalToken() != null) {
            return config.getExternalToken();
        }

        checkServerInfo(config, cmd);

        RealmConfigData realmConfig = config.sessionRealmConfigData();

        long now = currentTimeMillis();

        // 检查 access_token 过期时间，距过期不足 5 秒则刷新
        // if it's less than 5s to expiry, renew it
        if (realmConfig.getExpiresAt() - now < AUTH_BUFFER_TIME) {

            // 检查 refresh_token 是否即将过期，若是则要求重新登录
            if (realmConfig.getRefreshExpiresAt() != null && realmConfig.getRefreshExpiresAt() - now < AUTH_BUFFER_TIME) {
                throw new RuntimeException("Session has expired. Login again with '" + cmd + " config credentials'");
            }

            if (realmConfig.getSigExpiresAt() != null && realmConfig.getSigExpiresAt() - now < AUTH_BUFFER_TIME) {
                throw new RuntimeException("Session has expired. Login again with '" + cmd + " config credentials'");
            }

            try {
                String authorization = null;
                StringBuilder body = new StringBuilder();
                if (realmConfig.getRefreshToken() != null) {
                    body.append("grant_type=refresh_token")
                            .append("&refresh_token=").append(realmConfig.getRefreshToken());
                } else {
                    body.append("grant_type=").append(realmConfig.getGrantTypeForAuthentication());
                }

                body.append("&client_id=").append(urlencode(realmConfig.getClientId()));

                if (realmConfig.getSigningToken() != null) {
                    body.append("&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                            .append("&client_assertion=").append(realmConfig.getSigningToken());
                } else if (realmConfig.getSecret() != null) {
                    authorization = BasicAuthHelper.RFC6749.createHeader(realmConfig.getClientId(), realmConfig.getSecret());
                }

                try (InputStream result = doPost(realmConfig.serverUrl() + "/realms/" + realmConfig.realm() + "/protocol/openid-connect/token",
                        APPLICATION_FORM_URL_ENCODED, APPLICATION_JSON, body.toString(), authorization)) {

                    AccessTokenResponse token = JsonSerialization.readValue(result, AccessTokenResponse.class);

                    saveMergeConfig(cfg -> {
                        RealmConfigData realmData = cfg.sessionRealmConfigData();
                        realmData.setToken(token.getToken());
                        realmData.setRefreshToken(token.getRefreshToken());
                        realmData.setExpiresAt(currentTimeMillis() + token.getExpiresIn() * 1000);
                        if (token.getRefreshToken() != null) {
                            realmData.setRefreshExpiresAt(currentTimeMillis() + token.getRefreshExpiresIn() * 1000);
                        }
                    });
                    return token.getToken();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to refresh access token - " + e.getMessage(), e);
            }
        }

        return realmConfig.getToken();
    }

    /** 使用 password grant 获取 access/refresh token。 */
    public static AccessTokenResponse getAuthTokens(String server, String realm, String user, String password, String clientId) {
        StringBuilder body = new StringBuilder();
        try {
            body.append("grant_type=password")
                    .append("&username=").append(urlencode(user))
                    .append("&password=").append(urlencode(password))
                    .append("&client_id=").append(urlencode(clientId));

            try (InputStream result = doPost(server + "/realms/" + realm + "/protocol/openid-connect/token",
                    APPLICATION_FORM_URL_ENCODED, APPLICATION_JSON, body.toString(), null)) {
                return JsonSerialization.readValue(result, AccessTokenResponse.class);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected error: ", e);
        } catch (IOException e) {
            throw new RuntimeException("Error receiving response: ", e);
        }
    }

    /** 使用 JWT client assertion 获取令牌（支持 password 或 client_credentials grant）。 */
    public static AccessTokenResponse getAuthTokensByJWT(String server, String realm, String user, String password, String clientId, String signedRequestToken) {
        StringBuilder body = new StringBuilder();
        try {
            body.append("client_id=").append(urlencode(clientId))
                    .append("&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                    .append("&client_assertion=").append(signedRequestToken);

            if (user != null) {
                if (password == null) {
                    throw new RuntimeException("No password specified");
                }
                body.append("&grant_type=password")
                        .append("&username=").append(urlencode(user))
                        .append("&password=").append(urlencode(password));
            } else {
                body.append("&grant_type=client_credentials");
            }

            try (InputStream result = doPost(server + "/realms/" + realm + "/protocol/openid-connect/token",
                    APPLICATION_FORM_URL_ENCODED, APPLICATION_JSON, body.toString(), null)) {
                return JsonSerialization.readValue(result, AccessTokenResponse.class);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected error: ", e);
        } catch (IOException e) {
            throw new RuntimeException("Error receiving response: ", e);
        }
    }

    /** 使用 client secret（Basic Auth）获取令牌（支持 password 或 client_credentials grant）。 */
    public static AccessTokenResponse getAuthTokensBySecret(String server, String realm, String user, String password, String clientId, String secret) {

        StringBuilder body = new StringBuilder();
        try {
            if (user != null) {
                if (password == null) {
                    throw new RuntimeException("No password specified");
                }

                body.append("client_id=").append(urlencode(clientId))
                        .append("&grant_type=password")
                        .append("&username=").append(urlencode(user))
                        .append("&password=").append(urlencode(password));
            } else {
                body.append("grant_type=client_credentials");
            }

            try (InputStream result = doPost(server + "/realms/" + realm + "/protocol/openid-connect/token",
                    APPLICATION_FORM_URL_ENCODED, APPLICATION_JSON, body.toString(), BasicAuthHelper.RFC6749.createHeader(clientId, secret))) {
                return JsonSerialization.readValue(result, AccessTokenResponse.class);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected error: ", e);
        } catch (IOException e) {
            throw new RuntimeException("Error receiving response: ", e);
        }
    }

    /**
     * 使用 keystore 私钥生成 JWT client assertion。
     *
     * @param keystore keystore 文件路径
     * @param storePass keystore 密码
     * @param keyPass 私钥密码
     * @param alias 密钥别名
     * @param sigLifetime 签名有效期（秒）
     * @param clientId 客户端 ID
     * @param realmInfoUrl realm 信息 URL（作为 JWT audience）
     * @return RSA256 签名的 JWT 字符串
     */
    public static String getSignedRequestToken(String keystore, String storePass, String keyPass, String alias, int sigLifetime, String clientId, String realmInfoUrl) {

        KeystoreUtil.KeystoreFormat keystoreType = Enum.valueOf(KeystoreUtil.KeystoreFormat.class, KeystoreUtil.getKeystoreType(null, keystore, KeystoreUtil.KeystoreFormat.JKS.toString()));
        KeyPair keypair = KeystoreUtil.loadKeyPairFromKeystore(keystore, storePass, keyPass, alias, keystoreType);

        JsonWebToken reqToken = new JsonWebToken();
        reqToken.id(SecretGenerator.getInstance().generateSecureID());
        reqToken.issuer(clientId);
        reqToken.subject(clientId);
        reqToken.audience(realmInfoUrl);

        long now = Time.currentTime();
        reqToken.iat(now);
        reqToken.exp(now + sigLifetime);
        reqToken.nbf(now);

        String signedRequestToken = new JWSBuilder()
                .jsonContent(reqToken)
                .rsa256(keypair.getPrivate());
        return signedRequestToken;
    }
}
