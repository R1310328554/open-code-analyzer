/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.error;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Keycloak 版 Jackson 未知属性异常映射器。
 * <p>显式覆盖 RestEasy 默认的 {@code UnrecognizedPropertyExceptionHandler}， 将 {@link UnrecognizedPropertyException} 转为带行/列信息的 400 响应。</p>
 * <p>参考：{@code org.jboss.resteasy.plugins.providers.jackson.UnrecognizedPropertyExceptionHandler}</p>
 */
@Provider
public class KcUnrecognizedPropertyExceptionHandler implements ExceptionMapper<UnrecognizedPropertyException> {

    /** 注入的 Keycloak 会话，供 {@link KeycloakErrorHandler} 使用 */
    @Context
    KeycloakSession session;

    /**
     * 将未知 JSON 字段异常映射为 Bad Request 响应。
     * @param exception Jackson 反序列化时遇到的未知属性异常
     * @return 包含类名、字段名及行列位置的错误响应
     */
    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {
        final String message = String.format("Invalid json representation for %s. Unrecognized field \"%s\" at line %s column %s.",
                exception.getReferringClass().getSimpleName(), exception.getPropertyName(),
                exception.getLocation().getLineNr(), exception.getLocation().getColumnNr());
        return KeycloakErrorHandler.getResponse(session, new BadRequestException(message));
    }
}