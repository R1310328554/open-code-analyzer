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

package org.keycloak.representations;

import org.keycloak.TokenCategory;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;
import org.keycloak.util.TokenUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * 刷新令牌 JWT，继承 {@link AccessToken} 并保留原始受众与刷新令牌提供者引用。
 * <p>
 * 类型固定为 refresh；离线令牌可能仅含 {@code session_state} 而无 {@code sid}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RefreshToken extends AccessToken {

    public static final String ORIGINAL_AUD = "aud_x";

    public static final String PROVIDER = "prov";

    @JsonProperty(ORIGINAL_AUD)
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    protected String[] originalAudience;

    /** 签发该刷新令牌的提供者标识。 */
    @JsonProperty(PROVIDER)
    private String provider;

    private RefreshToken() {
        type(TokenUtil.TOKEN_TYPE_REFRESH);
    }

    /**
     * 从访问令牌深拷贝 iss、sub、azp、会话等字段以构造刷新令牌。
     *
     * @param token 源访问令牌
     * @param confirmation 可选 cnf；认证流程可能使用但不一定写入响应
     * @param provider 刷新令牌提供者 ID
     */
    public RefreshToken(AccessToken token, Confirmation confirmation, String provider) {
        this();
        this.issuer = token.issuer;
        this.subject = token.subject;
        this.issuedFor = token.issuedFor;
        this.sessionId = token.sessionId;
        this.nonce = token.nonce;
        this.audience = new String[] { token.issuer };
        this.originalAudience = token.audience;
        this.scope = token.scope;
        this.authorizationDetails = token.authorizationDetails;
        this.confirmation = confirmation;
        this.provider = provider;
    }

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.INTERNAL;
    }

    @Override
    public String getSessionId() {
        String sessionId = super.getSessionId();
        // Keycloak 14 及更早离线令牌可能只有 session_state 而无 sid
        return sessionId != null ? sessionId : (String) getOtherClaims().get(IDToken.SESSION_STATE);
    }

    public String[] getOriginalAudience() {
        return originalAudience;
    }

    public String getProvider() {
        return provider;
    }
}
