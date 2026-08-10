/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.representations;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * mTLS 端点别名映射：在 OIDC 发现文档 {@code mtls_endpoint_aliases} 中列出需通过双向 TLS 访问的端点 URL。
 * <p>
 * 对应 OAuth mTLS 规范，客户端在持有客户端证书时应使用此处别名 URL 而非默认端点。
 */
public class MTLSEndpointAliases {

    /** 令牌端点（mTLS 别名）。 */
    @JsonProperty("token_endpoint")
    private String tokenEndpoint;
    /** 令牌撤销端点（mTLS 别名）。 */
    @JsonProperty("revocation_endpoint")
    private String revocationEndpoint;
    /** 令牌内省端点（mTLS 别名）。 */
    @JsonProperty("introspection_endpoint")
    private String introspectionEndpoint;
    /** 设备授权端点（mTLS 别名）。 */
    @JsonProperty("device_authorization_endpoint")
    private String deviceAuthorizationEndpoint;
    /** 动态客户端注册端点（mTLS 别名）。 */
    @JsonProperty("registration_endpoint")
    private String registrationEndpoint;
    /** UserInfo 端点（mTLS 别名）。 */
    @JsonProperty("userinfo_endpoint")
    private String userInfoEndpoint;
    /** 推送授权请求（PAR）端点（mTLS 别名）。 */
    @JsonProperty("pushed_authorization_request_endpoint")
    private String pushedAuthorizationRequestEndpoint;
    /** 后台通道认证（CIBA）端点（mTLS 别名）。 */
    @JsonProperty("backchannel_authentication_endpoint")
    private String backchannelAuthenticationEndpoint;

    /** 预留的自定义端点声明（扩展字段）。 */
    protected Map<String, Object> otherClaims = new HashMap<String, Object>();

    public MTLSEndpointAliases() { }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getRevocationEndpoint() {
        return revocationEndpoint;
    }

    public void setRevocationEndpoint(String revocationEndpoint) {
        this.revocationEndpoint = revocationEndpoint;
    }

    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
    }

    public String getDeviceAuthorizationEndpoint() {
        return deviceAuthorizationEndpoint;
    }

    public void setDeviceAuthorizationEndpoint(String deviceAuthorizationEndpoint) {
        this.deviceAuthorizationEndpoint = deviceAuthorizationEndpoint;
    }

    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String getPushedAuthorizationRequestEndpoint() {
        return pushedAuthorizationRequestEndpoint;
    }

    public void setPushedAuthorizationRequestEndpoint(String pushedAuthorizationRequestEndpoint) {
        this.pushedAuthorizationRequestEndpoint = pushedAuthorizationRequestEndpoint;
    }

    public String getBackchannelAuthenticationEndpoint() {
        return backchannelAuthenticationEndpoint;
    }

    public void setBackchannelAuthenticationEndpoint(String backchannelAuthenticationEndpoint) {
        this.backchannelAuthenticationEndpoint = backchannelAuthenticationEndpoint;
    }

    /** 返回未建模的额外 JSON 字段。 */
    @JsonAnyGetter
    public Map<String, Object> getOtherClaims() {
        return otherClaims;
    }

    /** 反序列化时捕获未知 JSON 属性。
     * @param name 属性名
     * @param value 属性值
     */
    @JsonAnySetter
    public void setOtherClaims(String name, Object value) {
        otherClaims.put(name, value);
    }
}
