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

package org.keycloak.authentication;

import java.net.URI;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Time;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * {@link RequiredActionContext} 的默认实现，封装必需操作执行期间的状态与表单辅助方法。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RequiredActionContextResult implements RequiredActionContext {
    /** 当前认证会话。 */
    protected AuthenticationSessionModel authenticationSession;
    /** 领域模型。 */
    protected RealmModel realm;
    /** 事件构建器。 */
    protected EventBuilder eventBuilder;
    /** Keycloak 会话。 */
    protected KeycloakSession session;
    /** 必需操作执行状态。 */
    protected Status status;
    /** 失败时的错误消息。 */
    protected String errorMessage;
    /** 挑战响应（如需展示表单）。 */
    protected Response challenge;
    /** 当前 HTTP 请求。 */
    protected HttpRequest httpRequest;
    /** 目标用户。 */
    protected UserModel user;
    /** 必需操作工厂。 */
    protected RequiredActionFactory factory;
    /** 必需操作配置。 */
    protected RequiredActionConfigModel config;

    /** 构造必需操作上下文。 */
    public RequiredActionContextResult(AuthenticationSessionModel authSession,
                                       RealmModel realm, EventBuilder eventBuilder, KeycloakSession session,
                                       HttpRequest httpRequest,
                                       UserModel user, RequiredActionFactory factory) {
        this.authenticationSession = authSession;
        this.realm = realm;
        this.eventBuilder = eventBuilder;
        this.session = session;
        this.httpRequest = httpRequest;
        this.user = user;
        this.factory = factory;
        this.config = realm.getRequiredActionConfigByAlias(factory.getId());
    }

    @Override
    public RequiredActionConfigModel getConfig() {
        return config;
    }

    /** @return 必需操作工厂 */
    public RequiredActionFactory getFactory() {
        return factory;
    }

    @Override
    public EventBuilder getEvent() {
        return eventBuilder;
    }

    @Override
    public UserModel getUser() {
        return user;
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    @Override
    public ClientConnection getConnection() {
        return session.getContext().getConnection();
    }

    @Override
    public UriInfo getUriInfo() {
        return session.getContext().getUri();
    }

    @Override
    public KeycloakSession getSession() {
        return session;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    /** 设置挑战响应并标记状态为 CHALLENGE。 */
    public void challenge(Response response) {
        status = Status.CHALLENGE;
        challenge = response;

    }

    @Override
    /** 标记必需操作失败。 */
    public void failure(String errorMessage) {
        this.errorMessage = errorMessage;
        status = Status.FAILURE;
    }

    @Override
    /** 标记必需操作成功完成。 */
    public void success() {
        status = Status.SUCCESS;

    }

    @Override
    /** 标记用户取消必需操作。 */
    public void cancel() {
        status = Status.CANCELLED;
    }

    @Override
    /** 标记忽略本次必需操作。 */
    public void ignore() {
        status = Status.IGNORE;
    }

    @Override
    public String getAction() {
        return getFactory().getId();
    }

    @Override
    /** 构建带会话码的必需操作 URL。 */
    public URI getActionUrl(String code) {
        ClientModel client = authenticationSession.getClient();
        return LoginActionsService.requiredActionProcessor(getUriInfo())
                .queryParam(LoginActionsService.SESSION_CODE, code)
                .queryParam(Constants.EXECUTION, getExecution())
                .queryParam(Constants.CLIENT_ID, client.getClientId())
                .queryParam(Constants.TAB_ID, authenticationSession.getTabId())
                .queryParam(Constants.CLIENT_DATA, AuthenticationProcessor.getClientData(session, authenticationSession))
                .build(getRealm().getName());
    }

    private String getExecution() {
        return factory.getId();
    }

    @Override
    /** 生成或复用客户端会话码。 */
    public String generateCode() {
        ClientSessionCode<AuthenticationSessionModel> accessCode = new ClientSessionCode<>(session, getRealm(), getAuthenticationSession());
        authenticationSession.getParentSession().setTimestamp(Time.currentTime());
        return accessCode.getOrGenerateCode();
    }


    @Override
    public URI getActionUrl() {
        String accessCode = generateCode();
        return getActionUrl(accessCode);

    }

    @Override
    /** 获取预配置 action URI 的登录表单提供者。 */
    public LoginFormsProvider form() {
        String accessCode = generateCode();
        URI action = getActionUrl(accessCode);
        LoginFormsProvider provider = getSession().getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(getAuthenticationSession())
                .setUser(getUser())
                .setActionUri(action)
                .setExecution(getExecution())
                .setClientSessionCode(accessCode);
        return provider;
    }


    @Override
    public Response getChallenge() {
        return challenge;
    }
}
