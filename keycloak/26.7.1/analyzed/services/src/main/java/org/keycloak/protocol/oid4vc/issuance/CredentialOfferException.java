/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

/**
 * 凭证发放（Credential Offer）流程中的运行时异常。
 * <p>携带 OID4VCI 错误类型标识，便于 REST 端点返回结构化错误响应。</p>
 */
public class CredentialOfferException extends RuntimeException {

    /** OID4VCI 错误类型（如 {@code invalid_credential_offer_request}）。 */
    private final String errorType;

    /**
     * 构造凭证发放异常。
     * @param errorType OID4VCI 错误类型
     * @param message 人类可读错误描述
     */
    public CredentialOfferException(String errorType, String message) {
        this(errorType, message, null);
    }

    /**
     * 构造带根因的凭证发放异常。
     * @param errorType OID4VCI 错误类型
     * @param message 人类可读错误描述
     * @param cause 底层异常
     */
    public CredentialOfferException(String errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;

    }

    /** @return OID4VCI 错误类型字符串 */
    public String getErrorType() {
        return errorType;
    }
}
