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

import java.io.InputStream;
import java.util.Map;

import org.keycloak.util.JsonSerialization;

/**
 * 带 HTTP 状态行的响应封装，继承 {@link HeadersBody}。
 * <p>
 * 提供状态码解析及 {@link #checkSuccess()} 校验：非 2xx 时解析 OAuth/REST
 * 错误 JSON 并抛出 {@link HttpResponseException}。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HeadersBodyStatus extends HeadersBody {

    /** Apache 状态行字符串（如 {@code HTTP/1.1 200 OK}）。 */
    private final String status;
    /** 是否将 Jakarta Bean Validation {@code violations} 纳入错误消息。 */
    private boolean allowJakartaValidationErrors;

    /**
     * 构造带状态行的响应对象。
     *
     * @param status HTTP 状态行
     * @param headers 响应头
     * @param body 响应正文流
     */
    public HeadersBodyStatus(String status, Headers headers, InputStream body) {
        super(headers, body);
        this.status = status;
    }

    /**
     * 构造带 Jakarta 校验错误解析选项的响应对象。
     *
     * @param status HTTP 状态行
     * @param headers 响应头
     * @param body 响应正文流
     * @param allowJakartaValidationErrors 是否格式化 violations 列表
     */
    public HeadersBodyStatus(String status, Headers headers, InputStream body, boolean allowJakartaValidationErrors) {
        this(status, headers, body);
        this.allowJakartaValidationErrors = allowJakartaValidationErrors;
    }

    /** 返回原始 HTTP 状态行。 */
    public String getStatus() {
        return status;
    }

    /** 从状态行截取「码 + 原因短语」部分。 */
    private String getStatusCodeAndReason() {
        return getStatus().substring(9);
    }

    /**
     * 断言响应为 2xx；否则解析错误体并抛出 {@link HttpResponseException}。
     * <p>
     * 优先读取 {@code errorMessage}、{@code error}/{@code error_description}；
     * 启用 Jakarta 模式时附加 {@code violations} 明细。
     */
    public void checkSuccess() {
        int code = getStatusCode();
        if (code < 200 || code >= 300) {
            String content = readBodyString();
            Map<String, Object> error = null;
            try {
                error = JsonSerialization.readValue(content, Map.class);
            } catch (Exception ignored) {
            }

            String message = null;
            if (error != null) {
                String description = (String) error.get("error_description");
                String err = (String) error.get("error");
                String msg = (String) error.get("errorMessage");

                if (allowJakartaValidationErrors) {
                    Object violations = error.get("violations");
                    if (violations instanceof Iterable<?> violationList) {
                        StringBuilder violationsString = new StringBuilder();
                        for (Object v : violationList) {
                            violationsString.append("\n\t- ").append(v);
                        }
                        message = "%s:%s".formatted(err != null ? err : "Invalid data", violationsString.toString());
                    }
                }

                if (message == null) {
                    message = msg != null ? msg : err != null ? (description != null ? description + " [" + err + "]" : err) : null;
                }
            }
            throw new HttpResponseException(getStatusCodeAndReason(), message, new RuntimeException(content));
        }
    }

    /** 从状态行解析数值 HTTP 状态码。 */
    public int getStatusCode() {
        return Integer.valueOf(status.split(" ")[1]);
    }
}
