/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc.encode;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 访问令牌的上下文信息：会话类型、令牌类型、授权类型与原始 token id。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AccessTokenContext {

    /** 用户会话类型（在线/离线/瞬态等）。 */
    private final SessionType sessionType;
    /** 访问令牌类型（常规/轻量）。 */
    private final TokenType tokenType;
    /** OAuth2 授权类型。 */
    private final String grantType;
    /** 未编码的原始 token id。 */
    private final String rawTokenId;

    /** 用户会话类型枚举，含查找策略与快捷编码。 */
    public enum SessionType {
        // 常规在线用户会话（有效客户端会话）
        ONLINE("on", false, true, false, false),

        // 常规离线用户会话
        OFFLINE("of", false, false, true, false),

        // 瞬态用户会话
        TRANSIENT("tr", true, false, false, false),

        // 在线用户会话 + 瞬态客户端会话
        ONLINE_TRANSIENT_CLIENT("nt", false, true, false, true),

        // 离线用户会话 + 瞬态客户端会话
        OFFLINE_TRANSIENT_CLIENT("ft", false, false, true, true),

        // 未知类型（可能来自 26.2 之前版本）
        UNKNOWN("un", true, true, true, false);

        private final String shortcut;
        private final boolean allowTransientUserSession;
        private final boolean allowLookupOnlineUserSession;
        private final boolean allowLookupOfflineUserSession;
        private final boolean allowTransientClientSession;

        SessionType(String shortcut, boolean allowTransientUserSession, boolean allowLookupOnlineUserSession, boolean allowLookupOfflineUserSession, boolean allowTransientClientSession) {
            this.shortcut = shortcut;
            this.allowTransientUserSession = allowTransientUserSession;
            this.allowLookupOnlineUserSession = allowLookupOnlineUserSession;
            this.allowLookupOfflineUserSession = allowLookupOfflineUserSession;
            this.allowTransientClientSession = allowTransientClientSession;
        }

        public String getShortcut() {
            return shortcut;
        }

        public boolean isAllowTransientUserSession() {
            return allowTransientUserSession;
        }

        public boolean isAllowLookupOnlineUserSession() {
            return allowLookupOnlineUserSession;
        }

        public boolean isAllowLookupOfflineUserSession() {
            return allowLookupOfflineUserSession;
        }

        public boolean isAllowTransientClientSession() {
            return allowTransientClientSession;
        }
    }

    /** 访问令牌类型枚举。 */
    public enum TokenType {
        /** 常规访问令牌 */
        REGULAR("rt"),
        /** 轻量访问令牌 */
        LIGHTWEIGHT("lt"),
        /** 未知类型 */
        UNKNOWN("un");

        private final String shortcut;

        TokenType(String shortcut) {
            this.shortcut = shortcut;
        }

        public String getShortcut() {
            return shortcut;
        }
    }

    /**
     * @param sessionType 会话类型
     * @param tokenType 令牌类型
     * @param grantType 授权类型
     * @param rawTokenId 原始 token id
     */
    @JsonCreator
    public AccessTokenContext(@JsonProperty("sessionType") SessionType sessionType, @JsonProperty("tokenType") TokenType tokenType, @JsonProperty("grantType") String grantType, @JsonProperty("rawTokenId") String rawTokenId) {
        Objects.requireNonNull(sessionType, "Null sessionType not allowed");
        Objects.requireNonNull(tokenType, "Null tokenType not allowed");
        Objects.requireNonNull(grantType, "Null grantType not allowed");
        Objects.requireNonNull(grantType, "Null rawTokenId not allowed");
        this.sessionType = sessionType;
        this.tokenType = tokenType;
        this.grantType = grantType;
        this.rawTokenId = rawTokenId;
    }

    /** @return 会话类型 */
    public SessionType getSessionType() {
        return sessionType;
    }

    /** @return 令牌类型 */
    public TokenType getTokenType() {
        return tokenType;
    }

    /** @return 授权类型 */
    public String getGrantType() {
        return grantType;
    }

    /** @return 原始 token id */
    public String getRawTokenId() {
        return rawTokenId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof AccessTokenContext that &&
                sessionType == that.sessionType &&
                tokenType == that.tokenType &&
                Objects.equals(grantType, that.grantType) &&
                Objects.equals(rawTokenId, that.rawTokenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionType, tokenType, grantType, rawTokenId);
    }
}
