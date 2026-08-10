/*
 * Copyright 2023
 *  Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * Jackson {@link MismatchedInputException} 专用异常映射器。
 * <p>显式覆盖 RestEasy Jackson 默认处理，将反序列化类型不匹配异常委托给 {@link KeycloakErrorHandler}。</p>
 */
@Provider
public class KeycloakMismatchedInputExceptionHandler implements ExceptionMapper<MismatchedInputException> {

    /** 注入的 Keycloak 会话 */
    @Context
    KeycloakSession session;

    /**
     * 返回经转义后的原始错误消息响应。
     * @param exception Jackson 输入不匹配异常
     * @return HTTP 响应
     */
    @Override
    public Response toResponse(MismatchedInputException exception) {
        return KeycloakErrorHandler.getResponse(session, exception);
    }
}