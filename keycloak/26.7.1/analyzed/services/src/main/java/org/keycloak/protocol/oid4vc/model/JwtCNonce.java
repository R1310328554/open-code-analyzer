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

import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 客户端 nonce（c_nonce）JWT 载荷。
 * <p>继承 {@link org.keycloak.representations.JsonWebToken}，通过 {@code salt} 为可预测的 c_nonce 值提供密码学随机盐。</p>
 *
 * @author Pascal Knüppel
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtCNonce extends JsonWebToken {

    /** 密码学强度随机字符串，作为 c_nonce 的盐值，避免 nonce 可预测。 */
    @JsonProperty("salt")
    private String salt;

    /** @return 盐值字符串 */
    public String getSalt() {
        return salt;
    }

    /** @param salt 盐值 @return 当前实例（链式调用） */
    public JwtCNonce salt(String salt) {
        this.salt = salt;
        return this;
    }
}
