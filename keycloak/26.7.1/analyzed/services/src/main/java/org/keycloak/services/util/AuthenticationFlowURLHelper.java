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

package org.keycloak.services.util;

import java.net.URI;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * 认证流程 URL 构建辅助类。
 * <p>生成登录操作端点的执行 URL，用于页面过期重定向及浏览器历史同步。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationFlowURLHelper {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AuthenticationFlowURLHelper.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 当前领域 */
    private final RealmModel realm;
    /** 当前请求 URI 信息 */
    private final UriInfo uriInfo;

    /** 构造 URL 辅助器。 */
    public AuthenticationFlowURLHelper(KeycloakSession session, RealmModel realm, UriInfo uriInfo) {
        this.session = session;
        this.realm = realm;
        this.uriInfo = uriInfo;
    }


    /** 显示页面过期页面并重定向至最后执行步骤 URL。 */
    public Response showPageExpired(AuthenticationSessionModel authSession) {
        URI lastStepUrl = getLastExecutionUrl(authSession);

        logger.debugf("Redirecting to 'page expired' now. Will use last step URL: %s", lastStepUrl);

        LocaleUtil.processLocaleParam(session, realm, authSession);
        return session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authSession)
                .setActionUri(lastStepUrl)
                .setExecution(getExecutionId(authSession))
                .createLoginExpiredPage();
    }


    /** 根据流程路径与执行 ID 构建登录操作 URL。 */
    public URI getLastExecutionUrl(String flowPath, String executionId, String clientId, String tabId, String clientData) {
        UriBuilder uriBuilder = LoginActionsService.loginActionsBaseUrl(uriInfo)
                .path(flowPath);

        if (executionId != null) {
            uriBuilder.queryParam(Constants.EXECUTION, executionId);
        }
        uriBuilder.queryParam(Constants.CLIENT_ID, clientId);
        uriBuilder.queryParam(Constants.TAB_ID, tabId);
        uriBuilder.queryParam(Constants.CLIENT_DATA, clientData);

        return uriBuilder.build(realm.getName());
    }


    /** 从认证会话中提取参数并构建最后执行步骤 URL。 */
    public URI getLastExecutionUrl(AuthenticationSessionModel authSession) {
        String executionId = getExecutionId(authSession);
        String latestFlowPath = authSession.getAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH);

        if (latestFlowPath == null) {
            latestFlowPath = authSession.getClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW);
        }

        if (latestFlowPath == null) {
            latestFlowPath = LoginActionsService.AUTHENTICATE_PATH;
        }

        String clientData = AuthenticationProcessor.getClientData(session, authSession);
        return getLastExecutionUrl(latestFlowPath, executionId, authSession.getClient().getClientId(), authSession.getTabId(), clientData);
    }

    /** 从认证会话 authNote 获取当前执行 ID。 */
    private String getExecutionId(AuthenticationSessionModel authSession) {
        return authSession.getAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
    }

}
