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

import java.util.Map;
import java.util.regex.Pattern;

import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserSessionModel;

import org.jboss.logging.Logger;

/**
 * OAuth2 授权码解析器：持久化、解析并校验一次性授权码。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuth2CodeParser {

    private static final Logger logger = Logger.getLogger(OAuth2CodeParser.class);

    private static final Pattern DOT = Pattern.compile("\\.");
    /** SingleUseObject 缓存键前缀 */
    public static final String CACHE_KEY_PREFIX = "code:";

    /**
     * 将授权码数据写入缓存并返回 OAuth2 握手用的 code 参数字符串。
     *
     * @return 格式为 {@code id.userSessionId.clientId} 的 code 参数
     */
    public static String persistCode(KeycloakSession session, AuthenticatedClientSessionModel clientSession, OAuth2Code codeData) {
        SingleUseObjectProvider codeStore = session.singleUseObjects();

        String key = codeData.getId();
        if (key == null) {
            throw new IllegalStateException("ID not present in the data");
        }

        Map<String, String> serialized = codeData.serializeCode();
        codeStore.put(CACHE_KEY_PREFIX + key, clientSession.getUserSession().getRealm().getAccessCodeLifespan(), serialized);
        return key + "." + clientSession.getUserSession().getId() + "." + clientSession.getClient().getId();
    }


    /**
     * 解析 code 参数，取出 {@link OAuth2Code} 与 {@link AuthenticatedClientSessionModel}，并校验是否已使用或过期。
     * <p>非法 code 时 {@link ParseResult#isIllegalCode()} 为 true；过期时 {@link ParseResult#isExpiredCode()} 为 true。</p>
     */
    public static ParseResult parseCode(KeycloakSession session, String code, RealmModel realm, EventBuilder event) {
        ParseResult result = new ParseResult(code);

        String[] parsed = DOT.split(code, 3);
        if (parsed.length < 3) {
            logger.warn("Invalid format of the code");
            return result.illegalCode();
        }

        String codeUUID = parsed[0];
        String userSessionId = parsed[1];
        String clientUUID = parsed[2];

        event.detail(Details.CODE_ID, userSessionId);
        event.session(userSessionId);

        // 加载用户会话
        var userSessionProvider = session.sessions();
        UserSessionModel userSession = userSessionProvider.getUserSessionIfClientExists(realm, userSessionId, false, clientUUID);
        if (userSession == null) {
            // 用于区分 code 无效与已被使用
            userSession = userSessionProvider.getUserSession(realm, userSessionId);
            if (userSession == null) {
                return result.illegalCode();
            }
        }

        result.clientSession = userSession.getAuthenticatedClientSessionByClient(clientUUID);

        SingleUseObjectProvider codeStore = session.singleUseObjects();
        Map<String, String> codeData = codeStore.remove(CACHE_KEY_PREFIX + codeUUID);

        // 缓存中无数据或 code 已被消费
        if (codeData == null) {
            logger.warnf("Code '%s' already used for userSession '%s' and client '%s'.", codeUUID, userSessionId, clientUUID);
            return result.illegalCode();
        }

        result.codeData = OAuth2Code.deserializeCode(codeData);

        String persistedUserSessionId = result.codeData.getUserSessionId();

        if (!userSessionId.equals(persistedUserSessionId)) {
            logger.warnf("Code '%s' is bound to a different session", codeUUID);
            return result.illegalCode();
        }

        // 最终校验 code 是否过期
        int currentTime = Time.currentTime();
        if (currentTime > result.codeData.getExpiration()) {
            return result.expiredCode();
        }

        logger.tracef("Successfully verified code '%s'. User session: '%s', client: '%s'", codeUUID, userSessionId, clientUUID);

        return result;
    }


    /** 授权码解析结果：含 code 数据、客户端会话及非法/过期标志 */
    public static class ParseResult {

        private final String code;
        private OAuth2Code codeData;
        private AuthenticatedClientSessionModel clientSession;

        private boolean isIllegalCode = false;
        private boolean isExpiredCode = false;


        private ParseResult(String code) {
            this.code = code;
        }


        /** @return 原始 code 参数字符串 */
        public String getCode() {
            return code;
        }

        /** @return 反序列化后的授权码数据 */
        public OAuth2Code getCodeData() {
            return codeData;
        }

        /** @return 关联的已认证客户端会话 */
        public AuthenticatedClientSessionModel getClientSession() {
            return clientSession;
        }

        /** @return code 格式非法或已被使用 */
        public boolean isIllegalCode() {
            return isIllegalCode;
        }

        /** @return code 已过期 */
        public boolean isExpiredCode() {
            return isExpiredCode;
        }


        private ParseResult illegalCode() {
            this.isIllegalCode = true;
            return this;
        }


        private ParseResult expiredCode() {
            this.isExpiredCode = true;
            return this;
        }
    }
}
