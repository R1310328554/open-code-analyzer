/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.protocol.oid4vc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI nonce 端点响应体。
 * <p>规范见 https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-nonce-response</p>
 *
 * @author Pascal Knüppel
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NonceResponse {

    /** 用于构造密钥持有证明的 c_nonce，必须不可预测。 */
    @JsonProperty("c_nonce")
    private String nonce;

    /** @return c_nonce 字符串 @see #nonce */
    public String getNonce() {
        return nonce;
    }

    /** @param nonce c_nonce @see #nonce */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
