/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.jaxrs.exception;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Sentinel JAX-RS 适配器提供的异常映射器。
 * 当用户抛出非 {@link javax.ws.rs.WebApplicationException} 且未被其他 ExceptionMapper 捕获的异常时，
 * 本映射器将异常转换为 Response，以便触发 ContainerResponseFilter 退出 Sentinel Entry。
 * 用户可添加自定义 ExceptionMapper，并通过 {@link javax.annotation.Priority} 设置更小的优先级值。
 *
 * @author sea
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .build();
    }
}
