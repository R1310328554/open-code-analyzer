/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.resteasy;

import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler;

/**
 * <p>设置 JAX-RS 资源方法声明的响应 {@link MediaType} 的 {@link ServerRestHandler}。
 *
 * <p>主要目的是让 {@link org.keycloak.headers.DefaultSecurityHeadersProvider} 能读取到实际产出的媒体类型。
 */
public class SetResponseContentTypeHandler implements ServerRestHandler {

    private MediaType producesMediaType;

    /**
     * @param producesMediaTypes 资源方法 @Produces 声明的媒体类型列表；为空时默认 application/json
     */
    public SetResponseContentTypeHandler(String[] producesMediaTypes) {
        if (producesMediaTypes.length == 0) {
            this.producesMediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            this.producesMediaType = MediaType.valueOf(producesMediaTypes[0]);
        }
    }

    /** 将解析后的媒体类型写入响应上下文。 */
    @Override
    public void handle(ResteasyReactiveRequestContext requestContext) {
        requestContext.setResponseContentType(producesMediaType);
    }
}
