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
package org.keycloak.client.cli.util;

/**
 * 非 2xx HTTP 响应异常。
 * <p>
 * 由 {@link HeadersBodyStatus#checkSuccess()} 在解析 OAuth/REST 错误体后抛出，
 * 携带状态码与可读错误消息。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HttpResponseException extends RuntimeException {

    /** 原始状态行片段（如 {@code "404 Not Found"}）。 */
    private String status;

    /**
     * 构造 HTTP 响应异常。
     *
     * @param status 状态描述
     * @param message 解析后的错误消息，可为 {@code null}
     * @param cause 原始响应正文等附加原因
     */
    HttpResponseException(String status, String message, Throwable cause) {
        super(message != null ? message : "HTTP error - " + status, cause);
        this.status = status;
    }

    /** 从状态字符串解析数值 HTTP 状态码。 */
    public int getStatusCode() {
        return Integer.valueOf(status.split(" ")[0]);
    }
}
