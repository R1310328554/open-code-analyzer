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

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 表单执行上下文：封装当前表单流程的状态（用户、领域、会话、HTTP 等）。
 *
 * Interface that encapsulates the current state of the current form being executed
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface FormContext {
    /**
     * 当前事件构建器。
     *
     * Current event builder being used
     *
     * @return
     */
    EventBuilder getEvent();

    /**
     * 创建新的 EventBuilder 供本上下文使用。
     *
     * Create a refresh new EventBuilder to use within this context
     *
     * @return
     */
    EventBuilder newEvent();

    /**
     * 流程中的当前认证执行步骤。
     *
     * The current execution in the flow
     *
     * @return
     */
    AuthenticationExecutionModel getExecution();

    /**
     * 当前流程关联的用户；尚未识别时为 null。
     *
     * Current user attached to this flow.  It can return null if no user has been identified yet
     *
     * @return
     */
    UserModel getUser();

    /**
     * 将指定用户绑定到本流程。
     *
     * Attach a specific user to this flow.
     *
     * @param user
     */
    void setUser(UserModel user);

    /**
     * 当前领域。
     *
     * Current realm
     *
     * @return
     */
    RealmModel getRealm();

    /**
     * 本流程关联的认证会话。
     *
     * AuthenticationSessionModel attached to this flow
     *
     * @return
     */
    AuthenticationSessionModel getAuthenticationSession();

    /**
     * 客户端连接信息（含 IP）。
     *
     * Information about the IP address from the connecting HTTP client.
     *
     * @return
     */
    ClientConnection getConnection();

    /**
     * 当前请求的 URI 信息。
     *
     * UriInfo of the current request
     *
     * @return
     */
    UriInfo getUriInfo();

    /**
     * 当前 Keycloak 会话。
     *
     * Current session
     *
     * @return
     */
    KeycloakSession getSession();

    /** 当前 HTTP 请求。 */
    HttpRequest getHttpRequest();

    /**
     * 当前执行步骤关联的认证器配置。
     *
     * Get any configuration associated with the current execution
     *
     * @return
     */
    AuthenticatorConfigModel getAuthenticatorConfig();
}
