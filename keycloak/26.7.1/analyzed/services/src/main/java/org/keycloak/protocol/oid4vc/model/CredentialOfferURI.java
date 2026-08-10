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

import org.keycloak.common.util.KeycloakUriBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 构建凭证发放（Credential Offer）URI 所需的全部信息。
 * <p>包含签发者基址、nonce 及可选 QR 码图像数据，用于生成用户可扫描的发放链接。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialOfferURI {

    /** 凭证签发者基址 URI。 */
    private String issuer;
    /** 发放 nonce，用于唯一标识本次 offer。 */
    private String nonce;

    /** Base64 编码的 QR 码 PNG 图像，可直接嵌入 HTML img 标签。 */
    @JsonProperty("qr_code")
    private String qrCode;

    /** @return 凭证签发者基址 URI */
    public String getIssuer() {
        return issuer;
    }

    /** @param issuer 凭证签发者基址 URI */
    public CredentialOfferURI setIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /** @return 发放 nonce */
    public String getNonce() {
        return nonce;
    }

    /** @param nonce 发放 nonce */
    public CredentialOfferURI setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /** @return Base64 编码的 QR 码图像数据 */
    public String getQrCode() {
        return qrCode;
    }

    /**
     * 设置可直接嵌入网页的 Base64 QR 码图像。
     * <p>示例：{@code <img src="data:image/png;base64,AAAA..." />}</p>
     *
     * @param qrCode Base64 编码 PNG 数据
     * @return 当前实例
     */
    public CredentialOfferURI setQrCode(String qrCode) {
        this.qrCode = qrCode;
        return this;
    }

    /**
     * 拼接签发者 URI 与 nonce，得到完整的凭证发放 URI。
     *
     * @return 凭证发放完整 URI 字符串
     */
    @JsonIgnore
    public String getCredentialOfferUri() {
        return KeycloakUriBuilder.fromUri(issuer).path(nonce).build().toString();
    }
}
