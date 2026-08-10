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

package org.keycloak.authentication.authenticators.broker;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 身份提供商（IdP）首次代理登录流程认证器的抽象基类，负责从认证会话读取 {@link BrokeredIdentityContext}、校验 IdP 启用状态并委派子类实现。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractIdpAuthenticator implements Authenticator {

    /** 认证会话 note：序列化的 {@link BrokeredIdentityContext}，存在即表示首次代理登录流程进行中。 */
    // The clientSession note encapsulating all the BrokeredIdentityContext info. When this note is in clientSession, we know that firstBrokerLogin flow is in progress
    public static final String BROKERED_CONTEXT_NOTE = "BROKERED_CONTEXT";

    /** 认证会话 note：已检测到重复账户的 {@link ExistingUserInfo} 序列化数据。 */
    // The clientSession note with all the info about existing user
    public static final String EXISTING_USER_INFO = "EXISTING_USER_INFO";

    /** 认证会话 note：强制显示资料更新页，即使配置为首次登录不更新资料。 */
    // The clientSession note flag to indicate that updateProfile page will be always displayed even if "updateProfileOnFirstLogin" is off
    public static final String ENFORCE_UPDATE_PROFILE = "ENFORCE_UPDATE_PROFILE";

    /** 认证会话 note：首次代理登录成功后写入，值为刚完成 first-broker-login 流程的 IdP providerId。 */
    // Set after firstBrokerLogin is successfully finished and contains the providerId of the provider, whose 'first-broker-login' flow was just finished
    public static final String FIRST_BROKER_LOGIN_SUCCESS = "FIRST_BROKER_LOGIN_SUCCESS";

    /** 认证会话 note：检测到嵌套首次代理登录时设置，用于报告详细错误。 */
    // Set if nested firstBrokerLogin is detected, allowing to report a detailed error
    public static final String NESTED_FIRST_BROKER_CONTEXT = "NESTED_FIRST_BROKER_CONTEXT";

    @Override
    /** 读取 Broker 上下文、校验 IdP 启用后调用 {@link #authenticateImpl}。 */
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }
        BrokeredIdentityContext brokerContext = serializedCtx.deserialize(context.getSession(), authSession);

        if (!brokerContext.getIdpConfig().isEnabled()) {
            sendFailureChallenge(context, Response.Status.BAD_REQUEST, Errors.IDENTITY_PROVIDER_ERROR, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }

        authenticateImpl(context, serializedCtx, brokerContext);
    }

    @Override
    /** 处理表单提交，校验 IdP 启用后调用 {@link #actionImpl}。 */
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel clientSession = context.getAuthenticationSession();

        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(clientSession, BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }
        BrokeredIdentityContext brokerContext = serializedCtx.deserialize(context.getSession(), clientSession);

        if (!brokerContext.getIdpConfig().isEnabled()) {
            sendFailureChallenge(context, Response.Status.BAD_REQUEST, Errors.IDENTITY_PROVIDER_ERROR, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }

        actionImpl(context, serializedCtx, brokerContext);
    }

    /** 子类实现：首次展示时的认证逻辑。 */
    protected abstract void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext);
    /** 子类实现：用户提交表单后的动作处理。 */
    protected abstract void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext);

    /** 记录事件错误并以错误页挑战终止当前步骤。 */
    protected void sendFailureChallenge(AuthenticationFlowContext context, Response.Status status, String eventError, String errorMessage, AuthenticationFlowError flowError) {
        context.getEvent().user(context.getUser())
                .error(eventError);
        Response challengeResponse = context.form()
                .setError(errorMessage)
                .createErrorPage(status);
        context.failureChallenge(flowError, challengeResponse);
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {

    }

    /** 从认证会话 note 反序列化并加载已检测到的重复用户，校验存在且已启用。 */
    public static UserModel getExistingUser(KeycloakSession session, RealmModel realm, AuthenticationSessionModel authSession) {
        String existingUserId = authSession.getAuthNote(EXISTING_USER_INFO);
        if (existingUserId == null) {
            throw new AuthenticationFlowException("Unexpected state. There is no existing duplicated user identified in ClientSession",
                    AuthenticationFlowError.INTERNAL_ERROR);
        }

        ExistingUserInfo duplication = ExistingUserInfo.deserialize(existingUserId);

        UserModel existingUser = session.users().getUserById(realm, duplication.getExistingUserId());
        if (existingUser == null) {
            throw new AuthenticationFlowException("User with ID '" + existingUserId + "' not found.", AuthenticationFlowError.INVALID_USER);
        }

        if (!existingUser.isEnabled()) {
            throw new AuthenticationFlowException("User with ID '" + existingUserId + "', username '" + existingUser.getUsername() + "' disabled.", AuthenticationFlowError.USER_DISABLED);
        }

        return existingUser;
    }
}
