/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.resources;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.RootAuthenticationSessionModel;

/**
 * 登出流程专用的会话码校验器。
 * <p>继承 {@link SessionCodeChecks}，针对登出场景跳过客户端事件上报、禁止从 KC_RESTART Cookie 重启会话，并使用登出专用错误页。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutSessionCodeChecks extends SessionCodeChecks {

    /**
     * 构造登出会话码校验器。
     * @param realm 领域
     * @param uriInfo 请求 URI
     * @param request HTTP 请求
     * @param clientConnection 客户端连接
     * @param session Keycloak 会话
     * @param event 事件构建器
     * @param code 会话码
     * @param clientId 客户端 ID
     * @param tabId 浏览器标签页 ID
     */
    public LogoutSessionCodeChecks(RealmModel realm, UriInfo uriInfo, HttpRequest request, ClientConnection clientConnection, KeycloakSession session, EventBuilder event,
                                   String code, String clientId, String tabId) {
        super(realm, uriInfo, request, clientConnection, session, event, null, code, null, clientId, tabId, null, null);
    }


    @Override
    /** {@inheritDoc} 登出事件不记录客户端 */
    protected void setClientToEvent(ClientModel client) {
        // 登出事件不上报客户端信息
    }

    @Override
    /** {@inheritDoc} 登出时返回登出失败错误页 */
    protected Response restartAuthenticationSessionFromCookie(RootAuthenticationSessionModel existingRootSession) {
        // 登出期间禁止从 KC_RESTART Cookie 重启认证会话
        getEvent().error(Errors.SESSION_EXPIRED);
        return ErrorPage.error(getSession(), null, Response.Status.BAD_REQUEST, Messages.FAILED_LOGOUT);
    }

    @Override
    /** {@inheritDoc} 校验登出动作码是否仍有效 */
    protected boolean isActionActive(ClientSessionCode.ActionType actionType) {
        if (!getClientCode().isActionActive(actionType)) {
            getEvent().clone().error(Errors.EXPIRED_CODE);
            return false;
        }
        return true;
    }

    @Override
    /** {@inheritDoc} 客户端禁用且存在会话码时视为失败 */
    protected boolean checkClientDisabled(ClientModel client) {
        return !client.isEnabled() && getClientCode() != null;
    }
}
