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

package org.keycloak.admin.client.resource;

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;


/**
 * HTTP Basic 认证请求过滤器，为出站请求附加 {@code Authorization: Basic ...} 头。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public class BasicAuthFilter implements ClientRequestFilter {

    private final String username;
    private final String password;

    /**
     * 构造 Basic 认证过滤器。
     *
     * @param username 用户名
     * @param password 密码
     */
    public BasicAuthFilter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 将 Base64 编码的用户名/密码对写入 {@link HttpHeaders#AUTHORIZATION} 请求头。
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String pair = username + ":" + password;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(pair.getBytes());
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
    }
    
    
}
