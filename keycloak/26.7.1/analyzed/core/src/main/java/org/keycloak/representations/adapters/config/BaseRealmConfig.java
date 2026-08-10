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

package org.keycloak.representations.adapters.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Keycloak adapter 的 realm 级公共配置基类，定义 realm 名称、公钥、认证服务器地址等核心连接参数。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@JsonPropertyOrder({"realm", "realm-public-key", "auth-server-url", "ssl-required"})
public class BaseRealmConfig {
    /** Realm 名称。 */
    @JsonProperty("realm")
    protected String realm;
    /** Realm 公钥（PEM 或 Base64）。 */
    @JsonProperty("realm-public-key")
    protected String realmKey;
    /** Keycloak 认证服务器根 URL。 */
    @JsonProperty("auth-server-url")
    protected String authServerUrl;
    /** SSL 要求级别（如 external、all、none）。 */
    @JsonProperty("ssl-required")
    protected String sslRequired;
    /** 机密端口（HTTPS 端口）。 */
    @JsonProperty("confidential-port")
    protected int confidentialPort;

    public String getSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(String sslRequired) {
        this.sslRequired = sslRequired;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getRealmKey() {
        return realmKey;
    }

    public void setRealmKey(String realmKey) {
        this.realmKey = realmKey;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public int getConfidentialPort() {
        return confidentialPort;
    }

    public void setConfidentialPort(int confidentialPort) {
        this.confidentialPort = confidentialPort;
    }
}
