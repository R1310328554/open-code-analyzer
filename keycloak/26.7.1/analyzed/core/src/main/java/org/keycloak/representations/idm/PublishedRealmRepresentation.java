/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.idm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 域公开端点（well-known）返回的精简域信息，供客户端发现令牌与账户服务 URL。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PublishedRealmRepresentation {
    /** 域名称。 */
    protected String realm;

    /** 域签名验证公钥（PEM 格式）。 */
    @JsonProperty("public_key")
    protected String publicKeyPem;

    /** 令牌服务端点基础 URL。 */
    @JsonProperty("token-service")
    protected String tokenServiceUrl;

    /** 账户管理服务端点 URL。 */
    @JsonProperty("account-service")
    protected String accountServiceUrl;

    /** 令牌生效起始时间（not-before，Unix 秒）。 */
    @JsonProperty("tokens-not-before")
    protected int notBefore;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public String getTokenServiceUrl() {
        return tokenServiceUrl;
    }

    public void setTokenServiceUrl(String tokenServiceUrl) {
        this.tokenServiceUrl = tokenServiceUrl;
    }

    public String getAccountServiceUrl() {
        return accountServiceUrl;
    }

    public void setAccountServiceUrl(String accountServiceUrl) {
        this.accountServiceUrl = accountServiceUrl;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }
}
