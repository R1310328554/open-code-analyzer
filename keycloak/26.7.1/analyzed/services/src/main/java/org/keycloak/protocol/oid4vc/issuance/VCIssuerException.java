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

package org.keycloak.protocol.oid4vc.issuance;

import org.keycloak.protocol.oid4vc.model.ErrorType;

/**
 * 凭证发放失败时抛出的运行时异常。
 * <p>携带 {@link ErrorType} 错误类型，供 OID4VCI 端点映射为协议层错误响应。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class VCIssuerException extends RuntimeException {

    /** OID4VCI 错误类型。 */
    private final ErrorType errorType;

    /**
     * @param errorType 错误类型
     * @param message 错误消息
     */
    public VCIssuerException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * @param errorType 错误类型
     * @param message 错误消息
     * @param cause 根因异常
     */
    public VCIssuerException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    /** @return 关联的 OID4VCI 错误类型 */
    public ErrorType getErrorType() {
        return errorType;
    }
}
