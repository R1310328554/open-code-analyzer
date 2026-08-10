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

import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ErrorRepresentation;

/**
 * REST/Admin API JSON 错误响应工厂：构建 {@link ErrorRepresentation} 并包装为 {@link ErrorResponseException}。
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ErrorResponse {

    /** 资源已存在（409 CONFLICT）。 @param message 错误消息 @return 可抛出的异常 */
    public static ErrorResponseException exists(String message) {
        return ErrorResponse.error(message, Response.Status.CONFLICT);
    }

    /** 单条错误消息。 @param message 错误文本 @param status HTTP 状态 @return 异常实例 */
    public static ErrorResponseException error(String message, Response.Status status) {
        return ErrorResponse.error(message, null, status);
    }
    
    /** 带参数的单条错误。 @param message 消息键 @param params 参数 @param status HTTP 状态 */
    public static ErrorResponseException error(String message, Object[] params, Response.Status status) {
        ErrorRepresentation error = new ErrorRepresentation();
        error.setErrorMessage(message);
        error.setParams(params);
        return new ErrorResponseException(Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build());
    }

    /** 多条错误；单条时可折叠为单对象响应。 */
    public static ErrorResponseException errors(List<ErrorRepresentation> s, Response.Status status) {
        return errors(s, status, true);
    }
    
    /**
     * 构建多条 {@link ErrorRepresentation} 的 JSON 错误响应。
     * @param s 错误列表
     * @param status HTTP 状态
     * @param shrinkSingleError 仅一条时是否直接返回该条而非包装 errors 数组
     */
        if (shrinkSingleError && s.size() == 1) {
            return new ErrorResponseException(Response.status(status).entity(s.get(0)).type(MediaType.APPLICATION_JSON).build());
        }
        ErrorRepresentation error = new ErrorRepresentation();
        error.setErrors(s);
        if(!shrinkSingleError && s.size() == 1) {
            error.setErrorMessage(s.get(0).getErrorMessage());
            error.setParams(s.get(0).getParams());
            error.setField(s.get(0).getField());
        }
        return new ErrorResponseException(Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build());
    }
}
