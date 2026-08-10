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
package org.keycloak.services.resources;

import jakarta.ws.rs.core.Response;

import org.keycloak.common.VerificationException;

/**
 * 登录操作服务专用异常。
 * <p>携带预构建的 JAX-RS {@link Response}，用于在验证失败时直接返回 HTTP 响应。</p>
 *
 * @author hmlnarik
 */
public class LoginActionsServiceException extends VerificationException {

    /** 关联的 HTTP 响应 */
    private final Response response;

    /**
     * 仅携带响应的构造器。
     * @param response HTTP 响应
     */
    public LoginActionsServiceException(Response response) {
        this.response = response;
    }

    /**
     * 携带响应与消息的构造器。
     * @param response HTTP 响应
     * @param message 异常消息
     */
    public LoginActionsServiceException(Response response, String message) {
        super(message);
        this.response = response;
    }

    /**
     * 携带响应、消息与原因的构造器。
     * @param response HTTP 响应
     * @param message 异常消息
     * @param cause 根本原因
     */
    public LoginActionsServiceException(Response response, String message, Throwable cause) {
        super(message, cause);
        this.response = response;
    }

    /**
     * 携带响应与原因的构造器。
     * @param response HTTP 响应
     * @param cause 根本原因
     */
    public LoginActionsServiceException(Response response, Throwable cause) {
        super(cause);
        this.response = response;
    }

    /** @return 关联的 HTTP 响应 */
    public Response getResponse() {
        return response;
    }

}
