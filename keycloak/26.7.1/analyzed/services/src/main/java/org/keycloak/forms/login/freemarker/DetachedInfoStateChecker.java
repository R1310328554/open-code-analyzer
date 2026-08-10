/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.forms.login.freemarker;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.VerificationException;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * detached 信息/错误页状态校验器：生成并验证 URL 中的 {@code kc_state_checker} 参数与 Cookie 一致性。
 * <p>用于 authenticationSession 不存在时仍能在切换语言后恢复 info/error 页内容。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DetachedInfoStateChecker {

    private static final Logger logger = Logger.getLogger(DetachedInfoStateChecker.class);

    /** URL 查询参数名，携带当前页状态校验 token。 */
    public static final String STATE_CHECKER_PARAM = "kc_state_checker";

    private final KeycloakSession session;
    private final RealmModel realm;

    /** @param session Keycloak 会话 @param realm 当前领域 */
    public DetachedInfoStateChecker(KeycloakSession session, RealmModel realm) {
        this.session = session;
        this.realm = realm;
    }

    /** 编码消息与状态写入 {@link CookieType#AUTH_DETACHED} Cookie，并返回 token 对象。 */
    public DetachedInfoStateCookie generateAndSetCookie(String messageKey, String messageType, Integer status, String clientId, Object[] messageParameters) {
        UriInfo uriInfo = session.getContext().getHttpRequest().getUri();

        String currentStateCheckerInUrl = uriInfo.getQueryParameters().getFirst(STATE_CHECKER_PARAM);
        String newStateChecker = KeycloakModelUtils.generateId();
        int cookieMaxAge = realm.getAccessCodeLifespanUserAction();

        DetachedInfoStateCookie cookie = new DetachedInfoStateCookie();
        cookie.setMessageKey(messageKey);
        cookie.setMessageType(messageType);
        cookie.setStatus(status);
        cookie.setClientUuid(clientId);
        cookie.setCurrentUrlState(currentStateCheckerInUrl);
        cookie.setRenderedUrlState(newStateChecker);
        if (messageParameters != null) {
            List<String> params = Stream.of(messageParameters)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            cookie.setMessageParameters(params);
        }

        String encoded = session.tokens().encode(cookie);

        session.getProvider(CookieProvider.class).set(CookieType.AUTH_DETACHED, encoded, cookieMaxAge);
        return cookie;
    }

    /** 校验 URL 状态参数与 Cookie 中 current/rendered 状态是否匹配。 */
    public DetachedInfoStateCookie verifyStateCheckerParameter(String stateCheckerParam) throws VerificationException {
        String cookieVal = session.getProvider(CookieProvider.class).get(CookieType.AUTH_DETACHED);
        if (cookieVal == null || cookieVal.isEmpty()) {
            throw new VerificationException("State checker cookie is empty");
        }
        if (stateCheckerParam == null || stateCheckerParam.isEmpty()) {
            throw new VerificationException("State checker parameter is empty");
        }

        DetachedInfoStateCookie cookie = session.tokens().decode(cookieVal, DetachedInfoStateCookie.class);
        if (cookie == null) {
            throw new VerificationException("Failed to verify DetachedInfoStateCookie");
        }

        // 可与 currentUrlState（刷新 detached 页）或 renderedUrlState（语言下拉切换）比对
        // currentUrlState 在浏览器 URL 中，renderedUrlState 在语言下拉链接中
        if (stateCheckerParam.equals(cookie.getCurrentUrlState()) || stateCheckerParam.equals(cookie.getRenderedUrlState())) {
            return cookie;
        } else {
            throw new VerificationException(String.format("Failed to verify state. StateCheckerParameter: %s, cookie current state checker: %s, Cookie rendered state checker: %s",
                    stateCheckerParam, cookie.getCurrentUrlState(), cookie.getRenderedUrlState()));
        }
    }
}
