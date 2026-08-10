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

import java.util.List;

import jakarta.ws.rs.core.Response;

/**
 * 认证器/表单动作抛出此异常以完全中止认证流程。
 *
 * Throw this exception from an Authenticator, FormAuthenticator, or FormAction if you want to completely abort the flow.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationFlowException extends RuntimeException {
    /** 关联的流程错误码。 */
    private AuthenticationFlowError error;
    /** 可选的 HTTP 响应体。 */
    private Response response;
    /** 聚合的多条子异常。 */
    private List<AuthenticationFlowException> afeList;
    /** 事件审计详情。 */
    private String eventDetails;
    /** 展示给用户的消息。 */
    private String userErrorMessage;

    /** 仅指定错误码。 */
    public AuthenticationFlowException(AuthenticationFlowError error) {
        this.error = error;
    }
    
    /** 指定错误码、事件详情与用户消息。 */
    public AuthenticationFlowException(AuthenticationFlowError error, String eventDetails, String userErrorMessage) {
        this.error = error;
        this.eventDetails = eventDetails;
        this.userErrorMessage = userErrorMessage;
    }

    /** 指定错误码与 HTTP 响应。 */
    public AuthenticationFlowException(AuthenticationFlowError error, Response response) {
        this.error = error;
        this.response = response;
    }

    /** 带消息与错误码。 */
    public AuthenticationFlowException(String message, AuthenticationFlowError error) {
        super(message);
        this.error = error;
    }

    /** 带消息、原因与错误码。 */
    public AuthenticationFlowException(String message, Throwable cause, AuthenticationFlowError error) {
        super(message, cause);
        this.error = error;
    }

    /** 带原因与错误码。 */
    public AuthenticationFlowException(Throwable cause, AuthenticationFlowError error) {
        super(cause);
        this.error = error;
    }

    /** 聚合多条子异常，错误码设为 INTERNAL_ERROR。 */
    public AuthenticationFlowException(List<AuthenticationFlowException> afeList){
        this.error = AuthenticationFlowError.INTERNAL_ERROR;
        this.afeList = afeList;
    }

    /** 完整 RuntimeException 构造并绑定错误码。 */
    public AuthenticationFlowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, AuthenticationFlowError error) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    /** 返回流程错误码。 */
    public AuthenticationFlowError getError() {
        return error;
    }

    /** 返回关联 HTTP 响应。 */
    public Response getResponse() {
        return response;
    }

    /** 返回子异常列表。 */
    public List<AuthenticationFlowException> getAfeList() {
        return afeList;
    }

    /** 返回事件详情。 */
    public String getEventDetails() {
        return eventDetails;
    }
    
    /** 返回用户可见错误消息。 */
    public String getUserErrorMessage() {
        return userErrorMessage;
    }
}
