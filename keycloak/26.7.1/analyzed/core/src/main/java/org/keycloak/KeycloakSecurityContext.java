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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.DelegatingSerializationFilter;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.util.JsonSerialization;

/**
 * Keycloak 安全上下文，持有访问令牌与 ID 令牌的原始字符串及解析结果。
 * <p>在受保护请求中可通过 {@code HttpServletRequest.getAttribute()} 获取；
 * 也可在 {@code HttpSession} 中以本类全限定名作为属性键存储。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeycloakSecurityContext implements Serializable {
    /** 访问令牌的 JWT 字符串。 */
    protected String tokenString;
    /** ID 令牌的 JWT 字符串。 */
    protected String idTokenString;

    // 解析后的令牌对象不写入 HTTP Session，避免序列化体积过大
    protected transient AccessToken token;
    protected transient IDToken idToken;
    protected transient AuthorizationContext authorizationContext;

    public KeycloakSecurityContext() {
    }

    /**
     * @param tokenString 访问令牌 JWT
     * @param token 已解析的访问令牌
     * @param idTokenString ID 令牌 JWT
     * @param idToken 已解析的 ID 令牌
     */
    public KeycloakSecurityContext(String tokenString, AccessToken token, String idTokenString, IDToken idToken) {
        this.tokenString = tokenString;
        this.token = token;
        this.idToken = idToken;
        this.idTokenString = idTokenString;
    }

    public AccessToken getToken() {
        return token;
    }

    public String getTokenString() {
        return tokenString;
    }

    public AuthorizationContext getAuthorizationContext() {
        return authorizationContext;
    }

    public IDToken getIdToken() {
        return idToken;
    }

    public String getIdTokenString() {
        return idTokenString;
    }

    /**
     * 从访问令牌 issuer 中提取 realm 名称（取 issuer URL 最后一段路径）。
     *
     * @return realm 名称
     */
    public String getRealm() {
        // 假设 issuer 末尾包含 realm 名
        return token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
    }

    // SERIALIZATION

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /** 反序列化后从 JWT 字符串重新解析令牌对象（不做签名验证）。 */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        DelegatingSerializationFilter.builder()
                .addAllowedClass(KeycloakSecurityContext.class)
                .setFilter(in);
        in.defaultReadObject();

        token = parseToken(tokenString, AccessToken.class);
        idToken = parseToken(idTokenString, IDToken.class);
    }

    /**
     * 仅解码 JWT payload，不进行任何密码学验证。
     *
     * @param encoded JWT 字符串
     * @param clazz 目标令牌类型
     * @return 解析后的对象；encoded 为 null 时返回 null
     */
    private <T> T parseToken(String encoded, Class<T> clazz) throws IOException {
        if (encoded == null)
            return null;

        String[] parts = encoded.split("\\.");
        if (parts.length < 2 || parts.length > 3) throw new IllegalArgumentException("Parsing error");

        byte[] bytes = Base64Url.decode(parts[1]);
        return JsonSerialization.readValue(bytes, clazz);
    }


}
