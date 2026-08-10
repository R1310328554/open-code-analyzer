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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 凭证发放中的预授权码（pre-authorized_code）授权条目。
 * <p>实现 {@link CredentialOfferGrant}，规范见 {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer}。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreAuthorizedCodeGrant implements CredentialOfferGrant {

    /** OAuth 预授权码 grant 类型 URN。 */
    public static final String PRE_AUTH_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    /** JSON 字段名：授权服务器标识。 */
    public static final String AUTHORIZATION_SERVER_PARAM = "authorization_server";
    /** JSON 字段名：预授权码。 */
    public static final String CODE_REQUEST_PARAM = "pre-authorized_code";
    /** JSON 字段名：用户输入的交易码元数据。 */
    public static final String TX_CODE_PARAM = "tx_code";

    /** @return 预授权码 grant 类型 URN */
    @Override
    @JsonIgnore
    public String getGrantType() {
        return PRE_AUTH_GRANT_TYPE;
    }

    /** 预授权码字符串。 */
    @JsonProperty(CODE_REQUEST_PARAM)
    private String preAuthorizedCode;

    /** 可选的用户交易码提示与长度约束。 */
    @JsonProperty(TX_CODE_PARAM)
    private TxCode txCode;

    /** 执行令牌交换的授权服务器标识。 */
    @JsonProperty(AUTHORIZATION_SERVER_PARAM)
    private String authorizationServer;

    /** @return 预授权码 */
    public String getPreAuthorizedCode() {
        return preAuthorizedCode;
    }

    /** @param preAuthorizedCode 预授权码 @return 当前实例 */
    public PreAuthorizedCodeGrant setPreAuthorizedCode(String preAuthorizedCode) {
        this.preAuthorizedCode = preAuthorizedCode;
        return this;
    }

    /** @return 交易码元数据 */
    public TxCode getTxCode() {
        return txCode;
    }

    /** @param txCode 交易码元数据 @return 当前实例 */
    public PreAuthorizedCodeGrant setTxCode(TxCode txCode) {
        this.txCode = txCode;
        return this;
    }

    /** @return 授权服务器标识 */
    public String getAuthorizationServer() {
        return authorizationServer;
    }

    /** @param authorizationServer 授权服务器 @return 当前实例 */
    public PreAuthorizedCodeGrant setAuthorizationServer(String authorizationServer) {
        this.authorizationServer = authorizationServer;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreAuthorizedCodeGrant that)) return false;
        return Objects.equals(preAuthorizedCode, that.preAuthorizedCode)
                && Objects.equals(txCode, that.txCode) && Objects.equals(authorizationServer, that.authorizationServer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preAuthorizedCode, txCode, authorizationServer);
    }
}
