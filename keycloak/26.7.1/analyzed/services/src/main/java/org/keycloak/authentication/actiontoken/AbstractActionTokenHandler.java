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
package org.keycloak.authentication.actiontoken;

import org.keycloak.Config.Scope;
import org.keycloak.TokenVerifier;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 操作令牌处理器抽象基类，同时实现 {@link ActionTokenHandler} 与 {@link ActionTokenHandlerFactory}。
 *
 * @author hmlnarik
 */
public abstract class AbstractActionTokenHandler<T extends JsonWebToken> implements ActionTokenHandler<T>, ActionTokenHandlerFactory<T> {

    /** 处理器/provider ID。 */
    private final String id;
    /** 令牌 Java 类型。 */
    private final Class<T> tokenClass;
    /** 默认用户可见错误消息键。 */
    private final String defaultErrorMessage;
    /** 默认审计事件类型。 */
    private final EventType defaultEventType;
    /** 默认事件错误码。 */
    private final String defaultEventError;

    /** 初始化处理器元数据。 */
    public AbstractActionTokenHandler(String id, Class<T> tokenClass, String defaultErrorMessage, EventType defaultEventType, String defaultEventError) {
        this.id = id;
        this.tokenClass = tokenClass;
        this.defaultErrorMessage = defaultErrorMessage;
        this.defaultEventType = defaultEventType;
        this.defaultEventError = defaultEventError;
    }

    @Override
    public ActionTokenHandler<T> create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void close() {
    }

    @Override
    public Class<T> getTokenClass() {
        return this.tokenClass;
    }

    @Override
    public EventType eventType() {
        return this.defaultEventType;
    }

    @Override
    public String getDefaultErrorMessage() {
        return this.defaultErrorMessage;
    }

    @Override
    public String getDefaultEventError() {
        return this.defaultEventError;
    }

    @Override
    /** 从 {@link DefaultActionToken} 提取复合认证会话 ID。 */
    public String getAuthenticationSessionIdFromToken(T token, ActionTokenContext<T> tokenContext, AuthenticationSessionModel currentAuthSession) {
        return token instanceof DefaultActionToken ? ((DefaultActionToken) token).getCompoundAuthenticationSessionId() : null;
    }

    @Override
    /** 创建新认证会话并在必需操作完成后终止登录。 */
    public AuthenticationSessionModel startFreshAuthenticationSession(T token, ActionTokenContext<T> tokenContext) {
        AuthenticationSessionModel authSession = tokenContext.createAuthenticationSessionForClient(token.getIssuedFor());
        authSession.setAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS, "true");
        return authSession;
    }
    
    @Override
    /** 默认允许令牌重复使用。 */
    public boolean canUseTokenRepeatedly(T token, ActionTokenContext<T> tokenContext) {
        return true;
    }

    /** 返回校验令牌邮箱与当前用户邮箱一致的谓词。 */
    protected TokenVerifier.Predicate<DefaultActionToken> verifyEmail(ActionTokenContext<? extends DefaultActionToken> context) {
        return TokenUtils.checkThat(
            t -> t.getEmail() == null || t.getEmail().equals(context.getAuthenticationSession().getAuthenticatedUser().getEmail()),
            Errors.INVALID_EMAIL, Messages.INVALID_EMAIL
        );
    }
}
