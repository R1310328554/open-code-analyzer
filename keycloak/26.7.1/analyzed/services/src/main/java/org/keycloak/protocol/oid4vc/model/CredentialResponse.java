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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 规范中的凭证响应（Credential Response）模型。
 * <p>签发者返回已签发的凭证列表，或延迟签发时的 transaction_id。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-response}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialResponse {

    /** 已签发的凭证对象列表。 */
    @JsonProperty("credentials")
    private List<Credential> credentials;

    /** 延迟签发场景下的交易标识符。 */
    @JsonProperty("transaction_id")
    private String transactionId;

    /** @return 凭证列表 */
    public List<Credential> getCredentials() {
        return credentials;
    }

    /** @param credentials 凭证列表 */
    public CredentialResponse setCredentials(List<Credential> credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * 追加单个凭证到响应列表。
     *
     * @param credential 凭证载荷（JWT、LD 等格式）
     * @return 当前实例
     */
        if (this.credentials == null) {
            this.credentials = new ArrayList<>();
        }
        this.credentials.add(new Credential().setCredential(credential));
        return this;
    }

    /** @return 延迟签发交易 ID */
    public String getTransactionId() {
        return transactionId;
    }

    /** @param transactionId 交易 ID */
    public CredentialResponse setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    /**
     * credentials 数组中的单个凭证包装对象。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Credential {
        /** 实际凭证载荷。 */
        @JsonProperty("credential")
        private Object credential;

        /** @return 凭证载荷 */
        public Object getCredential() {
            return credential;
        }

        /** @param credential 凭证载荷 */
        public Credential setCredential(Object credential) {
            this.credential = credential;
            return this;
        }
    }
}
