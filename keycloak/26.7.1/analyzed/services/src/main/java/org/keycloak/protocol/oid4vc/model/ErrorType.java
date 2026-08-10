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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * OID4VCI 凭证签发流程中的标准错误类型枚举。
 * <p>覆盖客户端、请求、proof、加密参数及凭证配置相关错误码。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-16.html}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public enum ErrorType {

    /** 客户端无效或未授权。 */
    INVALID_CLIENT("invalid_client"),
    /** 请求格式或参数无效。 */
    INVALID_REQUEST("invalid_request"),
    /** 授权 grant 无效或过期。 */
    INVALID_GRANT("invalid_grant"),
    /** 凭证发放请求无效。 */
    INVALID_CREDENTIAL_OFFER_REQUEST("invalid_credential_offer_request"),
    /** 凭证请求无效。 */
    INVALID_CREDENTIAL_REQUEST("invalid_credential_request"),
    /** 访问令牌无效。 */
    INVALID_TOKEN("invalid_token"),
    /** 未知的凭证配置 ID。 */
    UNKNOWN_CREDENTIAL_CONFIGURATION("unknown_credential_configuration"),
    /** 未知的凭证标识符。 */
    UNKNOWN_CREDENTIAL_IDENTIFIER("unknown_credential_identifier"),
    /** proof 校验失败。 */
    INVALID_PROOF("invalid_proof"),
    /** nonce 无效或已过期。 */
    INVALID_NONCE("invalid_nonce"),
    /** 加密参数无效或不支持。 */
    INVALID_ENCRYPTION_PARAMETERS("invalid_encryption_parameters"),
    /** 缺少凭证配置。 */
    MISSING_CREDENTIAL_CONFIG("missing_credential_config"),
    /** 同时缺少 credential_identifier 与 credential_configuration_id。 */
    MISSING_CREDENTIAL_IDENTIFIER_AND_CONFIGURATION_ID("missing_credential_identifier_and_configuration_id");

    /** OAuth/OID4VCI 错误码字符串值。 */
    private final String value;

    /** @param value 错误码字符串 */
    ErrorType(String value) {
        this.value = value;
    }

    /** @return JSON 序列化用的错误码 */
    @JsonValue
    public String getValue() {
        return value;
    }

    /** @return 错误码字符串 */
    @Override
    public String toString() {
        return value;
    }
}
