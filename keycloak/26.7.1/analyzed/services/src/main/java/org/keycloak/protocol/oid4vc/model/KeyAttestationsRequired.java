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
 */

package org.keycloak.protocol.oid4vc.model;

import java.util.List;
import java.util.Objects;

import org.keycloak.models.oid4vci.CredentialScopeModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 凭证颁发者元数据中声明的密钥证明要求。
 * <p>约束密钥存储安全级别与用户认证的抗攻击能力。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-15.html#name-credential-issuer-metadata-p">
 * Credential Issuer Metadata Parameters</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyAttestationsRequired {

    /** 要求的密钥存储级别列表。 */
    @JsonProperty("key_storage")
    private List<String> keyStorage;

    /** 要求的用户认证抗攻击级别列表。 */
    @JsonProperty("user_authentication")
    private List<String> userAuthentication;

    /** 无参构造，供 Jackson 反序列化使用。 */
    public KeyAttestationsRequired() {
        // Jackson 反序列化占位
    }

    /**
     * 从凭证范围模型解析密钥证明要求；未启用时返回 {@code null}。
     * @param credentialScope 凭证范围模型
     * @return 密钥证明要求或 {@code null}
     */
    public static KeyAttestationsRequired parse(CredentialScopeModel credentialScope) {
        KeyAttestationsRequired keyAttestationsRequired = null;
        if (credentialScope.isKeyAttestationRequired()) {
            keyAttestationsRequired = new KeyAttestationsRequired();
            keyAttestationsRequired.setKeyStorage(credentialScope.getRequiredKeyAttestationKeyStorage());
            keyAttestationsRequired.setUserAuthentication(credentialScope.getRequiredKeyAttestationUserAuthentication());
        }
        return keyAttestationsRequired;
    }

    /** @return 要求的密钥存储级别 */
    public List<String> getKeyStorage() {
        return keyStorage;
    }

    /** @param keyStorage 密钥存储级别 @return 当前实例 */
    public KeyAttestationsRequired setKeyStorage(List<String> keyStorage) {
        this.keyStorage = keyStorage;
        return this;
    }

    /** @return 要求的用户认证级别 */
    public List<String> getUserAuthentication() {
        return userAuthentication;
    }

    /** @param userAuthentication 用户认证级别 @return 当前实例 */
    public KeyAttestationsRequired setUserAuthentication(List<String> userAuthentication) {
        this.userAuthentication = userAuthentication;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyAttestationsRequired that = (KeyAttestationsRequired) o;
        return Objects.equals(keyStorage, that.keyStorage) &&
                Objects.equals(userAuthentication, that.userAuthentication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyStorage, userAuthentication);
    }
}
