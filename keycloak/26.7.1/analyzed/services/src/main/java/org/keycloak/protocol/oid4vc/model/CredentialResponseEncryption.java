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

import org.keycloak.jose.jwk.JWK;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 凭证请求中的 credential_response_encryption 对象。
 * <p>钱包指定接收加密凭证响应时使用的算法、压缩方式及公钥 JWK。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-request}
 *
 * @author <a href="mailto:Bertrand.Ogen@adorsys.com">Bertrand Ogen</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialResponseEncryption {

    /** 必填。内容加密算法（enc），须为签发者元数据 enc_values_supported 中的值。 */
    private String enc;

    /** 可选。加密前压缩算法（zip）。 */
    private String zip;

    /** 请求含 credential_response_encryption 时必填；凭证响应将加密到此 JWK 公钥。 */
    private JWK jwk;

    /** @return 内容加密算法 */
    public String getEnc() {
        return enc;
    }

    /** @param enc 内容加密算法 */
    public CredentialResponseEncryption setEnc(String enc) {
        this.enc = enc;
        return this;
    }

    /** @return 压缩算法 */
    public String getZip() {
        return zip;
    }

    /** @param zip 压缩算法 */
    public CredentialResponseEncryption setZip(String zip) {
        this.zip = zip;
        return this;
    }

    /** @return 接收方公钥 JWK */
    public JWK getJwk() {
        return jwk;
    }

    /** @param jwk 接收方公钥 JWK */
    public CredentialResponseEncryption setJwk(JWK jwk) {
        this.jwk = jwk;
        return this;
    }
}
