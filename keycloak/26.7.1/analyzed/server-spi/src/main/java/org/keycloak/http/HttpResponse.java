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

package org.keycloak.http;

import jakarta.ws.rs.core.NewCookie;

/**
 * 表示出站 HTTP 响应的抽象接口。
 * <p>Represents an out coming HTTP response.</p>
 * <p>实例可通过 {@link org.keycloak.models.KeycloakContext#getHttpResponse} 获取。</p>
 */
public interface HttpResponse {
    /**
     * 获取当前状态码。
     * Gets the current status code.
     *
     * <p><strong>Warning:</strong> this will typically not be the actual status returned as that will be managed by JAX-RS based upon the return value or exception from the endpoint method.</p>
     * <p><strong>注意：</strong>实际返回的状态码通常由 JAX-RS 根据端点方法的返回值或异常决定，与此处读取的值可能不一致。</p>
     */
    int getStatus();

    /**
     * 设置 HTTP 状态码。
     * Sets a status code.
     *
     * <p><strong>Warning:</strong> this value is silently overwritten by the JAX-RS runtime based upon the return value or exception from the endpoint method. Please use a JAX-RS compatible way of setting the status instead.</p>
     * <p><strong>注意：</strong>该值会被 JAX-RS 运行时静默覆盖，请优先使用 JAX-RS 兼容方式设置状态码。</p>
     *
     * @param statusCode the status code
     */
    void setStatus(int statusCode);

    /**
     * 向指定名称的响应头追加一个值（保留已有值）。
     * Add a value to the current list of values for the header with the given {@code name}.
     *
     * @param name the header name
     * @param value the header value
     */
    void addHeader(String name, String value);

    /**
     * 设置响应头，替换该名称下的所有已有值。
     * Set a header. Any existing values will be replaced.
     *
     * @param name the header name
     * @param value the header value
     */
    void setHeader(String name, String value);

    /**
     * 若尚未设置则写入 Cookie（幂等）。
     * Sets a new cookie only if not yet set.
     *
     * @param cookie the cookie
     */
    void setCookieIfAbsent(NewCookie cookie);

}
