/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 错误响应体，携带规范定义的错误码与描述。
 * <p>用于凭证端点、令牌端点等 OID4VCI 接口的失败响应序列化。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** 错误码字符串（见 {@link ErrorType}）。 */
    private String error;

    /** 人类可读的错误描述。 */
    @JsonProperty("error_description")
    private String errorDescription;

    /** @return 错误码 */
    public String getError() {
        return error;
    }

    /**
     * 从 {@link ErrorType} 枚举设置错误码。
     *
     * @param errorType 错误类型枚举
     * @return 当前实例
     */
        this.error = errorType == null ? null : errorType.getValue();
        return this;
    }

    /** @param error 错误码字符串 */
    public ErrorResponse setError(String error) {
        this.error = error;
        return this;
    }

    /** @return 错误描述 */
    public String getErrorDescription() {
        return errorDescription;
    }

    /** @param errorDescription 错误描述 */
    public ErrorResponse setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }
}
