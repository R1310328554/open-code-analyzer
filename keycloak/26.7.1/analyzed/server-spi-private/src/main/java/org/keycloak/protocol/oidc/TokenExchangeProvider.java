/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.protocol.oidc;

import jakarta.ws.rs.core.Response;

import org.keycloak.provider.Provider;

/**
 * 令牌交换提供者：对支持的令牌类型执行 RFC 8693 令牌交换。
 * <p>Provides token exchange mechanism for supported tokens</p>
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public interface TokenExchangeProvider extends Provider {

    /**
     * 判断本提供者是否支持该交换请求。
     * @param context token exchange context
     * @return true if the request is supported
     */
    boolean supports(TokenExchangeContext context);

    /**
     * 执行令牌交换并返回新令牌响应。
     * @param context
     * @return response with a new token
     */
    Response exchange(TokenExchangeContext context);


}
