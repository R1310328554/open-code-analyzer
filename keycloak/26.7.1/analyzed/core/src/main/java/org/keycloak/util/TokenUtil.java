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

package org.keycloak.util;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.jose.jwe.JWEException;
import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.RefreshToken;

import org.jboss.logging.Logger;

import static org.keycloak.representations.AccessToken.REALM_ACCESS;
import static org.keycloak.representations.AccessToken.RESOURCE_ACCESS;

/**
 * OAuth/OIDC 令牌与 JWE 编解码辅助工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenUtil {

    /** Bearer 令牌类型标识。 */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /** DPoP 令牌类型标识。 */
    public static final String TOKEN_TYPE_DPOP = "DPoP";

    // RFC 8693 令牌交换成功响应中提及的类型
    public static final String TOKEN_TYPE_NA = "N_A";

    // RFC 9068 定义的 JWT 访问令牌类型
    public static final String TOKEN_TYPE_JWT_ACCESS_TOKEN = "at+jwt";
    /** 带 {@code application/} 前缀的 JWT 访问令牌类型。 */
    public static final String TOKEN_TYPE_JWT_ACCESS_TOKEN_PREFIXED = "application/" + TOKEN_TYPE_JWT_ACCESS_TOKEN;

    // OpenID Connect 后台登出令牌类型
    public static final String TOKEN_TYPE_JWT_LOGOUT_TOKEN = "logout+jwt";

    /** Keycloak 内部序列化 ID 令牌类型。 */
    public static final String TOKEN_TYPE_KEYCLOAK_ID = "Serialized-ID";

    /** ID 令牌类型标识。 */
    public static final String TOKEN_TYPE_ID = "ID";

    /** 刷新令牌类型标识。 */
    public static final String TOKEN_TYPE_REFRESH = "Refresh";

    /** 离线令牌类型标识。 */
    public static final String TOKEN_TYPE_OFFLINE = "Offline";

    /** 登出令牌类型标识。 */
    public static final String TOKEN_TYPE_LOGOUT = "Logout";

    /** 后台通道登出事件 URI。 */
    public static final String TOKEN_BACKCHANNEL_LOGOUT_EVENT = "http://schemas.openid.net/event/backchannel-logout";
    
    /** 后台登出事件中撤销离线访问的声明名。 */
    public static final String TOKEN_BACKCHANNEL_LOGOUT_EVENT_REVOKE_OFFLINE_TOKENS = "revoke_offline_access";

    private static final Logger logger = Logger.getLogger(TokenUtil.class);

    /**
     * 确保 scope 参数包含 {@code openid}。
     *
     * @param scopeParam 原始 scope 字符串
     * @return 含 openid 的 scope 字符串
     */
    public static String attachOIDCScope(String scopeParam) {
        if (scopeParam == null || scopeParam.isEmpty()) {
            return OAuth2Constants.SCOPE_OPENID;
        } else if (hasScope(scopeParam, OAuth2Constants.SCOPE_OPENID)) {
            return scopeParam;
        } else {
            return OAuth2Constants.SCOPE_OPENID + " " + scopeParam;
        }
    }

    /**
     * 判断 scope 是否包含 {@code openid}，即是否为 OIDC 请求。
     *
     * @param scopeParam scope 字符串
     * @return 包含 openid 时返回 true
     */
    public static boolean isOIDCRequest(String scopeParam) {
        return hasScope(scopeParam, OAuth2Constants.SCOPE_OPENID);
    }

    /**
     * 判断 scope 是否请求离线访问（offline_access）。
     *
     * @param scopeParam scope 字符串
     * @return 包含 offline_access 时返回 true
     */
    public static boolean isOfflineTokenRequested(String scopeParam) {
        return hasScope(scopeParam, OAuth2Constants.OFFLINE_ACCESS);
    }

    /**
     * 检查 scope 字符串是否包含指定 scope。
     *
     * @param scopeParam 空格分隔的 scope 列表
     * @param targetScope 目标 scope
     * @return 包含时返回 true
     */
    public static boolean hasScope(String scopeParam, String targetScope) {
        if (scopeParam == null || targetScope == null) {
            return false;
        }

        String[] scopes = scopeParam.split(" ");
        for (String scope : scopes) {
            if (targetScope.equals(scope)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查 prompt 参数是否包含指定值。
     *
     * @param promptParam 空格分隔的 prompt 列表
     * @param targetPrompt 目标 prompt
     * @return 包含时返回 true
     */
    public static boolean hasPrompt(String promptParam, String targetPrompt) {
        if (promptParam == null || targetPrompt == null) {
            return false;
        }

        String[] prompts = promptParam.split(" ");
        for (String prompt : prompts) {
            if (targetPrompt.equals(prompt)) {
                return true;
            }
        }
        return false;
    }



    /**
     * 从已解码的字节内容解析刷新令牌或离线令牌。
     *
     * @param decodedToken JWS 载荷字节
     * @return {@link RefreshToken} 实例
     */
    public static RefreshToken getRefreshToken(byte[] decodedToken) throws JWSInputException {
        try {
            return JsonSerialization.readValue(decodedToken, RefreshToken.class);
        } catch (IOException e) {
            throw new JWSInputException(e);
        }
    }

    /**
     * 从 Compact JWS 字符串解析刷新令牌。
     *
     * @param refreshToken Compact 序列化的刷新令牌
     * @return {@link RefreshToken} 实例
     */
    public static RefreshToken getRefreshToken(String refreshToken) throws JWSInputException {
        byte[] encodedContent = new JWSInput(refreshToken).getContent();
        return getRefreshToken(encodedContent);
    }

    /**
     * 判断给定刷新令牌是否为离线令牌。
     *
     * @param refreshToken Compact 序列化的刷新令牌
     * @return 类型为 {@link #TOKEN_TYPE_OFFLINE} 时返回 true
     */
    public static boolean isOfflineToken(String refreshToken) throws JWSInputException {
        RefreshToken token = getRefreshToken(refreshToken);
        return token.getType().equals(TOKEN_TYPE_OFFLINE);
    }


    /**
     * 使用 dir 算法将 JWT 直接加密为 JWE 字符串。
     *
     * @param aesKey AES 内容加密密钥
     * @param hmacKey HMAC 完整性密钥（可为 null，使用 GCM 模式）
     * @param jwt 待加密的 JWT
     * @return Compact JWE 字符串
     */
    public static String jweDirectEncode(Key aesKey, Key hmacKey, JsonWebToken jwt) throws JWEException {
        try {
            byte[] contentBytes = JsonSerialization.writeValueAsBytes(jwt);
            return jweDirectEncode(aesKey, hmacKey, contentBytes);
        } catch (IOException ioe) {
            throw new JWEException(ioe);
        }
    }


    /**
     * 解密 JWE 并反序列化为指定 JWT 类型。
     *
     * @param aesKey AES 内容加密密钥
     * @param hmacKey HMAC 完整性密钥
     * @param jweStr Compact JWE 字符串
     * @param expectedClass 期望的 JWT 类型
     * @return 解密后的 JWT 对象
     */
    public static <T extends JsonWebToken> T jweDirectVerifyAndDecode(Key aesKey, Key hmacKey, String jweStr, Class<T> expectedClass) throws JWEException {
        byte[] contentBytes = jweDirectVerifyAndDecode(aesKey, hmacKey, jweStr);
        try {
            return JsonSerialization.readValue(contentBytes, expectedClass);
        } catch (IOException ioe) {
            throw new JWEException(ioe);
        }
    }

    /**
     * 使用密钥加密算法编码 JWE，内容类型默认为 {@code JWT}。
     */
    public static String jweKeyEncryptionEncode(Key encryptionKEK, byte[] contentBytes, String algAlgorithm, String encAlgorithm, String kid, JWEAlgorithmProvider jweAlgorithmProvider, JWEEncryptionProvider jweEncryptionProvider) throws JWEException {
        return jweKeyEncryptionEncode(encryptionKEK, contentBytes, algAlgorithm, encAlgorithm, kid, jweAlgorithmProvider, jweEncryptionProvider, "JWT");
    }

    /**
     * 使用密钥加密算法编码 JWE。
     *
     * @param encryptionKEK 密钥加密密钥
     * @param contentBytes 明文载荷
     * @param algAlgorithm JWE 头部 alg
     * @param encAlgorithm JWE 头部 enc
     * @param kid 密钥 ID
     * @param jweAlgorithmProvider 算法提供器
     * @param jweEncryptionProvider 内容加密提供器
     * @param jweContentType cty 头字段值
     * @return Compact JWE 字符串
     */
    public static String jweKeyEncryptionEncode(Key encryptionKEK, byte[] contentBytes, String algAlgorithm,
                                                String encAlgorithm, String kid, JWEAlgorithmProvider jweAlgorithmProvider,
                                                JWEEncryptionProvider jweEncryptionProvider, String jweContentType) throws JWEException {
        JWEHeader jweHeader = new JWEHeader(algAlgorithm, encAlgorithm, null, kid, jweContentType);
        return jweKeyEncryptionEncode(encryptionKEK, contentBytes, jweHeader, jweAlgorithmProvider, jweEncryptionProvider);
    }

    /** 使用指定 JWE 头部与提供器编码密钥加密 JWE。 */
    private static String jweKeyEncryptionEncode(Key encryptionKEK, byte[] contentBytes, JWEHeader jweHeader, JWEAlgorithmProvider jweAlgorithmProvider, JWEEncryptionProvider jweEncryptionProvider) throws JWEException {
        JWE jwe = new JWE()
                .header(jweHeader)
                .content(contentBytes);
        jwe.getKeyStorage()
                .setEncryptionKey(encryptionKEK);
        String encodedContent = jwe.encodeJwe(jweAlgorithmProvider, jweEncryptionProvider);
        return encodedContent;
    }

    /**
     * 使用 KEK 解密 JWE 并返回明文载荷。
     *
     * @param decryptionKEK 解密密钥
     * @param encodedContent Compact JWE 字符串
     * @return 解密后的字节内容
     */
    public static byte[] jweKeyEncryptionVerifyAndDecode(Key decryptionKEK, String encodedContent) throws JWEException {
        JWE jwe = new JWE();
        jwe.getKeyStorage()
            .setDecryptionKey(decryptionKEK);
        jwe.verifyAndDecodeJwe(encodedContent);
        return jwe.getContent();
    }

    /**
     * 使用指定算法与加密提供器解密 JWE。
     */
    public static byte[] jweKeyEncryptionVerifyAndDecode(Key decryptionKEK, String encodedContent, JWEAlgorithmProvider algorithmProvider, JWEEncryptionProvider encryptionProvider) throws JWEException {
        JWE jwe = new JWE();
        jwe.getKeyStorage()
            .setDecryptionKey(decryptionKEK);
        jwe.verifyAndDecodeJwe(encodedContent, algorithmProvider, encryptionProvider);
        return jwe.getContent();
    }

    /** 使用 dir 算法加密字节内容（无 kid）。 */
    public static String jweDirectEncode(Key aesKey, Key hmacKey, byte[] contentBytes) throws JWEException {
        return jweDirectEncode(null, aesKey, hmacKey, contentBytes);
    }

    /**
     * 使用 dir 算法加密字节内容为 JWE。
     *
     * @param kid 可选密钥 ID
     * @param aesKey AES CEK
     * @param hmacKey HMAC 密钥（null 时使用 AEAD GCM）
     * @param contentBytes 明文
     * @return Compact JWE 字符串
     */
    public static String jweDirectEncode(String kid, Key aesKey, Key hmacKey, byte[] contentBytes) throws JWEException {
        int keyLength = aesKey.getEncoded().length;
        String encAlgorithm;
        switch (keyLength) {
            case 16: encAlgorithm = hmacKey != null ? JWEConstants.A128CBC_HS256 : JWEConstants.A128GCM;
                break;
            case 24: encAlgorithm = hmacKey != null ? JWEConstants.A192CBC_HS384 : JWEConstants.A192GCM;
                break;
            case 32: encAlgorithm = hmacKey != null ? JWEConstants.A256CBC_HS512 : JWEConstants.A256GCM;
                break;
            default: throw new IllegalArgumentException("Bad size for Encryption key: " + aesKey + ". Valid sizes are 16, 24, 32.");
        }

        JWEHeader jweHeader = new JWEHeader(JWEConstants.DIRECT, encAlgorithm, null, kid);
        JWE jwe = new JWE()
                .header(jweHeader)
                .content(contentBytes);

        jwe.getKeyStorage()
                .setCEKKey(aesKey, JWEKeyStorage.KeyUse.ENCRYPTION)
                .setCEKKey(hmacKey, JWEKeyStorage.KeyUse.SIGNATURE);

        return jwe.encodeJwe();

    }

    /**
     * 解密 dir 算法 JWE 并返回明文载荷。
     *
     * @param aesKey AES CEK
     * @param hmacKey HMAC 密钥
     * @param jweStr Compact JWE 字符串
     * @return 解密后的字节内容
     */
    public static byte[] jweDirectVerifyAndDecode(Key aesKey, Key hmacKey, String jweStr) throws JWEException {
        JWE jwe = new JWE();
        jwe.getKeyStorage()
                .setCEKKey(aesKey, JWEKeyStorage.KeyUse.ENCRYPTION)
                .setCEKKey(hmacKey, JWEKeyStorage.KeyUse.SIGNATURE);

        jwe.verifyAndDecodeJwe(jweStr);

        return jwe.getContent();

    }

    /**
     * 若 {@code otherClaims} 中存在 {@code realm_access} 或 {@code resource_access}，
     * 则将其合并到令牌顶层的对应字段并移除 otherClaims 中的副本。
     *
     * @param token 访问令牌
     */
    public static void convertTokenRolesFromOtherClaims(AccessToken token) {
        if (token.getOtherClaims() == null) return;

        if (token.getOtherClaims().containsKey(REALM_ACCESS)) {
            AccessToken.Access otherClaimsRealmAccess = convertToAccess(token.getOtherClaims().get(REALM_ACCESS), REALM_ACCESS);
            if (otherClaimsRealmAccess != null) {
                token.setRealmAccess(mergeAccess(token.getRealmAccess(), otherClaimsRealmAccess));
                token.getOtherClaims().remove(REALM_ACCESS);
            }
        }

        if (token.getOtherClaims().containsKey(RESOURCE_ACCESS)) {
            token.setResourceAccess(new HashMap<>(token.getResourceAccess())); // 重建 HashMap，原 map 可能不可变

            if (token.getOtherClaims().get(RESOURCE_ACCESS) instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) token.getOtherClaims().get(RESOURCE_ACCESS)).entrySet()) {
                    String clientId = entry.getKey();
                    AccessToken.Access otherClaimsClientAccess = convertToAccess(entry.getValue(), RESOURCE_ACCESS + "." + clientId);
                    AccessToken.Access mergedAccess = mergeAccess(token.getResourceAccess(clientId), otherClaimsClientAccess);
                    if (mergedAccess != null) {
                        token.getResourceAccess().put(clientId, mergedAccess);
                    }
                }
                token.getOtherClaims().remove(RESOURCE_ACCESS);
            } else {
                logger.warnf("Claim resource_access in the incorrect format in the access token of user %s. Ignoring", token.getPreferredUsername());
            }
        }
    }

    /** 将 otherClaims 中的角色对象转换为 {@link AccessToken.Access}。 */
    private static AccessToken.Access convertToAccess(Object accessClaim, String claimName) {
        try {
            return JsonSerialization.readValue(JsonSerialization.writeValueAsString(accessClaim), AccessToken.Access.class);
        } catch (IOException ioe) {
            logger.warnf( "Failed to convert roles from claim %s. Ignoring", claimName);
            return null;
        }
    }

    /** 合并两个 Access 对象的角色列表。 */
    private static AccessToken.Access mergeAccess(AccessToken.Access access1, AccessToken.Access access2) {
        if (access1 == null) return access2;
        if (access2 == null) return access1;
        access1.getRoles().addAll(access2.getRoles());
        return access1;
    }
}
