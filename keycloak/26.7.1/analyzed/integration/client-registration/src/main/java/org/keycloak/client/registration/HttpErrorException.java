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

package org.keycloak.client.registration;

import java.io.IOException;

import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.util.JsonSerialization;

import org.apache.http.StatusLine;

/**
 * 客户端注册 HTTP 请求返回非成功状态码时抛出的 IO 异常。
 * <p>
 * 保留 {@link StatusLine} 与原始错误响应体，可通过 {@link #toErrorRepresentation()}
 * 解析为 {@link OAuth2ErrorRepresentation} 以获取 OAuth2 标准错误字段。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class HttpErrorException extends IOException {

    private final StatusLine statusLine;
    private final String errorResponse;

    /**
     * @param statusLine HTTP 响应状态行
     * @param errorResponse 响应正文（可能为 JSON 错误体）
     */
    public HttpErrorException(StatusLine statusLine, String errorResponse) {
        this.statusLine = statusLine;
        this.errorResponse = errorResponse;
    }

    /** @return HTTP 状态行 */
    public StatusLine getStatusLine() {
        return statusLine;
    }

    /** @return 原始错误响应字符串 */
    public String getErrorResponse() {
        return errorResponse;
    }

    /**
     * 将错误响应体解析为 OAuth2 错误表示。
     *
     * @return 解析成功时返回 {@link OAuth2ErrorRepresentation}，无响应体时返回 {@code null}
     * @throws RuntimeException 响应体不是合法 OAuth2 错误 JSON 时
     */
    public OAuth2ErrorRepresentation toErrorRepresentation() {
        if (errorResponse == null) {
            return null;
        }

        try {
            return JsonSerialization.readValue(errorResponse, OAuth2ErrorRepresentation.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Not OAuth2 error");
        }
    }
}
