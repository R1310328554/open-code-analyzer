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
package com.alibaba.csp.sentinel.adapter.jaxrs.request;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名）。
 *
 * @author sea
 */
public interface RequestOriginParser {

    /**
     * 从给定 HTTP 请求中解析来源标识。
     *
     * @param request HTTP 请求
     * @return 解析出的来源标识
     */
    String parseOrigin(ContainerRequestContext request);
}
