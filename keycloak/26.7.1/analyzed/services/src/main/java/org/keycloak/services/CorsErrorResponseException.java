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

package org.keycloak.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.services.cors.Cors;

/**
 * 带 CORS 头的 OAuth2 错误响应异常。
 * <p>继承 {@link WebApplicationException}，在 {@link #getResponse()} 中构建 {@link OAuth2ErrorRepresentation} JSON 响应并通过 {@link Cors} 附加跨域头。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class CorsErrorResponseException extends WebApplicationException {

    /** CORS 处理器，用于向响应添加 Access-Control 头 */
    private final Cors cors;
    /** OAuth2 错误码（如 invalid_grant） */
    private final String error;
    /** OAuth2 错误描述 */
    private final String errorDescription;
    /** HTTP 响应状态 */
    private final Response.Status status;

    /**
     * @param cors CORS 处理器
     * @param error OAuth2 错误码
     * @param errorDescription 错误描述
     * @param status HTTP 状态
     */
    public CorsErrorResponseException(Cors cors, String error, String errorDescription, Response.Status status) {
        super(error, status);
        this.cors = cors;
        this.error = error;
        this.errorDescription = errorDescription;
        this.status = status;
    }

    /** @return OAuth2 错误描述 */
    public String getErrorDescription() {
        return errorDescription;
    }

    /** 构建含 OAuth2 错误体与 CORS 头的 JAX-RS 响应 @return Response 实例 */
    @Override
    public Response getResponse() {
        OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
        Response.ResponseBuilder builder = Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE);
        return cors.add(builder);
    }

}
