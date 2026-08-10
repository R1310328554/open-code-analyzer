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
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 必需操作执行上下文：封装当前 Required Action 的状态、表单 URL 与流程控制。
 *
 * Interface that encapsulates information about the current required action
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequiredActionContext {
    /** 必需操作执行状态。 */
    enum Status {
        /** 已发送挑战（待用户响应）。 */
        CHALLENGE,
        /** 成功完成。 */
        SUCCESS,
        /** 已取消（AIA 场景）。 */
        CANCELLED,
        /** 忽略并继续下一动作。 */
        IGNORE,
        /** 失败中止。 */
        FAILURE
    }

    /** kc_action 参数对应的 AIA 结果状态。 */
    enum KcActionStatus {
        /** 成功。 */
        SUCCESS,
        /** 取消。 */
        CANCELLED,
        /** 错误。 */
        ERROR
    }

    /** 当前必需操作的 provider ID。 */
    String getAction();

    /**
     * 构造必需操作的 action URL（须传入客户端会话 access code）。
     *
     * Get the action URL for the required action.
     *
     * @param code client sessino access code
     * @return
     */
    URI getActionUrl(String code);

    /**
     * 构造 action URL 并自动生成 access code。
     *
     * Get the action URL for the required action.  This auto-generates the access code.
     *
     * @return
     */
    URI getActionUrl();

    /**
     * 创建预置用户、action URI 与 access code 的 Freemarker 表单构建器。
     *
     * Create a Freemarker form builder that presets the user, action URI, and a generated access code
     *
     * @return
     */
    LoginFormsProvider form();


    /**
     * 若已发送挑战，返回对应 JAX-RS Response。
     *
     * If challenge has been sent this returns the JAX-RS Response
     *
     * @return
     */
    Response getChallenge();


    /**
     * 当前事件构建器。
     *
     * Current event builder being used
     *
     * @return
     */
    EventBuilder getEvent();

    /**
     * 当前用户。
     *
     * Current user
     *
     * @return
     */
    UserModel getUser();
    /** 当前领域。 */
    RealmModel getRealm();
    /** 认证会话。 */
    AuthenticationSessionModel getAuthenticationSession();
    /** 客户端连接信息。 */
    ClientConnection getConnection();
    /** 请求 URI 信息。 */
    UriInfo getUriInfo();
    /** Keycloak 会话。 */
    KeycloakSession getSession();
    /** HTTP 请求。 */
    HttpRequest getHttpRequest();

    /**
     * 当前必需操作配置；不可配置时为 {@literal null}。
     *
     * The configuration of the current required action. Returns {@literal null} if the current required action is not configurable.
     * @return
     */
    RequiredActionConfigModel getConfig();

    /**
     * 生成 access code 并更新客户端会话时间戳；表单回调须携带该 code 查询参数。
     *
     * Generates access code and updates clientsession timestamp
     * Access codes must be included in form action callbacks as a query parameter.
     *
     * @return
     */
    String generateCode();

    /** 当前执行状态。 */
    Status getStatus();

    /** 失败时的错误消息。 */
    String getErrorMessage();

    /**
     * 向用户发送挑战响应。
     *
     * Send a challenge Response back to user
     *
     * @param response
     */
    void challenge(Response response);

    /**
     * 以错误中止认证，可选用户可见错误消息。
     *
     * 以错误中止认证。
     *
     * Abort the authentication with an error, optionally with an erroMessage.
     *
     */
    void failure(String errorMessage);

    /**
     * Abort the authentication with an error
     *
     */
    default void failure() {
        failure(null);
    }

    /**
     * 标记必需操作成功，并从 UserModel 移除该动作。
     *
     * Mark this required action as successful.  The required action will be removed from the UserModel
     *
     */
    void success();

    /**
     * 标记动作已取消，仅用于 AIA 场景。
     *
     * Mark this action as cancelled. Can be only used in AIA
     */
    void cancel();

    /**
     * 忽略本必需操作，继续下一动作或完成流程。
     *
     * Ignore this required action and go onto the next, or complete the flow.
     *
     */
    void ignore();

}
