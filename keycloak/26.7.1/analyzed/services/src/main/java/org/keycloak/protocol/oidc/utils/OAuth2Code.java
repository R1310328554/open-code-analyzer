/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 授权码关联数据：在重定向前写入缓存，token 端点换码时一次性消费。
 * <p>生命周期极短，含 nonce、scope、PKCE、DPoP 等换 token 所需信息。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuth2Code {

    /** 授权码 ID 的序列化键 */
    public static final String ID_NOTE = "id";
    /** 过期时间的序列化键 */
    public static final String EXPIRATION_NOTE = "exp";
    private static final String NONCE_NOTE = "nonce";
    private static final String SCOPE_NOTE = "scope";
    private static final String RESOURCE_NOTE = "resource";
    private static final String REDIRECT_URI_PARAM_NOTE = "redirectUri";
    private static final String CODE_CHALLENGE_NOTE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD_NOTE = "code_challenge_method";
    private static final String DPOP_JKT_NOTE = "dpop_jkt";
    /** 用户会话 ID 的序列化键 */
    public static final String USER_SESSION_ID_NOTE = "user_session_id";

    private final String id;

    private final int expiration;

    private final String nonce;

    private final String scope;
    private final String resource;

    private final String redirectUriParam;

    private final String codeChallenge;
    private final String codeChallengeMethod;

    private final String dpopJkt;

    private final String userSessionId;


    /** 简化构造：不含 resource、redirect_uri、PKCE 与 DPoP 字段 */
    public OAuth2Code(String id, int expiration, String nonce, String scope, String userSessionId) {
        this.id = id;
        this.expiration = expiration;
        this.nonce = nonce;
        this.scope = scope;
        this.resource = null;
        this.redirectUriParam = null;
        this.codeChallenge = null;
        this.codeChallengeMethod = null;
        this.dpopJkt = null;
        this.userSessionId = userSessionId;
    }

    public OAuth2Code(String id, int expiration, String nonce, String scope, String resource, String redirectUriParam,
                      String codeChallenge, String codeChallengeMethod, String dpopJkt, String userSessionId) {
        this.id = id;
        this.expiration = expiration;
        this.nonce = nonce;
        this.scope = scope;
        this.resource = resource;
        this.redirectUriParam = redirectUriParam;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.dpopJkt = dpopJkt;
        this.userSessionId = userSessionId;
    }

    private OAuth2Code(Map<String, String> data) {
        id = data.get(ID_NOTE);
        expiration = Integer.parseInt(data.get(EXPIRATION_NOTE));
        nonce = data.get(NONCE_NOTE);
        scope = data.get(SCOPE_NOTE);
        resource = data.get(RESOURCE_NOTE);
        redirectUriParam = data.get(REDIRECT_URI_PARAM_NOTE);
        codeChallenge = data.get(CODE_CHALLENGE_NOTE);
        codeChallengeMethod = data.get(CODE_CHALLENGE_METHOD_NOTE);
        dpopJkt = data.get(DPOP_JKT_NOTE);
        userSessionId = data.get(USER_SESSION_ID_NOTE);
    }


    /** 从缓存 Map 反序列化为 {@link OAuth2Code} */
    public static OAuth2Code deserializeCode(Map<String, String> data) {
        return new OAuth2Code(data);
    }


    /** 序列化为可存入 SingleUseObject 缓存的 Map */
    public Map<String, String> serializeCode() {
        Map<String, String> result = new HashMap<>();

        result.put(ID_NOTE, id);
        result.put(EXPIRATION_NOTE, String.valueOf(expiration));
        result.put(NONCE_NOTE, nonce);
        result.put(SCOPE_NOTE, scope);
        result.put(RESOURCE_NOTE, resource);
        result.put(REDIRECT_URI_PARAM_NOTE, redirectUriParam);
        result.put(CODE_CHALLENGE_NOTE, codeChallenge);
        result.put(CODE_CHALLENGE_METHOD_NOTE, codeChallengeMethod);
        result.put(DPOP_JKT_NOTE, dpopJkt);
        result.put(USER_SESSION_ID_NOTE, userSessionId);

        return result;
    }

    /** @return 授权码 UUID */
    public String getId() {
        return id;
    }

    /** @return 过期时间（Unix 秒） */
    public int getExpiration() {
        return expiration;
    }

    /** @return OIDC nonce 值 */
    public String getNonce() {
        return nonce;
    }

    /** @return 授权 scope 字符串 */
    public String getScope() {
        return scope;
    }

    /** @return 资源指标（RFC 8707） */
    public String getResource() {
        return resource;
    }

    /** @return 授权请求中的 redirect_uri */
    public String getRedirectUriParam() {
        return redirectUriParam;
    }

    /** @return PKCE code_challenge */
    public String getCodeChallenge() {
        return codeChallenge;
    }

    /** @return PKCE code_challenge_method */
    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    /** @return DPoP 公钥 JKT thumbprint */
    public String getDpopJkt() {
        return dpopJkt;
    }

    /** @return 绑定的用户会话 ID */
    public String getUserSessionId() {
        return userSessionId;
    }
}
